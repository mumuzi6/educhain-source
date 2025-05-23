package com.kryos.educhain.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kryos.educhain.common.BaseResponse;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.common.ResultUtils;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.model.request.UserLoginRequest;
import com.kryos.educhain.model.request.UserRegisterRequest;
import com.kryos.educhain.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Set;

import static com.kryos.educhain.constant.UserConstant.USER_LOGIN_STATE;
import com.kryos.educhain.job.PreCacheJob;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PreCacheJob preCacheJob;

    @Resource
    private Gson gson;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        // 用户不存在或已被删除
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 根据标签搜索用户（分页版，带缓存）
     *
     * @param tagNameList 标签列表
     * @param pageSize 页面大小
     * @param pageNum 当前页码
     * @return 分页用户数据
     */
    @GetMapping("/search/tags/page")
    public BaseResponse<Page<User>> searchUsersByTagsWithPagination(
            @RequestParam(required = false) List<String> tagNameList,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(defaultValue = "1") long pageNum) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 使用优化后的方法替代原方法，毫秒级响应
        Page<User> userPage = userService.searchUsersByTagsOptimized(tagNameList, pageSize, pageNum);
        return ResultUtils.success(userPage);
    }
    
//    /**
//     * 根据标签搜索用户（传统版本，保留用于比较）
//     *
//     * @param tagNameList 标签列表
//     * @param pageSize 页面大小
//     * @param pageNum 当前页码
//     * @return 分页用户数据
//     */
//    @GetMapping("/search/tags/page/legacy")
//    public BaseResponse<Page<User>> searchUsersByTagsWithPaginationLegacy(
//            @RequestParam(required = false) List<String> tagNameList,
//            @RequestParam(defaultValue = "10") long pageSize,
//            @RequestParam(defaultValue = "1") long pageNum) {
//        if (CollectionUtils.isEmpty(tagNameList)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        
//        // 对标签列表进行排序，确保相同的标签组合生成相同的缓存键
//        List<String> sortedTagList = new ArrayList<>(tagNameList);
//        Collections.sort(sortedTagList);
//        
//        // 构造Redis缓存键 - 包含标签列表、页码和页大小
//        String redisKey = String.format("educhain:user:tags:%s:page:%d:size:%d", 
//                String.join(",", sortedTagList), pageNum, pageSize);
//                
//        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
//        
//        // 尝试从缓存中获取分页结果
//        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
//        if (userPage != null) {
//            log.info("从缓存中获取标签 {} 的用户列表 (页码:{}, 每页大小:{})", 
//                    String.join(",", sortedTagList), pageNum, pageSize);
//            return ResultUtils.success(userPage);
//        }
//        
//        // 缓存未命中，从数据库查询并进行分页处理
//        log.info("从数据库获取标签 {} 的用户列表 (页码:{}, 每页大小:{})", 
//                String.join(",", sortedTagList), pageNum, pageSize);
//        
//        // 使用Service层的分页方法直接获取分页结果
//        userPage = userService.searchUsersByTagsWithPagination(tagNameList, pageSize, pageNum);
//        
//        // 写入缓存，设置过期时间为30分钟
//        try {
//            valueOperations.set(redisKey, userPage, 30, TimeUnit.MINUTES);
//            
//            // 生成缓存键模式，用于后续失效相关缓存
//            String redisKeyPattern = String.format("educhain:user:tags:%s:*", String.join(",", sortedTagList));
//            // 将键模式添加到一个集合中，方便管理
//            String cacheKeysSetKey = "educhain:cache:tagSearchKeys";
//            redisTemplate.opsForSet().add(cacheKeysSetKey, redisKeyPattern);
//        } catch (Exception e) {
//            log.error("Redis设置缓存失败", e);
//        }
//        
//        return ResultUtils.success(userPage);
//    }

    // todo 推荐多个，未实现，写到Service?
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("educhain:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS); //timeout过期时间
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        
        // 根据优化版的标签搜索缓存策略清理相关缓存
        if (result > 0 && user.getTags() != null) {
            try {
                log.info("用户 {} 更新了标签，开始清理相关缓存", loginUser.getId());
                
                // 获取用户标签列表
                String tagsStr = user.getTags();
                if (tagsStr != null && !tagsStr.isEmpty()) {
                    List<String> tagList = gson.fromJson(tagsStr, new TypeToken<List<String>>(){}.getType());
                    if (tagList != null && !tagList.isEmpty()) {
                        
                        // 1. 清理标签-用户映射缓存
                        // 格式: "tag:{标签名}:userIds"
                        for (String tag : tagList) {
                            String tagUserMappingKey = String.format("tag:%s:userIds", tag);
                            Boolean exists = redisTemplate.hasKey(tagUserMappingKey);
                            if (exists != null && exists) {
                                redisTemplate.delete(tagUserMappingKey);
                                log.info("已清除标签 {} 的用户映射缓存", tag);
                            }
                            
                            // 2. 清理标签关联的缓存键集合
                            // 格式: "tag:{标签名}:keys"
                            String tagKeysSetKey = String.format("tag:%s:keys", tag);
                            Set<Object> cacheKeys = redisTemplate.opsForSet().members(tagKeysSetKey);
                            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                                int deletedCount = 0;
                                for (Object cacheKey : cacheKeys) {
                                    String key = cacheKey.toString();
                                    Boolean keyExists = redisTemplate.hasKey(key);
                                    if (keyExists != null && keyExists) {
                                        redisTemplate.delete(key);
                                        deletedCount++;
                                    }
                                }
                                if (deletedCount > 0) {
                                    log.info("已清除标签 {} 关联的 {} 个缓存键", tag, deletedCount);
                                }
                            }
                        }
                        
                        // 3. 清理用户详情缓存
                        // 格式: "user:{用户ID}"
                        String userCacheKey = String.format("user:%d", user.getId());
                        Boolean userKeyExists = redisTemplate.hasKey(userCacheKey);
                        if (userKeyExists != null && userKeyExists) {
                            redisTemplate.delete(userCacheKey);
                            log.info("已清除用户 {} 的详情缓存", user.getId());
                        }
                        
                        // 4. 清理包含这些标签的组合查询缓存
                        // 格式: "user:tags:optimized:{标签1}_{标签2}:page:{页码}:size:{页大小}"
                        for (String tag : tagList) {
                            // 查找所有包含此标签的优化缓存键
                            Set<String> optimizedKeys = redisTemplate.keys(String.format("user:tags:optimized:*%s*:page:*:size:*", tag));
                            if (optimizedKeys != null && !optimizedKeys.isEmpty()) {
                                redisTemplate.delete(optimizedKeys);
                                log.info("已清除包含标签 {} 的 {} 个标签组合缓存", tag, optimizedKeys.size());
                            }
                        }
                    }
                }
                
                log.info("用户 {} 标签相关缓存清理完成", loginUser.getId());
            } catch (Exception e) {
                log.error("清除标签搜索缓存失败: {}", e.getMessage(), e);
            }
        }
        
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public BaseResponse<User> getUserById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return ResultUtils.success(userService.getSafetyUser(user));
    }

    /**
     * 手动触发缓存预热（仅管理员可用）
     */
    @GetMapping("/admin/trigger-cache-preheat")
    public BaseResponse<Boolean> triggerCachePreheat(HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        
        log.info("管理员手动触发缓存预热");
        try {
            // 在新线程中执行，避免阻塞响应
            Thread preCacheThread = new Thread(() -> {
                preCacheJob.manualTriggerCachePreheating();
            });
            preCacheThread.setName("manual-cache-preheat");
            preCacheThread.start();
            
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("手动触发缓存预热失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "触发失败");
        }
    }
    
    /**
     * 手动触发标签-用户映射预热（仅管理员可用）
     */
    @GetMapping("/admin/trigger-tags-user-mapping")
    public BaseResponse<Boolean> triggerTagsUserMappingPreheat(HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        
        log.info("管理员手动触发标签-用户映射预热");
        try {
            // 在新线程中执行，避免阻塞响应
            Thread preCacheThread = new Thread(() -> {
                userService.preHeatTagsUserMapping();
                log.info("标签-用户映射预热完成");
            });
            preCacheThread.setName("manual-tags-user-mapping");
            preCacheThread.start();
            
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("手动触发标签-用户映射预热失败", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "触发失败");
        }
    }

}
