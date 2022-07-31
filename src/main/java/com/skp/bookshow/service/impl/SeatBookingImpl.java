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
import com.skp.bookshow.service.SeatBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SeatBookingImpl implements SeatBookingService {
    @Autowired
    ShowRepo showRepo;

    @Autowired
    BookingRepo bookingRepo;

    @Override
    public BookedSeat bookSeat(BookShowRequest bookShowRequest) throws BookingAlreadyExist, BookingUnSuccessful {
        Optional<BookedSeat> booking = bookingRepo.findByPhoneNumber(bookShowRequest.getPhoneNumber());

        if(booking.isPresent()) {
            throw new BookingAlreadyExist("Booking with phone number already exist: " + bookShowRequest.getPhoneNumber());
        } else {
            var seat = BookedSeat.builder()
                    .phoneNumber(bookShowRequest.getPhoneNumber())
                    .showNumber(bookShowRequest.getShowNumber())
                    .bookedSeat(bookShowRequest.getBlockedSeat())
                    .seatStatus(SeatStatus.BLOCKED)
                    .build();
            var show =  showRepo.findById(bookShowRequest.getShowNumber()).map(s -> {
                 List<String> blockedSeat = bookShowRequest.getBlockedSeat();
                 if(s.getAvailableSeats().containsAll(blockedSeat)) {
                     ArrayList<String> availableSeats = new ArrayList<>(s.getAvailableSeats());
                     availableSeats.removeAll(blockedSeat);
                     s.setAvailableSeats(availableSeats);
                     ArrayList<String> blockedSeats = new ArrayList<>(s.getBlockedSeats());
                     blockedSeats.addAll(blockedSeat);
                     s.setBlockedSeats(blockedSeats);
                     showRepo.save(s);
                     return s;
                 } else return null;
             });
            if(show.isPresent()) {
                return bookingRepo.save(seat);
            } else {
                throw new BookingUnSuccessful("There was error while booking try again");
            }
        }

    }

    @Override
    public BookedSeat updateBooking(BookShowRequest bookShowRequest, SeatStatus seatStatus) throws BookingNotFound {
       return  bookingRepo.findByPhoneNumber(bookShowRequest.getPhoneNumber())
               .map(booking -> {
                   booking.setSeatStatus(seatStatus);
                   return showRepo.findById(bookShowRequest.getShowNumber()).map(s -> {
                       var blockedSeat = bookShowRequest.getBlockedSeat();
                       if (seatStatus.equals(SeatStatus.BOOKED)) {
                           var cur = Date.from(Instant.now());
                           if(cur.getTime() - booking.getBookingTime().getTime() < s.getCancellationWindow()) {
                               updateShowForBooked(s, blockedSeat);
                           } else {
                               updateShowForCancel(s, blockedSeat);
                           }
                       }
                       if (seatStatus.equals(SeatStatus.CANCELED)) {
                           updateShowForCancel(s, blockedSeat);
                       }
                       bookingRepo.save(booking);
                       showRepo.save(s);
                       return booking;
                   }).orElseThrow(() ->new RuntimeException("Error while processing"));
               }).orElseThrow(() -> new BookingNotFound("Booking not found"));
    }

    @Override
    public BookedSeat findBooking(Long phoneNumber) throws BookingNotFound {
        return bookingRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                new BookingNotFound("No booking found for the given phone number: "+ phoneNumber.toString()));
    }

    @Override
    public BookedSeat findBookingByTicketId(Long tickerId) throws BookingNotFound {
       return bookingRepo.findByTicketId(tickerId)
        .orElseThrow(() ->
                new BookingNotFound("No booking found for the given ticket id: "+ tickerId.toString()));
    }

    @Override
    public List<BookedSeat> findAllBookingForShow(Long showId) {
        return bookingRepo.findByShowNumber(showId);
    }

    @Override
    public BookedSeat cancelBookingTicketById(Long ticketId) throws BookingNotFound, ShowNotFound {
        var booking = findBookingByTicketId(ticketId);
        booking.setSeatStatus(SeatStatus.CANCELED);
        bookingRepo.save(booking);
        var show =  showRepo.findById(booking.getShowNumber()).map(s -> {
            var blockedSeat = booking.getBookedSeat();
            updateShowForCancel(s, blockedSeat);
            showRepo.save(s);
            return s;
        });
        if(!show.isPresent()) throw new ShowNotFound("something went wrong show could not be found");
        return booking;
    }

    private void updateShowForCancel(Show s, List<String> blockedSeat) {
        ArrayList<String> availableSeats = new ArrayList<>(s.getAvailableSeats());
        availableSeats.addAll(blockedSeat);
        s.setAvailableSeats(availableSeats);
        ArrayList<String> blockedSeats = new ArrayList<>(s.getBlockedSeats());
        blockedSeats.removeAll(blockedSeat);
        s.setBlockedSeats(blockedSeats);
        ArrayList<String> bookedSeats = new ArrayList<>(s.getBlockedSeats());
        bookedSeats.removeAll(blockedSeat);
        s.setBookedSeats(blockedSeats);
    }

    private void updateShowForBooked(Show s, List<String> blockedSeat) {
        ArrayList<String> availableSeats = new ArrayList<>(s.getAvailableSeats());
        availableSeats.removeAll(blockedSeat);
        s.setAvailableSeats(availableSeats);
        ArrayList<String> blockedSeats = new ArrayList<>(s.getBlockedSeats());
        blockedSeats.removeAll(blockedSeat);
        s.setBlockedSeats(blockedSeats);
        ArrayList<String> bookedSeats = new ArrayList<>(s.getBlockedSeats());
        bookedSeats.addAll(blockedSeat);
        s.setBookedSeats(blockedSeats);
    }
}
