package com.flowtask.domain.repository;

import com.flowtask.domain.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findBySystemIdOrderByInsertDateDesc(Long systemId);

    @Modifying
    @Query("DELETE FROM UploadedFile f WHERE f.system.id = :systemId")
    void deleteBySystemId(@Param("systemId") Long systemId);
}
