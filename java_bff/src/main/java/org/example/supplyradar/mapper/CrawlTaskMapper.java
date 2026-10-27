package org.example.supplyradar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.supplyradar.entity.CrawlTask;

/**
 * 采集任务 Mapper。
 */
@Mapper
public interface CrawlTaskMapper extends BaseMapper<CrawlTask> {
}
