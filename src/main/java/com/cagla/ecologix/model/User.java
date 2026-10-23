package com.cagla.ecologix.model;

import com.cagla.ecologix.model.enums.UserRole; // ADMIN, COURIER, CUSTOMER
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private Long courierId; // Eğer kuryeyse kendi verilerine erişebilmesi için bağlantı ID'si
    @Embedded
    private BranchInfo branchInfo;
}