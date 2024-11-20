package com.SpringProject.BankApp.Service;


import com.SpringProject.BankApp.Model.Transaction;
import com.SpringProject.BankApp.Repository.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    public List<Transaction> findByAccountId(Long accountId)
    {
        return transactionRepo.findByAccountId(accountId);
    }

    public void saveTransaction(Transaction transaction)
    {
        transactionRepo.save(transaction);
    }
}

