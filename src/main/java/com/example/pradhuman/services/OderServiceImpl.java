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
        if (userRepository.findById(order.getUserId()).isPresent()) {
            if (userRepository.findById(order.getUserId()).get().isDisabled()) {
                payment.setPaymentStatus(PaymentStatus.REJECTED);
                paymentRepository.save(payment);
                throw new UserNotFoundException(String.format("User is disabled %s", order.getUserId()));
            }
        } else {
            order.setStatus(OrderStatus.FAIL.getStatus());
            payment.setPaymentStatus(PaymentStatus.NOT_INITIATE);
            paymentRepository.save(payment);
            throw new UserNotFoundException(String.format("User not found for id : %s", order.getUserId()));
        }
        if (Jutil.isPriceValid(order)) {
            Wallet wallet = walletRepository.getUserWallet(order.getUserId());
            if (wallet != null && wallet.getBalance() >= order.getTotalAmount()) {
                order.setStatus(OrderStatus.SUCCESS.getStatus());
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                wallet.setBalance(Jutil.formatDouble(wallet.getBalance() - order.getTotalAmount()));
                wallet.setAuditLogs(wallet.getAuditLogs() + AuditLogs.BOOKING.value(order.getTotalAmount(), order.getOrderId()));
                walletRepository.save(wallet);
                payment.setAmount(Jutil.formatDouble(order.getTotalAmount()));
                Payment paymentDb = paymentRepository.saveAndFlush(payment);
                order.setPaymentId(paymentDb.getId());
                orderRepository.save(order);
            } else {
                payment.setPaymentStatus(PaymentStatus.PENDING);
                order.setStatus(OrderStatus.NEW.getStatus());
                Payment paymentDb = paymentRepository.saveAndFlush(payment);
                order.setPaymentId(paymentDb.getId());
                orderRepository.save(order);
                wallet.setAuditLogs(wallet.getAuditLogs() + AuditLogs.FAILED.value(order.getTotalAmount(), order.getOrderId()));
                walletRepository.save(wallet);
            }
        } else {
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
        if (orderRepository.findById(order.getOrderId()).isPresent() &&
                userRepository.findById(order.getUserId()).isPresent()) {
            oldOrder = orderRepository.findById(order.getOrderId()).get();
            Jutil.getUpdatedOrder(oldOrder, order);
            if (oldOrder.getStatus().equals(OrderStatus.FAIL))
                throw new RuntimeException("Items price Validation failed. price should be > 0");
            orderRepository.save(oldOrder);
        } else {
            throw new UserNotFoundException(String.format("Oder or user for orderId : %s and userId : %s not found",
                    order.getOrderId(), order.getUserId()));
        }
        return oldOrder;
    }

    @Override
    public boolean deleteOrder(String orderId) throws RuntimeException {
        if (orderRepository.findById(orderId).isPresent()) {
            Order order = orderRepository.findById(orderId).get();
            order.setDeleted(true);
            orderRepository.save(order);
            return true;
        }
        throw new RuntimeException(String.format("Order not found for id : %s", orderId));
    }

    @Override
    public Order doRepayment(String orderId) {
        Order order;
        Payment payment;
        Wallet wallet;
        if (orderRepository.findById(orderId).isPresent()) {
            order = orderRepository.findById(orderId).get();
            if (paymentRepository.findById(order.getPaymentId()).isPresent()) {
                payment = paymentRepository.findById(order.getPaymentId()).get();
                wallet = walletRepository.getUserWallet(order.getUserId());
                if (wallet != null && wallet.getBalance() >= order.getTotalAmount()) {
                    wallet.setBalance(wallet.getBalance() - order.getTotalAmount());
                    wallet.setAuditLogs(wallet.getAuditLogs() + AuditLogs.RESETTLEMENT.value(order.getTotalAmount(),
                            order.getOrderId()));
                    payment.setPaymentStatus(PaymentStatus.SUCCESS);
                    payment.setPaymentType(PaymentType.RESETTLEMENT);
                    walletRepository.save(wallet);
                    paymentRepository.save(payment);
                    order.setStatus(OrderStatus.SUCCESS.getStatus());
                    orderRepository.save(order);
                } else {
                    throw new RuntimeException(String.format("No Wallet found for userId %s or insufficient funds"
                            , order.getUserId()));
                }
            } else {
                throw new RuntimeException(String.format("Payment not found for order %s", order.getPaymentId()));
            }
        } else {
            throw new RuntimeException(String.format("Order not found for orderId %s", orderId));
        }
        return order;
    }

    @Override
    public Order cancelOrder(String orderId) {
        Order order;
        Wallet wallet;
        Payment payment;
        if (orderRepository.findById(orderId).isPresent()) {
            order = orderRepository.findById(orderId).get();
            if (!order.isDeleted() && order.getStatus().equals(OrderStatus.SUCCESS) &&
                    order.getOrderType().equals(OrderType.BOOKING)) {
                if (paymentRepository.findById(order.getPaymentId()).isPresent()) {
                    payment = paymentRepository.findById(order.getPaymentId()).get();
                    wallet = walletRepository.getUserWallet(order.getUserId());
                    if (wallet != null && payment.getPaymentStatus().equals(PaymentStatus.SUCCESS)) {
                        order.setStatus(OrderStatus.CANCELED.getStatus());
                        Payment paymentRefund = Payment.builder().paymentStatus(PaymentStatus.SUCCESS).
                                paymentType(PaymentType.REFUND).orderId(order.getOrderId()).amount(payment.getAmount())
                                .build();
                         paymentRefund = paymentRepository.saveAndFlush(paymentRefund);
                         order.setPaymentId(paymentRefund.getId());
                         wallet.setBalance(Jutil.formatDouble(wallet.getBalance() + paymentRefund.getAmount()));
                         wallet.setAuditLogs(wallet.getAuditLogs() + AuditLogs.REFUND.value(paymentRefund.getAmount()
                                 , order.getOrderId()));
                         walletRepository.save(wallet);
                         orderRepository.save(order);
                    } else {
                        throw new RuntimeException(String.format("No wallet exist for this user %s or payment " +
                                        "status is not Success"
                                , order.getUserId()));
                    }
                } else {
                    throw new RuntimeException(String.format("No payment found for this orderId %s", orderId));
                }
            } else {
                throw new RuntimeException(String.format("Failed to cancel order for id %s order status should " +
                        "be success and order type should be booking", orderId));
            }
        } else {
            throw new RuntimeException(String.format("No order found for id %s", orderId));
        }
        return order;
    }
}
