package com.customcheckin.service.salesforce;

import java.util.Calendar;
import java.util.Date;
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
	private EnvironmentUserVO intUser;
	private EnvironmentUserVO gitUser;
	private EnvironmentUserVO jiraUser;
	private List<ProjectVO> activeProjects;
	private ProjectVO currentProject;
	private EnvironmentVO gitEnvirnment;
	private EnvironmentVO jiraEnvirnment;
	
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
			pmoUser = (UserVO) gate.querySingle("Select Id, Firstname, Lastname, Username, UserType__c, CurrentProjectId__c from User where Username=?", new Object[]{username});
		}
		return pmoUser;
	}
	
	public EnvironmentVO getJiraEnvirnment() {
		if (jiraEnvirnment == null) {
			jiraEnvirnment = (EnvironmentVO) gate.querySingle("select Id, URL__c, ExternalId1__c from Environment__c where Project__c=? and Type__c=?", 
					new Object[]{getCurrentPMOUser().getCurrentProjectId__c(), "JIRA"});
		}
		return jiraEnvirnment;
	}
	
	public EnvironmentVO getGitEnvirnment() {
		if (gitEnvirnment == null) {
			gitEnvirnment = (EnvironmentVO) gate.querySingle("select Id, URL__c, ExternalId1__c from Environment__c where Project__c=? and Type__c=?", 
					new Object[]{getCurrentPMOUser().getCurrentProjectId__c(), "GitHub"});
		}
		return gitEnvirnment;
	}
	
	public boolean isCurrentUserCMAdmin() {
		return "CM Admin".equals(getCurrentPMOUser().getUserType__c());
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
		gate.updateSingle("CurrentProjectId__c", pmoUser);
	}
	
	public void storeLastCheckInDate() {
		getSalesforceDevUser().setLastCheckInDate__c(Calendar.getInstance().getTime());
		gate.updateSingle("LastCheckInDate__c", devUser);
	}
	
	public EnvironmentUserVO getSalesforceDevUser() {
		if (devUser == null) {
			devUser = (EnvironmentUserVO) gate.querySingle("Select Id, Name, Password__c from EnvironmentUser__c where Environment__r.Type__c=? and "
					+ "Environment__r.Project__c=? and PMOUser__c=? and Environment__r.Active__c=true", 
					new Object[]{"Salesforce DEV", getCurrentProject().getId(), getCurrentPMOUser().getId()});
		}
		return devUser;
	}
	
	public EnvironmentUserVO getSalesforceIntUser() {
		if (intUser == null) {
			intUser = (EnvironmentUserVO) gate.querySingle("Select Id, Name, Password__c from EnvironmentUser__c where Environment__r.Type__c=? and "
					+ "Environment__r.Project__c=? and PMOUser__c=? and Environment__r.Active__c=true", 
					new Object[]{"Salesforce INT", getCurrentProject().getId(), getCurrentPMOUser().getId()});
		}
		return intUser;
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
			gate.createSingle("Environment__c, Name, Password__c, PMOUser__c", devUser);
		}
		else {
			devUser.setName(username);
			devUser.setPassword__c(password);
			gate.updateSingle("Name, Password__c", devUser);
		}
	}
	
	public EnvironmentUserVO getJiraUser() {
		if (jiraUser == null) {
			jiraUser = (EnvironmentUserVO) gate.querySingle("Select Id, Name, Password__c from EnvironmentUser__c where Environment__r.Type__c=? and "
					+ "PMOUser__c=? and Environment__r.Active__c=true", 
					new Object[]{"JIRA", getCurrentPMOUser().getId()});
		}
		return jiraUser;
	}
	
	public void storeJiraDevUser(String username, String password) {
		if (getJiraUser() == null) {
			EnvironmentVO jiraEnv = (EnvironmentVO) gate.querySingle("Select Id from Environment__c where Type__c=? and Active__c=true", 
					new Object[]{"JIRA"});
			jiraUser = new EnvironmentUserVO();
			jiraUser.setEnvironment__c(jiraEnv.getId());
			jiraUser.setPMOUser__c(getCurrentPMOUser().getId());
			jiraUser.setName(username);
			jiraUser.setPassword__c(password);
			gate.createSingle("create.environment.user", jiraUser);
		}
		else {
			jiraUser.setName(username);
			jiraUser.setPassword__c(password);
			gate.updateSingle("update.environment.user", jiraUser);
		}
	}
	
	public EnvironmentUserVO getGITUser() {
		if (gitUser == null) {
			gitUser = (EnvironmentUserVO) gate.querySingle("Select Id, Name, Password__c,LocalWorkspacePath__c from EnvironmentUser__c where Environment__r.Type__c=? and "
					+ "PMOUser__c=? and Environment__r.Active__c=true", 
					new Object[]{"GitHub", getCurrentPMOUser().getId()});
		}
		return gitUser;
	}
	
	public void storeGITUser(String username, String password) {
		if (getGITUser() == null) {
			EnvironmentVO gitEnv = (EnvironmentVO) gate.querySingle("Select Id from Environment__c where Type__c=? and Active__c=true", 
					new Object[]{"GitHub"});
			gitUser = new EnvironmentUserVO();
			gitUser.setEnvironment__c(gitEnv.getId());
			gitUser.setPMOUser__c(getCurrentPMOUser().getId());
			gitUser.setName(username);
			gitUser.setPassword__c(password);
			gate.createSingle("create.environment.user", gitUser);
		}
		else {
			gitUser.setName(username);
			gitUser.setPassword__c(password);
			gate.updateSingle("update.environment.user", gitUser);
		}
	}
	
	public EnvironmentUserVO getDevUser() {
		return devUser;
	}
	
	public static void main(String[] args) {
		SalesforcePMOConnection pmo = SalesforcePMOConnection.getInstance();
		pmo.getActiveProjects();
		pmo.storeCurrentProject("a0AG000000gxW1P");
		pmo.storeSalesforceDevUser("manasi.gangal@ggframework.dev", "test1234");

	}

}
