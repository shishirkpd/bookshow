package com.skp.bookshow.model.response;

import com.skp.bookshow.model.BookedSeat;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BookingResponse implements Serializable {
    String message;
    BookedSeat bookedSeat;
}
