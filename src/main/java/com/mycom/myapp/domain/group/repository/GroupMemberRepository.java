package com.mycom.myapp.domain.group.repository;

import com.mycom.myapp.domain.group.GroupMemberRole;
import com.mycom.myapp.domain.group.entity.GroupMember;
import com.mycom.myapp.domain.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
    // 유저 삭제 구현시 반영
    void deleteByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    @Query("""
        select gm
        from GroupMember gm
        join gm.user u
        where gm.group.id = :groupId
          and (:keyword is null
               or u.name like concat('%', :keyword, '%')
               or u.email like concat('%', :keyword, '%'))
          and (:role is null or gm.role = :role)
    """)
    Page<GroupMember> searchMembers(@Param("groupId") Long groupId,
                                    @Param("keyword") String keyword,
                                    @Param("role") GroupMemberRole role,
                                    Pageable pageable);

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
