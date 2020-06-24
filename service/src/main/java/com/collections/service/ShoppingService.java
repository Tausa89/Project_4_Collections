package com.collections.service;

import com.collections.persistence.Customer;
import com.collections.persistence.Order;
import com.collections.persistence.Product;
import com.collections.persistence.converter.JsonConverterOrders;
import com.collections.service.exception.ShoppingServiceException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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





    public Customer getCustomerWhichPaidTheMost() {
        var collect = shopping.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> totalPurchaseByCustomer(e.getValue())));
        return collect.entrySet()
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


    public Customer getCustomerWhichPaidMostInGivenCategory(String categoryName){

        if(Objects.isNull(categoryName)){
            throw new ShoppingServiceException("category name is null");
        }



        var result = shopping
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> getFilteredShoppingList(e.getValue(), categoryName)))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> totalPurchaseByCustomer(e.getValue())));
        return result.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();

    }


    private Map<Product, Integer> getFilteredShoppingList(Map<Product,Integer> customerOrders, String filename){

        return customerOrders
                .entrySet()
                .stream()
                .filter(x -> x.getKey().getCategory().equals(filename))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }


    public Map<Integer, String> getMostPopularProductsForEveryAge(){


        return shopping
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        t -> t.getValue()
                                .keySet()
                                .stream()
                                .map(Product::getCategory)
                                .collect(Collectors.toList())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> getMostCommonElementFromList(p.getValue())))
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(p -> p.getKey().getAge(),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> getMostCommonElementFromList(p.getValue())));

    }

    private String getMostCommonElementFromList(List<String> list) {
        return list
                .stream()
                .collect(Collectors
                        .groupingBy(Function.identity(),
                                Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
    }


}