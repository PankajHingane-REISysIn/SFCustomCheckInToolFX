package com.customcheckin.service.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;



/**
 * Simple snippet which shows how to clone a repository from a remote source
 * via ssh protocol and username/password authentication.
 *
 * @author dominik.stadler at gmx.at
 */
public class GITHelper {
	private GITCredential gitCredential;
	
	public GITHelper(GITCredential gitCredential) {
		this.gitCredential = gitCredential;
	}
	
	public Boolean cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(gitCredential.getRemoteURL());
		File localPath = new File(gitCredential.getLocalURL());
		//localPath.delete();
    	cloneCommand.setDirectory(localPath);
    	cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider( gitCredential.getUserName(), gitCredential.getPassword()));
    	cloneCommand.call();
    	System.out.println("Lib cloned");
    	return true;
	}
	
	public Boolean pullRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		/*CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(gitCredential.getRemoteURL());
		File localPath = new File(gitCredential.getLocalURL());
		localPath.delete();
    	cloneCommand.setDirectory(localPath);
    	cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider( gitCredential.getUserName(), gitCredential.getPassword()));
*/
    	/*Git.init()
        .setDirectory(new File(gitCredential.getLocalURL())).call();
    	Repository repository = FileRepositoryBuilder.create(new File(gitCredential.getLocalURL(), ".git"));
    	Git git = new Git(repository);
    	git.checkout().call();*/
		Git git = Git.open(new File(gitCredential.getLocalURL()));
		Repository repo = git.getRepository();
		String branch = repo.getBranch();
		System.out.println(branch);
		//System.out.println(repo.ge);
		
		git.add().addFilepattern(".").call();
		/*git.commit()
        .setMessage("Commit all changes including additions")
        .call();*/
		
		PushCommand pushcmd = git.push();
		pushcmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "edeb76f18c361c42a870f5034796692183af28fe", ""));
		RefSpec spec = new RefSpec("refs/heads/master:refs/heads/x");
		pushcmd.setRemote(gitCredential.getRemoteURL()).setRefSpecs(spec).call();
		
		
		/*FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(new File(gitCredential.getLocalURL()))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build()) {
            System.out.println("Having repository: " + repository.getDirectory());

            // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)
            Ref head = repository.exactRef("refs/heads/master");
            System.out.println("Ref of refs/heads/master: " + head);
        }*/
    	
    	//System.out.println("Lib cloned");
    	return true;
	}

    public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
        // this is necessary when the remote host does not have a valid certificate, ideally we would install the certificate in the JVM
        // instead of this unsecure workaround!
    	GITCredential gitCredential = new GITCredential("PankajHingane-REISysIn", "Pankaj@123.com", "d:\\tempFolder", 
    						"https://github.com/PankajHingane-REISysIn/SFCustomCheckInToolStruts.git");
    	GITHelper gitHelper = new GITHelper(gitCredential);
    	//gitHelper.cloneRepo();
    	gitHelper.pullRepo();
    	/*CloneCommand cloneCommand = Git.cloneRepository();
    	cloneCommand.setURI( "https://github.com/PankajHingane-REISysIn/SFCustomCheckInToolStruts.git" );
    	 File localPath = File.createTempFile("TestGitRepositoryPankaj", "");
         localPath.delete();
    	cloneCommand.setDirectory(localPath);
    	cloneCommand.setCredentialsProvider( new UsernamePasswordCredentialsProvider( "PankajHingane-REISysIn", "Pankaj@123.com" ) );
    	cloneCommand.call();*/
    	/*CredentialsProvider allowHosts = new CredentialsProvider() {

            @Override
            public boolean supports(CredentialItem... items) {
                for(CredentialItem item : items) {
                    if((item instanceof CredentialItem.YesNoType)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
                for(CredentialItem item : items) {
                    if(item instanceof CredentialItem.YesNoType) {
                        ((CredentialItem.YesNoType)item).setValue(true);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean isInteractive() {
                return false;
            }
        };
        
        // prepare a new folder for the cloned repository
        File localPath = File.createTempFile("TestGitRepository", "");
        localPath.delete();

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        try (Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(localPath)
                .setCredentialsProvider(allowHosts)
                .call()) {
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
	        System.out.println("Having repository: " + result.getRepository().getDirectory());
        }*/
    }
}