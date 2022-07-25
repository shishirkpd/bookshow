package com.skp.bookshow.model;

import com.skp.bookshow.model.enums.SeatStatus;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "BookedSeat")
public class BookedSeat {
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    Long ticketId;
    Long phoneNumber;
    @Column
    @ElementCollection(targetClass=String.class)
    List<String> bookedSeat;
    @Enumerated(EnumType.STRING)
    SeatStatus seatStatus;
    Long showNumber;
    Date bookingTime = Date.from(Instant.now());
}
