package com.main.netbankingapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.main.netbankingapp.model.Role;
import com.main.netbankingapp.model.RoleEnum;
import com.main.netbankingapp.repository.RoleRepository;


@Service
public class RoleService {
	
	@Autowired
	private RoleRepository roleRepository;
	
	
	public void RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	public Role findOrCreateRole(RoleEnum name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(null, name)));
    }
}
