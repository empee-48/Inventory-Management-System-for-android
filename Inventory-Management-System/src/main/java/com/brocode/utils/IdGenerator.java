package com.brocode.utils;

import com.brocode.entity.Order;
import com.brocode.entity.Product;
import com.brocode.entity.Sale;

public class IdGenerator {

    public static String generateOrderId(Order order){
        String prefix = "OD";
        Long suffix = 1000 + order.getId();
        return String.format("%s%d",prefix,suffix);
    }

    public static String generateSaleId(Sale sale){
        String prefix = "S";
        Long suffix = 1000 + sale.getId();
        return String.format("%s%d",prefix,suffix);
    }

    public static String generateProductKey(Product product){
        String prefix = "PR";
        Long suffix = 1000 + product.getId();
        return String.format("%s%d",prefix,suffix);
    }
}
