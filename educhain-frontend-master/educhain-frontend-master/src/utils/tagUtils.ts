/**
 * 将标签字符串解析为数组
 * @param tagsStr 标签JSON字符串
 * @returns 标签数组
 */
export const parseTags = (tagsStr?: string): string[] => {
  if (!tagsStr) {
    return [];
  }
  
  try {
    return JSON.parse(tagsStr);
  } catch (error) {
    console.error('解析标签出错', error);
    return [];
  }
};

/**
 * 将标签数组转换为字符串
 * @param tags 标签数组
 * @returns 标签JSON字符串
 */
export const stringifyTags = (tags: string[]): string => {
  return JSON.stringify(tags);
}; 