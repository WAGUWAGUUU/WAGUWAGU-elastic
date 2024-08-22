package com.example.elastic.service;

import com.example.elastic.domain.entity.ElasticStores;
import com.example.elastic.domain.request.ElasticInformation;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticStoresImpl implements ElasticStoresService {

    private final ElasticStoresRepository elasticStoresRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public ElasticStores saveStore(ElasticInformation elasticInformation) {
        ElasticStores elasticStores = ElasticStores.toEntity(elasticInformation);
        System.out.println("elastic 저장직전 값확인 : "+elasticStores.toString());
        return elasticStoresRepository.save(elasticStores);
    }

    @Override
    public Page<ElasticStores> findByKeyword(Pageable pageable, String keyword) {
        Query query = NativeQuery.builder()
                .withMinScore(0.3f)
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .query(keyword)
                                .fields("menuName", "menuIntroduction", "storeName", "storeIntroduction")
                                .analyzer("korean_mixed")
                        )
                )
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(pageable)
                .build();

        SearchHits<ElasticStores> searchHits = elasticsearchOperations.search(query, ElasticStores.class);
        List<ElasticStores> storesList = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(storesList, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<ElasticStores> findByStoreName(Pageable pageable, String storeName) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(
                                        m -> m.match(ma -> ma.field("storeName").query(storeName))
                                )
                        )
                )
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(pageable)
                .withMinScore(0.1f)
                .build();

        SearchHits<ElasticStores> searchHits = elasticsearchOperations.search(query, ElasticStores.class);

        List<ElasticStores> storesList = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(storesList, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<ElasticStores> findByMenuName(Pageable pageable, String menuName) {

        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(
                                        m -> m.match(ma -> ma.field("menuName").query(menuName))
                                )
                                .must(
                                        m -> m.match(ma -> ma.field("menuIntroduction").query(menuName))
                                )
                        )
                )
                .withSort(Sort.by(Sort.Order.desc("_score")))
                .withPageable(pageable)
                .withMinScore(0.1f)
                .build();

        SearchHits<ElasticStores> searchHits = elasticsearchOperations.search(query, ElasticStores.class);

        List<ElasticStores> storesList = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(storesList, pageable, searchHits.getTotalHits());
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
}

