package com.jhb.wechat.service;

public interface MessageService {
    /**
     * 推送待审批
     * @param processId
     * @param userId
     * @param taskId
     */
    void pushPendingMessage(Long processId, Long userId, String taskId);
}
