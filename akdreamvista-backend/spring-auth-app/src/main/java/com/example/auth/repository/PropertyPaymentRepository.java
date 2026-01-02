package com.example.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth.model.PropertyPayment;

public interface PropertyPaymentRepository extends JpaRepository<PropertyPayment, Long> {
	PropertyPayment findTopByPropertyIdOrderByIdDesc(Long propertyId);

}

