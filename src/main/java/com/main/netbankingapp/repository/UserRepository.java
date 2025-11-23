package com.main.netbankingapp.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.main.netbankingapp.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findByPhoneNumber(String phoneNumber);
	Optional<User> findByEmail(String email);
}
