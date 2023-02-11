package com.example.pradhuman.entities;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Item implements Serializable {

    private String category;
    private int quantity;
    private double price;
}
