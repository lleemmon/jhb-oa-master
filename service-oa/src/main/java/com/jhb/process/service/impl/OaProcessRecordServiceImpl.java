package com.jhb.process.service.impl;

import com.jhb.auth.mapper.SysUserMapper;
import com.jhb.model.process.ProcessRecord;
import com.jhb.model.system.SysUser;
import com.jhb.process.mapper.OaProcessRecordMapper;
import com.jhb.process.service.OaProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhb.security.custom.LoginUserInfoHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
@Service
public class OaProcessRecordServiceImpl extends ServiceImpl<OaProcessRecordMapper, ProcessRecord> implements OaProcessRecordService {

    @Resource
    private OaProcessRecordMapper oaProcessRecordMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserMapper.selectById(userId);
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUser(sysUser.getName());
        processRecord.setOperateUserId(userId);
        oaProcessRecordMapper.insert(processRecord);
    }
}
