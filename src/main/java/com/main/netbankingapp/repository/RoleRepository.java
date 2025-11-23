package com.main.netbankingapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.main.netbankingapp.model.Role;
import com.main.netbankingapp.model.RoleEnum;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
	Optional<Role> findByName(RoleEnum name);
}
