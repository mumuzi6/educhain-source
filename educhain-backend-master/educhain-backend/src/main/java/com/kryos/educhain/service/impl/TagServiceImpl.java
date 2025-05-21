package com.kryos.educhain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kryos.educhain.exception.BusinessException;
import com.kryos.educhain.common.ErrorCode;
import com.kryos.educhain.mapper.TagMapper;
import com.kryos.educhain.model.domain.Tag;
import com.kryos.educhain.service.TagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签服务实现类
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Map<String, Object>> listTagsByCategory() {
        // 1. 查询所有父标签（即分类）
        QueryWrapper<Tag> parentQuery = new QueryWrapper<>();
        parentQuery.eq("isParent", 1)
                  .eq("isDelete", 0);
        List<Tag> parentTags = this.list(parentQuery);
        
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        // 对每个父标签（分类），查询其子标签
        for (Tag parentTag : parentTags) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("text", parentTag.getTagName());
            
            // 查询该分类下的所有子标签
            QueryWrapper<Tag> childQuery = new QueryWrapper<>();
            childQuery.eq("parentId", parentTag.getId())
                     .eq("isParent", 0)
                     .eq("isDelete", 0);
            List<Tag> childTags = this.list(childQuery);
            
            // 构建子标签列表
            List<Map<String, Object>> children = new ArrayList<>();
            for (Tag childTag : childTags) {
                Map<String, Object> childMap = new HashMap<>();
                childMap.put("text", childTag.getTagName());
                childMap.put("id", childTag.getTagName());
                children.add(childMap);
            }
            
            categoryMap.put("children", children);
            resultList.add(categoryMap);
        }
        
        return resultList;
    }
    
    @Override
    public List<Tag> getUserTags(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 查询指定用户创建的标签
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                   .eq("isDelete", 0);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public boolean removeUserTag(Long tagId, Long userId) {
        if (tagId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        // 查询标签是否存在且属于该用户
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", tagId)
                   .eq("userId", userId)
                   .eq("isDelete", 0);
        
        Tag tag = this.getOne(queryWrapper);
        if (tag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不存在或不属于当前用户");
        }
        
        // 逻辑删除标签
        return this.removeById(tagId);
    }
} 