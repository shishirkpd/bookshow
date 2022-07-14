package com.skp.bookshow.service.impl;

import com.skp.bookshow.exception.AccountNotFound;
import com.skp.bookshow.model.Account;
import com.skp.bookshow.repository.AccountRepo;
import com.skp.bookshow.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepo accountRepo;

    @Override
    public Account createAccount(Account account) {
        return accountRepo.save(account);
    }

    @Override
    public Boolean login(Long id, String password) throws AccountNotFound {
        var optionAccount = accountRepo.findById(id);
        if(optionAccount.isPresent())
           return optionAccount.get().getPassword().equals(password);
        else throw new AccountNotFound("Account for given phone number: " + id);
    }
}
