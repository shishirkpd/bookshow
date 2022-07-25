package com.skp.bookshow.controller;

import com.skp.bookshow.exception.*;
import com.skp.bookshow.model.BookedSeat;
import com.skp.bookshow.model.Show;
import com.skp.bookshow.model.enums.SeatStatus;
import com.skp.bookshow.model.request.BookShowRequest;
import com.skp.bookshow.model.request.ShowRequest;
import com.skp.bookshow.model.response.BookingResponse;
import com.skp.bookshow.model.response.ShowDetailsForGuestResponse;
import com.skp.bookshow.service.AccountService;
import com.skp.bookshow.service.SeatBookingService;
import com.skp.bookshow.service.ShowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookShowControllerTest {

    @Mock
    ShowService showService;

    @Mock
    SeatBookingService seatBookingService;

    @Mock
    AccountService accountService;

    @InjectMocks
    BookShowController bookShowController = new BookShowController();

    @Test
    void bookShow_should_be_successful() throws BookingAlreadyExist, BookingUnSuccessful {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.BLOCKED)
                .bookedSeat(List.of("A1"))
                .build();

        when(seatBookingService.bookSeat(Mockito.any())).thenReturn(bookedSeat);

        ResponseEntity<BookingResponse> responseEntity =  bookShowController.bookShow(bookShowRequest);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.BLOCKED, responseEntity.getBody().getBookedSeat().getSeatStatus());
        assertEquals("Please confirm the booking", responseEntity.getBody().getMessage());
    }

    @Test
    void bookShow_should_throw_exception_if_booking_exist() throws BookingAlreadyExist, BookingUnSuccessful {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();


        when(seatBookingService.bookSeat(Mockito.any())).thenThrow(new BookingAlreadyExist("Booking already exist"));

        ResponseEntity<BookingResponse> responseEntity =  bookShowController.bookShow(bookShowRequest);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("Booking already exist", responseEntity.getBody());
    }

    @Test
    void bookShow_should_be_unsuccessful() throws BookingAlreadyExist, BookingUnSuccessful {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();

        when(seatBookingService.bookSeat(Mockito.any())).thenThrow(new BookingUnSuccessful("Booking failed"));
        ResponseEntity<BookingResponse> responseEntity =  bookShowController.bookShow(bookShowRequest);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Booking failed", responseEntity.getBody());
    }

    @Test
    void confirmBooking_should_be_successful() throws BookingUnSuccessful, BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.BOOKED)
                .bookedSeat(List.of("A1"))
                .build();
        when(seatBookingService.updateBooking(Mockito.any(), Mockito.any())).thenReturn(bookedSeat);
        ResponseEntity<BookingResponse> responseEntity = bookShowController.confirmBooking(bookShowRequest, true);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.BOOKED, responseEntity.getBody().getBookedSeat().getSeatStatus());

    }

    @Test
    void confirmBooking_should_be_successful_for_not_confirming() throws BookingUnSuccessful, BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();

        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.CANCELED)
                .bookedSeat(List.of("A1"))
                .build();
        when(seatBookingService.updateBooking(Mockito.any(), Mockito.any())).thenReturn(bookedSeat);
        ResponseEntity<BookingResponse> responseEntity = bookShowController.confirmBooking(bookShowRequest, false);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.CANCELED, responseEntity.getBody().getBookedSeat().getSeatStatus());

    }

    @Test
    void confirmBooking_should_throw_exception() throws BookingUnSuccessful, BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();

        when(seatBookingService.updateBooking(Mockito.any(), Mockito.any())).thenThrow(new BookingNotFound("Booking not found"));
        ResponseEntity<BookingResponse> responseEntity = bookShowController.confirmBooking(bookShowRequest, true);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Booking not found", responseEntity.getBody());

    }

    @Test
    void confirmBooking_should_throw_runtime_exception() throws BookingUnSuccessful, BookingNotFound {
        BookShowRequest bookShowRequest = BookShowRequest.builder()
                .phoneNumber(1L)
                .showNumber(2L)
                .blockedSeat(List.of("A1"))
                .build();

        when(seatBookingService.updateBooking(Mockito.any(), Mockito.any())).thenThrow(new RuntimeException("Something went wrong"));
        ResponseEntity<BookingResponse> responseEntity = bookShowController.confirmBooking(bookShowRequest, true);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Something went wrong", responseEntity.getBody());

    }

    @Test
    void cancelBookingById_should_be_successful() throws BookingNotFound, BookingUnSuccessful {
        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.BLOCKED)
                .bookedSeat(List.of("A1"))
                .build();

        BookedSeat bookedSeat2 = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.CANCELED)
                .bookedSeat(List.of("A1"))
                .build();

        when(seatBookingService.findBookingByTicketId(Mockito.any())).thenReturn(bookedSeat);
        when(seatBookingService.updateBooking(Mockito.any(), Mockito.any())).thenReturn(bookedSeat2);
        ResponseEntity<BookingResponse> responseEntity = bookShowController.cancelBooking(123L);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.CANCELED, responseEntity.getBody().getBookedSeat().getSeatStatus());
    }

    @Test
    void cancelBookingById_should_throw_booking_not_found() throws ShowNotFound, BookingNotFound {
        when(seatBookingService.findBookingByTicketId(Mockito.any())).thenThrow(new BookingNotFound("Booking not found"));
        ResponseEntity<BookingResponse> responseEntity = bookShowController.cancelBooking(123L);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Booking not found", responseEntity.getBody());
    }


    @Test
    void findBookingByPhoneNumber_should_be_successful_for_blocked() throws BookingNotFound {
        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.BLOCKED)
                .bookedSeat(List.of("A1"))
                .build();
        when(seatBookingService.findBooking(Mockito.any())).thenReturn(bookedSeat);
        ResponseEntity<BookingResponse> responseEntity = bookShowController.findBookingByPhoneNumber(123L);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.BLOCKED, responseEntity.getBody().getBookedSeat().getSeatStatus());
        assertEquals("Please confirm the booking", responseEntity.getBody().getMessage());
    }

    @Test
    void findBookingByPhoneNumber_should_be_successful_for_booked() throws BookingNotFound {
        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.BOOKED)
                .bookedSeat(List.of("A1"))
                .build();
        when(seatBookingService.findBooking(Mockito.any())).thenReturn(bookedSeat);
        ResponseEntity<BookingResponse> responseEntity = bookShowController.findBookingByPhoneNumber(123L);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.BOOKED, responseEntity.getBody().getBookedSeat().getSeatStatus());
        assertEquals("Booking confirmed", responseEntity.getBody().getMessage());
    }

    @Test
    void findBookingByPhoneNumber_should_be_successful_for_canceled() throws BookingNotFound {
        BookedSeat bookedSeat = BookedSeat.builder()
                .showNumber(2L)
                .ticketId(123L)
                .seatStatus(SeatStatus.CANCELED)
                .bookedSeat(List.of("A1"))
                .build();
        when(seatBookingService.findBooking(Mockito.any())).thenReturn(bookedSeat);
        ResponseEntity<BookingResponse> responseEntity = bookShowController.findBookingByPhoneNumber(123L);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(SeatStatus.CANCELED, responseEntity.getBody().getBookedSeat().getSeatStatus());
        assertEquals("Booking canceled", responseEntity.getBody().getMessage());
    }

    @Test
    void findBookingByPhoneNumber_should_be_throw_exception() throws BookingNotFound {
        when(seatBookingService.findBooking(Mockito.any())).thenThrow(new BookingNotFound("No Booking found"));
        ResponseEntity<BookingResponse> responseEntity = bookShowController.findBookingByPhoneNumber(123L);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }


    @Test
    void findShowStatus_should_be_successful() {
        BookedSeat bookedSeat = BookedSeat.builder()
                .phoneNumber(1L)
                .bookedSeat(List.of("A1"))
                .seatStatus(SeatStatus.BLOCKED)
                .ticketId(12L)
                .showNumber(1L)
                .build();
        when(seatBookingService.findAllBookingForShow(Mockito.any())).thenReturn(List.of(bookedSeat));
        ResponseEntity<List<BookedSeat>> responseEntity = bookShowController.findShowStatus(1L);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, responseEntity.getBody().size());
    }

    @Test
    void createShow_should_be_successful() {
        ShowRequest showRequest = ShowRequest.builder()
                .showTime(Date.from(Instant.now()))
                .movieId(1L)
                .numberOfRows(2)
                .numberOfSeatsPerRows(4)
                .cancellationWindow(2)
                .build();
        Show show = Show.builder()
                .id(1L)
                .build();
        when(showService.createShow(Mockito.any())).thenReturn(show);
        ResponseEntity<Show> responseEntity = bookShowController.createShow(showRequest);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, responseEntity.getBody().getId());
    }

    @Test
    void findShowBy_should_be_successful() throws ShowNotFound {
        ShowDetailsForGuestResponse showDetailsForGuestResponse = ShowDetailsForGuestResponse.builder()
                .bookedSeat(List.of())
                .showNumber(1L)
                .availableSeat(List.of("A1"))
                .blockedSeat(List.of())
                .build();
        when(showService.findById(Mockito.anyLong())).thenReturn(showDetailsForGuestResponse);
        ResponseEntity<ShowDetailsForGuestResponse> responseEntity = bookShowController.findShowBy(1L);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, responseEntity.getBody().getShowNumber());
    }

    @Test
    void findShowBy_should_throw_exception() throws ShowNotFound {
        when(showService.findById(Mockito.anyLong())).thenThrow(new ShowNotFound("Show not found"));
        ResponseEntity<ShowDetailsForGuestResponse> responseEntity = bookShowController.findShowBy(1L);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Show not found", responseEntity.getBody());
    }

    @Test
    void findShowBy_should_throw_runtime_exception() throws ShowNotFound {
        when(showService.findById(Mockito.anyLong())).thenThrow(new RuntimeException("Something went wrong"));
        ResponseEntity<ShowDetailsForGuestResponse> responseEntity = bookShowController.findShowBy(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Something went wrong", responseEntity.getBody());
    }

    @Test
    void findAllShows_for_empty_response() {
        when(showService.findAll()).thenReturn(List.of());
        ResponseEntity<List<Show>> responseEntity = bookShowController.findAllShows();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().isEmpty());
    }

    @Test
    void findAllShows_for_list_of_show_response() {
        Show show = Show.builder()
                .id(1L)
                .availableSeats(List.of("A1"))
                .blockedSeats(List.of())
                .bookedSeats(List.of())
                .build();
        when(showService.findAll()).thenReturn(List.of(show));
        ResponseEntity<List<Show>> responseEntity = bookShowController.findAllShows();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, responseEntity.getBody().size());
        assertEquals(1, responseEntity.getBody().get(0).getId());
    }

    @Test
    void login_should_be_successful() throws AccountNotFound {
        when(accountService.login(Mockito.any(), Mockito.any())).thenReturn(true);
        var res = bookShowController.login(1L, "1234");
        assertTrue(res);
    }

    @Test
    void login_should_be_unsuccessful() throws AccountNotFound {
        when(accountService.login(Mockito.any(), Mockito.any())).thenReturn(false);
        var res = bookShowController.login(1L, "1234");
        assertFalse(res);
    }

    @Test
    void login_should_throw_exception() throws AccountNotFound {
        when(accountService.login(Mockito.any(), Mockito.any())).thenThrow(new AccountNotFound("Account not found"));
        var res = assertThrows(AccountNotFound.class, () -> bookShowController.login(1L, "1234"));
        assertEquals("Account not found", res.getMessage());

    }
}