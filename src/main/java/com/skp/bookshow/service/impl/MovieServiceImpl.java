package com.skp.bookshow.service.impl;

import com.skp.bookshow.model.Movie;
import com.skp.bookshow.repository.MovieRepo;
import com.skp.bookshow.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {
    @Autowired
    private MovieRepo movieRepo;

    @Override
    public Movie create(Movie movie) {
        return movieRepo.save(movie);
    }

    @Override
    public List<Movie> findAll() {
        return movieRepo.findAll();
    }
}
