package com.customcheckin.service.git;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.lib.util.StringUtils;

public class GITConnection {
	private String remoteURL = "https://github.com/PankajHingane-REISysIn/SFCustomCheckInToolStruts.git";
	private String localURL;
	private UsernamePasswordCredentialsProvider gitUserPass;
	private Git git;
	private static GITConnection instance;
	private static Logger log = Logger.getRootLogger();
	
	private GITConnection() throws IOException {
		EnvironmentUserVO gitUserInfo = SalesforcePMOConnection.getInstance().getGITUser();
		if (gitUserInfo != null && StringUtils.isNonEmpty(gitUserInfo.getName()) && StringUtils.isNonEmpty(gitUserInfo.getPassword__c())) {
			gitUserPass = new UsernamePasswordCredentialsProvider(gitUserInfo.getName(), gitUserInfo.getPassword__c());
			localURL = gitUserInfo.getLocalWorkspacePath__c();
			// todo - read from PMO
			//remoteURL = gitUserInfo.get
			init();
			log.info("Jira==" + gitUserInfo.getPassword__c());
		}
	}
	
	private void init() throws IOException{
		//todo - verify user name
		Repository existingRepo = new FileRepositoryBuilder()
				.setGitDir(new File(localURL+"\\.git"))
				.build();
		git = new Git(existingRepo);
	}
	
	public static GITConnection getInstance() throws IOException{
		if (instance == null) {
			instance = new GITConnection();
		}
		return instance;
	}
	
	public Boolean cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(remoteURL);
		File localPath = new File(localURL);
		//localPath.delete();
    	cloneCommand.setDirectory(localPath);
    	cloneCommand.setCredentialsProvider(gitUserPass);
    	cloneCommand.call();
    	log.info("Lib cloned");
    	return true;
	}
	
	public Boolean pushRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		Repository repo = git.getRepository();
		String branch = repo.getBranch();
		log.info("Branch=="+branch);
		git.add().addFilepattern(".").call();
		git.commit()
        .setMessage("Commit all changes including additions.  Jira ticket no. goes here")
        .call();
		
		PushCommand pushcmd = git.push();
		pushcmd.setCredentialsProvider(gitUserPass);
		//pushcmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "edeb76f18c361c42a870f5034796692183af28fe", ""));
		RefSpec spec = new RefSpec("refs/heads/master");
		Iterable<PushResult> pushList = pushcmd.setRemote(remoteURL).setRefSpecs(spec).call();
		log.info("=====Completed===" + pushList);
    	return true;
	}
	
	public static void main(String str[]) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		GITConnection.getInstance().pushRepo();
	}
	
}
