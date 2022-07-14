package com.skp.bookshow.model.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ShowDetailsForGuestResponse implements Serializable {
    Long showNumber;
    List<String> bookedSeat;
    List<String> availableSeat;
    List<String> blockedSeat;
}
