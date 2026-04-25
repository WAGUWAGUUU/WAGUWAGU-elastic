package com.example.elastic.service;

import com.example.elastic.domain.entity.ElasticStores;
import com.example.elastic.domain.request.ElasticInformation;
import com.example.elastic.domain.response.ElasticStoreSearchResult;
import com.example.elastic.repository.ElasticStoresRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticStoresImpl implements ElasticStoresService {

    private final ElasticStoresRepository elasticStoresRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public ElasticStores saveStore(ElasticInformation elasticInformation) {
        ElasticStores elasticStores = ElasticStores.toEntity(elasticInformation);
        log.info("Saving store document. storeId={}, customerId={}", elasticStores.getStoreId(), elasticStores.getCustomerId());
        return elasticStoresRepository.save(elasticStores);
    }

    @Override
    public Page<ElasticStores> findByKeyword(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }

        Query query = NativeQuery.builder()
                .withMinScore(0.5f)
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(keyword.trim())
                                .fields("menuName", "menuIntroduction", "storeName", "storeIntroduction")
                                .analyzer("korean_mixed_analyzer")
                        )
                )
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(pageable)
                .build();

        return toPage(pageable, query);
    }

    @Override
    public Page<ElasticStores> findByStoreName(Pageable pageable, String storeName) {
        if (storeName == null || storeName.isBlank()) {
            return Page.empty(pageable);
        }

        Query query = NativeQuery.builder()
                .withMinScore(0.5f)
                .withQuery(q -> q
                        .match(m -> m
                                .field("storeName")
                                .query(storeName.trim())
                                .analyzer("korean_mixed_analyzer")
                        )
                )
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(pageable)
                .build();

        return toPage(pageable, query);
    }

    @Override
    public Page<ElasticStores> findByMenuName(Pageable pageable, String menuName) {
        if (menuName == null || menuName.isBlank()) {
            return Page.empty(pageable);
        }

        Query query = buildMenuRelevanceQuery(pageable, menuName);

        return toPage(pageable, query);
    }

    @Override
    public Page<ElasticStoreSearchResult> findMenuByRelevance(Pageable pageable, String menuName) {
        if (menuName == null || menuName.isBlank()) {
            return Page.empty(pageable);
        }

        Query query = buildMenuRelevanceQuery(pageable, menuName);
        SearchHits<ElasticStores> searchHits = elasticsearchOperations.search(query, ElasticStores.class);
        List<ElasticStoreSearchResult> results = searchHits.getSearchHits().stream()
                .map(ElasticStoreSearchResult::from)
                .toList();

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    @Override
    public void deleteByCustomerId(Long customerId) {
        Query deleteQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t.field("customerId").value(customerId))
                )
                .build();

        elasticsearchOperations.delete(deleteQuery, ElasticStores.class);
    }

    private Page<ElasticStores> toPage(Pageable pageable, Query query) {
        SearchHits<ElasticStores> searchHits = elasticsearchOperations.search(query, ElasticStores.class);
        List<ElasticStores> storesList = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .toList();

        return new PageImpl<>(storesList, pageable, searchHits.getTotalHits());
    }

    private Query buildMenuRelevanceQuery(Pageable pageable, String menuName) {
        String normalizedMenuName = menuName.trim();

        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(m -> m.match(ma -> ma
                                        .field("menuName")
                                        .query(normalizedMenuName)
                                        .analyzer("korean_mixed_analyzer")
                                        .boost(4.0f)
                                        .fuzziness("AUTO")
                                ))
                                .should(m -> m.match(ma -> ma
                                        .field("menuIntroduction")
                                        .query(normalizedMenuName)
                                        .analyzer("korean_mixed_analyzer")
                                        .boost(2.0f)
                                        .fuzziness("AUTO")
                                ))
                                .should(m -> m.match(ma -> ma
                                        .field("storeName")
                                        .query(normalizedMenuName)
                                        .analyzer("korean_mixed_analyzer")
                                        .boost(1.2f)
                                        .fuzziness("AUTO")
                                ))
                                .should(m -> m.match(ma -> ma
                                        .field("storeIntroduction")
                                        .query(normalizedMenuName)
                                        .analyzer("korean_mixed_analyzer")
                                        .boost(1.0f)
                                        .fuzziness("AUTO")
                                ))
                                .minimumShouldMatch("1")
                        )
                )
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(pageable)
                .withMinScore(0.3f)
                .build();
    }
}
