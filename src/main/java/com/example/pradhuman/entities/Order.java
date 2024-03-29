package com.example.pradhuman.entities;

import com.example.pradhuman.utils.Jutil;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

@Entity(name = "order")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@Builder
@AllArgsConstructor
@Table(name = "order1")
@NoArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private String orderId;


    private String userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderType orderType = OrderType.BOOKING;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private List<Item> items;

    @Builder.Default
    private boolean deleted = false;

    private double totalAmount;

    @Builder.Default
    private Long paymentId = -1L;


    public void setUserId(String userId) {
        if(!Jutil.isNullOrEmpty(userId))
            this.userId = userId;
    }

    public void setStatus(String status) {
        if(!Jutil.isNullOrEmpty(status))
            this.status = OrderStatus.getOrderStatus(status);
    }

    public void setItems(List<Item> items) {
        if(items.size() > 0)
            this.items = items;
    }
}
