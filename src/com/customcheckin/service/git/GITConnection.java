package com.customcheckin.service.git;
import java.io.IOException;
import java.util.Collection;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;


public class GITConnection {
	
	private String repoName;
	private static final String orgnizatioName = "REI-Systems";
	private String userName;
	private String pass;
	private GitHub gitHub;
	
	public GITConnection(String userName, String pass, String repoName) {
		this.userName = userName;
		this.pass = pass;
		this.repoName = repoName;
	}
	
	public GitHub getGitHub() {
		return gitHub;
	}
	
	public GitHub connectToGit() throws IOException {
		gitHub = GitHub.connectUsingPassword(userName, pass);
		return gitHub;
	}
	
	public GHRepository getRepository() throws IOException {
		return gitHub.getOrganization(orgnizatioName).getRepository(repoName);
	}
	
	public static void main(String args[]) throws IOException {
		GITConnection gitConnection = new GITConnection("PankajHingane-REISysIn", "Pankaj@123.com", "REI-Systems");
		Collection<GHRepository> lst = gitConnection.connectToGit().getUser("PankajHingane-REISysIn").getRepositories().values();
		GHRepository repo = null;
		for (GHRepository r : lst) {
			repo = r;
	        System.out.println(r.getName());
	    }
	}
}
