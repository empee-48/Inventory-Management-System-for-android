package com.brocode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String createdBy;
    private String lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        ZoneId harareZone = ZoneId.of("Africa/Harare");
        LocalDateTime now = LocalDateTime.now(harareZone);

        createdAt = now;
        lastModifiedAt = now;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean USER_IS_ANONYMOUS = auth.getName().equalsIgnoreCase("anonymousUser");

        if(auth.isAuthenticated() && !USER_IS_ANONYMOUS){
            createdBy = auth.getName();
            lastModifiedBy = auth.getName();
        } else {
            lastModifiedBy = "System";
            createdBy = "System";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ZoneId harareZone = ZoneId.of("Africa/Harare");
        lastModifiedAt = LocalDateTime.now(harareZone);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean USER_IS_ANONYMOUS = auth.getName().equalsIgnoreCase("anonymousUser");

        if(auth.isAuthenticated() && !USER_IS_ANONYMOUS){
            lastModifiedBy = auth.getName();
        } else {
            lastModifiedBy = "System";
        }
    }
}