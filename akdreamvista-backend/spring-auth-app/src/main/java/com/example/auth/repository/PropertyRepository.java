package com.example.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.auth.entity.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

	List<Property> findAllByOrderByIdDesc();

	@Query("SELECT COUNT(p.id) FROM Property p")
	long totalCount();

	@Query("SELECT COUNT(p.id) FROM Property p WHERE p.isFeePaid = true")
	long paidCount();

	@Query("SELECT COUNT(p.id) FROM Property p WHERE p.isFeePaid = false OR p.isFeePaid IS NULL")
	long unpaidCount();

	List<Property> findByIsFeePaid(boolean isFeePaid);
}
