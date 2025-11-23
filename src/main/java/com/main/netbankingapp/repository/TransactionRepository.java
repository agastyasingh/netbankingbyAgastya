package com.main.netbankingapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.main.netbankingapp.model.Account;
import com.main.netbankingapp.model.Transaction;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	List<Transaction> findByAccount(Account account);
	
	Optional<Transaction> findByTransactionIdAndAccount(Long transactionId, Account account);
	
	@Query("SELECT t FROM Transaction t JOIN t.account a JOIN a.user u WHERE u.id = :userId AND a.id = :accountId AND t.transactionId = :transactionId")
	Optional<Transaction> findTransactionByUserAccountAndTransactionId(
	        @Param("userId") Long userId,
	        @Param("accountId") Long accountId,
	        @Param("transactionId") Long transactionId
	);
}
