package org.example.supplyradar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.supplyradar.entity.TargetCompany;

/**
 * 重点监测企业 Mapper。
 */
@Mapper
public interface TargetCompanyMapper extends BaseMapper<TargetCompany> {
}
