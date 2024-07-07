package com.ecom.security.models;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.core.sym.Name;

@Entity
@Table(name = "AUTHORITIES")
public class EcomAuthorities implements Serializable{

	@EmbeddedId
	private EcomAuthoritiesID ecomAuthId;

	public EcomAuthoritiesID getEcomAuthId() {
		return ecomAuthId;
	}

	public void setEcomAuthId(EcomAuthoritiesID ecomAuthId) {
		this.ecomAuthId = ecomAuthId;
	}
	
	@OneToOne
	@JoinColumn(name="role_id",referencedColumnName = "id",insertable=false, updatable=false )
	private EcomRole ecomRoles;

	public EcomRole getEcomRoles() {
		return ecomRoles;
	}

	public void setEcomRoles(EcomRole ecomRoles) {
		this.ecomRoles = ecomRoles;
	}
	
	@ManyToOne
	@JoinColumn(name = "user_id",referencedColumnName = "id",nullable=false,insertable=false, updatable=false)
	private EcomUser user;

	public EcomUser getUser() {
		return user;
	}

	public void setUser(EcomUser user) {
		this.user = user;
	}
	
	
	
	
}
