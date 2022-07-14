package com.skp.bookshow;

import com.skp.bookshow.controller.BookShowController;
import com.skp.bookshow.exception.AccountNotFound;
import com.skp.bookshow.model.BookedSeat;
import com.skp.bookshow.model.request.BookShowRequest;
import com.skp.bookshow.model.request.ShowRequest;
import com.skp.bookshow.model.response.BookingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication
public class ConsoleApplication implements CommandLineRunner {

    private BookShowController bookShowController;

    private static Logger LOG = LoggerFactory
      .getLogger(ConsoleApplication.class);

    public ConsoleApplication(BookShowController bookShowController) {
        this.bookShowController = bookShowController;
    }
    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(ConsoleApplication.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");

        Scanner in = new Scanner(System.in);
        boolean runApp = true;
        while (runApp) {
            System.out.println("*************************");
            System.out.println("Welcome");

            System.out.println("Select option \n 1. Admin \n 2. Guest");

            var userType = in.nextInt();
            switch (userType ) {
                case 1:
                    System.out.println("Enter id");
                    String user = in.next();
                    System.out.println("Enter password");
                    String pass = in.next();
                    try {
                        var res = bookShowController.login(Long.parseLong(user), pass);
                        boolean loggedIn = true;
                        while (loggedIn) {
                            System.out.println("\nWelcome to Admin: " + user);
                            System.out.println("\nPlease select the option:");
                            System.out.println("\n1. Create show");
                            System.out.println("\n2. View show information");
                            System.out.println("\n3. Exit");
                            String option = in.next();
                            switch (Integer.parseInt(option)) {
                                case 1:
                                    System.out.println("Enter the following information\n");
                                    System.out.println("showTime eg: 2022-08-16T20:00:00.000Z\n");
                                    String st = in.next();
                                    var instant = Instant.parse(st);
                                    Date showTime = Date.from(instant);
                                    System.out.println("movieId\n");
                                    Long movieId = Long.parseLong(in.next());
                                    System.out.println("numberOfRows\n");
                                    int numberOfRows = in.nextInt();
                                    System.out.println("numberOfSeatsPerRows\n");
                                    int numberOfSeatsPerRows = in.nextInt();
                                    System.out.println("cancellationWindow\n");
                                    int cancellationWindow = in.nextInt();

                                    ShowRequest showRequest = ShowRequest.builder()
                                            .showTime(showTime)
                                            .movieId(movieId)
                                            .numberOfRows(numberOfRows)
                                            .numberOfSeatsPerRows(numberOfSeatsPerRows)
                                            .cancellationWindow(cancellationWindow)
                                            .build();
                                    var result = bookShowController.createShow(showRequest);
                                    System.out.println("Response: " + result.getBody());
                                    continue;
                                case 2:
                                    System.out.println("Enter the show id");
                                    Long showId = in.nextLong();
                                    bookShowController.findShowStatus(showId);
                                    ResponseEntity<List<BookedSeat>> showStatusResult = bookShowController.findShowStatus(showId);
                                    System.out.println("Response: " + showStatusResult.getBody());
                                    continue;
                                case 3: loggedIn = false; continue;
                                default: loggedIn = false;
                                    continue;
                            }
                        }
                        System.out.println("\nPlease select the option:");
                    } catch (AccountNotFound ex) {
                        System.out.println("\nFailed to login");
                    }
                    continue;
                case 2:
                    var buyerView = true;
                    while (buyerView) {
                        System.out.println("\n****** Welcome Guest *******");
                        System.out.println("Select one option:\n");
                        System.out.println("1. View All shows timings\n");
                        System.out.println("2. View Availability for shows \n");
                        System.out.println("3. Book seat for show \n");
                        System.out.println("4. Cancel a booking \n");
                        System.out.println("5. Exit \n");
                        var buyer = in.nextInt();
                        switch (buyer) {
                            case 1:
                                var allShowRes = bookShowController.findAllShows();
                                var showList = allShowRes.getBody().stream().map(x ->Pair.of(x.getId(), x.getShowTime())).collect(Collectors.toList());
                                System.out.println("\nDetails of shows: " + showList);
                                continue;
                            case 2:
                                System.out.println("\nEnter the show id: ");
                                Long showId = in.nextLong();
                                var allShows = bookShowController.findAllShows();
                                var selectedShow = allShows.getBody().stream().filter(x -> x.getId().equals(showId)).collect(Collectors.toList());
                                System.out.println("\nShow details: " + selectedShow);
                                continue;
                            case 3:
                                System.out.println("\nEnter the details for booking");
                                System.out.println("\nEnter the phone number");
                                Long phoneNumber = in.nextLong();
                                System.out.println("\nEnter the show id");
                                Long showIdForGuest = in.nextLong();
                                System.out.println("\nEnter the seat number Like: A1,A2 as per availability");
                                String seats = in.next();
                                List<String> seatsList = Arrays.stream(seats.split(",")).collect(Collectors.toList());
                                BookShowRequest bookShowRequest = BookShowRequest.builder()
                                        .showNumber(showIdForGuest)
                                        .blockedSeat(seatsList)
                                        .phoneNumber(phoneNumber)
                                        .build();
                                var bookingRes = bookShowController.bookShow(bookShowRequest);
                                if(bookingRes.getStatusCode() == HttpStatus.OK) {
                                    Instant cur = Instant.now();
                                    System.out.println("\nBooking details: " + bookingRes.getBody());
                                    System.out.println("\nPlease confirm your booking enter \n1.Yes \n2.No");
                                    var confirmation = in.next();
                                    ResponseEntity<BookingResponse> confirmationRes;
                                    if(confirmation.equalsIgnoreCase("Yes")) {
                                        confirmationRes = bookShowController.confirmBooking(bookShowRequest, true);
                                    } else {
                                        confirmationRes = bookShowController.confirmBooking(bookShowRequest, false);
                                    }
                                    System.out.println("\nConfirmation details: " + confirmationRes.getBody());
                                } else {
                                    System.out.println("\nBooking details: " + bookingRes.getBody());
                                }
                                continue;
                            case 4:
                                System.out.println("\nEnter the ticketId to cancel");
                                Long ticketId = in.nextLong();
                                var cancelRes = bookShowController.cancelBooking(ticketId);
                                System.out.println("\nCancel response: " + cancelRes.getBody());
                                continue;
                            case 5: buyerView = false;continue;
                            default: buyerView = false;
                            continue;
                        }
                    }
                case 3: runApp = false; break;
                default: runApp = false;
                break;
            }
        }
    }
}