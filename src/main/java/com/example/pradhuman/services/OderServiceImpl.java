package com.example.pradhuman.services;

import com.example.pradhuman.entities.*;
import com.example.pradhuman.repositories.OrderRepository;
import com.example.pradhuman.repositories.PaymentRepository;
import com.example.pradhuman.repositories.UserRepository;
import com.example.pradhuman.repositories.WalletRepository;
import com.example.pradhuman.utils.Jutil;
import com.example.pradhuman.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OderServiceImpl implements OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    WalletRepository walletRepository;

    @Override
    public List<Order> getAllByUserID(String userId) {
        return orderRepository.getAllOrdersByUserId(userId);
    }

    @Override
    public Order createOrder(Order order) throws UserNotFoundException {
        Payment payment = Payment.builder().paymentStatus(PaymentStatus.NEW).paymentType(PaymentType.BOOKING)
                .orderId(order.getOrderId()).build();
         if(userRepository.findById(order.getUserId()).isPresent()){
             if(userRepository.findById(order.getUserId()).get().isDisabled()){
                 payment.setPaymentStatus(PaymentStatus.REJECTED);
                 paymentRepository.save(payment);
                 throw new UserNotFoundException(String.format("User is disabled %s", order.getUserId()));
             }
         }else {
             order.setStatus(OrderStatus.FAIL.getStatus());
             payment.setPaymentStatus(PaymentStatus.NOT_INITIATE);
             paymentRepository.save(payment);
             throw new UserNotFoundException(String.format("User not found for id : %s", order.getUserId()));
         }
        if(Jutil.isPriceValid(order)){
            Wallet wallet = walletRepository.getUserWallet(order.getUserId());
            if(wallet != null && wallet.getBalance() >= order.getTotalAmount()){
                order.setStatus(OrderStatus.SUCCESS.getStatus());
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                wallet.setBalance(Jutil.formatDouble(wallet.getBalance() - order.getTotalAmount()));
                wallet.setAuditLogs(wallet.getAuditLogs() + AuditLogs.BOOKING.value(order.getTotalAmount(), order.getOrderId()));
                walletRepository.save(wallet);
                payment.setAmount(Jutil.formatDouble(order.getTotalAmount()));
                Payment paymentDb = paymentRepository.saveAndFlush(payment);
                order.setPaymentId(paymentDb.getId());
                orderRepository.save(order);
            }else {
                payment.setPaymentStatus(PaymentStatus.PENDING);
                order.setStatus(OrderStatus.NEW.getStatus());
                Payment paymentDb = paymentRepository.saveAndFlush(payment);
                order.setPaymentId(paymentDb.getId());
                orderRepository.save(order);
                wallet.setAuditLogs(wallet.getAuditLogs() + AuditLogs.FAILED.value(order.getTotalAmount(), order.getOrderId()));
                walletRepository.save(wallet);
            }
        }else {
            order.setStatus(OrderStatus.FAIL.getStatus());
            payment.setPaymentStatus(PaymentStatus.NOT_INITIATE);
            paymentRepository.save(payment);
            throw new RuntimeException("Price Validation Failed price should be > 0");
        }
        return order;
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.getAllOrders();
    }

    @Override
    public Order getById(String id) throws RuntimeException {
        if (orderRepository.findById(id).isPresent())
            return orderRepository.findById(id).get();
        throw new RuntimeException(String.format("Order not found for id : %s", id));
    }

    @Override
    public Order updateOrder(Order order) throws UserNotFoundException {
        Order oldOrder;
        if(orderRepository.findById(order.getOrderId()).isPresent() &&
         userRepository.findById(order.getUserId()).isPresent()){
             oldOrder = orderRepository.findById(order.getOrderId()).get();
            Jutil.getUpdatedOrder(oldOrder, order);
            if(oldOrder.getStatus().equals(OrderStatus.FAIL))
                throw new RuntimeException("Items price Validation failed. price should be > 0");
            orderRepository.save(oldOrder);
        }else {
            throw new UserNotFoundException(String.format("Oder or user for orderId : %s and userId : %s not found",
                    order.getOrderId(),order.getUserId()));
        }
        return oldOrder;
    }

    @Override
    public boolean deleteOrder(String orderId) throws RuntimeException {
        if(orderRepository.findById(orderId).isPresent()) {
            Order order = orderRepository.findById(orderId).get();
            order.setDeleted(true);
            orderRepository.save(order);
            return true;
        }
        throw new RuntimeException(String.format("Order not found for id : %s", orderId));
    }
}
