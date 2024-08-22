package com.example.elastic.domain.request;

import com.example.elastic.domain.entity.ElasticStores;

public record ElasticInformation(

        Long storeId,
        Long customerId,
        String storeName,
        String storeIntroduction,
        String menuName,
        String menuIntroduction

) {

    public static ElasticStores toEntity(ElasticInformation elasticInformation){


        return ElasticStores.builder()
                .storeId(elasticInformation.storeId())
                .customerId(elasticInformation.customerId())
                .storeName(elasticInformation.storeName())
                .storeIntroduction(elasticInformation.storeIntroduction())
                .menuName(elasticInformation.menuName())
                .menuIntroduction(elasticInformation.menuIntroduction())
                .build();
    }

}
