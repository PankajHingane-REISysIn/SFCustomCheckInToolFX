package com.customcheckin.service.salesforce;

import java.util.List;

import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.service.salesforce.vo.EnvironmentVO;
import com.customcheckin.service.salesforce.vo.ProjectVO;
import com.customcheckin.service.salesforce.vo.UserVO;
import com.customcheckin.util.PropertyManager;
import com.customcheckin.util.SalesforceConnection;
import com.lib.util.StringUtils;

public class SalesforcePMOConnection extends SalesforceConnection {
	
	private static SalesforcePMOConnection instance;
	private String username;
	private UserVO pmoUser;
	private EnvironmentUserVO devUser;
	private List<ProjectVO> activeProjects;
	private ProjectVO currentProject;
	
	private SalesforcePMOConnection() {
		username = PropertyManager.getInstance().getString("pmo.username");
		String password = PropertyManager.getInstance().getString("pmo.password");
		init(username, password);
	}
	
	public static SalesforcePMOConnection getInstance() {
		if (instance == null) {
			instance = new SalesforcePMOConnection();
		}
		return instance;
	}
	
	public UserVO getCurrentPMOUser() {
		if (pmoUser == null) {
			pmoUser = (UserVO) gate.querySingle("Select Id, Firstname, Lastname, Username, CurrentProjectId__c from User where Username=?", new Object[]{username});
		}
		return pmoUser;
	}
	
	public List<ProjectVO> getActiveProjects() {
		if (activeProjects == null) {
			activeProjects = gate.queryMultiple("Select Id, Name from Project__c where Active__c=true and EnableCustomCheckIn__c=true");
		}
		return activeProjects;
	}
	
	public ProjectVO getCurrentProject() {
		if (currentProject == null && StringUtils.isNonEmpty(getCurrentPMOUser().getCurrentProjectId__c())) {
			currentProject = (ProjectVO) gate.querySingle("Select Id, Name from Project__c where Id=?", new Object[]{getCurrentPMOUser().getCurrentProjectId__c()});
		}
		return currentProject;
	}
	
	public void storeCurrentProject(String projectId) {
		getCurrentPMOUser().setCurrentProjectId__c(projectId);
		gate.updateSingle("update.user.current.project", pmoUser);
	}
	
	public EnvironmentUserVO getSalesforceDevUser() {
		if (devUser == null) {
			devUser = (EnvironmentUserVO) gate.querySingle("Select Id, Name, Password__c from EnvironmentUser__c where Environment__r.Type__c=? and "
					+ "Environment__r.Project__c=? and PMOUser__c=? and Environment__r.Active__c=true", 
					new Object[]{"Salesforce DEV", getCurrentProject().getId(), getCurrentPMOUser().getId()});
		}
		return devUser;
	}
	
	public void storeSalesforceDevUser(String username, String password) {
		if (getSalesforceDevUser() == null) {
			EnvironmentVO devEnv = (EnvironmentVO) gate.querySingle("Select Id from Environment__c where Project__c=? and Type__c=? and Active__c=true", 
					new Object[]{currentProject.getId(), "Salesforce DEV"});
			devUser = new EnvironmentUserVO();
			devUser.setEnvironment__c(devEnv.getId());
			devUser.setPMOUser__c(getCurrentPMOUser().getId());
			devUser.setName(username);
			devUser.setPassword__c(password);
			gate.createSingle("create.environment.user", devUser);
		}
		else {
			devUser.setName(username);
			devUser.setPassword__c(password);
			gate.updateSingle("update.environment.user", devUser);
		}
	}
	
	public static void main(String[] args) {
		SalesforcePMOConnection pmo = SalesforcePMOConnection.getInstance();
		pmo.getActiveProjects();
		pmo.storeCurrentProject("a0AG000000gxW1P");
		pmo.storeSalesforceDevUser("manasi.gangal@ggframework.dev", "test1234");

	}

}
