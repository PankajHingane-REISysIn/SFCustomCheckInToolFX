package com.customcheckin.model;

import java.util.Comparator;

public class ConfigObject implements Comparator<ConfigObject>, Comparable<ConfigObject> {
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
	
	public String getlabel() {
		return label;
	}

	@Override
	public int compareTo(ConfigObject arg0) {
		// TODO Auto-generated method stub
		return (this.label).compareTo(arg0.label);
	}

	@Override
	public int compare(ConfigObject o1, ConfigObject o2) {
		// TODO Auto-generated method stub
		return o1.getlabel().compareTo(o2.getlabel());
	}
	
}
