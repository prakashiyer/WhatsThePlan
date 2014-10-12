package com.theiyer.whatstheplan.entity;


public class Plan {

	private int id;
	private String title;
	private String startTime;
	private String endTime;
	private String userPhone;
	private String userRsvp;
	private String docPhone;
	private String docName;
	private String docRsvp;
	private String centerPlanFlag;
	private String centerId;
	private String centerName;
	private String planFile;

	public Plan() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getUserRsvp() {
		return userRsvp;
	}

	public void setUserRsvp(String userRsvp) {
		this.userRsvp = userRsvp;
	}

	public String getDocPhone() {
		return docPhone;
	}

	public void setDocPhone(String docPhone) {
		this.docPhone = docPhone;
	}

	public String getDocRsvp() {
		return docRsvp;
	}

	public void setDocRsvp(String docRsvp) {
		this.docRsvp = docRsvp;
	}

	public String getCenterPlanFlag() {
		return centerPlanFlag;
	}

	public void setCenterPlanFlag(String centerPlanFlag) {
		this.centerPlanFlag = centerPlanFlag;
	}

	public String getCenterId() {
		return centerId;
	}

	public void setCenterId(String centerId) {
		this.centerId = centerId;
	}

	public String getPlanFile() {
		return planFile;
	}

	public void setPlanFile(String planFile) {
		this.planFile = planFile;
	}

	public String getCenterName() {
		return centerName;
	}

	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	
}
