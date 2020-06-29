package com.dansoftware.libraryapp.appdata.config;

import com.dansoftware.libraryapp.db.Account;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginData {

    private final List<Account> lastAccounts;
    private Account loggedAccount;

    public LoginData() {
        this(new ArrayList<>(), null);
    }

    @SuppressWarnings("unchecked")
    public LoginData(List<Account> lastAccounts, Account loggedAccount) {
        this.lastAccounts = (List<Account>) ListUtils.predicatedList(
                Objects.requireNonNull(lastAccounts, "lastAccounts mustn't be null"),
                PredicateUtils.notNullPredicate()
        );
        this.loggedAccount = loggedAccount;

        if (Objects.nonNull(loggedAccount) && !this.lastAccounts.contains(loggedAccount)) {
            this.lastAccounts.add(loggedAccount);
        }
    }

    public void setLoggedAccount(Account loggedAccount) {
        this.loggedAccount = loggedAccount;
    }

    public List<Account> getLastAccounts() {
        return lastAccounts;
    }

    public Account getLoggedAccount() {
        return loggedAccount;
    }
}
