package com.mycom.myapp.domain.group.repository;

import com.mycom.myapp.domain.group.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
    Page<Group> searchGroupsByCondition(String keyword, Pageable pageable);
}
