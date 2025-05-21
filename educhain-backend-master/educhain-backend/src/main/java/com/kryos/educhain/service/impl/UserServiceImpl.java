package com.kryos.educhain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.constant.UserConstant;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.model.vo.UserVO;
import com.kryos.educhain.service.UserService;
import com.kryos.educhain.mapper.UserMapper;
import com.kryos.educhain.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kryos.educhain.constant.UserConstant.SALT;
import static com.kryos.educhain.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】'；：\"\"'。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】'；：\"\"'。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户（SQL 查询版）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 先看是否有缓存
        String cacheKey = String.format("user:tags:%s:all", 
                String.join("_", tagNameList));
        Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return (List<User>) cachedResult;
        }
        
        // 没有缓存，查询数据库 - 使用SQL LIKE查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 只查询必要字段
        queryWrapper.select("id", "username", "userAccount", "avatarUrl", "gender", 
                "phone", "email", "userStatus", "createTime", "tags", "userRole", "planetCode");
        queryWrapper.eq("userStatus", 0);
        queryWrapper.eq("isDelete", 0);
        
        // 拼接查询条件：所有标签必须都匹配（AND连接）
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        
        // 直接查询筛选
        List<User> userList = userMapper.selectList(queryWrapper);
        log.info("SQL标签查询: 标签={}, 匹配用户数={}", String.join(",", tagNameList), userList.size());
        
        // 转换为安全用户
        List<User> result = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
        
        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);
        
        // 记录缓存键到标签集合
        for (String tag : tagNameList) {
            String tagKeysSet = "tag:" + tag + ":keys";
            redisTemplate.opsForSet().add(tagKeysSet, cacheKey);
        }
        
        return result;
    }
    
    /**
     * 根据标签搜索用户（内存过滤 + 分页）
     *
     * @param tagNameList 用户要拥有的标签
     * @param pageSize 页面大小
     * @param pageNum 当前页码
     * @return 分页用户数据
     */
    @Override
    public Page<User> searchUsersByTagsWithPagination(List<String> tagNameList, long pageSize, long pageNum) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 对标签列表进行排序，确保不同顺序的相同标签生成相同的缓存键
        List<String> sortedTags = new ArrayList<>(tagNameList);
        Collections.sort(sortedTags);
        
        // 生成缓存键：user:tags:Java_Python:page:1:size:10
        String cacheKey = String.format("user:tags:%s:page:%d:size:%d", 
                String.join("_", sortedTags), pageNum, pageSize);
        
        log.info("查询缓存，键: {}", cacheKey);
        
        // 查询缓存
        Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            log.info("缓存命中，返回缓存结果");
            return (Page<User>) cachedResult;
        }
        
        log.info("缓存未命中，执行标签搜索 - 标签：{}，页码：{}，每页数量：{}", tagNameList, pageNum, pageSize);
        
        // 创建查询包装器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 设置基本条件
        queryWrapper.eq("userStatus", 0);
        queryWrapper.eq("isDelete", 0);
        
        // 拼接查询条件：所有标签必须都匹配（AND连接）
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        
        // 创建计数查询包装器 - 注意：不要在计数查询中使用select()方法
        QueryWrapper<User> countQueryWrapper = new QueryWrapper<>();
        countQueryWrapper.eq("userStatus", 0);
        countQueryWrapper.eq("isDelete", 0);
        for (String tagName : tagNameList) {
            countQueryWrapper = countQueryWrapper.like("tags", tagName);
        }
        
        // 计算总记录数
        long total = userMapper.selectCount(countQueryWrapper);
        log.info("SQL标签查询匹配用户总数: {}", total);
        
        // 创建分页查询对象
        Page<User> page = new Page<>(pageNum, pageSize);
        
        // 如果没有匹配的记录，直接返回空页
        if (total == 0) {
            page.setRecords(new ArrayList<>());
            // 缓存空结果，避免缓存穿透，设置较短的过期时间
            redisTemplate.opsForValue().set(cacheKey, page, 5, TimeUnit.MINUTES);
            
            // 记录缓存键到标签集合
            for (String tag : tagNameList) {
                String tagKeysSet = "tag:" + tag + ":keys";
                redisTemplate.opsForSet().add(tagKeysSet, cacheKey);
            }
            
            return page;
        }
        
        // 设置查询字段 - 仅适用于分页查询，不用于计数查询
        queryWrapper.select("id", "username", "userAccount", "avatarUrl", "gender", 
                "phone", "email", "userStatus", "createTime", "tags", "userRole", "planetCode");
                
        // 执行分页查询
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为安全用户列表
        List<User> safetyUsers = userPage.getRecords().stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toList());
        userPage.setRecords(safetyUsers);
        
        // 将分页结果存入缓存，设置30分钟过期
        log.info("将搜索结果存入缓存，键: {}", cacheKey);
        redisTemplate.opsForValue().set(cacheKey, userPage, 30, TimeUnit.MINUTES);
        
        // 将缓存键加入标签缓存集合，用于后续清除
        for (String tag : tagNameList) {
            String tagKeysSet = "tag:" + tag + ":keys";
            redisTemplate.opsForSet().add(tagKeysSet, cacheKey);
        }
        
        return userPage;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        
        // 查询旧用户信息
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        
        // 检查参数，如果用户没有传任何要更新的值，直接返回错误
        boolean hasUpdates = false;
        
        // 检查用户标签是否更新
        boolean isTagsUpdated = false;
        if (user.getTags() != null && !user.getTags().equals(oldUser.getTags())) {
            isTagsUpdated = true;
            hasUpdates = true;
            log.info("用户 {} 的标签已更新: {} -> {}", userId, oldUser.getTags(), user.getTags());
        }
        
        // 检查其他字段是否有更新
        if (user.getUsername() != null && !user.getUsername().equals(oldUser.getUsername())) {
            hasUpdates = true;
        }
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().equals(oldUser.getAvatarUrl())) {
            hasUpdates = true;
        }
        // 可以继续添加其他字段的检查...
        
        // 如果没有任何更新，直接返回
        if (!hasUpdates) {
            log.info("用户 {} 没有提供任何需要更新的信息", userId);
            return 0;
        }
        
        // 设置用户信息更新时间
        user.setUpdateTime(new Date());
        
        // 更新用户信息
        int result = userMapper.updateById(user);
        
        // 如果标签已更新且更新成功，清除该用户相关的缓存
        if (isTagsUpdated && result > 0) {
            log.info("标签更新成功，用户ID: {}，清除相关缓存", userId);
            clearUserTagsCache(oldUser);
        }
        
        return result;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(Pair.of(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 清除用户标签相关的缓存
     */
    private void clearUserTagsCache(User user) {
        if (user == null || StringUtils.isBlank(user.getTags())) {
            return;
        }
        
        try {
            // 解析用户标签
            Gson gson = new Gson();
            Set<String> userTags = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {}.getType());
            
            if (userTags != null && !userTags.isEmpty()) {
                // 遍历用户的每个标签
                for (String tag : userTags) {
                    String tagKeysSet = "tag:" + tag + ":keys";
                    
                    // 获取该标签关联的所有缓存键
                    Set<Object> cacheKeys = redisTemplate.opsForSet().members(tagKeysSet);
                    
                    if (cacheKeys != null && !cacheKeys.isEmpty()) {
                        // 删除所有关联的缓存
                        for (Object cacheKey : cacheKeys) {
                            log.info("删除标签缓存，键: {}", cacheKey);
                            redisTemplate.delete(cacheKey.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("清除用户标签缓存异常: {}", e.getMessage(), e);
        }
    }

}




