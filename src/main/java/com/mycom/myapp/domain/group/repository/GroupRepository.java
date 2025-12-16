package com.mycom.myapp.domain.group.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mycom.myapp.domain.group.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @EntityGraph(attributePaths = "owner")
    @Query("""
                select g
                from Group g
                where (:keyword is null or :keyword = ''
                       or g.name like concat('%', :keyword, '%')
                       or g.description like concat('%', :keyword, '%')
                )
            """)
    Page<Group> searchGroupsByCondition(@Param("keyword") String keyword, Pageable pageable);
}
