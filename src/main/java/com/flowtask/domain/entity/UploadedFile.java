package com.flowtask.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", nullable = false)
    private BlockedSystem system;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @Column(name = "insert_date", updatable = false)
    private LocalDateTime insertDate;

    @Column(name = "insert_id", updatable = false, length = 50)
    private String insertId;

    @PrePersist
    protected void onCreate() {
        insertDate = LocalDateTime.now();
        insertId   = "admin";
    }

    public static UploadedFile create(BlockedSystem system, String originalName,
                                       String storedName, Long fileSize, String extension) {
        UploadedFile f = new UploadedFile();
        f.system       = system;
        f.originalName = originalName;
        f.storedName   = storedName;
        f.fileSize     = fileSize;
        f.extension    = extension;
        return f;
    }
}
