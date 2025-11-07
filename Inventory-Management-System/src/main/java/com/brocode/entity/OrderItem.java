package com.brocode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Double orderPrice;
}