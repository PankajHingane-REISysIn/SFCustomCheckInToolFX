package com.customcheckin.service.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.atlassian.jira.util.json.JSONException;
import com.customcheckin.service.jira.JIRAConnection;
import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.customcheckin.service.salesforce.vo.EnvironmentVO;
import com.customcheckin.util.Utility;
import com.lib.util.StringUtils;

public class GITConnection {
	private String remoteURL = SalesforcePMOConnection.getInstance().getGitEnvirnment().getURL__c();
	private UsernamePasswordCredentialsProvider gitUserPass;
	private Git git;
	private EnvironmentUserVO gitUserInfo;
	private static GITConnection instance;
	private static Logger log = Logger.getRootLogger();
	
	private GITConnection() throws IOException, InvalidRemoteException, GitAPIException {
		gitUserInfo = SalesforcePMOConnection.getInstance().getGITUser();
		if (gitUserInfo != null && StringUtils.isNonEmpty(gitUserInfo.getName())
									&& StringUtils.isNonEmpty(gitUserInfo.getPassword__c())) {
			gitUserPass = new UsernamePasswordCredentialsProvider(gitUserInfo.getName(), gitUserInfo.getPassword__c());
			init();
		}
	}
	
	private void init() throws IOException, InvalidRemoteException, GitAPIException {
		createLocalRepo();
		
		Repository existingRepo = new FileRepositoryBuilder()
				.setGitDir(new File(gitUserInfo.getLocalWorkspacePath__c()+"\\.git"))
				.build();
		git = new Git(existingRepo);
		
		createUserBranch();
	}
	
	private void createLocalRepo() throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		EnvironmentVO gitEnv = SalesforcePMOConnection.getInstance().getGitEnvirnment();
		String remoteRepoURL = gitEnv.getURL__c();
		log.info("remoteRepoURL======:"+remoteRepoURL);
		if(gitUserInfo.getLocalWorkspacePath__c() ==null ||  gitUserInfo.getLocalWorkspacePath__c().isEmpty()) {
			String localRepoPath = remoteRepoURL.substring(remoteRepoURL.lastIndexOf("/") + 1)+"/LocalRepository";
			log.info("localRepoPath======:"+localRepoPath);
			SalesforcePMOConnection.getInstance().storeCurrentProjectLocalGITPath(localRepoPath);
		}
		File localRepo = new File(gitUserInfo.getLocalWorkspacePath__c());
		log.info("localRepo.absoulate path======:"+localRepo.getAbsolutePath());
		// todo - add validation for .git - Not sure if required.
		if(!localRepo.exists()) {
			log.info("repo does not exist");
			if(!localRepo.getParentFile().exists()) {
				localRepo.getParentFile().mkdirs();
			}
			localRepo.mkdir();
			cloneRepo();
		}
	}
	
	private void createUserBranch() throws IOException, RefAlreadyExistsException, RefNotFoundException, 
										InvalidRefNameException, CheckoutConflictException, GitAPIException {
		if( gitUserInfo.getGITBranchName__c() == null || gitUserInfo.getGITBranchName__c().isEmpty() ) {
			SalesforcePMOConnection.getInstance().storeGITBranch(gitUserInfo.getName());
			log.info("Branch Name is empty on PMO");
		}
		// Check if PMO git user branch and local branch is same. If not then set
		// Check if branch is present if yes then set to pointer to that branch.
		log.info("git.getRepository().getBranch()========" + git.getRepository().getBranch());
		if(gitUserInfo.getGITBranchName__c() != null && !gitUserInfo.getGITBranchName__c().isEmpty() && 
				!git.getRepository().getBranch().equalsIgnoreCase(gitUserInfo.getGITBranchName__c())) {
			git.checkout().setCreateBranch(true).setName(gitUserInfo.getGITBranchName__c())
	        .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
	        .setStartPoint("refs/heads/master").call();
		}
	}
	
	public static GITConnection getInstance() throws IOException, InvalidRemoteException, GitAPIException {
		if (instance == null) {
			instance = new GITConnection();
		}
		return instance;
	}
	
	public Boolean cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		log.info("Clone begin.");
		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(remoteURL);
		File localPath = new File(gitUserInfo.getLocalWorkspacePath__c());
    	cloneCommand.setDirectory(localPath);
    	cloneCommand.setCredentialsProvider(gitUserPass);
    	cloneCommand.call();
    	log.info("Clone End.");
    	return true;
	}
	
	public Boolean pushRepo(String jiraTicketNo, List<String> filesToAdd) 
				throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		Repository repo = git.getRepository();
		String branch = repo.getBranch();
		log.info("Branch=="+branch);
		
		/*Ref head = repo.getRef("HEAD");
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
		}*/
		
		//git.add().addFilepattern(".").call();
		AddCommand ac = git.add();
		for (String file : filesToAdd) {
			log.info("Adding File TO repo:" + file);
			ac.addFilepattern(file);
		}
		ac.call();
		git.commit()
        .setMessage("CheckIn By Tool- Jira Ticket No :" + jiraTicketNo)
        .call();
		
		PushCommand pushcmd = git.push();
		pushcmd.setCredentialsProvider(gitUserPass);
		RefSpec spec = new RefSpec("refs/heads/"+gitUserInfo.getGITBranchName__c());
		Iterable<PushResult> pushList = pushcmd.setRemote(remoteURL).setRefSpecs(spec).call();
		log.info("=====Completed===");
		for(PushResult pushResult : pushList) {
			mergeBranchWithMaster(pushResult.getAdvertisedRef("refs/heads/PankajHingane-REISysIn1"));
			for (Ref rru : pushResult.getAdvertisedRefs()) {
				log.info("rru.getName()===" + rru.getName());
				log.info("rru.getName()===" + rru.getObjectId());
				
			}
			for (RemoteRefUpdate rru : pushResult.getRemoteUpdates()) {
				RemoteRefUpdate.Status status = rru.getStatus();
				log.info("rru.getRemoteName()===" + rru.getRemoteName());
				log.info("rru.getSrcRef()===" + rru.getSrcRef());
				log.info("rru.getMessage()===" + rru.getMessage());
				log.info("NewObjectId===" + rru.getNewObjectId());
				log.info("pushResult===" + pushResult.getAdvertisedRefs());
				updateResultTOJira(jiraTicketNo, rru.getNewObjectId().getName());
			}
		}
    	return true;
	}
	
	public void mergeBranchWithMaster(Ref sideCommit) throws NoHeadException, ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException, WrongRepositoryStateException, NoMessageException, GitAPIException {
		MergeResult result = git.merge().include(sideCommit)
				.setStrategy(MergeStrategy.RESOLVE).call();
		// https://github.com/eclipse/jgit/blob/master/org.eclipse.jgit.test/tst/org/eclipse/jgit/api/PullCommandWithRebaseTest.java
	}
	
	private void updateResultTOJira(String ticketNo, String revisonNo) {
		try {
			JIRAConnection.getInstance().updateField(ticketNo, "Git Commit IDs", revisonNo, false);
		} catch (JSONException | URISyntaxException e) {
			// TODO Auto-generated catch block
			log.error(e);
			e.printStackTrace();
		}
	}
	
	private Set<String> getFileWithRevisions(RevCommit commit) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		Set<String> filesPath = new HashSet<String>();
		RevWalk revWalk = new RevWalk( git.getRepository() );
		RevCommit realParant = commit.getParentCount() > 0 ? commit.getParent( 0 ) : commit;
		RevCommit parent = revWalk.parseCommit( realParant.getId() );
		DiffFormatter df = new DiffFormatter( DisabledOutputStream.INSTANCE );
		df.setRepository( git.getRepository() );
		df.setDiffComparator( RawTextComparator.DEFAULT );
		df.setDetectRenames( true );
		 List<DiffEntry> diffs = df.scan( parent.getTree(), commit.getTree() );
		 for ( DiffEntry diff : diffs ) {
			 filesPath.add(diff.getNewPath());
		 }
		 revWalk.dispose();
		log.info( commit.getFullMessage() );
		return addMetadataFiles(filesPath);
		//revWalk.dispose();
	}
	
	private Set<String> addMetadataFiles(Set<String> filesPath) {
		Set<String> filePathToReturn = new HashSet<String>();
		//todo - 
		for(String filePath : filesPath) {
			filePathToReturn.add(filePath);
			if(filePath.endsWith(".cls") || filePath.endsWith(".component") ||
					filePath.endsWith(".page")) {
				filePathToReturn.add(filePath+"-meta.xml");
			}
			if(filePath.endsWith("-meta.xml")) {
				filePathToReturn.add(filePath.replace("-meta.xml", ""));
			}
		}
		return filePathToReturn;
	}
	
	private List<RevCommit> getRevCommits(List<String> commitNos) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		RevWalk revWalk = new RevWalk( git.getRepository() );
		Map<Integer, RevCommit> checkInTimeByCommit = new HashMap<>();
		for(String commitNo : commitNos) {
			RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitNo));
			checkInTimeByCommit.put(commit.getCommitTime(), commit);
		}
		List<Integer> intLst = new ArrayList<Integer>(checkInTimeByCommit.keySet());
		//sort by time
		Collections.sort(intLst);
		List<RevCommit> revCommits = new ArrayList<>();
		for(Integer date : intLst) {
			revCommits.add(checkInTimeByCommit.get(date));
		}
		return revCommits;
	}
	
	public void revertFile(List<String> fileNames) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {
		CheckoutCommand chCMD = git.checkout();
		for(String filePath : fileNames) {
			chCMD.addPath(filePath);
		}
		chCMD.call();
	}
	
	public void getFiles(String basePath, List<String> commitNos) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		List<RevCommit> revCommits = getRevCommits(commitNos);
		// use http://download.eclipse.org/jgit/docs/jgit-2.3.1.201302201838-r/apidocs/org/eclipse/jgit/api/CheckoutCommand.html link to download metadata files.
		for(RevCommit commit : revCommits) {
			Set<String> filesForRevision = getFileWithRevisions(commit);
			for(String file : filesForRevision) {
				log.info("file path:" + file);
			}
			RevWalk revWalk = new RevWalk( git.getRepository() );
			if (commit == null) {
				continue;
			}
			TreeWalk tw = new TreeWalk(git.getRepository());
			tw.reset();
			tw.addTree(commit.getTree());
			if (!StringUtils.isEmpty(basePath)) {
				PathFilter f = PathFilter.create(basePath);
				tw.setFilter(f);
			}
			tw.setRecursive(true);
			MutableObjectId id = new MutableObjectId();
			while (tw.next()) {
				FileMode mode = tw.getFileMode(0);
				log.info("tw.getPathString()=======" + tw.getPathString());
				if (mode == FileMode.GITLINK || mode == FileMode.TREE) {
					continue;
				}
				if(filesForRevision.contains(tw.getPathString())) {
					log.info("Got a file:" + tw.getPathString());
					tw.getObjectId(id, 0);
					
					ObjectLoader ldr = git.getRepository().open(id);
					log.info(ldr);
					File targetFile = new File(Utility.getMetadataDeployBaseURL()+"\\"+( tw.getPathString()));
					if(!targetFile.exists()) {
						if(!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						targetFile.createNewFile();
					}
					OutputStream out = new FileOutputStream(targetFile);
					ldr.copyTo(out);
				}
			}
		}
	}
	
	public void getFiles(String basePath, RevCommit commit, Set<String> filesForRevision) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		RevWalk revWalk = new RevWalk( git.getRepository() );
		TreeWalk tw = new TreeWalk(git.getRepository());
		tw.reset();
		tw.addTree(commit.getTree());
		if (!StringUtils.isEmpty(basePath)) {
			PathFilter f = PathFilter.create(basePath);
			tw.setFilter(f);
		}
		tw.setRecursive(true);
		MutableObjectId id = new MutableObjectId();
		while (tw.next()) {
			FileMode mode = tw.getFileMode(0);
			if (mode == FileMode.GITLINK || mode == FileMode.TREE) {
				continue;
			}
			if(filesForRevision.contains(tw.getPathString())) {
				
				tw.getObjectId(id, 0);
				
				ObjectLoader ldr = git.getRepository().open(id);
				log.info(ldr);
				File targetFile = new File(Utility.getMetadataDeployBaseURL()+tw.getPathString());
				if(!targetFile.exists()) {
					if(!targetFile.getParentFile().exists()) {
						targetFile.getParentFile().mkdirs();
					}
					targetFile.createNewFile();
				}
				OutputStream out = new FileOutputStream(targetFile);
				ldr.copyTo(out);
			}
		}
	}
	
	protected File getRelativeFile(String path) { 
        return new File(gitUserInfo.getLocalWorkspacePath__c(), trimLeadingSlash(path)); 
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
	
	public EnvironmentUserVO getGitUserInfo() {
		return gitUserInfo;
	}
	
	public static void main(String str[]) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		//GITConnection.getInstance().createNewRepo();
		/*List<String> filePath = new ArrayList<String>();
		filePath.add("src/classes/TestHelper.cls");
		//GITConnection.getInstance().pushRepo("fdd", filePath);*/
		
		
		/*List<String> coomitLst = new ArrayList<String>();
		coomitLst.add("e676f82c0f1df2172e37ac50f851af1faa00e0cb");
		coomitLst.add("5654ae8c5f79a060b20c39991694812eb32ce1b5");
		
		GITConnection.getInstance().getFiles("src", coomitLst);*/
	}
	
}
