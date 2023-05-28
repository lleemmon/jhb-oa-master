package com.jhb.auth.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ProcessTest {
    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Test
    public void findTaskList(){
        String assign = "zhangsan";
        List<Task> list = taskService.createTaskQuery().taskAssignee(assign).list();
        list.forEach(item -> {
            System.out.println("流程实例id" + item.getProcessInstanceId());
            System.out.println("任务id" + item.getId());
            System.out.println("任务负责人" + item.getAssignee());
            System.out.println("任务名称" + item.getName());
        });
    }

    //处理当前任务
    @Test
    public void completTask(){
        taskService.createTaskQuery().list();

        Task task = taskService.createTaskQuery().taskAssignee("zhangsan").singleResult();
        // 完成任务 参数 id
        taskService.complete(task.getId());
    }

    //查询已处理任务
    @Test
    public void findCompletedTask(){
        List<HistoricTaskInstance> zhangsan = historyService.createHistoricTaskInstanceQuery().taskAssignee("zhangsan")
                .finished().list();
        zhangsan.forEach(item -> {
            System.out.println("流程实例id:" + item.getProcessInstanceId());
            System.out.println("任务id:" + item.getId());
            System.out.println("任务负责人:" + item.getAssignee());
            System.out.println("任务名称:" + item.getName());
        });
    }

    @Test
    private void deployProcess(){
//        Deployment deploy = repositoryService.createDeployment()
//                .addClasspathResource("process/qingjia.bpmn20.xml")
//                .addClasspathResource("process/请假.png")
//                .name("请假申请流程")
//                .deploy();
//        System.out.println(deploy.getId());
//        System.out.println(deploy.getName());
//
//        ProcessInstance qingjia = runtimeService.startProcessInstanceByKey("qingjia");
//        System.out.println("流程定义id:" + qingjia.getProcessDefinitionId());
//        System.out.println("流程实例id:" + qingjia.getId());
//        System.out.println("流程活动id" + qingjia.getActivityId());

        //  ############2#################
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/jiaban.bpmn20.xml")
                .addClasspathResource("process/jiaban.png")
                .name("加班申请流程")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());

        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee1", "zhangsan");
        variables.put("assignee2", "lisi");
        ProcessInstance jiaban = runtimeService.startProcessInstanceByKey("jiaban", variables);
        System.out.println("流程定义id:" + jiaban.getProcessDefinitionId());
        System.out.println("流程实例id:" + jiaban.getId());
        System.out.println("流程活动id" + jiaban.getActivityId());
    }

    @Test
    public void startUpProcessAddBusinessKey(){
        String businessKey = "1";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia", businessKey);
        System.out.println(processInstance.getBusinessKey());
    }

    //挂起所有流程实例
    @Test
    public void suspendProcessInstance(){
        ProcessDefinition qingjia = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("qingjia").singleResult();
        boolean suspended = qingjia.isSuspended();
        if(suspended){
            //g挂起 可以进行激活 参数1 流程定义的id 参数2 是否激活 参数3 时间点
            repositoryService.activateProcessDefinitionById(qingjia.getId(), true, null);
            System.out.println("流程定义：" + qingjia.getId() + "激活");
        }else{
            repositoryService.suspendProcessDefinitionById(qingjia.getId(), true, null);
            System.out.println("流程定义：" + qingjia.getId() + "挂起");
        }
    }

    //挂起单个所有流程实例
    //对应流程定义被挂起了 所属的流程实例会找不到
    @Test
    public void suspendSingleProcessInstance(){
        String processInstanceId = "";
        ProcessInstance qingjia = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).
                singleResult();
        boolean suspended = qingjia.isSuspended();
        if(suspended){
            runtimeService.activateProcessInstanceById(processInstanceId);
            System.out.println("流程实例：" + qingjia.getId() + "激活");
        }else{
            runtimeService.suspendProcessInstanceById(processInstanceId);
            System.out.println("流程实例：" + qingjia.getId() + "挂起");
        }
    }
}
