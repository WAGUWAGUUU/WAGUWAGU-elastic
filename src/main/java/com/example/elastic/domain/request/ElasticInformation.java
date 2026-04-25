package com.example.elastic.domain.request;

public record ElasticInformation(

        Long storeId,
        Long customerId,
        String storeName,
        String storeIntroduction,
        String menuName,
        String menuIntroduction

) {}
