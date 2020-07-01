package com.collections.ui;

import com.collections.service.ShoppingService;
import com.collections.ui.menu.ShoppingServiceMenu;

public class App {

    public static void main(String[] args) {

        final String FILE_NAME = "dataFileOne.json";


        ShoppingService shoppingService = new ShoppingService(FILE_NAME);

        ShoppingServiceMenu shoppingServiceMenu = new ShoppingServiceMenu(shoppingService);

        shoppingServiceMenu.mainMenu();








    }
}
