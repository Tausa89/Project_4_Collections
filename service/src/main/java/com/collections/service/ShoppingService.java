package com.collections.service;

import com.collections.persistence.Customer;
import com.collections.persistence.Order;
import com.collections.persistence.Product;
import com.collections.persistence.converter.JsonConverterOrders;
import com.collections.service.exception.ShoppingServiceException;
import org.eclipse.collections.impl.collector.Collectors2;

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

    public Map<Customer, Map<Product, Long>> getShopping() {
        return shopping;
    }

    private Map<Customer, Map<Product, Long>> init(String... filenames) {


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



    public Customer getCustomerWhoPaidTheMost() {

        return shopping
                .entrySet()
                .stream()
                .max(Comparator.comparing(e -> totalPurchaseByCustomer(e.getValue())))
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
                .max(Comparator.comparing(e -> totalPurchaseByCategory(e.getValue(), categoryName)))
                .orElseThrow(() -> new ShoppingServiceException("....."))
                .getKey();



    }

    private BigDecimal totalPurchaseByCategory(Map<Product, Long> customerOrders, String category) {
        return customerOrders.entrySet()
                .stream()
                .flatMap(e -> Collections.nCopies(e.getValue().intValue(), e.getKey()).stream())
                .filter(p -> p.getCategory().equals(category))
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    public Map<Integer, String> getMostPopularCategoryForEveryAge() {


        return shopping
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(p -> p.getKey().getAge(),
                        Collectors.collectingAndThen(Collectors
                                .flatMapping(g-> g.getValue().keySet().stream(),Collectors.toList()),
                                products -> products.stream().map(Product::getCategory).collect(Collectors.toList())
                                        )))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> p.getValue()
                                .stream()
                                .collect(Collectors.groupingBy(Function.identity(),
                                        Collectors.counting()))
                                .entrySet()
                                .stream()
                                .max(Map.Entry.comparingByValue())
                                .orElseThrow(() ->  new ShoppingServiceException("Error"))
                                .getKey()));


//        return shopping
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        t -> t.getValue()
//                                .keySet()
//                                .stream()
//                                .map(Product::getCategory)
//                                .collect(Collectors.toList())))
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        p -> getMostCommonElementFromList(p.getValue())))
//                .entrySet()
//                .stream()
//                .collect(Collectors.groupingBy(p -> p.getKey().getAge(),
//                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        p -> getMostCommonElementFromList(p.getValue())));

    }

//    private String getMostCommonElementFromList(List<String> list) {
//
//
//        return list
//                .stream()
//                .collect(Collectors
//                        .groupingBy(Function.identity(),
//                                Collectors.counting()))
//                .entrySet()
//                .stream()
//                .max(Map.Entry.comparingByValue())
//                .orElseThrow()
//                .getKey();
//    }


    public Map<String, BigDecimal> getAveragePriceForEveryCategory() {


        return shopping
                .values()
                .stream()
                .collect(Collectors.flatMapping(p -> p.keySet().stream(),Collectors.toList()))
                .stream().collect(Collectors.groupingBy(Product::getCategory))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                        prod -> prod
                                .getValue()
                                .stream()
                                .collect(Collectors2.summarizingBigDecimal(Product::getPrice)).getAverage()));


//        var groupedByCategory =
//                preparedMapWithCategoriesAndProducts();
//
//
//        return groupedByCategory
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        p -> getAveragePrice(p.getValue())));

    }

//    private BigDecimal getAveragePrice(List<Product> list) {
//
//        if (Objects.isNull(list)) {
//            throw new ShoppingServiceException("Required data list is null ");
//        }
//
//        BigDecimal productsTotalPriceForCategory =
//                list
//                        .stream()
//                        .map(Product::getPrice)
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        return productsTotalPriceForCategory.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.DOWN);
//    }

    public Map<String, Product> getMostExpensiveProductForEveryCategory(){

       return shopping
               .values()
               .stream()
               .collect(Collectors.flatMapping(p -> p.keySet().stream(),Collectors.toList()))
               .stream().collect(Collectors.groupingBy(Product::getCategory))
               .entrySet()
               .stream()
               .collect(Collectors.toMap(Map.Entry::getKey,
                       p -> p.getValue()
                               .stream()
                               .max(Comparator.comparing(Product::getPrice))
                               .orElseThrow(() -> new ShoppingServiceException("Error"))));



//        Map<String, List<Product>> collect = preparedMapWithCategoriesAndProducts();
//
//        return getSortedPricesForEveryCategory(collect)
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().get(0)));


    }

    public Map<String, Product> getTheCheapestProductForEveryCategory(){


        return shopping
                .values()
                .stream()
                .collect(Collectors.flatMapping(p -> p.keySet().stream(),Collectors.toList()))
                .stream().collect(Collectors.groupingBy(Product::getCategory))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        p -> p.getValue()
                                .stream()
                                .min(Comparator.comparing(Product::getPrice))
                                .orElseThrow(() -> new ShoppingServiceException("Error"))));

//        Map<String, List<Product>> collect = preparedMapWithCategoriesAndProducts();
//
//        return getSortedPricesForEveryCategory(collect)
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().get(p.getValue().size()-1)));

    }

//    private Map<String, List<Product>> preparedMapWithCategoriesAndProducts() {
//        var mapOfValues
//                = shopping
//                .values()
//                .stream()
//                .collect(Collectors.toMap(Map::keySet, Map::values));
//
//        Set<Set<Product>> products = new HashSet<>(mapOfValues.keySet());
//
//        Set<Product> allProducts = new HashSet<>();
//
//        products.forEach(allProducts::addAll);
//
//
//        return allProducts
//                .stream()
//                .collect(Collectors.groupingBy(Product::getCategory));
//    }

//    private Map<String, List<Product>> getSortedPricesForEveryCategory(Map<String, List<Product>> mapToSort) {
//
//        if (Objects.isNull(mapToSort)) {
//            throw new ShoppingServiceException("Required data map is null ");
//        }
//
//        return mapToSort
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey,
//                        p -> p.getValue()
//                                .stream()
//                                .sorted(Comparator.comparing(Product::getPrice,Comparator.reverseOrder()))
//                                .collect(Collectors.toList())));
//    }



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