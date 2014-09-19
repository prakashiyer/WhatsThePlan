package com.theiyer.whatstheplan.entity;

import java.util.List;

public class User {
	

	private int id;
	private String name;
	private String phone;
	private String bloodGroup;
	private String dob;
	private String sex;
	private String address;
	private String doctorFlag;
	private int primaryCenterId;
	private int primaryDoctorId;
	private byte[] image;
	private List<String> centers;
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

	public String getBloodGroup() {
		return bloodGroup;
	}

	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDoctorFlag() {
		return doctorFlag;
	}

	public void setDoctorFlag(String doctorFlag) {
		this.doctorFlag = doctorFlag;
	}

	public int getPrimaryCenterId() {
		return primaryCenterId;
	}

	public void setPrimaryCenterId(int primaryCenterId) {
		this.primaryCenterId = primaryCenterId;
	}

	public int getPrimaryDoctorId() {
		return primaryDoctorId;
	}

	public void setPrimaryDoctorId(int primaryDoctorId) {
		this.primaryDoctorId = primaryDoctorId;
	}

	public List<String> getCenters() {
		return centers;
	}

	public void setCenters(List<String> centers) {
		this.centers = centers;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
}
