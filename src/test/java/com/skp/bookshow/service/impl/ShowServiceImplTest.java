package com.skp.bookshow.service.impl;

import com.skp.bookshow.exception.ShowNotFound;
import com.skp.bookshow.model.Show;
import com.skp.bookshow.model.request.ShowRequest;
import com.skp.bookshow.repository.ShowRepo;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowServiceImplTest {
    @Mock
    ShowRepo showRepo;

    @InjectMocks
    ShowServiceImpl showServiceImpl = new ShowServiceImpl();

    @Test
    public void it_should_create_show() {
        ShowRequest showRequest = ShowRequest.builder()
                .cancellationWindow(1)
                .numberOfSeatsPerRows(2)
                .numberOfRows(3)
                .movieId(1L)
                .showTime(Date.from(Instant.now()))
                .build();

        Show show = showServiceImpl.constructShow(showRequest);
        show.setId(1L);
        when(showRepo.save(Mockito.any())).thenReturn(show);

        var result = showServiceImpl.createShow(showRequest);
        assertEquals(6, result.getAvailableSeats().size());
        assertEquals(List.of("A1", "A2", "B1", "B2", "C1", "C2"), result.getAvailableSeats());
        assertEquals(0, result.getBlockedSeats().size());
        assertEquals(0, result.getBookedSeats().size());
    }

    @Test
    public void findAll_should_return_the_list_of_show() {
        Show show = Show.builder()
                .id(1L)
                .cancellationWindow(1)
                .numberOfSeatsPerRows(2)
                .numberOfRows(3)
                .movieId(1L)
                .showTime(Date.from(Instant.now()))
                .availableSeats(List.of("A1", "A2", "B1", "B2", "C1", "C2"))
                .blockedSeats(List.of())
                .blockedSeats(List.of())
                .build();
        Show show1 = Show.builder()
                .id(2L)
                .cancellationWindow(1)
                .numberOfSeatsPerRows(2)
                .numberOfRows(3)
                .movieId(1L)
                .showTime(Date.from(Instant.now()))
                .availableSeats(List.of("A1", "A2", "B1", "B2", "C1", "C2"))
                .blockedSeats(List.of())
                .blockedSeats(List.of())
                .build();
        when(showRepo.findAll()).thenReturn(List.of(show, show1));

        var result = showServiceImpl.findAll();
        assertEquals( 2, result.size());
        assertEquals( 1L, result.get(0).getId());
        assertEquals( 2L, result.get(1).getId());
    }

    @Test
    public void findBy_should_return_the_show_if_present() throws ShowNotFound {
        Show show = Show.builder()
                .id(1L)
                .cancellationWindow(1)
                .numberOfSeatsPerRows(2)
                .numberOfRows(3)
                .movieId(1L)
                .showTime(Date.from(Instant.now()))
                .availableSeats(List.of("A1", "A2", "B1", "B2", "C1", "C2"))
                .blockedSeats(List.of())
                .blockedSeats(List.of())
                .build();

        when(showRepo.findById(Mockito.anyLong())).thenReturn(Optional.of(show));

        var result = showServiceImpl.findById(1L);
        assertEquals(1, result.getShowNumber());
    }

    @Test
    public void findBy_should_throw_exception_the_show_if_not_present() {
        Show show = Show.builder()
                .id(1L)
                .cancellationWindow(1)
                .numberOfSeatsPerRows(2)
                .numberOfRows(3)
                .movieId(1L)
                .showTime(Date.from(Instant.now()))
                .availableSeats(List.of("A1", "A2", "B1", "B2", "C1", "C2"))
                .blockedSeats(List.of())
                .blockedSeats(List.of())
                .build();

        var res = assertThrows(ShowNotFound.class, () -> showServiceImpl.findById(2L));

        assertEquals("Show not found with given id: 2", res.getMessage());

    }


}