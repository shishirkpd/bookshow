package com.skp.bookshow.model.request;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BookShowRequest implements Serializable {
    private Long phoneNumber;
    private List<String> blockedSeat;
    private Long showNumber;
}
