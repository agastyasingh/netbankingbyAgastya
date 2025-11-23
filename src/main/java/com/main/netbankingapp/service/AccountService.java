package com.main.netbankingapp.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.main.netbankingapp.exceptions.AccountNotFoundException;
import com.main.netbankingapp.model.Account;
import com.main.netbankingapp.model.Transaction;
import com.main.netbankingapp.repository.AccountRepository;
import com.main.netbankingapp.repository.TransactionRepository;
import com.main.netbankingapp.repository.UserRepository;
import com.main.netbankingapp.model.User;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@Service
public class AccountService {
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	
	public AccountService(AccountRepository accountRepository, 
			TransactionRepository transactionRepository,
			UserRepository userRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
	}
	
	//get live exchange rates for user's account balance
	public Map<String, Double> getUsdRate() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.freecurrencyapi.com/v1/latest?apikey=fca_live_rKzKMr6IjjOQOVBNkBbSHuwpTnjdEPPqyR1rUOTT&currencies=EUR%2CUSD%2CCAD%2CAUD%2CGBP&base_currency=INR"))
				.GET()
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode root = objectMapper.readTree(response.body());
		
		Map<String, Double> currencyMap = objectMapper.convertValue(
				root.get("data"),
				new TypeReference<Map<String, Double>>() {}
				);
		
		return currencyMap;

	}
	
	
	// get accounts by user 
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }
	
	//creates/sets accountNumber
	public String setAccountNumber(String name) {
		String tmp = "";
		
		if(name == null) {
			return "Please provide both your Name and Phone No to create an account.";
		}else {
			tmp = name.replaceAll("\\s+", "");
		}
		
		String accountNumber = tmp + currentTimeInMs();
		return accountNumber;
	}
	
	private String currentTimeInMs(){
        return String.valueOf(System.currentTimeMillis());
     }
	
	//creates account and maps to currently logged-in users
	public String createAccount(String userEmail, String name, String phoneNo) {
        try {
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Account newAccount = new Account();
            newAccount.setUser(user);  //maps to the current logged-in user
            newAccount.setAccountBalance(1000.00);
            newAccount.setAccountNumber(setAccountNumber(name));
            newAccount.setAccountHolderName(name);
            
            accountRepository.save(newAccount);
            return "Account created successfully with default balance of 1000";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error Occurred: " + e.getMessage();
        }
    }
	
	@Cacheable("accountDetailsCacheByNo")
	public Account getDetailsByAccountNo(String accountNumber) {
		Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
		
		if(account.isPresent()) {
			return account.get();
		}else {
			throw new AccountNotFoundException("No account found with account number" + accountNumber);
		}
	}
	
	@Cacheable("accountDetailsCacheByName")
	public Account getDetailsByAccountHolderName(String name) {
		Optional<Account> account = accountRepository.findByAccountHolderName(name);
		
		if(account.isPresent()) {
			return account.get();
		}else {
			throw new AccountNotFoundException("No account found associated with Name: " + name);
		}
	}
	
	@Cacheable(value = "accountBalance", key = "#accountNumber")
	public Double getAccountBalance(String accountNumber) {
		return accountRepository.findByAccountNumber(accountNumber)
				.map(Account::getAccountBalance)
				.orElseThrow(() -> new RuntimeException("Account not found"));
	}
	
	@Transactional
	public void transfer(String fromAccount, String toAccount, Double amount) {
		Account from = accountRepository.findByAccountNumber(fromAccount)
				.orElseThrow(() -> new RuntimeException("Source account not found"));
		
		Account to = accountRepository.findByAccountNumber(toAccount)
				.orElseThrow(() -> new RuntimeException("To account not found"));
		
//		Long id = from.getId();
//		User user = userRepository.findById(id);
		
		if(from.getAccountBalance() < amount) throw new RuntimeException("Not enough balance");
		
		from.setAccountBalance(from.getAccountBalance() - amount);
		
		to.setAccountBalance(to.getAccountBalance() + amount);
		
		accountRepository.save(from);
		accountRepository.save(to);
		
		Transaction debit = new Transaction();
		debit.setType("DEBIT");
		debit.setAmount(amount);
		debit.setAccount(from);
		debit.setTimestamp(LocalDateTime.now());
		transactionRepository.save(debit);
		
		Transaction credit = new Transaction();
		credit.setType("CREDIT");
		credit.setAmount(amount);
		credit.setAccount(to);
		credit.setTimestamp(LocalDateTime.now());
		transactionRepository.save(credit);
	}
	
}
