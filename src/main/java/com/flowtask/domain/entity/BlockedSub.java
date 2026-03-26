package com.flowtask.domain.entity;

import com.flowtask.domain.enums.BlockedType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_sub")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", nullable = false)
    private BlockedSystem system;

    @Column(name = "blocked_extension", nullable = false, length = 20)
    private String blockedExtension;

    @Enumerated(EnumType.STRING)
    @Column(name = "blocked_type", nullable = false, length = 10)
    private BlockedType blockedType;

    @Column(name = "insert_date", updatable = false)
    private LocalDateTime insertDate;

    @Column(name = "insert_id", updatable = false, length = 50)
    private String insertId;

    @Column(name = "comment")
    private String comment;

    @PrePersist
    protected void onCreate() {
        insertDate = LocalDateTime.now();
        insertId = "admin";
    }

    public static BlockedSub create(BlockedSystem system, String extension, BlockedType type) {
        BlockedSub sub = new BlockedSub();
        sub.system = system;
        sub.blockedExtension = extension.toLowerCase().trim();
        sub.blockedType = type;
        return sub;
    }
}
