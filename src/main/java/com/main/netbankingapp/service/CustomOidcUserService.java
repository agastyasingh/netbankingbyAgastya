package com.main.netbankingapp.service;

import com.main.netbankingapp.model.*;
import com.main.netbankingapp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private RoleService roleService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("========== CustomOidcUserService.loadUser() CALLED ==========");
        
        // Delegate to the default implementation for loading the user
        OidcUser oidcUser = super.loadUser(userRequest);
        
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
//        String phoneNumber = oidcUser.getPhoneNumber();
        
        logger.info("OIDC User Details - Email: {}, Name: {}", email, name);
        
        try {
            // Find or create user
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                logger.info("Creating NEW user for email: {}", email);
                
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setAccountHolderName(name);
                // Phone number is not typically available from Google OIDC
                newUser.setPhoneNumber(null);
                
                // Assign default USER role
                Role userRole = roleService.findOrCreateRole(RoleEnum.USER);
                logger.info("Role fetched/created: {}", userRole.getName());
                
                newUser.setRoles(Set.of(userRole));
                newUser = userRepository.save(newUser);
                logger.info("User SAVED with ID: {}", newUser.getId());
                
                // Create account
                Account account = new Account();
                account.setUser(newUser);
                account.setAccountHolderName(name);
                account.setAccountBalance(1000.0);
                account.setAccountNumber(accountService.setAccountNumber(name));
                accountRepository.save(account);
                logger.info("Account SAVED with number: {}", account.getAccountNumber());
                
                return newUser;
            });
            
            // Build authorities from database roles
            Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
            
            logger.info("Authorities granted: {}", authorities);
            
            // Return OIDC user with custom authorities
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            
        } catch (Exception e) {
            logger.error("ERROR in loadUser: ", e);
            throw new OAuth2AuthenticationException("Error processing OIDC user");
        }
    }


}
