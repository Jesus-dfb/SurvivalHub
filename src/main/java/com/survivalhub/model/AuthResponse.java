package com.survivalhub.model;

public class AuthResponse {

    private String token;

    private AppUser user;

    public AuthResponse() {
    }

    public AuthResponse(String token, AppUser user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }
}
