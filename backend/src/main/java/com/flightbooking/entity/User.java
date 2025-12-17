package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_user", 
       uniqueConstraints = @UniqueConstraint(name = "auth_user_email_key", columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(nullable = false, length = 255)
    private String id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(length = 255)
    private String phone;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(length = 255)
    private String role;
    
    @Column(length = 255)
    private String status;
    
    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

