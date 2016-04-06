package com.customcheckin.model;


public class ConfigObject{
	private String label;
	private String objectAPIName;
	public ConfigObject(String label, String objectAPIName) {
		this.label = label;
		this.objectAPIName = objectAPIName;
	}
	
	@Override
	public String toString() {
		return label+" ("+ objectAPIName + ")";
	}
	
	public String getObjectAPIName() {
		return objectAPIName;
	}
	
}
