package com.fullstack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_detail")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //JPA annotation used to automatically generated primary key (ID) for DB.
    private Long userId;

    private String userName;

    @Column(unique = true)
    @Email(message = "Email ID must be valid.")
    private String userEmailId;

    private String userPassword;

}
