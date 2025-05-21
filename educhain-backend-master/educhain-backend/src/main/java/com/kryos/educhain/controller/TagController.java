package com.kryos.educhain.controller;

import com.kryos.educhain.common.BaseResponse;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.common.ResultUtils;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.model.domain.Tag;
import com.kryos.educhain.model.domain.User;
import com.kryos.educhain.service.TagService;
import com.kryos.educhain.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.kryos.educhain.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 标签接口
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class TagController {

    @Resource
    private TagService tagService;
    
    @Resource
    private UserService userService;

    /**
     * 获取所有标签（按分类分组）
     * @return 标签列表
     */
    @GetMapping("/tags")
    public BaseResponse<List<Map<String, Object>>> getAllTags() {
        List<Map<String, Object>> tagList = tagService.listTagsByCategory();
        return ResultUtils.success(tagList);
    }

    /**
     * 添加标签（管理员使用）
     * @param tag 标签信息
     * @return 是否成功
     */
    @PostMapping("/tags")
    public BaseResponse<Boolean> addTag(@RequestBody Tag tag) {
        boolean result = tagService.save(tag);
        return ResultUtils.success(result);
    }
    
    /**
     * 用户添加自定义标签
     * @param tag 标签信息
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/user/tags")
    public BaseResponse<Boolean> addUserTag(@RequestBody Tag tag, HttpServletRequest request) {
        // 校验登录状态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User loginUser = (User) userObj;
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        // 设置标签所属用户ID
        tag.setUserId(loginUser.getId());
        // 设置为普通标签(非父标签)
        tag.setIsParent(0);
        
        boolean result = tagService.save(tag);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户自定义标签
     * @param request HTTP请求
     * @return 用户标签列表
     */
    @GetMapping("/user/tags")
    public BaseResponse<List<Tag>> getUserTags(HttpServletRequest request) {
        // 校验登录状态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User loginUser = (User) userObj;
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        List<Tag> userTags = tagService.getUserTags(loginUser.getId());
        return ResultUtils.success(userTags);
    }

    /**
     * 删除标签（管理员使用）
     * @param id 标签id
     * @return 是否成功
     */
    @DeleteMapping("/tags/{id}")
    public BaseResponse<Boolean> deleteTag(@PathVariable Long id) {
        boolean result = tagService.removeById(id);
        return ResultUtils.success(result);
    }
    
    /**
     * 用户删除自己的标签
     * @param id 标签id
     * @param request HTTP请求
     * @return 是否成功
     */
    @DeleteMapping("/user/tags/{id}")
    public BaseResponse<Boolean> deleteUserTag(@PathVariable Long id, HttpServletRequest request) {
        // 校验登录状态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User loginUser = (User) userObj;
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        boolean result = tagService.removeUserTag(id, loginUser.getId());
        return ResultUtils.success(result);
    }
} 