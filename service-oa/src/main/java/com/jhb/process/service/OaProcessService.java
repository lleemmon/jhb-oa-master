package com.jhb.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jhb.model.process.Process;
import com.jhb.vo.process.ApprovalVo;
import com.jhb.vo.process.ProcessFormVo;
import com.jhb.vo.process.ProcessQueryVo;
import com.jhb.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
public interface OaProcessService extends IService<Process> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);

    void deployByZip(String deployPath);

    void startUp(ProcessFormVo processFormVo);

    IPage<ProcessVo> findfindPending(Page<java.lang.Process> pageParam);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    IPage<ProcessVo> findProcessed(Page<ProcessVo> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
