package com.m1namoto.etc;

public class RegRequest {
    private String name;
    private String surname;
    private String login;
    private String password;
    private String stat;
    
    public RegRequest(String name, String surname, String login, String password, String stat) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.stat = stat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
    
}
