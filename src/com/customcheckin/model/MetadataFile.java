package com.customcheckin.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class MetadataFile {
	private StringProperty name;
	private BooleanProperty isSelected = new SimpleBooleanProperty(false);
	
	public MetadataFile(StringProperty name, BooleanProperty isSelected) {
		this.setName(name);
		this.setIsSelected(isSelected);
	}

	/**
	 * @return the name
	 */
	public StringProperty getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(StringProperty name) {
		this.name = name;
	}

	/**
	 * @return the isSelected
	 */
	public BooleanProperty getIsSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected the isSelected to set
	 */
	public void setIsSelected(BooleanProperty isSelected) {
		this.isSelected = isSelected;
	}
}
