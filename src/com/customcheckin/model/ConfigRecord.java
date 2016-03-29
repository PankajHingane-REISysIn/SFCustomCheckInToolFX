package com.customcheckin.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class ConfigRecord {
	private StringProperty name;
	private StringProperty internalUniqueId;
	private StringProperty col1;
	private StringProperty col2;
	private BooleanProperty isSelected;
	
	public ConfigRecord(StringProperty name, StringProperty internalUniqueId, StringProperty col1, StringProperty col2) {
		this.setName(name);
		this.setInternalUniqueId(internalUniqueId);
		this.setCol1(col1);
		this.setCol2(col2);
		this.isSelected = new SimpleBooleanProperty(false);
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
	 * @return the internalUniqueId
	 */
	public StringProperty getInternalUniqueId() {
		return internalUniqueId;
	}

	/**
	 * @param internalUniqueId the internalUniqueId to set
	 */
	public void setInternalUniqueId(StringProperty internalUniqueId) {
		this.internalUniqueId = internalUniqueId;
	}

	/**
	 * @return the col1
	 */
	public StringProperty getCol1() {
		return col1;
	}

	/**
	 * @param col1 the col1 to set
	 */
	public void setCol1(StringProperty col1) {
		this.col1 = col1;
	}

	/**
	 * @return the col2
	 */
	public StringProperty getCol2() {
		return col2;
	}

	/**
	 * @param col2 the col2 to set
	 */
	public void setCol2(StringProperty col2) {
		this.col2 = col2;
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
