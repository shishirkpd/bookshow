package com.skp.bookshow.repository;

import com.skp.bookshow.model.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<BookedSeat, Long> {
    Optional<BookedSeat> findByPhoneNumber(Long aLong);

    List<BookedSeat> findByShowNumber(Long showNumber);

    Optional<BookedSeat> findByTicketId(Long ticketId);
}
