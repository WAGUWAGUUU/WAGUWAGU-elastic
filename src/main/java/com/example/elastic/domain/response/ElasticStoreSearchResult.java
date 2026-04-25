package com.example.elastic.domain.response;

import com.example.elastic.domain.entity.ElasticStores;
import org.springframework.data.elasticsearch.core.SearchHit;

public record ElasticStoreSearchResult(
        Long storeId,
        Long customerId,
        String storeName,
        String storeIntroduction,
        String menuName,
        String menuIntroduction,
        float score
) {

    public static ElasticStoreSearchResult from(SearchHit<ElasticStores> hit) {
        ElasticStores store = hit.getContent();
        return new ElasticStoreSearchResult(
                store.getStoreId(),
                store.getCustomerId(),
                store.getStoreName(),
                store.getStoreIntroduction(),
                store.getMenuName(),
                store.getMenuIntroduction(),
                hit.getScore()
        );
    }
}
