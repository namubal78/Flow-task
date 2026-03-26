package com.flowtask.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_system")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "insert_date", updatable = false)
    private LocalDateTime insertDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "insert_id", updatable = false, length = 50)
    private String insertId;

    @Column(name = "update_id", length = 50)
    private String updateId;

    @PrePersist
    protected void onCreate() {
        insertDate = LocalDateTime.now();
        updateDate = LocalDateTime.now();
        insertId = "admin";
        updateId = "admin";
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
        updateId = "admin";
    }

    public static BlockedSystem create(String comment) {
        BlockedSystem system = new BlockedSystem();
        system.comment = comment;
        return system;
    }
}
