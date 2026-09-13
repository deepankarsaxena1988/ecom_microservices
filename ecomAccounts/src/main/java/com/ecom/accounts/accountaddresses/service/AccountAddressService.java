package com.ecom.accountaddresses.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecom.accountaddresses.dto.AccountAddressDTO;
import com.ecom.accountaddresses.entity.AccountAddress;
import com.ecom.accountaddresses.repository.AccountAddressRepository;

@Service
public class AccountAddressService {
    private final AccountAddressRepository accountAddressRepository;

    public AccountAddressService(AccountAddressRepository accountAddressRepository) {
        this.accountAddressRepository = accountAddressRepository;
    }

    public AccountAddress addAddress(AccountAddressDTO accountAddressDTO) {
        AccountAddress accountAddress = toEntity(accountAddressDTO);
        applyDefaults(accountAddress);
        return accountAddressRepository.save(accountAddress);
    }

    public AccountAddress updateAddress(Long id, AccountAddress updatedAddress) {
        AccountAddress existingAddress = accountAddressRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

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

    public List<AccountAddressDTO> getAddressesByAccountId(Long acntId) {
        return accountAddressRepository.findByAcntId(acntId).stream()
            .map(this::toDTO)
            .toList();
    }

    public List<AccountAddressDTO> getAllAddresses() {
        return accountAddressRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    private AccountAddress toEntity(AccountAddressDTO accountAddressDTO) {
        AccountAddress accountAddress = new AccountAddress();
        accountAddress.setAcntId(accountAddressDTO.getAcntId());
        accountAddress.setAddressType(resolveAddressType(accountAddressDTO));
        accountAddress.setFullName(accountAddressDTO.getFullName());
        accountAddress.setMobileNumber(accountAddressDTO.getMobileNumber());
        accountAddress.setAddressLine1(accountAddressDTO.getAddressLine1());
        accountAddress.setAddressLine2(accountAddressDTO.getAddressLine2());
        accountAddress.setLandmark(accountAddressDTO.getLandmark());
        accountAddress.setCity(accountAddressDTO.getCity());
        accountAddress.setState(accountAddressDTO.getState());
        accountAddress.setPostalCode(accountAddressDTO.getPostalCode());
        accountAddress.setCountry(accountAddressDTO.getCountry());
        accountAddress.setIsDefault(accountAddressDTO.getIsDefault());
        return accountAddress;
    }

    private AccountAddressDTO toDTO(AccountAddress accountAddress) {
        AccountAddressDTO accountAddressDTO = new AccountAddressDTO();
        accountAddressDTO.setId(accountAddress.getId());
        accountAddressDTO.setAcntId(accountAddress.getAcntId());
        accountAddressDTO.setAddressType(accountAddress.getAddressType());
        accountAddressDTO.setFullName(accountAddress.getFullName());
        accountAddressDTO.setMobileNumber(accountAddress.getMobileNumber());
        accountAddressDTO.setAddressLine1(accountAddress.getAddressLine1());
        accountAddressDTO.setAddressLine2(accountAddress.getAddressLine2());
        accountAddressDTO.setLandmark(accountAddress.getLandmark());
        accountAddressDTO.setCity(accountAddress.getCity());
        accountAddressDTO.setState(accountAddress.getState());
        accountAddressDTO.setPostalCode(accountAddress.getPostalCode());
        accountAddressDTO.setCountry(accountAddress.getCountry());
        accountAddressDTO.setIsDefault(accountAddress.getIsDefault());
        accountAddressDTO.setCreatedAt(accountAddress.getCreatedAt());
        accountAddressDTO.setUpdatedAt(accountAddress.getUpdatedAt());
        return accountAddressDTO;
    }

    private String resolveAddressType(AccountAddressDTO accountAddressDTO) {
        if (!"Other".equalsIgnoreCase(accountAddressDTO.getAddressType())) {
            return accountAddressDTO.getAddressType();
        }

        String customLabel = accountAddressDTO.getCustomLabel();
        if (customLabel == null || customLabel.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "customLabel is required when addressType is Other"
            );
        }
        return customLabel.trim();
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
