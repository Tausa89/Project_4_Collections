package com.collections.ui.data;

import com.collections.ui.exception.UserDataException;

import java.util.Scanner;

public final class UserDataService {


    private UserDataService() {
    }


    private static final Scanner scanner = new Scanner(System.in);



    public static int getInt(String message){

        System.out.println(message);
        String value = scanner.nextLine();

        if(!value.matches("[0-9]*")){
            throw new UserDataException("Wrong input");
        }

        return Integer.parseInt(value);

    }



    public static String getString(String message){

        System.out.println(message);
        return scanner.nextLine();
    }
}
