package com.example.pradhuman.entities;


public enum PaymentType {
    REFUND("Refund to user wallet"),  // order cancelled by user
    BOOKING("Deducted from user wallet"), // order placed by user
    TOP_UP("wallet top-up by user"), // amount added by user in wallet
    RESETTLEMENT("Repayment for pending"); // with in 10 min gap repayment after wallet top_up

    PaymentType(String val){
        this.value = val;
    }

    public String value;

    public String value(){
        return value;
    }

}
