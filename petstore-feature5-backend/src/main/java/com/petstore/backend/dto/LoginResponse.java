package com.petstore.backend.dto;

public class LoginResponse {
    private String token;
    private String userName;
    private String email;
    private String role;
    private boolean success;
    private String message;

    // Constructores
    public LoginResponse() {
    }

    public LoginResponse(String token, String userName, String email, String role) {
        this.token = token;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.success = true;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
