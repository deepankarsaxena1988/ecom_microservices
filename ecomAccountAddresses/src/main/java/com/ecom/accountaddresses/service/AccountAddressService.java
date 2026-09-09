package com.ecom.accountaddresses.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecom.accountaddresses.entity.AccountAddress;
import com.ecom.accountaddresses.repository.AccountAddressRepository;

@Service
public class AccountAddressService {

    private final AccountAddressRepository accountAddressRepository;

    public AccountAddressService(AccountAddressRepository accountAddressRepository) {
        this.accountAddressRepository = accountAddressRepository;
    }

    public AccountAddress addAddress(AccountAddress accountAddress) {
        accountAddress.setId(null);
        applyDefaults(accountAddress);
        return accountAddressRepository.save(accountAddress);
    }

    public AccountAddress updateAddress(Long id, AccountAddress updatedAddress) {
        AccountAddress existingAddress = accountAddressRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        existingAddress.setUserId(updatedAddress.getUserId());
        existingAddress.setAddressType(updatedAddress.getAddressType());
        existingAddress.setFullName(updatedAddress.getFullName());
        existingAddress.setMobileNumber(updatedAddress.getMobileNumber());
        existingAddress.setAddressLine1(updatedAddress.getAddressLine1());
        existingAddress.setAddressLine2(updatedAddress.getAddressLine2());
        existingAddress.setLandmark(updatedAddress.getLandmark());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setState(updatedAddress.getState());
        existingAddress.setPostalCode(updatedAddress.getPostalCode());
        existingAddress.setCountry(updatedAddress.getCountry());
        existingAddress.setIsDefault(updatedAddress.getIsDefault());

        applyDefaults(existingAddress);
        return accountAddressRepository.save(existingAddress);
    }

    public AccountAddress getAddress(Long id) {
        return accountAddressRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    public List<AccountAddress> getAddressesByUserId(Long userId) {
        return accountAddressRepository.findByUserId(userId);
    }

    public List<AccountAddress> getAllAddresses() {
        return accountAddressRepository.findAll();
    }

    private void applyDefaults(AccountAddress accountAddress) {
        if (accountAddress.getCountry() == null || accountAddress.getCountry().isBlank()) {
            accountAddress.setCountry("India");
        }
        if (accountAddress.getIsDefault() == null) {
            accountAddress.setIsDefault(Boolean.FALSE);
        }
    }
}
