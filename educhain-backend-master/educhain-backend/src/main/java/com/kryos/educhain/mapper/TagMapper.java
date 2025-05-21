package com.kryos.educhain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kryos.educhain.model.domain.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
} 