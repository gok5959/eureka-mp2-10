package com.mycom.myapp.domain.group.repository;

import com.mycom.myapp.domain.group.entity.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    long countByGroupId(Long groupId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
    // 유저 삭제 구현시 반영
    void deleteByUserId(Long userId);

    @Query("""
            select gm
            from GroupMember gm
            join fetch gm.group g
            join fetch g.owner
            where gm.user.id = :userId
              and (
                    :keyword is null
                    or :keyword = ''
                    or g.name like concat('%', :keyword, '%')
                    or g.description like concat('%', :keyword, '%')
              )
            """)
    Page<GroupMember> searchMembershipsByUserId(@Param("userId") Long userId,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}
