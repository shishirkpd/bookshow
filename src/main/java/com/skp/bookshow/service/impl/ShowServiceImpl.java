package com.skp.bookshow.service.impl;

import com.skp.bookshow.exception.ShowNotFound;
import com.skp.bookshow.model.Show;
import com.skp.bookshow.model.request.ShowRequest;
import com.skp.bookshow.model.response.ShowDetailsForGuestResponse;
import com.skp.bookshow.repository.MovieRepo;
import com.skp.bookshow.repository.ShowRepo;
import com.skp.bookshow.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShowServiceImpl implements ShowService {
    @Autowired
    private ShowRepo showRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Override
    public Show createShow(ShowRequest showRequest) {
        Show show = constructShow(showRequest);
        return showRepo.save(show);
    }

    @Override
    public List<Show> findAll() {
        return showRepo.findAll();
    }

    @Override
    public ShowDetailsForGuestResponse findById(Long showId) throws ShowNotFound {
        return showRepo.findById(showId)
                .map(s ->
                ShowDetailsForGuestResponse.builder()
                        .showNumber(s.getId())
                        .availableSeat(s.getAvailableSeats())
                        .blockedSeat(s.getBlockedSeats())
                        .bookedSeat(s.getBookedSeats())
                        .build())
                .orElseThrow(() -> new ShowNotFound("Show not found with given id: " + showId));

    }

    public Show constructShow(ShowRequest showRequest) {

        List<String> availableSeats = createSeats(showRequest.getNumberOfRows(), showRequest.getNumberOfSeatsPerRows());

        return Show.builder()
                .showTime(showRequest.getShowTime())
                .cancellationWindow(showRequest.getCancellationWindow())
                .numberOfRows(showRequest.getNumberOfRows())
                .numberOfSeatsPerRows(showRequest.getNumberOfSeatsPerRows())
                .availableSeats(availableSeats)
                .bookedSeats(List.of())
                .blockedSeats(List.of())
                .movieId(showRequest.getMovieId())
                .build();
    }

    private List<String> createSeats(int numberOfRows, int numberOfSeatsPerRows) {
        List<String> seats = new ArrayList<>();
        for(int i =0; i < numberOfRows; i++) {
            for(int j =1; j <= numberOfSeatsPerRows; j++) {
                String seat = (char) (65 + i) + String.valueOf(j);
                seats.add(seat);
            }
        }
        return seats;
    }
}
