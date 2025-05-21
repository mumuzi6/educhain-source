package com.kryos.educhain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kryos.educhain.model.domain.Tag;

import java.util.List;
import java.util.Map;

/**
 * 标签服务
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取所有标签，按分类分组
     * @return 分类分组的标签
     */
    List<Map<String, Object>> listTagsByCategory();
    
    /**
     * 获取用户自定义标签
     * @param userId 用户id
     * @return 用户标签列表
     */
    List<Tag> getUserTags(Long userId);
    
    /**
     * 删除用户自定义标签
     * @param tagId 标签id
     * @param userId 用户id
     * @return 是否成功
     */
    boolean removeUserTag(Long tagId, Long userId);
} 