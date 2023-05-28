package com.jhb.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhb.model.process.ProcessRecord;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
public interface OaProcessRecordService extends IService<ProcessRecord> {
    void record(Long processId, Integer status, String description);
}
