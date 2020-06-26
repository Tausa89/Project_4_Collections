package com.collections.ui;

import com.collections.service.ShoppingService;

public class App {

    public static void main(String[] args) {

        final String FILE_NAME = "dataFileOne.json";


        ShoppingService shoppingService = new ShoppingService(FILE_NAME);

//        shoppingService.getShopping().forEach((k,v) -> System.out.println(k + "----->" + v));


//        var result = shoppingService.getCustomerWhichPaidMostInGivenCategory("Elektronika");
        shoppingService.getAveragePriceForEveryCategory();






    }
}
