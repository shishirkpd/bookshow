package com.skp.bookshow.service.impl;

import com.skp.bookshow.model.Movie;
import com.skp.bookshow.repository.MovieRepo;
import com.skp.bookshow.service.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    MovieRepo movieRepo;

    @InjectMocks
    MovieService movieService = new MovieServiceImpl();

    @Test
    void it_should_create_movie() {
        Movie movie = Movie.builder()
                .id(1L)
                .descriptions("some description")
                .title("some title")
                .durationInMinutes(240)
                .build();

        when(movieRepo.save(Mockito.any())).thenReturn(movie);

        var result = movieService.create(movie);

        assertEquals(movie.getId(), result.getId());
        assertEquals(movie.getTitle(), result.getTitle());
    }

    @Test
    void findAll_should_return_list_of_movies() {
        Movie movie = Movie.builder()
                .id(1L)
                .descriptions("some description")
                .title("some title")
                .durationInMinutes(240)
                .build();
        Movie movie2= Movie.builder()
                .id(2L)
                .descriptions("some description2")
                .title("some title2")
                .durationInMinutes(240)
                .build();

        when(movieRepo.findAll()).thenReturn(List.of(movie, movie2));

        var result = movieService.findAll();

        assertEquals(2, result.size());
    }
}