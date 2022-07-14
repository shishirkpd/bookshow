package com.skp.bookshow.repository;

import com.skp.bookshow.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepo extends JpaRepository<Show, Long> {
}
