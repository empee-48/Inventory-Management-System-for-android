package com.brocode.utils;

import com.brocode.entity.Order;

public class IdGenerator {

    public static String generateOrderId(Order order){
        String prefix = "OD";
        Long suffix = 1000 + order.getId();
        return String.format("%s%d",prefix,suffix);
    }

}
