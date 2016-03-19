package com.customcheckin.service.git;

public class GITCredential {
	private String userName;
	private String password;
	private String localURL;
	private String remoteURL;
	
	public GITCredential(String userName, String password, String localURL, String remoteURL) {
		this.userName = userName;
		this.password = password;
		this.localURL = localURL;
		this.remoteURL = remoteURL;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the localURL
	 */
	public String getLocalURL() {
		return localURL;
	}

	/**
	 * @param localURL the localURL to set
	 */
	public void setLocalURL(String localURL) {
		this.localURL = localURL;
	}

	/**
	 * @return the remoteURL
	 */
	public String getRemoteURL() {
		return remoteURL;
	}

	/**
	 * @param remoteURL the remoteURL to set
	 */
	public void setRemoteURL(String remoteURL) {
		this.remoteURL = remoteURL;
	}
}
