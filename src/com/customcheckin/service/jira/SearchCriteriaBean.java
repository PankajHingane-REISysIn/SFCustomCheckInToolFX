package com.customcheckin.service.jira;



public class SearchCriteriaBean {
	
	private String key;
	private String oprator;
	private String value;

	public SearchCriteriaBean(String key, String operator, String value) {
		this.key = key;
		this.oprator = operator;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return " " + key + " " + oprator + " " + value;
	}
}
