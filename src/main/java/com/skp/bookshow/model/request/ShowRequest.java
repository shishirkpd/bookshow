package com.skp.bookshow.model.request;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ShowRequest {
    private Date showTime;
    private Long movieId;
    private int numberOfRows;
    private int numberOfSeatsPerRows;
    private int cancellationWindow;
}
