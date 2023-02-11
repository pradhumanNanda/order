package com.example.pradhuman.entities;

public enum PaymentStatus {
    NEW,  // when we create order at very first step
    SUCCESS, // after wallet updated and amount deducted
    FAILED, // some exception in payment service
    NOT_INITIATE, // when failed to create order i.e. order status FAILED
    PENDING, // when wallet does not have sufficient amount can be completed within 10 min
    ABORTED, // pending to aborted after 10 min timestamp
    REJECTED; // user disabled
}
