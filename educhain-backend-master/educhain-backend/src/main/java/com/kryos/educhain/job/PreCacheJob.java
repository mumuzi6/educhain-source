package com.kryos.educhain.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kryos.educhain.mapper.UserMapper;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L);
    
    // 热门标签列表
    private List<List<String>> hotTagsList = Arrays.asList(
            Collections.singletonList("音乐"),        // 单标签：音乐
            Collections.singletonList("Java"),        // 单标签：Java
            Arrays.asList("音乐", "游戏"),            // 多标签组合：音乐+游戏
            Arrays.asList("Java", "Python")           // 多标签组合：Java+Python
    );
    
    // 定义分页大小和预热的页码
    private static final int PAGE_SIZE = 10;
    private static final List<Integer> PRELOAD_PAGE_NUMS = Arrays.asList(1, 2); // 预热第1页和第2页

    // 每天执行，预热推荐用户
    @Scheduled(cron = "0 0 2 * * *") // 秒 分 时 日 月 年
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("educhain:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("educhain:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally { // 一定要放到finally
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
    
    /**
     * 缓存预热热门标签搜索结果（分页版）
     * 每隔4小时执行一次
     */
    @Scheduled(cron = "0 0 */4 * * *") // 每隔4小时执行一次
    public void doCacheTagSearch() {
        log.info("开始执行标签搜索缓存预热");
        RLock lock = redissonClient.getLock("educhain:precachejob:docachetags:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("获取到标签搜索缓存预热锁，线程ID: {}", Thread.currentThread().getId());
                
                // 记录预热开始时间
                long startTime = System.currentTimeMillis();
                int totalCacheCount = 0;
                
                // 预热所有热门标签组合的所有记录集合(不分页)
                for (List<String> tags : hotTagsList) {
                    log.info("预热标签组合完整列表: {}", String.join("_", tags));
                    try {
                        // 调用不分页的方法先缓存完整结果
                        List<User> allUsers = userService.searchUsersByTags(tags);
                        log.info("标签组合 {} 完整列表预热成功，共 {} 名用户", 
                                String.join("_", tags), allUsers.size());
                        totalCacheCount++;
                    } catch (Exception e) {
                        log.error("标签组合完整列表预热失败: {}", String.join("_", tags), e);
                    }
                }
                
                // 为每个热门标签组合预热分页结果
                for (List<String> tags : hotTagsList) {
                    // 对标签进行排序，确保相同的标签组合生成相同的缓存键
                    List<String> sortedTags = new ArrayList<>(tags);
                    Collections.sort(sortedTags);
                    
                    // 为每个热门标签组合预热指定的页码
                    for (Integer pageNum : PRELOAD_PAGE_NUMS) {
                        log.info("预热分页结果: {}，页码: {}, 每页大小: {}", 
                                String.join("_", sortedTags), pageNum, PAGE_SIZE);
                        
                        try {
                            // 直接调用分页方法获取结果并缓存
                            Page<User> userPage = userService.searchUsersByTagsWithPagination(tags, PAGE_SIZE, pageNum);
                            log.info("标签 {} 第 {} 页预热成功，共找到 {} 名用户", 
                                    String.join("_", sortedTags), pageNum, userPage.getTotal());
                            totalCacheCount++;
                        } catch (Exception e) {
                            log.error("标签分页预热失败: tags={}, page={}, size={}", 
                                    String.join("_", sortedTags), pageNum, PAGE_SIZE, e);
                        }
                    }
                }
                
                // 添加额外的热门组合预热
                List<List<String>> additionalCombinations = Arrays.asList(
                    Arrays.asList("Java", "开发"),
                    Arrays.asList("作曲", "唱歌"),
                    Arrays.asList("游戏", "键盘手"),
                    Arrays.asList("Java", "音乐", "动漫")
                );
                
                for (List<String> tags : additionalCombinations) {
                    try {
                        // 先缓存完整列表
                        List<User> allUsers = userService.searchUsersByTags(tags);
                        log.info("额外标签组合 {} 完整列表预热成功，共 {} 名用户", 
                                String.join("_", tags), allUsers.size());
                        totalCacheCount++;
                        
                        // 只预热第一页
                        log.info("预热额外标签组合: {}，页码: 1", String.join("_", tags));
                        Page<User> userPage = userService.searchUsersByTagsWithPagination(tags, PAGE_SIZE, 1);
                        log.info("额外标签组合 {} 预热成功，共找到 {} 名用户", 
                                String.join("_", tags), userPage.getTotal());
                        totalCacheCount++;
                    } catch (Exception e) {
                        log.error("额外标签组合预热失败: tags={}", String.join("_", tags), e);
                    }
                }
                
                // 记录预热完成情况
                long endTime = System.currentTimeMillis();
                log.info("标签缓存预热完成，共预热 {} 个缓存，耗时 {} 秒", 
                        totalCacheCount, (endTime - startTime) / 1000);
            }
        } catch (InterruptedException e) {
            log.error("标签搜索缓存预热任务异常", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("释放标签搜索缓存预热锁，线程ID: {}", Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
    
    /**
     * 手动触发标签缓存预热（用于测试和重置缓存）
     */
    public void manualTriggerCachePreheating() {
        log.info("手动触发标签缓存预热");
        doCacheTagSearch();
    }
    
    /**
     * 清理过期的缓存键模式
     * 每天凌晨3点执行一次
     */
    @Scheduled(cron = "0 0 3 * * *") // 每天凌晨3点执行
    public void cleanExpiredCacheKeys() {
        log.info("开始执行缓存键清理任务");
        RLock lock = redissonClient.getLock("educhain:precachejob:cleancache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("获取到缓存清理任务锁，线程ID: {}", Thread.currentThread().getId());
                
                // 清理所有标签相关的集合键
                Set<String> tagSetKeys = redisTemplate.keys("tag:*:keys");
                if (tagSetKeys != null && !tagSetKeys.isEmpty()) {
                    log.info("检查 {} 个标签集合键", tagSetKeys.size());
                    
                    for (String tagSetKey : tagSetKeys) {
                        // 获取标签集合中的所有缓存键
                        Set<Object> cacheKeys = redisTemplate.opsForSet().members(tagSetKey);
                        
                        if (cacheKeys != null && !cacheKeys.isEmpty()) {
                            // 检查每个缓存键是否存在，不存在则从集合中移除
                            for (Object cacheKey : cacheKeys) {
                                String key = cacheKey.toString();
                                Boolean exists = redisTemplate.hasKey(key);
                                
                                if (exists != null && !exists) {
                                    redisTemplate.opsForSet().remove(tagSetKey, key);
                                    log.info("从集合 {} 中移除过期的键: {}", tagSetKey, key);
                                }
                            }
                        } else {
                            // 如果集合为空，则删除整个集合
                            redisTemplate.delete(tagSetKey);
                            log.info("删除空的标签集合键: {}", tagSetKey);
                        }
                    }
                } else {
                    log.info("没有标签集合键需要清理");
                }
            }
        } catch (InterruptedException e) {
            log.error("缓存键清理任务异常", e);
        } catch (Exception e) {
            log.error("缓存键清理过程中出现异常", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("释放缓存清理任务锁，线程ID: {}", Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
