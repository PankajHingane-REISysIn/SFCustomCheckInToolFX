package com.customcheckin.service.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.kohsuke.github.GitHub;

public class GITConnection {
	private String repoName;
	private static final String orgnizatioName = "REI-Systems";
	private String userName;
	private String pass;
	
	private static final String REMOTE_URL = "https://github.com/vendisys/VendisysBox.git";
	
    public static void main(String[] args) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
        // prepare a new folder for the cloned repository
        File localPath = File.createTempFile("TestGitRepository", "");
        localPath.delete();
	
        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        try (Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(localPath)
                .call()) {
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
	        System.out.println("Having repository: " + result.getRepository().getDirectory());
	    }
	}
}
