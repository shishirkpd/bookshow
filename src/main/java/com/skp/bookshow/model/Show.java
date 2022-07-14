package com.skp.bookshow.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "Show")
@Entity
public class Show implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date showTime;
    private long movieId;
    private int numberOfRows;
    private int numberOfSeatsPerRows;
    private int cancellationWindow;

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> availableSeats = new ArrayList<String>();

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> bookedSeats = new ArrayList<String>();

    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> blockedSeats = new ArrayList<String>();

}
