package com.ecom.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecom.security.models.EcomUser;
import com.ecom.security.models.EcomUserRepository;


@Service
public class EcomUserDetailservice implements UserDetailsService {

	@Autowired
	EcomUserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Optional<EcomUser> ecomUser=userRepository.findByUserName(userName);
		
		ecomUser.orElseThrow(()-> new UsernameNotFoundException("User Name "+userName+" not found"));
		
		System.out.println();
		return ecomUser.map(EcomUserDetails::new).get();
	}

}
