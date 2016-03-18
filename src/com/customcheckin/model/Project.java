package com.customcheckin.model;

public class Project {
	
	private String id;
	private String name;
	
	public Project(String id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	@Override
    public String toString() {
        return name;
    }

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
