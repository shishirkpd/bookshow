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
import com.skp.bookshow.service.MovieService;
import com.skp.bookshow.service.SeatBookingService;
import com.skp.bookshow.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ComponentScan("com.skp.bookshow")
@RequestMapping("api/v1/")
public class BookShowController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowService showService;

    @Autowired
    private SeatBookingService seatBookingService;

    @PostMapping("/show/booking/confirm/{status}")
    public ResponseEntity<BookingResponse> confirmBooking(@RequestBody BookShowRequest bookShowRequest, @PathVariable Boolean status) {
        try {
            BookedSeat bookedSeat;
            if(status) {
                bookedSeat = seatBookingService.updateBooking(bookShowRequest, SeatStatus.BOOKED);
            } else {
                bookedSeat = seatBookingService.updateBooking(bookShowRequest, SeatStatus.CANCELED);
            }
            BookingResponse bookingResponse = getBookingResponse(bookedSeat);

            return new ResponseEntity(bookingResponse, HttpStatus.OK);
        } catch (BookingNotFound e) {
            return new ResponseEntity( e.getMessage(),HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity( e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/show/booking/cancel/{tickerId}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long ticketId) {
        try {
            var bookingByTicketId = seatBookingService.findBookingByTicketId(ticketId);
            BookShowRequest bookShowRequest = BookShowRequest.builder()
                    .phoneNumber(bookingByTicketId.getPhoneNumber())
                    .showNumber(bookingByTicketId.getShowNumber())
                    .blockedSeat(bookingByTicketId.getBookedSeat())
                    .build();
            BookedSeat bookedSeat = seatBookingService.updateBooking(bookShowRequest, SeatStatus.CANCELED);
            BookingResponse bookingResponse = getBookingResponse(bookedSeat);
            return new ResponseEntity(bookingResponse, HttpStatus.OK);
        } catch (BookingNotFound e) {
            return new ResponseEntity( e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/show/booking")
    public ResponseEntity<BookingResponse> bookShow(@RequestBody BookShowRequest bookShowRequest) {
        ResponseEntity responseEntity;
        try {
            BookedSeat bookedSeat = seatBookingService.bookSeat(bookShowRequest);

            BookingResponse bookingResponse = getBookingResponse(bookedSeat);

            responseEntity = new ResponseEntity(bookingResponse, HttpStatus.OK);

        } catch (BookingAlreadyExist e) {
            responseEntity =  new ResponseEntity( e.getMessage(),HttpStatus.CONFLICT);
        } catch (BookingUnSuccessful e) {
            responseEntity = new ResponseEntity( e.getMessage(), HttpStatus.OK);
        }
        return responseEntity;
    }

    @GetMapping("/show/booking/{phoneNumber}")
    public ResponseEntity<BookingResponse> findBookingByPhoneNumber(@PathVariable Long phoneNumber) {
        try {
            BookedSeat bookedSeat = seatBookingService.findBooking(phoneNumber);

            BookingResponse bookingResponse = getBookingResponse(bookedSeat);

            return new ResponseEntity(bookingResponse, HttpStatus.OK);

        } catch (BookingNotFound e) {
            return new ResponseEntity( e.getMessage(),HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/show/status/{id}")
    public ResponseEntity<List<BookedSeat>> findShowStatus(@PathVariable Long id){
        return new ResponseEntity(seatBookingService.findAllBookingForShow(id), HttpStatus.OK);
    }

    @PostMapping("/show")
    public ResponseEntity<Show> createShow(@RequestBody ShowRequest showRequest){
        return new ResponseEntity(showService.createShow(showRequest), HttpStatus.OK);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<ShowDetailsForGuestResponse> findShowBy(@PathVariable Long id){
        try {
            return new ResponseEntity(showService.findById(id), HttpStatus.OK);
        } catch (ShowNotFound e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/show")
    public ResponseEntity<List<Show>> findAllShows(){
        return new ResponseEntity(showService.findAll(), HttpStatus.OK);
    }


    private BookingResponse getBookingResponse(BookedSeat bookedSeat) {
        String message = "";
        switch(bookedSeat.getSeatStatus().name()) {
            case "BLOCKED": message = "Please confirm the booking";
                break;
            case "BOOKED": message = "Booking confirmed";
                break;
            case "CANCELED": message = "Booking canceled";
                break;
        }
        var bookingResponse = BookingResponse.builder()
                .message(message)
                .bookedSeat(bookedSeat)
                .build();
        return bookingResponse;
    }

    public Boolean login(Long id, String password) throws AccountNotFound {
        return accountService.login(id, password);
    }
}

