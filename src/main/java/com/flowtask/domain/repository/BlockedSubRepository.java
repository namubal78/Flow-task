package com.flowtask.domain.repository;

import com.flowtask.domain.entity.BlockedSub;
import com.flowtask.domain.enums.BlockedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockedSubRepository extends JpaRepository<BlockedSub, Long> {

    List<BlockedSub> findBySystemIdAndBlockedType(Long systemId, BlockedType type);

    List<BlockedSub> findBySystemIdAndBlockedTypeOrderByBlockedExtensionAsc(Long systemId, BlockedType type);

    List<BlockedSub> findBySystemIdAndBlockedTypeOrderByBlockedExtensionDesc(Long systemId, BlockedType type);

    List<BlockedSub> findBySystemIdAndBlockedTypeOrderByInsertDateDesc(Long systemId, BlockedType type);

    Optional<BlockedSub> findBySystemIdAndBlockedExtensionIgnoreCaseAndBlockedType(
            Long systemId, String blockedExtension, BlockedType type);

    boolean existsBySystemIdAndBlockedExtensionIgnoreCase(Long systemId, String blockedExtension);

    long countBySystemIdAndBlockedType(Long systemId, BlockedType type);

    // 전체 삭제 - bulk delete (SELECT 후 개별 delete 방지)
    @Modifying
    @Query("DELETE FROM BlockedSub b WHERE b.system.id = :systemId AND b.blockedType = :type")
    void deleteBySystemIdAndBlockedType(@Param("systemId") Long systemId, @Param("type") BlockedType type);
}
