package com.skp.bookshow.service.impl;

import com.skp.bookshow.exception.AccountNotFound;
import com.skp.bookshow.model.Account;
import com.skp.bookshow.model.enums.AccountType;
import com.skp.bookshow.repository.AccountRepo;
import com.skp.bookshow.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private AccountService accountService = new AccountServiceImpl();


    @Test
    public void it_should_create_account() {
        Account account = new Account();
        account.setAccounttype(AccountType.ADMIN);
        account.setEmail("some@s.com");
        account.setName("first last");
        account.setPhone(123456L);
        account.setPassword("A123");

        when(accountRepo.save(account)).thenReturn(account);

        var result = accountService.createAccount(account);

        Assertions.assertTrue(result.getPhone().equals(account.getPhone()));
    }

    @Test
    public void login_should_be_successful() throws AccountNotFound {
        Account account = new Account();
        account.setAccounttype(AccountType.ADMIN);
        account.setEmail("some@s.com");
        account.setName("first last");
        account.setPhone(123456L);
        account.setPassword("A123");

        when(accountRepo.findById(Mockito.any())).thenReturn(Optional.of(account));

        var phoneNumber = 123456L;
        var pass = "A123";
        var result = accountService.login(phoneNumber, pass);

        Assertions.assertTrue(result);
    }

    @Test
    public void login_should_be_exception() {
        Account account = new Account();
        account.setAccounttype(AccountType.ADMIN);
        account.setEmail("some@s.com");
        account.setName("first last");
        account.setPhone(123456L);
        account.setPassword("A123");

        when(accountRepo.findById(Mockito.any())).thenReturn(Optional.empty());

        var id = 123456L;
        var pass = "A1234";

        var result = assertThrows(AccountNotFound.class, () -> accountService.login(id, pass));

        Assertions.assertEquals("Account for given phone number: 123456", result.getMessage());
    }
}