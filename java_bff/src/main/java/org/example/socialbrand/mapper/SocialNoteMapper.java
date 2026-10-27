package org.example.socialbrand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.socialbrand.entity.SocialNote;

/**
 * 公开笔记 Mapper。
 */
@Mapper
public interface SocialNoteMapper extends BaseMapper<SocialNote> {
}
