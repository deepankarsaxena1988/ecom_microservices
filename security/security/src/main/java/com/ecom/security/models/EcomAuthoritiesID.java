package com.ecom.security.models;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EcomAuthoritiesID implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "user_id",nullable = false)
	private int userId;
	
	@Column(name = "role_id",nullable = false)
	private int roleId;
	
	public EcomAuthoritiesID(int userId,int roleId) {
		this.roleId=roleId;
		this.userId=userId;
	}

	
	public EcomAuthoritiesID() {
		
	}


	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EcomAuthoritiesID)) return false;
        EcomAuthoritiesID that = (EcomAuthoritiesID) o;
        return Objects.equals(getRoleId(), that.getRoleId()) &&
                Objects.equals(getUserId(), that.getUserId());
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getRoleId());
    }
	
	
}
