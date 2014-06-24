package com.theiyer.whatstheplan.entity;

import java.util.List;

public class Plan {

	private int id;
	private String name;
	private String groupName;
	private String startTime;
	private String endTime;
	private String location;
	private List<String> memberNames;
	private List<String> membersInvited;
	private List<String> groupsInvited;
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
	
	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
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
	
	public List<String> getMembersInvited() {
		return membersInvited;
	}

	public void setMembersInvited(List<String> membersInvited) {
		this.membersInvited = membersInvited;
	}

	public List<String> getGroupsInvited() {
		return groupsInvited;
	}

	public void setGroupsInvited(List<String> groupsInvited) {
		this.groupsInvited = groupsInvited;
	}
}
