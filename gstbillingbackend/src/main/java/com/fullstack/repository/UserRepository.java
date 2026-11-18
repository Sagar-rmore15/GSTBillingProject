package com.fullstack.repository;

import com.fullstack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository //marks the interface with DB access component //spring data JPA
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserEmailId(String userEmailId);
}
