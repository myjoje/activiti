package com.myjoje.act;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.myjoje.util.Message;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class ActService {

    public final static String PROC_DEF_KEY = "myProcess_1";

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private IdentityService identityService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    /**
     * 使用IdentityService添加用户
     */
    public Object addUser() {
        User user;
        for (int i = 2; i <= 11; i++) {
            user = new UserEntity();
            user.setEmail("56187825@qq.com");
            user.setFirstName("user" + i);
            user.setLastName("user" + i);
            user.setId("000" + i);
            identityService.saveUser(user);
        }
        return Message.success("保存成功");
    }

    /**
     * 部署流程引擎
     */
    public Object deploy() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("processes/process.bpmn")
                .deploy();
        return JSONUtil.toJsonStr(deploy);
    }

    /**
     * 查询流程部署信息
     */
    public Object queryDeploy() {
        List<Deployment> list = repositoryService.createDeploymentQuery().list();
        return JSONUtil.toJsonStr(list);
    }

    /**
     * 删除流程部署信息
     */
    public Object delDeploy() {
        //删除后三张表信息会全部删除
        repositoryService.deleteDeployment("15001");
        return Message.success("删除成功");
    }

    /**
     * 查询流程定义信息
     */
    public Object queryProcDef() {
        ProcessDefinition result = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("myProcess_1").latestVersion().singleResult();
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 挂起流程定义：不允许新的流程实例创建提交
     */
    public Object suspendProcDef() {
        repositoryService.suspendProcessDefinitionByKey("myProcess_1");
        return Message.success("挂起成功");
    }

    /**
     * 激活流程定义
     */
    public Object activeProcDef() {
        repositoryService.activateProcessDefinitionByKey("myProcess_1");
        return Message.success("激活成功");
    }

    /**
     * 启动流程
     */
    public Object start() {
        //启动流程的业务主键
        String businessKey = "001";
        //启动流程携带的额外参数,供整个流程去使用
        Map variables = MapUtil.of("name", "jack");
        //设置流程的发起人
        identityService.setAuthenticatedUserId("0002");
        //启动流程
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, businessKey, variables);

        System.out.println(instance.getId());
        return Message.success();
    }

    /**
     * 完成任务
     */
    public Object completeTask() {
        //根据执行人去查询
        List<Task> list = taskService.createTaskQuery().taskAssignee("0003").list();
        for (Task task : list) {
            Map variables = MapUtil.of("content", "同意");
            //本地的在act_hi_varinst
            taskService.setVariablesLocal(task.getId(), variables);
            taskService.complete(task.getId());
        }
        return Message.success();
    }

    /**
     * 创建bpmn
     */
    public Object createBpmn() {
        // 1. Build up the model from scratch
        BpmnModel model = new BpmnModel();
        org.activiti.bpmn.model.Process process = new Process();
        model.addProcess(process);
        process.setId("my-process");

        process.addFlowElement(createStartEvent());
        process.addFlowElement(createUserTask("task1", "审批流程一", "fred"));
        process.addFlowElement(createUserTask("task2", "审批流程二", "john"));
        process.addFlowElement(createEndEvent());

        process.addFlowElement(createSequenceFlow("start", "task1"));
        process.addFlowElement(createSequenceFlow("task1", "task2"));
        process.addFlowElement(createSequenceFlow("task2", "end"));

        // 2. Generate graphical information
        new BpmnAutoLayout(model).execute();

        // 3. Deploy the process to the engine
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("dynamic-model.bpmn", model).name("Dynamic process deployment").deploy();

        // 4. Start a process instance
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("my-process");

        // 5. Check if task is available
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId()).list();
        System.out.println(tasks);

        // 6. Save process diagram to a file
        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());

        FileUtil.writeFromStream(processDiagram, new File("target/diagram.png"));

        // 7. Save resulting BPMN xml to a file
        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), "dynamic-model.bpmn");
        FileUtil.writeFromStream(processBpmn, new File("target/process.bpmn20.xml"));

        return Message.success();
    }


    /**
     * 创建流程任务节点
     */
    private UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /**
     * 流程任务节点连接
     */
    private SequenceFlow createSequenceFlow(String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    /**
     * 流程开始节点
     */
    private StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }

    /**
     * 流程结束节点
     */
    private EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }
}
