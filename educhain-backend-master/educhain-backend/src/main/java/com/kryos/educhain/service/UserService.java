package com.kryos.educhain.service;

import com.kryos.educhain.common.BaseResponse;
import com.kryos.educhain.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kryos.educhain.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 根据标签搜索用户（分页版）
     *
     * @param tagNameList 标签列表
     * @param pageSize 页面大小
     * @param pageNum 当前页码
     * @return 分页用户数据
     */
    Page<User> searchUsersByTagsWithPagination(List<String> tagNameList, long pageSize, long pageNum);

    /**
     * 根据标签搜索用户（Redis优化版，毫秒级响应）
     *
     * @param tagNameList 标签列表
     * @param pageSize 页面大小
     * @param pageNum 当前页码
     * @return 分页用户数据
     */
    Page<User> searchUsersByTagsOptimized(List<String> tagNameList, long pageSize, long pageNum);
    
    /**
     * 根据用户名或用户账号搜索用户（毫秒级响应）
     *
     * @param searchText 搜索文本，可以是用户名或账号
     * @param pageSize 页面大小
     * @param pageNum 当前页码
     * @return 分页用户数据
     */
    Page<User> searchUsersByUsernameOrAccount(String searchText, long pageSize, long pageNum);
    
    /**
     * 预热标签到用户的映射缓存
     * 全量缓存所有标签和用户的对应关系到Redis
     */
    void preHeatTagsUserMapping();

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);
}
