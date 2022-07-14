package com.skp.bookshow.service;

import com.skp.bookshow.exception.AccountNotFound;
import com.skp.bookshow.model.Account;
import org.springframework.stereotype.Component;

@Component
public interface AccountService {
    Account createAccount(Account account);

    Boolean login(Long id, String password) throws AccountNotFound;
}
