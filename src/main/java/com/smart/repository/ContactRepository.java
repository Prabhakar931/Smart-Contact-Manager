package com.smart.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	@Query("from Contact as d where d.user.id=:userId")
	public Page<Contact> findContactsByUserName(@Param("userId") int userId, Pageable pageable);
}
