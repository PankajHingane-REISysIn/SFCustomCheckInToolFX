package com.customcheckin.service.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.customcheckin.service.salesforce.SalesforcePMOConnection;
import com.customcheckin.service.salesforce.vo.EnvironmentUserVO;
import com.lib.util.StringUtils;

public class GITConnection {
	//todo - read from SF PMO
	private String remoteURL = "https://github.com/PankajHingane-REISysIn/TestSFRepo";
	private String localURL;
	private UsernamePasswordCredentialsProvider gitUserPass;
	private Git git;
	private EnvironmentUserVO gitUserInfo;
	private static GITConnection instance;
	private static Logger log = Logger.getRootLogger();
	
	private GITConnection() throws IOException {
		gitUserInfo = SalesforcePMOConnection.getInstance().getGITUser();
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
		//File myfile = new File(repo.getDirectory().getParent(), "testfile1");
		//log.info("repo.getDirectory().getParent()=======" + repo.getDirectory().getParent());
        //myfile.createNewFile();
        //ac.addFilepattern("src/layouts/ApprovalDecisionActionConfig__c-Approval Decision Action Config Layout.layout");
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
	
	public Set<String> getFileWithRevisions(ObjectId commitId) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		Set<String> filePath = new HashSet<String>();
		RevWalk revWalk = new RevWalk( git.getRepository() );
		RevCommit commit = revWalk.parseCommit( commitId );
		RevCommit realParant = commit.getParentCount() > 0 ? commit.getParent( 0 ) : commit;
		RevCommit parent = revWalk.parseCommit( realParant.getId() );
		DiffFormatter df = new DiffFormatter( DisabledOutputStream.INSTANCE );
		df.setRepository( git.getRepository() );
		df.setDiffComparator( RawTextComparator.DEFAULT );
		df.setDetectRenames( true );
		 List<DiffEntry> diffs = df.scan( parent.getTree(), commit.getTree() );
		 for ( DiffEntry diff : diffs ) {
			 filePath.add(diff.getNewPath());
		 }
		 revWalk.dispose();
		log.info( commit.getFullMessage() );
		return filePath;
		//revWalk.dispose();
	}
	
	public boolean getFiles(String basePath, String commitNo, OutputStream os) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		ObjectId commitId = ObjectId.fromString( commitNo );
		Set<String> filesForRevision = getFileWithRevisions(commitId);
		RevWalk revWalk = new RevWalk( git.getRepository() );
		File fileToZip = new File("D:\\tempFolder\\test");
		String zipFileName = fileToZip.getName() + ".cls";
	    File zipFile = new File(fileToZip.getParent(), zipFileName);
		RevCommit commit = revWalk.parseCommit( commitId );
		if (commit == null) {
			return false;
		}
		boolean success = false;
		TreeWalk tw = new TreeWalk(git.getRepository());
		try {
			tw.reset();
			tw.addTree(commit.getTree());
			ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fileToZip);
			zos.setComment("Generated by FXTool");
			if (!StringUtils.isEmpty(basePath)) {
				PathFilter f = PathFilter.create(basePath);
				tw.setFilter(f);
			}
			tw.setRecursive(true);
			MutableObjectId id = new MutableObjectId();
			ObjectReader reader = tw.getObjectReader();
			long modified = commit.getAuthorIdent().getWhen().getTime();
			while (tw.next()) {
				FileMode mode = tw.getFileMode(0);
				if (mode == FileMode.GITLINK || mode == FileMode.TREE) {
					continue;
				}
				if(filesForRevision.contains(tw.getPathString())) {
					log.info(tw.getPathString());
					//log.info("Size" + reader.getObjectSize(id, Constants.OBJ_BLOB));
					//log.info(commit.getName());
					//log.info(mode.getBits());
					//log.info(modified);
					
					tw.getObjectId(id, 0);
					
					/*ZipArchiveEntry entry = new ZipArchiveEntry(tw.getPathString());
				entry.setSize(reader.getObjectSize(id, Constants.OBJ_BLOB));
				entry.setComment(commit.getName());
				entry.setUnixMode(mode.getBits());
				entry.setTime(modified);
				zos.putArchiveEntry(entry);*/
					
					ObjectLoader ldr = git.getRepository().open(id);
					log.info(ldr);
					File targetFile = new File("D:\\tempFolder\\"+tw.getPathString());
					if(!targetFile.exists()) {
						if(!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						targetFile.createNewFile();
					}
					OutputStream out = new FileOutputStream(targetFile);
					ldr.copyTo(out);
					//zos.closeArchiveEntry();
				}
			}
			//zos.finish();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
			//error(e, repository, "{0} failed to zip files from commit {1}", commit.getName());
		} finally {
			//tw.release();
		}
		return success;
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
	
	public EnvironmentUserVO getGitUserInfo() {
		return gitUserInfo;
	}
	
	public static void main(String str[]) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		//GITConnection.getInstance().pushRepo("fdd", new ArrayList<String>());
		GITConnection.getInstance().getFiles("src", "e676f82c0f1df2172e37ac50f851af1faa00e0cb", null);
	}
	
}
