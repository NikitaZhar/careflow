package com.careflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "client_invitations")
public class ClientInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false)
    private boolean used;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    public ClientInvitation() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public boolean isUsed() {
        return used;
    }

    public User getProvider() {
        return provider;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }
}