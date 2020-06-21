package com.collections.persistence.converter;

import com.collections.persistence.Order;
import com.collections.persistence.converter.generic.JsonConverter;

import java.util.List;

public class JsonConverterOrders extends JsonConverter<List<Order>> {

    public JsonConverterOrders(String jsonFilename) {
        super(jsonFilename);
    }
}
