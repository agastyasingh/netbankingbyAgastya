package com.main.netbankingapp.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main.netbankingapp.dto.AccountDTO;
import com.main.netbankingapp.model.Role;
import com.main.netbankingapp.model.User;
import com.main.netbankingapp.repository.AccountRepository;
import com.main.netbankingapp.repository.RoleRepository;
import com.main.netbankingapp.repository.UserRepository;
import com.main.netbankingapp.service.RoleService;


@RestController
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@GetMapping("/data")
    public ResponseEntity<List<AccountDTO>> adminData(@AuthenticationPrincipal Jwt jwt) 
    		throws IOException, InterruptedException{
		
        if(jwt == null) {
        	return ResponseEntity.status(401).build();
        }
        
        String email = jwt.getClaim("email");
        User user = userRepository.findByEmail(email)
        		.orElseThrow(() -> new RuntimeException("Admin not found."));
        
        Set<Role> roles = user.getRoles();
        String isAdmin = "";
        
        for(Role role: roles) {
        	if(role.getName().toString().equals("ADMIN")) {
        		isAdmin += "ADMIN";
        	}
        }
        if(isAdmin.equals("")) throw new RuntimeException("You are not registered as Admin");
        
        List<AccountDTO> accounts = accountRepository.findAll()
        		.stream()
        		.map(acc -> new AccountDTO(
        				acc.getId(),
                        acc.getAccountHolderName(),
                        acc.getAccountNumber(),
                        acc.getAccountBalance()
        		))
        		.toList();
        
        return ResponseEntity.ok(accounts);
    }
}
