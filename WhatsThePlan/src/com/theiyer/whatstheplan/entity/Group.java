package com.theiyer.whatstheplan.entity;

import java.util.List;

public class Group {

	private int id;
	private String name;
	private List<String> members;
	private List<String> planNames;
	private List<String> pendingMembers;
	private String admin;
	private byte[] image;
	private boolean selected;
	
	public Group(){
		
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

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public List<String> getPlanNames() {
		return planNames;
	}

	public void setPlanNames(List<String> planNames) {
		this.planNames = planNames;
	}

	public List<String> getPendingMembers() {
		return pendingMembers;
	}

	public void setPendingMembers(List<String> pendingMembers) {
		this.pendingMembers = pendingMembers;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
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
