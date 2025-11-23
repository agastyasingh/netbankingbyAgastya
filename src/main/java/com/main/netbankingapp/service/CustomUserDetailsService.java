package com.main.netbankingapp.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.main.netbankingapp.model.User;
import com.main.netbankingapp.repository.UserRepository;


@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleService roleService;
	
	public void CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));
		
		Set<GrantedAuthority> authorities = user.getRoles().stream()
			    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
			    .collect(Collectors.toSet());
		
		return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "",
                authorities
        );
	}
}
