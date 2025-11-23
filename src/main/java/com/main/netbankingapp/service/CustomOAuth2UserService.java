package com.main.netbankingapp.service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.main.netbankingapp.model.Account;
import com.main.netbankingapp.model.Role;
import com.main.netbankingapp.model.RoleEnum;
import com.main.netbankingapp.model.User;
import com.main.netbankingapp.repository.AccountRepository;
import com.main.netbankingapp.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{
	
	private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
	
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private AccountRepository accountRepository;
    

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    	logger.info("========== CustomOAuth2UserService.loadUser() CALLED ==========");
    	
    	try {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String phoneNumber = oAuth2User.getAttribute("phone_number"); // If available
        
        logger.info("OAuth2 User Details - Email: {}, Name: {}", email, name);
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        logger.info("User exists in DB: {}", existingUser.isPresent());

        //create a new user and assign an account if not already present
        User user = existingUser
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setAccountHolderName(name);
                    if(phoneNumber != null) newUser.setPhoneNumber(phoneNumber);
                    
                    
                    Role userRole = roleService.findOrCreateRole(RoleEnum.USER);
                    logger.info("Role fetched/created: {}", userRole.getName());
                    newUser.setRoles(Set.of(userRole));
                    
                    newUser = userRepository.save(newUser);
                    logger.info("User SAVED with ID: {}", newUser.getId());
                    
                    Account newAccount = new Account();
                    newAccount.setUser(newUser);
                    newAccount.setAccountHolderName(name);
                    newAccount.setAccountBalance(1000.00);
                    newAccount.setAccountNumber(accountService.setAccountNumber(name));
                    accountRepository.save(newAccount);
                    
                    return newUser;
                });
        
        //if user details have changed since last login, then update
        if(!Objects.equals(user.getAccountHolderName(), name) || 
        		(phoneNumber!=null && !Objects.equals(user.getPhoneNo(), phoneNumber))) {
        	user.setAccountHolderName(name);
        	if(phoneNumber!=null) {
        		user.setPhoneNumber(phoneNumber);
        	}
        	userRepository.save(user);
        }
        
        
        //if user was created manually in DB then assign account if not present
        if(accountRepository.findByUser(user).isEmpty()) {
        	Account newAccount = new Account();
        	newAccount.setUser(user);
            newAccount.setAccountHolderName(name);
            newAccount.setAccountBalance(1000.00);
            newAccount.setAccountNumber(accountService.setAccountNumber(name));
            accountRepository.save(newAccount);
        }

        //grants authority from user's actual roles in DB
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
        

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
        
    	}catch(Exception e) {
    		logger.error("ERROR in loadUser: ", e);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user");
    	}
    }
}
