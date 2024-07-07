package com.ecom.security;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecom.security.models.EcomAuthorities;
import com.ecom.security.models.EcomUser;


public class EcomUserDetails implements UserDetails {

	
	private String userName;
	
	private String password;
	
	private String active;
	
	public EcomUserDetails(EcomUser ecomUser) {
		this.userName=ecomUser.getUserName();
		this.password=ecomUser.getPassword();
		this.active=ecomUser.getActive();
		
		System.out.println(userName+" "+password+" "+active);
		
		Iterator<EcomAuthorities> iterator = ecomUser.getUserAuthoritiesList().iterator();
		while(iterator.hasNext()) {
			EcomAuthorities ecomauth=iterator.next();
			System.out.println(ecomauth.getEcomAuthId().getRoleId());
			System.out.println(ecomauth.getEcomRoles().getRoleName());
		}
	}
	
	public EcomUserDetails() {
		
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.userName;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
