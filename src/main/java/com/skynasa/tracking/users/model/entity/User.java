package com.skynasa.tracking.users.model.entity;

import com.skynasa.tracking.users.model.dto.UserDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "uss_idx_parent_id", columnList = "parent_id"),
        @Index(name = "uss_idx_username", columnList = "username"),
        @Index(name = "uss_idx_email", columnList = "email"),
})
public class User {

   @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "identify_number")
    private String identifyNumber;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_code")
    private String phoneCode;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role")
    private String role;

    @Column(name = "status")
    private Boolean status;

    // @Column(name = "password")
    // private String password;

    @Column(name = "address")
    private String address;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "locale")
    private String locale;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "keycloak_user_id", unique = true)
    private String keycloakUserId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static User toEntity(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .id(dto.getId())
                .name(dto.getName())
                .identifyNumber(dto.getIdentifyNumber())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneCode(dto.getPhoneCode())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .status(dto.getStatus())
                // .password(dto.getPassword())
                .address(dto.getAddress())
                .subscriptionStartDate(dto.getSubscriptionStartDate())
                .timezone(dto.getTimezone())
                .locale(dto.getLocale())
                .parentId(dto.getParentId())
                .keycloakUserId(dto.getKeycloakUserId())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
