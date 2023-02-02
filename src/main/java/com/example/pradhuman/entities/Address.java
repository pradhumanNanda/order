package com.example.pradhuman.entities;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Address {

    private String city;
    private String state;
    private String pincode;
}
