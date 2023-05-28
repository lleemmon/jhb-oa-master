package com.jhb.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhb.model.process.ProcessType;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
public interface OaProcessTypeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();
}
