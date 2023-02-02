package com.example.pradhuman.entities;

import com.example.pradhuman.utils.Jutil;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

@Entity(name = "order1")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@SuperBuilder
@NoArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private String orderId;

    private String userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private List<Item> items;

    @Builder.Default
    private boolean deleted = false;

    private double totalAmount;

    public void setUserId(String userId) {
        if(Jutil.isNullOrEmpty(userId))
            this.userId = userId;
    }

    public void setStatus(OrderStatus status) {
        if(Jutil.isNullOrEmpty(status.getStatus()))
            this.status = status;
    }

    public void setItems(List<Item> items) {
        if(items.size() > 0)
            this.items = items;
    }
}
