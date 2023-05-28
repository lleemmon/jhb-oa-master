package com.jhb.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhb.auth.service.SysUserService;
import com.jhb.model.process.ProcessRecord;
import com.jhb.model.process.ProcessTemplate;
import com.jhb.model.system.SysUser;
import com.jhb.process.mapper.OaProcessMapper;
import com.jhb.process.mapper.OaProcessTemplateMapper;
import com.jhb.process.service.OaProcessRecordService;
import com.jhb.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhb.process.service.OaProcessTemplateService;
import com.jhb.security.custom.LoginUserInfoHelper;
import com.jhb.vo.process.ApprovalVo;
import com.jhb.vo.process.ProcessFormVo;
import com.jhb.vo.process.ProcessQueryVo;
import com.jhb.vo.process.ProcessVo;

import com.jhb.wechat.service.MessageService;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.jhb.model.process.Process;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;
import org.activiti.engine.task.Task;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author jhb
 * @since 2023-03-24
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private OaProcessTemplateMapper oaProcessTemplateMapper;
    @Resource
    private RuntimeService runtimeService;

    @Resource
    private OaProcessRecordService processRecordService;
    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Resource
    private MessageService messageService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        return baseMapper.selectPage(pageParam, processQueryVo);
    }

    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
    }

    @Override
    @Transactional
    public void startUp(ProcessFormVo processFormVo) {
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        ProcessTemplate processTemplate = oaProcessTemplateMapper.selectById(processFormVo.getProcessTemplateId());
        // 保存提交审批信息到业务表 oa_process
        Process process = new Process();
        //复制processFormVo 到 process中
        BeanUtils.copyProperties(processFormVo, process);
        process.setStatus(1);//审批中
        process.setUserId(sysUser.getId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName()+"发起"+processTemplate.getName()+"申请");
        baseMapper.insert(process);
        //启动流程实例
        //流程定义key
        //业务key processId
        // 流程参数 form表单json数据 转换map集合
        String formValues = processFormVo.getFormValues();
        JSONObject formValueObject = JSON.parseObject(formValues);
        JSONObject formDataObject = formValueObject.getJSONObject("formData");
        Map<String, Object> formDataMap = new HashMap<>();
        formDataMap.put("data", new HashMap<>(formDataObject));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processTemplate.getProcessDefinitionKey(),
                String.valueOf(process.getId()), formDataMap);
        //查询下一个审批人 审批人可能有多个
        List<Task> list = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        list.forEach(task -> {
            String assignee = task.getAssignee();
            SysUser user = sysUserService.getByUsername(assignee);
            nameList.add(user.getName());
            messageService.pushPendingMessage(process.getId(), user.getId(),task.getId());
        });
        //业务和流程关联 更新 oa_process数据
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");
        baseMapper.updateById(process);

        //记录操作审批信息
        processRecordService.record(process.getId(), 1, "发起申请");
    }

    @Override
    public IPage<ProcessVo> findfindPending(Page<java.lang.Process> pageParam) {
        //1根据当前登录的用户名称进行查询
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime().desc();
        //参数1 开始位置 参数2 每页显示记录数
        int begin = (int)((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        long total = taskQuery.count();
        List<Task> tasks = taskQuery.listPage(begin, size);
        List<ProcessVo> processVos = new ArrayList<>();
        //转换task 为 processVo
        tasks.forEach(task -> {
            String processInstanceId = task.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceTenantId(processInstanceId)
                    .singleResult();
            String businessKey = processInstance.getBusinessKey();
            if(!StringUtils.isEmpty(businessKey)){
                Process process = baseMapper.selectById(businessKey);
                ProcessVo processVo = new ProcessVo();
                BeanUtils.copyProperties(process, processVo);
                processVo.setTaskId(task.getId());
                processVos.add(processVo);
            }
        });
        //2调用方法分页查询 返回list集合 待办任务合集
        //3封装返回list集合数据 到List<ProcessVo>中
        //4封装返回IPage对象
        IPage<ProcessVo>page = new Page<>(pageParam.getCurrent(),
                pageParam.getSize(), total);
        page.setRecords(processVos);
        return page;
    }

    //查看审批详情信息
    @Override
    public Map<String, Object> show(Long id) {
        //1 根据流程id获取流程信息process
        Process process = baseMapper.selectById(id);
        //2 根据流程id获取流程记录信息
        LambdaQueryWrapper<ProcessRecord>wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId, process);
        List<ProcessRecord> processRecords = processRecordService.list(wrapper);
        //3 根据模板id查询模板信息
        ProcessTemplate processTemplate = oaProcessTemplateMapper.selectById(process.getProcessTemplateId());
        //4 判断当前用户是否可以进行审批
        boolean isApprove = false;
        List<Task> currentTaskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for(Task task: currentTaskList){
            String username = LoginUserInfoHelper.getUsername();
            if(task.getAssignee().equals(username)){
                isApprove = true;
            }
        }
        //5 将查询的数据进行封装
        Map<String, Object>map = new HashMap<>();
        map.put("isApprove", isApprove);//??
        map.put("process", process);
        map.put("processRecordList", processRecords);
        map.put("processTemplate", processTemplate);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //1 从approvalVo获取任务id，根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        //2 判断审批状态值
        Map<String, Object> variables = taskService.getVariables(taskId);
        // 1 审批通过         // -1 驳回
        if(approvalVo.getStatus() == 1){
            Map<String, Object> variable = new HashMap<>();
            taskService.complete(taskId, variable);
        }else{
            //taskService
            this.endTask(taskId);
        }
        //3 记录审批相关过程信息
        String desc = approvalVo.getDescription();
        if(StringUtils.isEmpty(desc)){
            desc = approvalVo.getStatus() == 1 ? "通过" : "驳回";
        }
        processRecordService.record(approvalVo.getProcessId(),
                approvalVo.getStatus(),
                desc);
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        //查询任务
        List<Task> currentTaskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(currentTaskList)){
            List<String> assignList = new ArrayList<>();
            currentTaskList.forEach(task -> {
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getByUsername(assignee);
                assignList.add(sysUser.getName());
                //公众号消息推送
            });
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else{
            if(approvalVo.getStatus() == 1){
                process.setDescription("审批完成(通过)");
                process.setStatus(2);
            }else{
                process.setDescription("审批完成(驳回)");
                process.setStatus(-1);
            }
        }
        //4 查询下一个审批人 更新process表记录
        baseMapper.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<ProcessVo> pageParam) {
        // 封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished().orderByTaskCreateTime().desc();
        // 调用方法条件分页查询，返回list集合
        int begin = (int)((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int)pageParam.getSize();
        int count = (int)query.count();
        query.listPage(begin, size);
        // 封装List<ProcessVo>
        //IPage封装分页查询所有数据，返回
        List<ProcessVo> processVos = new ArrayList<>();
        processVos.forEach(item -> {
            String processInstanceId = item.getProcessInstanceId();
            LambdaQueryWrapper<Process>wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId, processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVos.add(processVo);
        });
        IPage<ProcessVo> processVoPage = new Page<>(begin, size, count);
        processVoPage.setRecords(processVos);
        return processVoPage;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        return baseMapper.selectPage(pageParam, processQueryVo);
    }

    // 结束流程
    private void endTask(String taskId) {
        //根据任务id获取任务对象 Task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //获取流程定义模型 BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //获取结束流向节点
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)){
            return;
        }
        FlowNode endFlowNode = endEventList.get(0);
        //当前流向的节点
        FlowNode currentFlowNode = (FlowNode)bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //临时保存当前活动的原始方向
        List<SequenceFlow> originalSequenceFlowList = new ArrayList();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //清理当前流动方向
        currentFlowNode.getOutgoingFlows().clear();
        //创建新流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlow");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        //当前节点指向新方向
        List<SequenceFlow> newSequenceFlowList = new ArrayList();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //完成当前任务
        taskService.complete(taskId);
    }

    private List<Task> getCurrentTaskList(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }


}
