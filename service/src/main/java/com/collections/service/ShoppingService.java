package com.collections.service;

import com.collections.persistence.Customer;
import com.collections.persistence.Order;
import com.collections.persistence.Product;
import com.collections.persistence.converter.JsonConverterOrders;
import com.collections.service.exception.ShoppingServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShoppingService {

    private final Map<Customer, Map<Product, Long>> shopping;


    public ShoppingService(String... filenames) {
        this.shopping = init(filenames);
    }


    private Map<Customer, Map<Product, Long>> init(String... filenames) {

        /*if(Objects.isNull(filenames)){
            throw new ShoppingServiceException("Data file is null");
        }
        List<Order> orders = Arrays
                .stream(filenames)
                .flatMap(filename -> new JsonConverterOrders(filename).fromJson().orElseThrow().stream())
                .collect(Collectors.toList());


        Map<Customer, Map<Product, Integer>> customers = new HashMap<>();

        for (Order order : orders) {
            if (!customers.containsKey(order.getCustomer())) {
                addNewCustomer(customers, order);
                for (Product product : order.getProducts()){
                    addNewCustomerProduct(product, customers.get(order.getCustomer()));
                }
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

        return customers;*/

        if(Objects.isNull(filenames)){
            throw new ShoppingServiceException("Data file is null");
        }

        return Arrays
                .stream(filenames)
                .flatMap(filename -> new JsonConverterOrders(filename).fromJson().orElseThrow().stream())
                .collect(Collectors.groupingBy(
                        Order::getCustomer,
                        Collectors.collectingAndThen(
                                Collectors.flatMapping(o -> o.getProducts().stream(), Collectors.toList()),
                                products -> products.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        )
                ));
    }


    /*private void updateCustomerProduct(Order order, Product product) {

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
    }*/

    /*private void addNewCustomerProduct(Product product, Map<Product, Integer> productIntegerMap) {
        if (Objects.isNull(product)) {
            throw new ShoppingServiceException("product is null");
        }
        if (Objects.isNull(productIntegerMap)) {
            throw new ShoppingServiceException("productMap is null");
        }
        productIntegerMap.put(product, 1);
    }*/

   /*private void addNewCustomer(Map<Customer, Map<Product, Integer>> customersMap, Order order) {

        if (Objects.isNull(order)) {
            throw new ShoppingServiceException("order is null");
        }
        if (Objects.isNull(customersMap)) {
            throw new ShoppingServiceException("customersMap is null");
        }

        Map<Product, Integer> temp = new HashMap<>();
        addNewCustomerProduct(order.getProducts().get(0), temp);
        customersMap.put(order.getCustomer(), temp);
    }*/


    public Customer getCustomerWhoPaidTheMost() {
        /*var collect = shopping.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> totalPurchaseByCustomer(e.getValue())));

        return collect.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();*/

        return shopping
                .entrySet()
                .stream()
                .max((e1, e2) -> totalPurchaseByCustomer(e1.getValue()).compareTo(totalPurchaseByCustomer(e2.getValue())))
                .orElseThrow(() -> new ShoppingServiceException("......"))
                .getKey();
    }

    private BigDecimal totalPurchaseByCustomer(Map<Product, Long> customerOrders) {
        return customerOrders.entrySet()
                .stream()
                .map(o -> o.getKey().getPrice().multiply(BigDecimal.valueOf(o.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public Customer getCustomerWhichPaidMostInGivenCategory(String categoryName) {

        if (Objects.isNull(categoryName)) {
            throw new ShoppingServiceException("category name is null");
        }

        return shopping
                .entrySet()
                .stream()
                .max((e1,e2) -> totalPurchaseByCategory(e1.getValue(),categoryName)
                        .compareTo(totalPurchaseByCategory(e2.getValue(),categoryName)))
                .orElseThrow(() -> new ShoppingServiceException("....."))
                .getKey();




//        var result = shopping
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> getFilteredShoppingList(e.getValue(), categoryName)))
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        e -> totalPurchaseByCustomer(e.getValue())));
//        return result.entrySet()
//                .stream()
//                .max(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .orElseThrow();

    }

    private BigDecimal totalPurchaseByCategory(Map<Product, Long> customerOrders, String category) {
        return customerOrders.entrySet()
                .stream()
                .flatMap(e -> Collections.nCopies(e.getValue().intValue(), e.getKey()).stream())
                .filter(p -> p.getCategory().equals(category))
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /*private Map<Product, Integer> getFilteredShoppingList(Map<Product, Integer> customerOrders, String categoryName) {

        if (Objects.isNull(customerOrders)) {
            throw new ShoppingServiceException("Customer orders map is null");
        }

        if (Objects.isNull(categoryName)) {
            throw new ShoppingServiceException("category name is null");
        }

        return customerOrders
                .entrySet()
                .stream()
                .filter(x -> x.getKey().getCategory().equals(categoryName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }*/


    public Map<Integer, String> getMostPopularCategoryForEveryAge() {


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


        if (Objects.isNull(list)) {
            throw new ShoppingServiceException("Required data list is null ");
        }
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


    public Map<String, BigDecimal> getAveragePriceForEveryCategory() {


        var groupedByCategory =
                preparedMapWithCategoriesAndProducts();


        return groupedByCategory
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> getAveragePrice(p.getValue())));

    }

    private BigDecimal getAveragePrice(List<Product> list) {

        if (Objects.isNull(list)) {
            throw new ShoppingServiceException("Required data list is null ");
        }

        BigDecimal productsTotalPriceForCategory =
                list
                        .stream()
                        .map(Product::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return productsTotalPriceForCategory.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.DOWN);
    }

    public Map<String, Product> getMostExpensiveProductForEveryCategory(){


        Map<String, List<Product>> collect = preparedMapWithCategoriesAndProducts();

        return getSortedPricesForEveryCategory(collect)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().get(0)));


    }

    public Map<String, Product> getTheCheapestProductForEveryCategory(){

        Map<String, List<Product>> collect = preparedMapWithCategoriesAndProducts();

        return getSortedPricesForEveryCategory(collect)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().get(p.getValue().size()-1)));

    }

    private Map<String, List<Product>> preparedMapWithCategoriesAndProducts() {
        var mapOfValues
                = shopping
                .values()
                .stream()
                .collect(Collectors.toMap(Map::keySet, Map::values));

        Set<Set<Product>> products = new HashSet<>(mapOfValues.keySet());

        Set<Product> allProducts = new HashSet<>();

        products.forEach(allProducts::addAll);


        return allProducts
                .stream()
                .collect(Collectors.groupingBy(Product::getCategory));
    }

    private Map<String, List<Product>> getSortedPricesForEveryCategory(Map<String, List<Product>> mapToSort) {

        if (Objects.isNull(mapToSort)) {
            throw new ShoppingServiceException("Required data map is null ");
        }

        return mapToSort
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> p.getValue()
                                .stream()
                                .sorted(Comparator.comparing(Product::getPrice,Comparator.reverseOrder()))
                                .collect(Collectors.toList())));
    }



    public Map<Customer, BigDecimal> getCustomersPayments(){

        return shopping
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> p.getValue()
                                .entrySet()
                                .stream()
                                .map(t -> t.getKey().getPrice().multiply(BigDecimal.valueOf(t.getValue())))
                                .reduce(p.getKey().getCash(), BigDecimal::subtract)));


    }





}