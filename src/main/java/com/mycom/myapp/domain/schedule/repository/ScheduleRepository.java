package com.mycom.myapp.domain.schedule.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycom.myapp.domain.group.entity.Group;
import com.mycom.myapp.domain.schedule.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>{
	List<Schedule> findByGroup_Id(Long groupId);
	
	// owner = 나, 그리고 group 이 없는(개인) 일정만
	List<Schedule> findByOwner_IdAndGroupIsNull(Long ownerId);

	// 그 유저가 만든 모든 일정(그룹 + 개인)
	List<Schedule> findByOwner_Id(Long ownerId);
}
