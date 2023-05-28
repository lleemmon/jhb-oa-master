package com.jhb.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jhb.model.process.ProcessType;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
@Mapper
public interface OaProcessTypeMapper extends BaseMapper<ProcessType> {

}
