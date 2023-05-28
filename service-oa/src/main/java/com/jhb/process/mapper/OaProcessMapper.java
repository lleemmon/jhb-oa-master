package com.jhb.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhb.vo.process.ProcessQueryVo;
import com.jhb.vo.process.ProcessVo;
import org.apache.ibatis.annotations.Mapper;
import com.jhb.model.process.Process;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
@Mapper
public interface OaProcessMapper extends BaseMapper<Process> {
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam,
                                @Param("vo") ProcessQueryVo processQueryVo);
}
