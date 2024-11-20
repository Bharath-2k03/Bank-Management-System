package com.SpringProject.BankApp.Service;

import com.SpringProject.BankApp.Model.Account;
import com.SpringProject.BankApp.Model.Transaction;
import com.SpringProject.BankApp.Repository.AccountRepo;
import com.SpringProject.BankApp.Repository.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    public Account findByAccountUsername(String username)
    {
        return accountRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account Not Found"));
    }

    public Account registerAccount(String username, String password)
    {
        if(accountRepo.findByUsername(username).isPresent())
        {
            throw new RuntimeException("Username Already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return accountRepo.save(account);
    }

    public void deposit(Account account,BigDecimal amount)
    {
        account.setBalance(account.getBalance().add(amount));
        accountRepo.save(account);

        Transaction transaction = new Transaction(
                amount,
                "Deposit",
                LocalDateTime.now(),
                account
        );
        transactionRepo.save(transaction);

    }

    public void withdraw(Account account,BigDecimal amount)
    {
        if(account.getBalance().compareTo(amount) < 0)
        {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepo.save(account);

        Transaction transaction = new Transaction(
                amount,
                "Withdrawal",
                LocalDateTime.now(),
                account
        );
        transactionRepo.save(transaction);

    }

    public List<Transaction> getTransactionHistory(Account account)
    {
        return transactionRepo.findByAccountId(account.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findByAccountUsername(username);
        if(account == null)
        {
            throw new UsernameNotFoundException("Username or Password Not Found");
        }

        return new Account(
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransactions(),
                authorities()
        );
    }

    public Collection<? extends GrantedAuthority> authorities()
    {
        return Arrays.asList(new SimpleGrantedAuthority("User"));
    }

    public void transferAccount(Account fromAccount, String toUsername, BigDecimal amount)
    {
        if(fromAccount.getBalance().compareTo(amount) < 0)
        {
            throw new RuntimeException("Insufficient funds");
        }

        Account toAccount = accountRepo.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Recipient Account Not found"));

        //debuct
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepo.save(fromAccount);

        //add
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepo.save(toAccount);

        //create transaction records
        Transaction debitTransaction = new Transaction(
                amount,
                "Transfer Out to " + toAccount.getUsername(),
                LocalDateTime.now(),
                fromAccount
        );
        transactionRepo.save(debitTransaction);

        Transaction creditTransaction = new Transaction(
                amount,
                "Transfer In to " + fromAccount.getUsername(),
                LocalDateTime.now(),
                toAccount
        );
        transactionRepo.save(creditTransaction);
    }
}

