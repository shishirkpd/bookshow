package com.skp.bookshow.service;

import com.skp.bookshow.exception.ShowNotFound;
import com.skp.bookshow.model.Show;
import com.skp.bookshow.model.request.ShowRequest;
import com.skp.bookshow.model.response.ShowDetailsForGuestResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ShowService {

    Show createShow(ShowRequest showRequest);

    List<Show> findAll();

    ShowDetailsForGuestResponse findById(Long showId) throws ShowNotFound;
}
