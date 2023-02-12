package com.example.pradhuman.controllers;

import com.example.pradhuman.entities.*;
import com.example.pradhuman.services.OrderService;
import com.example.pradhuman.services.PaymentService;
import com.example.pradhuman.utils.BaseEntityResponse;
import com.example.pradhuman.utils.OrderResponse;
import com.example.pradhuman.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @PostMapping("/create")
    public BaseEntityResponse createOrder(@RequestBody Order order) {
        BaseEntityResponse response;
        order.setOrderId(UUID.randomUUID().toString());
        try {
            order.setStatus(OrderStatus.NEW.getStatus());
            orderService.createOrder(order);
            response = OrderResponse.getSuccessResponse(String.format("order created successfully for id : %s",
                    order.getOrderId()));
            response.setEntity(order);
            return response;
        } catch (UserNotFoundException e) {
            return OrderResponse.getFailedResponse(e.getMessage());
        }
    }

    @GetMapping("/")
    public BaseEntityResponse getOrderById(@RequestParam String orderId) {
        BaseEntityResponse<Order> response;
        try {
            Order order = orderService.getById(orderId);
            response = OrderResponse.getSuccessResponse(String.format("found order for id : %s", order.getOrderId()));
            response.setEntity(order);
        } catch (RuntimeException e) {
            response = OrderResponse.getFailedResponse(e.getMessage());
        }
        return response;
    }

    @GetMapping("/get-all")
    public BaseEntityResponse getAllOrders() {
        BaseEntityResponse<Order> response;
        List<Order> orders = orderService.getAll();
        if (orders.size() > 0) {
            response = OrderResponse.getSuccessResponse(String.format("Found %s Orders", orders.size()));
            response.setEntities(orders);
        } else {
            response = OrderResponse.getFailedResponse("No orders Found");
        }
        return response;
    }

    @GetMapping("/get-all-by-userid")
    public BaseEntityResponse getAllByUserId(@RequestParam String userId) {
        BaseEntityResponse<Order> response;
        List<Order> orders = orderService.getAllByUserID(userId);
        if (orders.size() > 0) {
            response = OrderResponse.getSuccessResponse(String.format("Found %s Orders for userId : %s"
                    , orders.size(), userId));
            response.setEntities(orders);
        } else {
            response = OrderResponse.getFailedResponse(String.format("No orders Found for userId : %s", userId));
        }
        return response;
    }

    @PutMapping("/update")
    public BaseEntityResponse updateOrder(@RequestBody Order order) {
        BaseEntityResponse<Order> response;
        try {
            Order updatedOrder = orderService.updateOrder(order);
            response = OrderResponse.getSuccessResponse(String.format("Successfully updated order for orderId : %s",
                    order.getOrderId()));
            response.setEntity(updatedOrder);
        } catch (UserNotFoundException e) {
            response = OrderResponse.getFailedResponse(e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/delete")
    public BaseEntityResponse<Order> deleteOrder(@RequestParam String orderId) {
        BaseEntityResponse<Order> response;
        try {
            orderService.deleteOrder(orderId);
            response = OrderResponse.getSuccessResponse(String.format("successfully deleted order for id : %s", orderId));
        } catch (RuntimeException e) {
            response = OrderResponse.getFailedResponse(e.getMessage());
        }
        return response;
    }

    @PostMapping("/top-up")
    public Payment topUp(@RequestBody Order order) {
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.NEW.getStatus());
        order.setOrderType(OrderType.TOP_UP);
        return paymentService.createPayment(order, AuditLogs.TOP_UP);
    }

    @GetMapping("/payments/{orderId}")
    public BaseEntityResponse getAllPaymentsByOrderId(@PathVariable String orderId) {
        BaseEntityResponse<Payment> response;
        List<Payment> payments = paymentService.getAllPaymentsByOrderId(orderId);
        if (payments.size() > 0) {
            response = BaseEntityResponse.getSuccessResponse("Here's all Payments");
            response.setEntities(payments);
        } else {
            response = BaseEntityResponse.getFailedResponse(String.format("No payment found for OrderId %s", orderId));
        }
        return response;
    }

    @PostMapping("/repayment")
    public Order doRepayment(@RequestParam String orderId) {
        return orderService.doRepayment(orderId);
    }

    @PostMapping("/cancel")
    public Order cancelOrder(@RequestParam String orderId) {
        return orderService.cancelOrder(orderId);
    }


    @ExceptionHandler({InvalidDataAccessResourceUsageException.class})
    public BaseEntityResponse sqlGrammarException() {
        return BaseEntityResponse.getFailedResponse("Sql grammar exception");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public BaseEntityResponse httpMessageNotReadable(HttpMessageNotReadableException e) {
        return BaseEntityResponse.getFailedResponse(e.getMessage());
    }

    @ExceptionHandler({RuntimeException.class})
    public BaseEntityResponse runTimeExHandler(RuntimeException e) {
        return BaseEntityResponse.getFailedResponse(e.getMessage());
    }

}
