package com.skynasa.tracking.users.model.dto;

import com.skynasa.tracking.users.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private UUID id;
    private String name;
    private String identifyNumber;
    private String username;
    private String email;
    private String phoneCode;
    private String phone;
    private String role;
    private Boolean status;
    private String password;
    private String address;
    private LocalDate subscriptionStartDate;
    private String timezone;
    private String locale;
    private UUID parentId;
    private String keycloakUserId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static UserDto toDto(User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .id(entity.getId())
                .name(entity.getName())
                .identifyNumber(entity.getIdentifyNumber())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .phoneCode(entity.getPhoneCode())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .status(entity.getStatus())
                // .password(entity.getPassword())
                .address(entity.getAddress())
                .subscriptionStartDate(entity.getSubscriptionStartDate())
                .timezone(entity.getTimezone())
                .locale(entity.getLocale())
                .parentId(entity.getParentId())
                .keycloakUserId(entity.getKeycloakUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
