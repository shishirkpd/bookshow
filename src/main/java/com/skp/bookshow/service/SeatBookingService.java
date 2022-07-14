package com.skp.bookshow.service;

import com.skp.bookshow.exception.BookingAlreadyExist;
import com.skp.bookshow.exception.BookingNotFound;
import com.skp.bookshow.exception.BookingUnSuccessful;
import com.skp.bookshow.exception.ShowNotFound;
import com.skp.bookshow.model.BookedSeat;
import com.skp.bookshow.model.enums.SeatStatus;
import com.skp.bookshow.model.request.BookShowRequest;

import java.util.List;

public interface SeatBookingService {
    BookedSeat bookSeat(BookShowRequest bookShowRequest) throws BookingAlreadyExist, BookingUnSuccessful;

    BookedSeat updateBooking(BookShowRequest bookedSeat, SeatStatus seatStatus) throws BookingNotFound;

    BookedSeat findBooking(Long phoneNumber) throws BookingNotFound;

    BookedSeat findBookingByTicketId(Long phoneNumber) throws BookingNotFound;

    List<BookedSeat> findAllBookingForShow(Long showId);

    BookedSeat cancelBookingTicketById(Long bookingId) throws BookingNotFound, ShowNotFound;
}
