package com.naturalmilk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String email;
    private String password;
    private String name; // Only for signup
    
    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    
    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String email;
        private String password;
        private String name;
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public AuthRequest build() {
            AuthRequest request = new AuthRequest();
            request.email = this.email;
            request.password = this.password;
            request.name = this.name;
            return request;
        }
    }
}
