package com.skp.bookshow.service.impl;

import com.skp.bookshow.exception.BookingAlreadyExist;
import com.skp.bookshow.exception.BookingNotFound;
import com.skp.bookshow.exception.BookingUnSuccessful;
import com.skp.bookshow.exception.ShowNotFound;
import com.skp.bookshow.model.BookedSeat;
import com.skp.bookshow.model.Show;
import com.skp.bookshow.model.enums.SeatStatus;
import com.skp.bookshow.model.request.BookShowRequest;
import com.skp.bookshow.repository.BookingRepo;
import com.skp.bookshow.repository.ShowRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatBookingImplTest {

    @Mock
    ShowRepo showRepo;

    @Mock
    BookingRepo bookingRepo;

    @InjectMocks
    SeatBookingImpl seatBooking = new SeatBookingImpl();

    @Test
    void bookSeat_should_be_successful() throws BookingAlreadyExist, BookingUnSuccessful {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();
        Show show = Show.builder()
                .id(1L)
                .availableSeats(bookShowRequest.getBlockedSeat())
                .blockedSeats(List.of())
                .bookedSeats(List.of())
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(1L)
                .seatStatus(SeatStatus.BLOCKED)
                .phoneNumber(bookShowRequest.getPhoneNumber())
                .bookedSeat(bookShowRequest.getBlockedSeat())
                .build();

        when(bookingRepo.findById(Mockito.any())).thenReturn(Optional.empty());
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.of(show));
        when(bookingRepo.save(Mockito.any())).thenReturn(bookedSeat);

        var result = seatBooking.bookSeat(bookShowRequest);

        Assertions.assertEquals(1L, result.getTicketId());
    }

    @Test
    void bookSeat_should_throw_exception_for_existing_booking() {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();
        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(1L)
                .seatStatus(SeatStatus.BLOCKED)
                .phoneNumber(bookShowRequest.getPhoneNumber())
                .bookedSeat(bookShowRequest.getBlockedSeat())
                .build();

        when(bookingRepo.findById(Mockito.any())).thenReturn(Optional.of(bookedSeat));

        var result = assertThrows(BookingAlreadyExist.class, () ->seatBooking.bookSeat(bookShowRequest));

        Assertions.assertEquals("Booking with phone number already exist: 12345", result.getMessage());
    }

    @Test
    void bookSeat_should_throw_exception_for_unsuccessful_booking_for_not_available_seats() {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();

        Show show = Show.builder()
                .id(1L)
                .availableSeats(List.of("C1", "A2"))
                .blockedSeats(List.of())
                .bookedSeats(List.of())
                .build();
        when(bookingRepo.findById(Mockito.any())).thenReturn(Optional.empty());
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.of(show));
        var result = assertThrows(BookingUnSuccessful.class, () -> seatBooking.bookSeat(bookShowRequest));

        Assertions.assertEquals("There was error while booking try again", result.getMessage());
    }

    @Test
    void bookSeat_should_throw_exception_for_unsuccessful_booking() {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();

        when(bookingRepo.findById(Mockito.any())).thenReturn(Optional.empty());
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.empty());
        var result = assertThrows(BookingUnSuccessful.class, () -> seatBooking.bookSeat(bookShowRequest));

        Assertions.assertEquals("There was error while booking try again", result.getMessage());
    }

    @Test
    void updateBooking_should_update_successfully_for_booked() throws BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(122L)
                .seatStatus(SeatStatus.BLOCKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .bookingTime(Date.from(Instant.now()))
                .build();

        Show show = Show.builder()
                .id(1L)
                .availableSeats(bookShowRequest.getBlockedSeat())
                .blockedSeats(bookedSeat.getBookedSeat())
                .bookedSeats(List.of())
                .build();
        when(bookingRepo.findByPhoneNumber(Mockito.any())).thenReturn(Optional.ofNullable(bookedSeat));
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.ofNullable(show));

        var res = seatBooking.updateBooking(bookShowRequest, SeatStatus.BOOKED);

        assertEquals(SeatStatus.BOOKED, res.getSeatStatus());

        Mockito.verify(bookingRepo, times(1)).save(Mockito.any());
        Mockito.verify(showRepo, times(1)).save(Mockito.any());

    }

    @Test
    void updateBooking_should_update_successfully_for_cancel() throws BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(122L)
                .seatStatus(SeatStatus.BLOCKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();

        Show show = Show.builder()
                .id(1L)
                .availableSeats(bookShowRequest.getBlockedSeat())
                .blockedSeats(bookedSeat.getBookedSeat())
                .bookedSeats(List.of())
                .build();
        when(bookingRepo.findByPhoneNumber(Mockito.any())).thenReturn(Optional.ofNullable(bookedSeat));
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.ofNullable(show));

        var res = seatBooking.updateBooking(bookShowRequest, SeatStatus.CANCELED);

        assertEquals(SeatStatus.CANCELED, res.getSeatStatus());

        Mockito.verify(bookingRepo, times(1)).save(Mockito.any());
        Mockito.verify(showRepo, times(1)).save(Mockito.any());

    }

    @Test
    void updateBooking_should_throw_exception_when_booking_not_found() throws BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(122L)
                .seatStatus(SeatStatus.BLOCKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();

        when(bookingRepo.findByPhoneNumber(Mockito.any())).thenReturn(Optional.ofNullable(bookedSeat));
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.empty());


        var res = assertThrows(RuntimeException.class, () -> seatBooking.updateBooking(bookShowRequest, SeatStatus.BOOKED));
        assertEquals("Error while processing",  res.getMessage());

        Mockito.verify(bookingRepo, times(0)).save(Mockito.any());
        Mockito.verify(showRepo, times(0)).save(Mockito.any());

    }

    @Test
    void updateBooking_should_throw_exception_when_show_not_found() {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .blockedSeat(List.of("A1", "A2"))
                .showNumber(1L)
                .phoneNumber(12345L)
                .build();

        when(bookingRepo.findByPhoneNumber(Mockito.any())).thenReturn(Optional.empty());

        var res = assertThrows(BookingNotFound.class, () -> seatBooking.updateBooking(bookShowRequest, SeatStatus.BOOKED));

        assertEquals("Booking not found",  res.getMessage());

        Mockito.verify(bookingRepo, times(0)).save(Mockito.any());
        Mockito.verify(showRepo, times(0)).save(Mockito.any());

    }

    @Test
    void findBooking_should_return_the_booking() throws BookingNotFound {
        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(122L)
                .seatStatus(SeatStatus.BOOKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();
        when(bookingRepo.findByPhoneNumber(Mockito.any())).thenReturn(Optional.of(bookedSeat));
        var result = seatBooking.findBooking(1L);

        assertEquals(1L, result.getPhoneNumber());
        assertEquals(List.of("A1"), result.getBookedSeat());
        assertEquals(SeatStatus.BOOKED, result.getSeatStatus());
    }

    @Test
    void findBooking_should_throw_exception_for_booking_not_exist() throws BookingNotFound {
        when(bookingRepo.findByPhoneNumber(Mockito.any())).thenReturn(Optional.empty());
        var result = assertThrows(BookingNotFound.class, () -> seatBooking.findBooking(1L));

        assertEquals("No booking found for the given phone number: 1", result.getMessage());
    }

    @Test
    void findBookingByTickerId_should_return_booking() throws BookingNotFound {
        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(121L)
                .seatStatus(SeatStatus.BOOKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();
        when(bookingRepo.findByTicketId(Mockito.any())).thenReturn(Optional.of(bookedSeat));
        var result = seatBooking.findBookingByTicketId(1L);

        assertEquals(1L, result.getPhoneNumber());
        assertEquals(List.of("A1"), result.getBookedSeat());
        assertEquals(SeatStatus.BOOKED, result.getSeatStatus());
    }

    @Test
    void findBookingByTickerId_should_throw_exception_for_ticket_id_not_found() throws BookingNotFound {
        when(bookingRepo.findByTicketId(Mockito.any())).thenReturn(Optional.empty());
        var result = assertThrows(BookingNotFound.class, () -> seatBooking.findBookingByTicketId(1L));

        assertEquals("No booking found for the given ticket id: 1", result.getMessage());
    }

    @Test
    void findAllBookingForShow_should_return_list_of_bookings() {
        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(121L)
                .seatStatus(SeatStatus.BOOKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();
        when(bookingRepo.findByShowNumber(Mockito.any())).thenReturn(List.of(bookedSeat));
        var result = seatBooking.findAllBookingForShow(1L);

        assertEquals(1L, result.size());
        assertEquals(1L, result.get(0).getPhoneNumber());
        assertEquals(List.of("A1"), result.get(0).getBookedSeat());
        assertEquals(SeatStatus.BOOKED, result.get(0).getSeatStatus());
    }

    @Test
    void cancelBookingById_should_cancel_the_booking_successfully() throws ShowNotFound, BookingNotFound {

        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(121L)
                .seatStatus(SeatStatus.BOOKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();
        Show show = Show.builder()
                .id(1L)
                .availableSeats(List.of())
                .blockedSeats(List.of())
                .bookedSeats(List.of("A1"))
                .build();

        when(bookingRepo.findByTicketId(Mockito.any())).thenReturn(Optional.ofNullable(bookedSeat));
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.ofNullable(show));

        var result = seatBooking.cancelBookingTicketById(1L);

        assertEquals(121L, result.getTicketId());
        assertEquals(1L, result.getPhoneNumber());
        assertEquals(List.of("A1"), result.getBookedSeat());
        assertEquals(SeatStatus.CANCELED, result.getSeatStatus());

        Mockito.verify(showRepo, times(1)).save(Mockito.any());
        Mockito.verify(bookingRepo, times(1)).save(Mockito.any());

    }

    @Test
    void cancelBookingById_should_throw_the_exception_when_booking_not_exist() throws ShowNotFound, BookingNotFound {

        when(bookingRepo.findByTicketId(Mockito.any())).thenReturn(Optional.empty());

        var result = assertThrows(BookingNotFound.class, () ->seatBooking.cancelBookingTicketById(1L));

        assertEquals("No booking found for the given ticket id: 1", result.getMessage());

    }

    @Test
    void cancelBookingById_should_throw_the_exception_when_booking_exist_but_show_not_found() throws ShowNotFound, BookingNotFound {

        BookedSeat bookedSeat = BookedSeat.builder()
                .ticketId(121L)
                .seatStatus(SeatStatus.BOOKED)
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .build();
        when(bookingRepo.findByTicketId(Mockito.any())).thenReturn(Optional.of(bookedSeat));
        when(showRepo.findById(Mockito.any())).thenReturn(Optional.empty());
        var result = assertThrows(ShowNotFound.class, () -> seatBooking.cancelBookingTicketById(1L));

        assertEquals("something went wrong show could not be found", result.getMessage());

    }
}