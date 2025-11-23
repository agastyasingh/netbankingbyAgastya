package com.main.netbankingapp.controller;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.main.netbankingapp.dto.AccountDTO;
import com.main.netbankingapp.model.Account;
import com.main.netbankingapp.model.User;
import com.main.netbankingapp.repository.UserRepository;
import com.main.netbankingapp.service.AccountService;


@RestController
@RequestMapping("/api/accounts")
//@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@CrossOrigin(origins = "https://lightyellow-jaguar-749540.hostingersite.com", allowCredentials = "true")
public class AccountController {

	@Autowired
	private AccountService accountService;
	
	@Autowired
    private UserRepository userRepository;
	
	public void AccountController(AccountService accountService) {
		this.accountService = accountService;
	}
	
	@GetMapping("/my-accounts")
	public ResponseEntity<List<AccountDTO>> getMyAccounts(@AuthenticationPrincipal OidcUser oidcUser) {
	    User user = userRepository.findByEmail(oidcUser.getEmail())
	        .orElseThrow(() -> new RuntimeException("User not found"));
	    
	    List<AccountDTO> accounts = accountService.getAccountsByUser(user)
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
	
	@GetMapping("/live-exchange-rates")
	public ResponseEntity<Map<String, Double>> getExchangeRates(@AuthenticationPrincipal OidcUser oidcUser) throws IOException, InterruptedException{
		User user = userRepository.findByEmail(oidcUser.getEmail())
		        .orElseThrow(() -> new RuntimeException("User not found"));
		
		List<Account> accounts = accountService.getAccountsByUser(user);
		double totalBalance = accounts.stream()
				.mapToDouble(Account::getAccountBalance)
				.sum();
		
		Map<String, Double> currencyMap = new HashMap<>(accountService.getUsdRate());
		
		Map<String, Double> updatedAmount = currencyMap.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						entry -> entry.getValue()*totalBalance
						));
		
		return ResponseEntity.ok(updatedAmount);
		
	}

    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userRepository.findByEmail(oidcUser.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Account> accounts = accountService.getAccountsByUser(user);
        double totalBalance = accounts.stream()
            .mapToDouble(Account::getAccountBalance)
            .sum();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBalance", totalBalance);
        summary.put("accountCount", accounts.size());
        summary.put("recentTransactions", 0); // TODO: implement transactions
        
        return ResponseEntity.ok(summary);
    }
	
    @PostMapping("/createAccount")
    public ResponseEntity<String> createAccount(
        @RequestParam String name, 
        @RequestParam String phoneNo,
        @AuthenticationPrincipal OidcUser oidcUser 
    ) {
        String message = accountService.createAccount(oidcUser.getEmail(), name, phoneNo);
        return ResponseEntity.ok(message);
    }
    
    
	
	
	@GetMapping("/{accountNumber}/balance")
	public ResponseEntity<Double> getBalance(@PathVariable String accountNumber){
		Double balance = accountService.getAccountBalance(accountNumber);
		return ResponseEntity.ok(balance);
	}
	
	@GetMapping("/{accountNumber}/getDetailsByAccountNo")
	public ResponseEntity<Account> getDetailsByAccountNo(@PathVariable String accountNumber){
		 Account account = accountService.getDetailsByAccountNo(accountNumber);
		return ResponseEntity.ok(account);
	}
	
	@GetMapping("/{name}/getDetailsByName")
	public ResponseEntity<Account> getDetailsByName(@PathVariable String name){
		 Account account = accountService.getDetailsByAccountHolderName(name);
		return ResponseEntity.ok(account);
	}
	
	@PostMapping("/transfer")
	public ResponseEntity<String> transferMoney(@RequestBody TransferRequest request){
		accountService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount());
		return ResponseEntity.ok("Transaction Successful");
	}
	
//	@PostMapping("/deleteAccount")
//	public ResponseEntity<String> deleteAccount(@RequestBody )
}
