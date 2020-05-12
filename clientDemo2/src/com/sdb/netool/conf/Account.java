package com.sdb.netool.conf;

public class Account {
    String account;
    String accountName;

    public Account() {
        
    }

    public Account(String account, String accountName) {
        super();
        this.account = account;
        this.accountName = accountName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

}
