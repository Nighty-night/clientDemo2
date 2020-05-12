package com.sdb.netool.conf;

import java.util.List;

public class CompanyData {
    String qydm;
    String name;
    List<Account> accounts;
    
    public CompanyData() {
        super();
    }
    
    public CompanyData(String qydm, String name, List<Account> accounts) {
        super();
        this.qydm = qydm;
        this.name = name;
        this.accounts = accounts;
    }

    public String getQydm() {
        return qydm;
    }

    public void setQydm(String qydm) {
        this.qydm = qydm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
    
    
}
