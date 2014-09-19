package com.theiyer.whatstheplan.entity;

import java.util.List;

public class Center
{
    private int id;
    private String name;
    private String adminPhone;
    private String  adminName;
    private String  address;
    private List<String> members;
	private byte[] image;
	private boolean selected;

	public Center()
    {
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getMembers()
    {
        return members;
    }

    public void setMembers(List<String> members)
    {
        this.members = members;
    }
    
    public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getAdminPhone() {
		return adminPhone;
	}

	public void setAdminPhone(String adminPhone) {
		this.adminPhone = adminPhone;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}

