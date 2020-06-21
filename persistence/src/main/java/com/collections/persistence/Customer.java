package com.collections.persistence;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Customer {

    private String name;
    private String surname;
    private Integer age;
    private BigDecimal cash;


}