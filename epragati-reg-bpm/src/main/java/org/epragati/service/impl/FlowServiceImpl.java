package org.epragati.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.epragati.constants.Flow;
import org.epragati.constants.FlowAction;
import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.flow.vo.FlowResponseVO;
import org.epragati.flow.vo.FlowTaskDetailsVO;
import org.epragati.flow.vo.FlowUsersVO;
import org.epragati.flow.vo.FlowVO;
import org.epragati.service.FlowService;
import org.epragati.util.AppMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FlowServiceImpl implements FlowService {

	@Autowired
	private AppMessages appMsg;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private IdentityService identityService;
	
	@Autowired
	private HistoryService historyService;


	private static final Logger logger = LoggerFactory.getLogger(FlowServiceImpl.class);


	private boolean validateFlow(String flowName) {

		List<Flow> flowList = Arrays.asList(Flow.values());

		if (flowList.contains(Flow.REGISTRATION_TR)) {
			return true;
		}

		return false;
	}

	private boolean validateActions(String action) throws IOException, URISyntaxException {

		ObjectMapper objectMapper = new ObjectMapper();
		byte[] jsonData = Files.readAllBytes(
				Paths.get(getClass().getClassLoader().getResource("bpm-process-actions/bpm-flow.json").toURI()));

		List<FlowAction> actionsList = objectMapper.readValue(jsonData, List.class);

		if (actionsList.contains(action)) {
			return true;
		}

		return false;
	}

	@Override
	public FlowResponseVO initiateFlow(FlowVO flowDetails) {

		//doCompleteValidation(flowDetails);

		FlowResponseVO flowResponse = new FlowResponseVO();
		List<String> list = new ArrayList<String>();
		logger.info("Flow Initiated  : "+flowDetails.getFlowName());
		ProcessInstance processInstance =  runtimeService.startProcessInstanceByKey(flowDetails.getFlowName());
		processInstance.getProcessInstanceId();
		list.add(processInstance.getProcessInstanceId());
		flowResponse.setProcessId(list);
		
		
		logger.info("Flow Process Response Id  : "+processInstance.getProcessInstanceId());
		return flowResponse; 
	}

	@Override
	public FlowResponseVO continueFlow(FlowVO flowDetails) {

//		doCompleteValidation(flowDetails);
		FlowResponseVO flowResponse = new FlowResponseVO();
		List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(flowDetails.getUserTaskId()).list();
		tasks.get(0).getTaskDefinitionKey();
		taskService.claim(flowDetails.getUserTaskId(),"admin");
		taskService.complete(flowDetails.getUserTaskId());

		return flowResponse;
		
	}

	private boolean doCompleteValidation(FlowVO flowDetails){
		if (!validateFlow(flowDetails.getFlowName())) {
			logger.error(appMsg.getLogMessage(MessageKeys.FLOW_INVALID), flowDetails.getFlowName());
			throw new BadRequestException(appMsg.getResponseMessage(MessageKeys.FLOW_INVALID));
		}

		try {
			if (!validateActions(flowDetails.getAction())) {
				logger.error(appMsg.getLogMessage(MessageKeys.FLOW_INVALID_ACTION_FOR_FLOW), flowDetails.getAction());
				throw new BadRequestException(appMsg.getResponseMessage(MessageKeys.FLOW_INVALID_ACTION_FOR_FLOW));
			}
		} catch (IOException e) {

			logger.error(appMsg.getLogMessage(MessageKeys.FLOW_IO_OPERATION_FAILED), flowDetails.getAction());
			throw new BadRequestException(appMsg.getResponseMessage(MessageKeys.FLOW_IO_OPERATION_FAILED));

		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}

		return true;
	}


	@Override
	public void createUsers(FlowUsersVO flowUsers) {

		// Creating group
		Group group = identityService.createGroupQuery().groupId(flowUsers.getGroupId()).singleResult();

		if(group == null){

			group = identityService.newGroup(flowUsers.getGroupId());
			group.setName(flowUsers.getGroupName());
			group.setType(flowUsers.getGroupName());
			identityService.saveGroup(group);
			logger.info("Group {} Created Success",flowUsers.getGroupId());

		}else{
			logger.info("Group {} already exist ",flowUsers.getGroupId());
		}

		// Creating a new User
		User user = identityService.createUserQuery().userId(flowUsers.getUserId()).singleResult();

		if(user == null){

			user = identityService.newUser(flowUsers.getUserId());
			user.setFirstName(flowUsers.getFirstName());
			user.setLastName(flowUsers.getLastName());
			user.setPassword(flowUsers.getPassword());

			identityService.saveUser(user);
			logger.info("User {} Created Success",flowUsers.getUserId());

		}else{
			logger.info("User {} already exist",flowUsers.getUserId());
		}

		// Creating MemberShip
		List<Group> userMemberShip = identityService.createGroupQuery().groupMember(flowUsers.getUserId()).list();

		if (userMemberShip == null) {
			identityService.createMembership(user.getId(), group.getId());
		} else {

			boolean isMatched = false;
			for(Group g : userMemberShip){
				if(g.getId().equalsIgnoreCase(group.getId())){
					isMatched = true;
					break;
				}
			}
			if(!isMatched){
				identityService.createMembership(user.getId(), group.getId());
			}
		}

	}

	@Override
	public void claimTask(FlowVO flowDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeProcess(FlowVO flowDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<FlowTaskDetailsVO> getTaskInfo(FlowVO flowDetails) {

		List<Task> taskList = taskService.createTaskQuery().taskCandidateGroup("MVI").processInstanceId("47514").list();
		List<FlowTaskDetailsVO> taskInfoList = new ArrayList<FlowTaskDetailsVO>();
		
		for (Task task : taskList) {
			
			FlowTaskDetailsVO rtaTask = new FlowTaskDetailsVO(
					task.getId(), 
					task.getTaskDefinitionKey(), 
					task.getName(),
					task.getProcessDefinitionId(), 
					null, 
					task.getProcessInstanceId()
			);
			taskInfoList.add(rtaTask);
		}
		
		return taskInfoList;
	}

	@Override
	public void getActiveList(String processInstanceId) {

		try {
			List<String> ids = runtimeService.getActiveActivityIds(processInstanceId);
			ProcessInstance processInstance =
					runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			for (String id : ids) {
				//RtaTaskInfo task = new RtaTaskInfo();
				
				logger.info("Id  : "+id);
			}
		} catch (org.activiti.engine.ActivitiObjectNotFoundException ex) {
		}
		//return taskList;

	}
}