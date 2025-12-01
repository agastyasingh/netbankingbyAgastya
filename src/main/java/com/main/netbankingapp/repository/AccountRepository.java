package com.main.netbankingapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.main.netbankingapp.model.Account;
import com.main.netbankingapp.model.User;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{
	
	List<Account> findByUser(User user);
	Optional<Account> findByAccountNumber(String accountNumber);
	Optional<Account> findByIdAndUser(Long accountId, User user);
	Optional<Account> findByAccountHolderName(String accountHolderName);
	
	List<Account> findAll();
	
}
