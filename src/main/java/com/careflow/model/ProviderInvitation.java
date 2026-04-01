package com.careflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "provider_invitations")
public class ProviderInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean used;

    public ProviderInvitation() {
    }

    public ProviderInvitation(String email, String token, boolean used) {
        this.email = email;
        this.token = token;
        this.used = used;
    }

    public Long getId() {
        return id;
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

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}