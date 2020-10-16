package org.epragati.gateway;

import java.util.List;

import org.epragati.exception.BadRequestException;
import org.epragati.flow.vo.FlowResponseVO;
import org.epragati.flow.vo.FlowTaskDetailsVO;
import org.epragati.flow.vo.FlowUsersVO;
import org.epragati.flow.vo.FlowVO;
import org.epragati.service.FlowService;
import org.epragati.util.GateWayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author pbattula
 *
 */
@CrossOrigin
@RestController
public class FlowGateWay {

	private static final Logger logger = LoggerFactory.getLogger(FlowGateWay.class);

	@Autowired
	private FlowService flowService;


	@RequestMapping(method = { RequestMethod.POST } , path = "intiateFlow")
	public GateWayResponse<?> initiateFlow(@RequestBody(required = true) FlowVO flowDetails) {

		FlowResponseVO flowResponseVO = null;

		try{
			flowResponseVO = flowService.initiateFlow(flowDetails);
		}catch(BadRequestException e){
			throw e;	
		}catch(Exception e){
			logger.error(e.getMessage());
		}

		return new GateWayResponse<FlowResponseVO>(true, HttpStatus.OK,flowResponseVO);
	}
	
	
	@RequestMapping(method = { RequestMethod.POST } , path = "getTaskInfo")
	public GateWayResponse<?> getTaskInfo(@RequestBody(required = true) FlowVO flowDetails) {

		List<FlowTaskDetailsVO> flowResponseVO = null;

		try{
			flowResponseVO = flowService.getTaskInfo(flowDetails);
		}catch(BadRequestException e){
			throw e;	
		}catch(Exception e){
			logger.error(e.getMessage());
		}

		return new GateWayResponse<List<FlowTaskDetailsVO>>(true, HttpStatus.OK,flowResponseVO);
	}

	@RequestMapping(method = { RequestMethod.POST } , path = "getActiveList")
	public GateWayResponse<?> getActiveList(@RequestBody(required = true) FlowVO flowDetails) {

		List<FlowTaskDetailsVO> flowResponseVO = null;

		try{
			flowService.getActiveList(flowDetails.getProcessId());
		}catch(BadRequestException e){
			throw e;	
		}catch(Exception e){
			logger.error(e.getMessage());
		}

		return new GateWayResponse<String>(true, HttpStatus.OK,"");
	}
	
	@RequestMapping(method = { RequestMethod.POST },  path="/continuewFlow")
	public GateWayResponse<?> continueFlow(@RequestBody(required = true) FlowVO flowDetails) {

		FlowResponseVO flowResponseVO = null;

		try{
			flowResponseVO = flowService.continueFlow(flowDetails);
		}catch(BadRequestException e){
			throw e;	
		}catch(Exception e){
			logger.error(e.getMessage());
		}

		return new GateWayResponse<FlowResponseVO>(true, HttpStatus.OK,flowResponseVO);

	}

	@RequestMapping(method = { RequestMethod.POST }, path="/createUser")
	public GateWayResponse<?> createUsers(@RequestBody(required=true) FlowUsersVO flowUsers) {
		try{
			flowService.createUsers(flowUsers);
			return new GateWayResponse<String>(true, HttpStatus.OK,"Success");
		}catch(Exception e){
			e.printStackTrace();
			return new GateWayResponse<String>(true, HttpStatus.NOT_FOUND,"Not Success");
		}
	}

/*	@RequestMapping(method = { RequestMethod.POST })
	public void claimTask() {

	}

	@RequestMapping(method = { RequestMethod.POST })
	public void removeProcess() {

	}*/
}
