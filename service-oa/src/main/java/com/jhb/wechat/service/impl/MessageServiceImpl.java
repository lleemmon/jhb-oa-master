package com.jhb.wechat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jhb.auth.service.SysUserService;
import com.jhb.model.process.Process;
import com.jhb.model.process.ProcessTemplate;
import com.jhb.model.system.SysUser;
import com.jhb.process.mapper.OaProcessMapper;
import com.jhb.process.mapper.OaProcessTemplateMapper;
import com.jhb.process.service.OaProcessTemplateService;
import com.jhb.wechat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {
    @Resource
    private OaProcessMapper oaProcessMapper;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private OaProcessTemplateMapper processTemplateMapper;
    @Resource
    private WxMpService wxMpService;

    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        //根据id查询数据
        Process process = oaProcessMapper.selectById(processId);
        SysUser sysUser = sysUserService.getById(userId);
        ProcessTemplate processTemplate = processTemplateMapper.selectById(process.getProcessTemplateId());
        //获取提交审批人的信息
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        //设置消息并发送信息
        // toUser 要给人发的openId
        String openId = sysUser.getOpenId();
        if(StringUtils.isEmpty(openId)){
            openId = "oTqDI6Ao13S60PKDeDz6k1atfsKY";
        }

        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer sb = new StringBuffer();
        for(Map.Entry entry: formShowData.entrySet()){
            sb.append(entry.getKey()).append("：").append(entry.getValue());
        }

        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(openId)
                .templateId("aLkNzQNdLDF1HwSV4_WyzAnDPQ_KYbdG9xvpjhtZlsE")
                .url("http://jhb.free.idcfengye.com#/show/" + processId + "/" + taskId).build();//点击消息 跳转的地址
        templateMessage.addData(new WxMpTemplateData("first", submitSysUser.getName() + "提交了" + processTemplate.getName() + ",请注意查看", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("content", sb.toString(), "#272727"));
        try {
            String msg = null;
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            log.info("推送消息返回:{}", msg);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
