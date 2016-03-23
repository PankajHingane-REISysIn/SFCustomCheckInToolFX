package com.customcheckin.service.git;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.lib.util.StringUtils;

public class GITConnection {
	//todo - read from SF PMO
	private String remoteURL = "https://github.com/PankajHingane-REISysIn/TestSFRepo";
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
	
	public Boolean pushRepo(String jiraTicketNo, List<String> filesToAdd) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		Repository repo = git.getRepository();
		String branch = repo.getBranch();
		log.info("Branch=="+branch);
		
		Ref head = repo.getRef("HEAD");
		RevWalk walk = new RevWalk(repo);
		RevCommit commit = walk.parseCommit(head.getObjectId());
		TreeWalk treeWalk = new TreeWalk(repo);
		RevTree tree = commit.getTree();
		treeWalk.addTree(tree);
		treeWalk.setRecursive(false);
		while (treeWalk.next()) {
		    if (treeWalk.isSubtree()) {
		        System.out.println("dir: " + treeWalk.getPathString());
		        treeWalk.enterSubtree();
		    } else {
		        System.out.println("file: " + treeWalk.getPathString());
		    }
		}
		
		//git.add().addFilepattern(".").call();
		AddCommand ac = git.add();
		//File myfile = new File(repo.getDirectory().getParent(), "testfile1");
		//log.info("repo.getDirectory().getParent()=======" + repo.getDirectory().getParent());
        //myfile.createNewFile();
        ac.addFilepattern("src/layouts/ApprovalDecisionActionConfig__c-Approval Decision Action Config Layout.layout");
		for (String file : filesToAdd) {
			log.info("Adding File TO repo:" + file);
			ac.addFilepattern(file);
		}
		ac.call();
		git.commit()
        .setMessage("CheckIn By FX Tool- Jira Ticket No :" + jiraTicketNo)
        .call();
		
		PushCommand pushcmd = git.push();
		pushcmd.setCredentialsProvider(gitUserPass);
		RefSpec spec = new RefSpec("refs/heads/master");
		Iterable<PushResult> pushList = pushcmd.setRemote(remoteURL).setRefSpecs(spec).call();
		log.info("=====Completed===" + pushList);
    	return true;
	}
	
	protected File getRelativeFile(String path) { 
        return new File(localURL, trimLeadingSlash(path)); 
    }
	
	public static String trimLeadingSlash(String name) { 
        if (name != null && name.startsWith("/")) { 
            name = name.substring(1); 
        } 
        return name; 
    }
	
	protected static String getFilePattern(String path) { 
        return trimLeadingSlash(path); 
    }
	
	public Boolean pullRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		PullCommand pullcmd = git.pull();
		pullcmd.setCredentialsProvider(gitUserPass);
		PullResult pullList = pullcmd.call();
		log.info("=====Completed===" + pullList);
    	return true;
	}
	
	public static void main(String str[]) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		GITConnection.getInstance().pushRepo("fdd", new ArrayList<String>());
	}
	
}
