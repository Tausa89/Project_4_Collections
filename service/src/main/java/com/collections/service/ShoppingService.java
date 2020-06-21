package com.collections.service;

import com.collections.persistence.Customer;
import com.collections.persistence.Order;
import com.collections.persistence.Product;
import com.collections.persistence.converter.JsonConverterOrders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShoppingService {

    private Map<Customer,Map<Product,Integer>> shopping;


    public ShoppingService(String ... filenames) {
        this.shopping = init(filenames);
    }




    private Map<Customer, Map<Product, Integer>> init(String ... filenames) {

        List<Order> orders = Arrays
                .stream(filenames)
                .flatMap(filename -> {
                    return new JsonConverterOrders(filename).fromJson().orElseThrow().stream();
                })
                .collect(Collectors.toList());


        Map<Customer, Map<Product, Integer>> customers = new HashMap<>();

        for (Order order : orders) {
            if (!customers.containsKey(order.getCustomer())) {
                addNewCustomer(customers, order);
            } else {
                for (Product product : order.getProducts()) {
                    if (!customers.get(order.getCustomer()).containsKey(product)) {
                        addNewCustomerProduct(product, customers.get(order.getCustomer()));
                    } else {
                        updateCustomerProduct(customers, order, product);
                    }
                }
            }
        }

        return customers;

    }


    private void updateCustomerProduct(Map<Customer, Map<Product, Integer>> customersMap, Order order, Product product) {
        customersMap.get(order.getCustomer())
                .replace(product,
                        customersMap.get(order.getCustomer()).get(product),
                        customersMap.get(order.getCustomer()).get(product) + 1);
    }

    private void addNewCustomerProduct(Product product, Map<Product, Integer> productIntegerMap) {
        productIntegerMap.put(product, 1);
    }

    private void addNewCustomer(Map<Customer, Map<Product, Integer>> customersMap, Order order) {
        Map<Product, Integer> temp = new HashMap<>();
        addNewCustomerProduct(order.getProducts().get(0), temp);
        customersMap.put(order.getCustomer(), temp);
    }


    public Map<Customer, Map<Product, Integer>> getShopping() {
        return shopping;
    }
}
