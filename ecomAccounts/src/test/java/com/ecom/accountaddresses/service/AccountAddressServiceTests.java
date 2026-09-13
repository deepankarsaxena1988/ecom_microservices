package com.ecom.accountaddresses.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ecom.accountaddresses.dto.AccountAddressDTO;
import com.ecom.accountaddresses.entity.AccountAddress;
import com.ecom.accountaddresses.repository.AccountAddressRepository;

class AccountAddressServiceTests {

    private final AccountAddressRepository repository = mock(AccountAddressRepository.class);
    private final AccountAddressService service = new AccountAddressService(repository);

    @Test
    void usesCustomLabelAsAddressTypeWhenTypeIsOther() {
        when(repository.save(any(AccountAddress.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AccountAddressDTO address = new AccountAddressDTO();
        address.setAddressType("Other");
        address.setCustomLabel("  Parents' House  ");

        AccountAddress savedAddress = service.addAddress(address);

        assertEquals("Parents' House", savedAddress.getAddressType());
    }

    @Test
    void rejectsOtherTypeWithoutCustomLabel() {
        AccountAddressDTO address = new AccountAddressDTO();
        address.setAddressType("Other");

        assertThrows(ResponseStatusException.class, () -> service.addAddress(address));
    }

    @Test
    void keepsStandardAddressType() {
        when(repository.save(any(AccountAddress.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AccountAddressDTO address = new AccountAddressDTO();
        address.setAddressType("HOME");
        address.setCustomLabel("Ignored");

        AccountAddress savedAddress = service.addAddress(address);

        assertEquals("HOME", savedAddress.getAddressType());
    }

    @Test
    void returnsAddressesAsDTOs() {
        AccountAddress address = new AccountAddress();
        address.setId(10L);
        address.setAcntId(20L);
        address.setAddressType("HOME");
        address.setCity("Pune");
        when(repository.findByAcntId(20L)).thenReturn(List.of(address));

        List<AccountAddressDTO> addresses = service.getAddressesByAccountId(20L);

        assertEquals(1, addresses.size());
        assertEquals(10L, addresses.get(0).getId());
        assertEquals(20L, addresses.get(0).getAcntId());
        assertEquals("HOME", addresses.get(0).getAddressType());
        assertEquals("Pune", addresses.get(0).getCity());
    }
}
