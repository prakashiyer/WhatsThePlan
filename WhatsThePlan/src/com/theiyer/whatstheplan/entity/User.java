package com.theiyer.whatstheplan.entity;

import java.util.List;

public class User {
	
	private int id;
	private String name;
	private String phone;
	private List<String> groupNames;
	private List<String> pendingGroupNames;
	private byte[] image;
	private boolean selected;
	
	public User(){
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(List<String> groupNames) {
		this.groupNames = groupNames;
	}

	public List<String> getPendingGroupNames() {
		return pendingGroupNames;
	}

	public void setPendingGroupNames(List<String> pendingGroupNames) {
		this.pendingGroupNames = pendingGroupNames;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	

}
