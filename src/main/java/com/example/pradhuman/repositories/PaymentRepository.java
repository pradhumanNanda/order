package com.example.pradhuman.repositories;

import com.example.pradhuman.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(value = "select * from payment p where p.order_id = :orderId",nativeQuery = true)
    List<Payment> getAllPaymentsByOrderId(@Param("orderId") String orderId);
}
