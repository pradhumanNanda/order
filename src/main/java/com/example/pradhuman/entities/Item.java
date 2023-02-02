package com.example.pradhuman.entities;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Item {

    private String category;
    private int quantity;
    private double price;
}
