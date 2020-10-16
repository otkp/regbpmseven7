package org.epragati.service;

import java.util.List;

import org.epragati.flow.vo.FlowResponseVO;
import org.epragati.flow.vo.FlowTaskDetailsVO;
import org.epragati.flow.vo.FlowUsersVO;
import org.epragati.flow.vo.FlowVO;

/**
 * 
 * @author pbattula
 *
 */
public interface FlowService {

	/**
	 * 
	 * @param flowDetails
	 * @return
	 */
	public FlowResponseVO initiateFlow(FlowVO flowDetails);
	
	/**
	 * Continue Existing Flow 
	 * @param processId
	 * @param flowName
	 */
	public FlowResponseVO continueFlow(FlowVO flowDetails);
	
	/**
	 * Create Users
	 * @param flowUsers
	 */
	public void createUsers(FlowUsersVO flowUsers);
	
	/**
	 * Claim Task
	 * @param flowDetails
	 */
	public void claimTask(FlowVO flowDetails);
	
	/**
	 * Remove Process
	 * @param flowDetails
	 */
	public void removeProcess(FlowVO flowDetails);
	
	
	/**
	 * Get User Task Info
	 * @param flowDetails
	 * @return
	 */
	public List<FlowTaskDetailsVO> getTaskInfo(FlowVO flowDetails);
	
	
	public void getActiveList(String processId);
	
}