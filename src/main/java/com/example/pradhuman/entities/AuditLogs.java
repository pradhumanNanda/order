package com.example.pradhuman.entities;


public enum AuditLogs {
    TOP_UP("#Top up of %s successfully by order_id %s"),
    CREATED("Wallet Created with amount 0"),
    REFUND("#Refund of 4s successfully for order_id %s"),
    BOOKING("#Amount of %s paid for order_id %s"),
    RESETTLEMENT("#Repayment of %s amount done for order_id %s"),
    FAILED("#Failed to pay %s for order_id %s due to insufficient funds");

    AuditLogs(String value) {
        this.value = value;
    }

    public String value;

    public String value(double amount, String orderId) {
        return String.format(this.value, amount, orderId);
    }
}
