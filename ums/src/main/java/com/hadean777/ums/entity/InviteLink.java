package com.hadean777.ums.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVITE_LINK")
public class InviteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "TOKEN", nullable = false, unique = true)
    private String token;

    @Column(name = "EXPIRATION_TIME", nullable = false)
    private LocalDateTime expirationTime;

    @Column(name = "USED", nullable = false)
    private boolean used = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
