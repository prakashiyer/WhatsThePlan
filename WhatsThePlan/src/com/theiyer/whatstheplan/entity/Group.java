package com.theiyer.whatstheplan.entity;

import java.util.List;

public class Group {

	private int id;
	private String name;
	private List<String> memberEmailIds;
	private List<String> planNames;
	private List<String> pendingMembers;
	private String admin;
	
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

	public List<String> getMemberEmailIds() {
		return memberEmailIds;
	}

	public void setMemberEmailIds(List<String> memberEmailIds) {
		this.memberEmailIds = memberEmailIds;
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
	
	
}
