package com.collections.service;

import com.collections.persistence.Customer;
import com.collections.persistence.Order;
import com.collections.persistence.Product;
import com.collections.persistence.converter.JsonConverterOrders;
import com.collections.service.exception.ShoppingServiceException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ShoppingService {

    private Map<Customer, Map<Product, Integer>> shopping;


    public ShoppingService(String... filenames) {
        this.shopping = init(filenames);
    }


    private Map<Customer, Map<Product, Integer>> init(String... filenames) {

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

        if (Objects.isNull(customersMap)) {
            throw new ShoppingServiceException("customersMap is null");
        }

        if (Objects.isNull(order)) {
            throw new ShoppingServiceException("order is null");
        }

        if (Objects.isNull(product)) {
            throw new ShoppingServiceException("product is null");
        }
        customersMap.get(order.getCustomer())
                .replace(product,
                        customersMap.get(order.getCustomer()).get(product),
                        customersMap.get(order.getCustomer()).get(product) + 1);
    }

    private void addNewCustomerProduct(Product product, Map<Product, Integer> productIntegerMap) {
        if (Objects.isNull(product)) {
            throw new ShoppingServiceException("product is null");
        }
        if (Objects.isNull(productIntegerMap)) {
            throw new ShoppingServiceException("productMap is null");
        }
        productIntegerMap.put(product, 1);
    }

    private void addNewCustomer(Map<Customer, Map<Product, Integer>> customersMap, Order order) {

        if (Objects.isNull(order)) {
            throw new ShoppingServiceException("order is null");
        }
        if (Objects.isNull(customersMap)) {
            throw new ShoppingServiceException("customersMap is null");
        }

        Map<Product, Integer> temp = new HashMap<>();
        addNewCustomerProduct(order.getProducts().get(0), temp);
        customersMap.put(order.getCustomer(), temp);
    }


    public Map<Customer, Map<Product, Integer>> getShopping() {
        return shopping;
    }





    public Customer getCustomerWhichPaidTheMost(Map<Customer, Map<Product, Integer>> customersMap) {
        Map<Customer, BigDecimal> customerPayments = customersMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> totalPurchaseByCustomer(e.getValue())));
        return customerPayments.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();
    }

    private BigDecimal totalPurchaseByCustomer(Map<Product, Integer> customerOrders) {
        return customerOrders.entrySet()
                .stream()
                .map(o -> o.getKey().getPrice().multiply(BigDecimal.valueOf(o.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
