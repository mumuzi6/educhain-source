import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn'; // 导入中文语言包
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';
import relativeTime from 'dayjs/plugin/relativeTime';

// 设置语言为中文
dayjs.locale('zh-cn');

// 添加插件
dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.extend(relativeTime);

// 中国时区 - 东八区
const TIMEZONE = 'Asia/Shanghai';

/**
 * 格式化日期时间，自动处理时区
 * @param date 日期字符串或日期对象
 * @param format 格式化模板，默认为 YYYY年MM月DD日 HH:mm:ss
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: string | Date | undefined, format: string = 'YYYY年MM月DD日 HH:mm:ss'): string {
  if (!date) return '未知时间';
  return dayjs(date).tz(TIMEZONE).format(format);
}

/**
 * 计算从现在到指定日期的剩余时间
 * @param date 目标日期
 * @returns 格式化的剩余时间字符串
 */
export function formatTimeToNow(date: string | Date | undefined): string {
  if (!date) return '';
  
  const targetDate = dayjs(date).tz(TIMEZONE);
  const now = dayjs().tz(TIMEZONE);
  
  if (targetDate.isBefore(now)) {
    return '已过期';
  }
  
  const days = targetDate.diff(now, 'day');
  if (days > 0) {
    return `${days}天后过期`;
  }
  
  const hours = targetDate.diff(now, 'hour');
  return `${hours}小时后过期`;
}

/**
 * 比较两个日期的大小（用于排序）
 * @param dateA 第一个日期
 * @param dateB 第二个日期
 * @returns 比较结果：正数表示dateB更新，负数表示dateA更新
 */
export function compareDates(dateA: string | Date, dateB: string | Date): number {
  return dayjs(dateB).tz(TIMEZONE).valueOf() - dayjs(dateA).tz(TIMEZONE).valueOf();
}

/**
 * 格式化相对时间（如：3天前，2小时前）
 * @param date 目标日期
 * @returns 相对时间字符串
 */
export function formatRelativeTime(date: string | Date | undefined): string {
  if (!date) return '';
  return dayjs(date).tz(TIMEZONE).fromNow();
}

/**
 * 检查日期是否过期
 * @param date 目标日期
 * @returns 是否已过期
 */
export function isExpired(date: string | Date | undefined): boolean {
  if (!date) return false;
  return dayjs(date).tz(TIMEZONE).isBefore(dayjs().tz(TIMEZONE));
} 