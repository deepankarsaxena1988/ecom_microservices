package com.ecom.accountaddresses.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.accountaddresses.dto.AccountAddressDTO;
import com.ecom.accountaddresses.entity.AccountAddress;
import com.ecom.accountaddresses.service.AccountAddressService;

@RestController
@RequestMapping("/account-addresses")
public class AccountAddressController {

    private final AccountAddressService accountAddressService;

    public AccountAddressController(AccountAddressService accountAddressService) {
        this.accountAddressService = accountAddressService;
    }

    @PostMapping
    public AccountAddress addAddress(@RequestBody AccountAddressDTO accountAddressDTO) {
        return accountAddressService.addAddress(accountAddressDTO);
    }

    @PutMapping("/{id}")
    public AccountAddress updateAddress(@PathVariable Long id, @RequestBody AccountAddress accountAddress) {
        return accountAddressService.updateAddress(id, accountAddress);
    }

    @GetMapping("/{id}")
    public AccountAddress getAddress(@PathVariable Long id) {
        System.out.println("Getting address with id: " + id);
        return accountAddressService.getAddress(id);
    }

    @GetMapping
    public List<AccountAddressDTO> getAddresses(@RequestParam(name = "acntId", required = false) Long acntId) {
        if (acntId != null) {
            return accountAddressService.getAddressesByAccountId(acntId);
        }
        return accountAddressService.getAllAddresses();
    }
}
