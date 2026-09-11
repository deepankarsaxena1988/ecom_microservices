package com.ecom.accountaddresses.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.accountaddresses.entity.AccountAddress;

@Repository
public interface AccountAddressRepository extends JpaRepository<AccountAddress, Long> {

    List<AccountAddress> findByUserId(Long userId);
}
