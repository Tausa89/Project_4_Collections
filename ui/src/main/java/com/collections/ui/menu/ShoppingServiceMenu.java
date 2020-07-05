package com.collections.ui.menu;


import com.collections.service.ShoppingService;
import com.collections.ui.data.UserDataService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ShoppingServiceMenu {

    private final ShoppingService shoppingService;


    public void mainMenu() {
        shoppingService.getShopping().forEach((k,v) -> System.out.println(k + "---->" + v));


        while (true) {
            try {
                var option = choseOption();
                switch (option) {
                    case 1 -> optionOne();
                    case 2 -> optionTwo();
                    case 3 -> optionThree();
                    case 4 -> optionFour();
                    case 5 -> optionFive();
                    case 6 -> optionSix();
                    case 7 -> {
                        System.out.println("Thx!");
                        return;
                    }
                    default -> System.out.println("Wrong option chose from 1 to 7");
                }
            } catch (Exception e) {
                System.out.println("----------------> EXCEPTION <---------------");
                System.out.println(e.getMessage());
            }

        }


    }



    private int choseOption() {

        System.out.println("1. Get customer who paid the most for products");
        System.out.println("2. Get customer who paid the most for products in given category");
        System.out.println("3. Get most popular products categories depends on customer age");
        System.out.println("4. Get average products price for every category");
        System.out.println("5. Get the most expensive and cheapest product for every category");
        System.out.println("6. Get customers and their eventual debts");
        System.out.println("7. Exit");


        return UserDataService.getInt("Chose option");
    }


    private void optionOne() {

        var customer = shoppingService.getCustomerWhoPaidTheMost();

        System.out.println("Customer who paid the most is " + customer.getName() + " "
                + customer.getSurname());
    }


    private void optionTwo() {

        var categoryName = UserDataService.getString("Pleas provide category name");

        var customer = shoppingService.getCustomerWhichPaidMostInGivenCategory(categoryName);

        System.out.println("Customer who paid the most in category " + categoryName.toUpperCase() + " is "
                + customer.getName() + " " + customer.getSurname());
    }

    private void optionThree() {


        var ageWithCategories = shoppingService.getMostPopularCategoryForEveryAge();

        ageWithCategories.forEach((k, v) -> System.out.println("For age " + k + " most popular category is " + v));
    }


    private void optionFour() {

        var averagePriceWithCategories = shoppingService.getAveragePriceForEveryCategory();

        averagePriceWithCategories.forEach((k,v) -> System.out.println("For category " + k + " average price is " + v));
    }

    private void optionFive() {

        var theMostExpensiveProducts = shoppingService.getMostExpensiveProductForEveryCategory();
        var theCheapestProducts = shoppingService.getTheCheapestProductForEveryCategory();

        theMostExpensiveProducts.forEach((k,v) -> System.out.println("The most expensive product in category "
                                + k + " is " + v.getName() + ", price = " + v.getPrice()));

        theCheapestProducts.forEach((k,v) -> System.out.println("The cheapest product in category "
                + k + " is " + v.getName() + ", price = " + v.getPrice()));

    }

    private void optionSix() {

        var customersDebts = shoppingService.getCustomersPayments();

        customersDebts.forEach((k,v) -> {
            if (v.compareTo(BigDecimal.ZERO) > 0){
                System.out.println("Customer " + k.getName() + " " + k.getSurname() + " have no debts");
            }
            else
                System.out.println("Customer " + k.getName() + " " + k.getSurname() + " " +
                        "still have uncompleted payment his debt equals " + v.negate().toString());

        });
    }



}
