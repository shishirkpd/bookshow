package com.skp.bookshow.service;

import com.skp.bookshow.model.Movie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MovieService {

    Movie create(Movie movie);

    List<Movie> findAll();
}
