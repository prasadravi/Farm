package com.naturalmilk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
    private String message;
    
    // Getters
    public String getToken() { return token; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
    
    // Setters
    public void setToken(String token) { this.token = token; }
    public void setUser(User user) { this.user = user; }
    public void setMessage(String message) { this.message = message; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String token;
        private User user;
        private String message;
        
        public Builder token(String token) {
            this.token = token;
            return this;
        }
        
        public Builder user(User user) {
            this.user = user;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.token = this.token;
            response.user = this.user;
            response.message = this.message;
            return response;
        }
    }
}
