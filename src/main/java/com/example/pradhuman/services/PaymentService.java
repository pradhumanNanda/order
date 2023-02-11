package com.example.pradhuman.services;

import com.example.pradhuman.entities.AuditLogs;
import com.example.pradhuman.entities.Order;
import com.example.pradhuman.entities.Payment;

import java.util.List;

public interface PaymentService {

    Payment createPayment(Order order, AuditLogs auditLogs);

    List<Payment> getAllPaymentsByOrderId(String orderId);
}
