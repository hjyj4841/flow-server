package com.master.flow.model.dao;

import com.master.flow.model.vo.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, Integer> {
}
