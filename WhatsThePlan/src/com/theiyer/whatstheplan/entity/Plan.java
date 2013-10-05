package com.theiyer.whatstheplan.entity;

import java.util.List;

public class Plan {

	private int id;
	private String name;
	private String groupName;
	private String startTime;
	private String location;
	private List<String> memberNames;
	private String creator;
	
	public Plan(){
		
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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<String> getMemberNames() {
		return memberNames;
	}

	public void setMemberNames(List<String> memberNames) {
		this.memberNames = memberNames;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	
}
