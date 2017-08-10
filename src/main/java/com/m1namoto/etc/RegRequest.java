package com.m1namoto.etc;

public class RegRequest {
    private final String name;
    private final String surname;
    private final String login;
    private final String password;
    private final String stat;

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

    public String getSurname() {
        return surname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getStat() {
        return stat;
    }
}
