package com.customcheckin.model;

import java.io.Serializable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class JIRATicket implements Serializable {
	private StringProperty id;
	private StringProperty name;
	private BooleanProperty isSelected = new SimpleBooleanProperty(false);
	
	public JIRATicket() {
		
	}
	public JIRATicket(StringProperty Id){
		this.id = Id;
	}
	public JIRATicket(StringProperty id, StringProperty name, BooleanProperty isSelected){
		this.id = id;
		this.name = name;
		this.isSelected = isSelected;
	}
	/**
	 * @return the id
	 */
	public StringProperty getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(StringProperty id) {
		this.id = id;
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
