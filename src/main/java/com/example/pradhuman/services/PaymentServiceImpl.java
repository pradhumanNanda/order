package com.example.pradhuman.services;

import com.example.pradhuman.entities.*;
import com.example.pradhuman.repositories.OrderRepository;
import com.example.pradhuman.repositories.PaymentRepository;
import com.example.pradhuman.repositories.UserRepository;
import com.example.pradhuman.repositories.WalletRepository;
import com.example.pradhuman.utils.Jutil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public synchronized Payment createPayment(Order order, AuditLogs auditLogs) {
        Payment payment = Payment.builder().paymentStatus(PaymentStatus.NEW).paymentType(PaymentType.TOP_UP)
                .orderId(order.getOrderId()).build();
        if(userRepository.findById(order.getUserId()).isPresent()){
            if(!userRepository.findById(order.getUserId()).get().isDisabled() && Jutil.isPriceValid(order)){
                Wallet wallet = walletRepository.getUserWallet(order.getUserId());
                wallet.setBalance(Jutil.formatDouble(wallet.getBalance() + order.getTotalAmount()));
                wallet.setAuditLogs(wallet.getAuditLogs() + auditLogs.value(order.getTotalAmount(),
                        order.getOrderId()));
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                walletRepository.save(wallet);
                order.setStatus(OrderStatus.SUCCESS.getStatus());
            }else {
                payment.setPaymentStatus(PaymentStatus.REJECTED);
                order.setStatus(OrderStatus.FAIL.getStatus());
            }
        }
        Payment paymentDb = paymentRepository.saveAndFlush(payment);
        order.setPaymentId(paymentDb.getId());
        orderRepository.save(order);
        return paymentDb;
    }

    @Override
    public List<Payment> getAllPaymentsByOrderId(String orderId) {
        return paymentRepository.getAllPaymentsByOrderId(orderId);
    }
}
