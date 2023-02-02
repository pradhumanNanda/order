package com.example.pradhuman.entities;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("New"),SUCCESS("Success"),FAIL("Fail");

    private String status;

    OrderStatus(String status){
        this.status = status;
    }
}
