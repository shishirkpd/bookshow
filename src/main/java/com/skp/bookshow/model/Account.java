package com.skp.bookshow.model;

import com.skp.bookshow.model.enums.AccountType;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "Account")
@Entity
public class Account implements Serializable {
    @Id
    private Long phone;
    private String password;
    private String name;
    private String email;
    @Enumerated(EnumType.STRING)
    private AccountType accounttype;
}
