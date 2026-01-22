package br.com.baggiotech.tecos_api.domain.passwordresettoken;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private String email;
    private String token;
    private LocalDateTime createdAt;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String email, String token, LocalDateTime createdAt) {
        this.email = email;
        this.token = token;
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
