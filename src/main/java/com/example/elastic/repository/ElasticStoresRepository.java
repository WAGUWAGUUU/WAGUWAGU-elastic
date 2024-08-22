package com.example.elastic.repository;

import com.example.elastic.domain.entity.ElasticStores;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticStoresRepository extends ElasticsearchRepository<ElasticStores, Long> {

}
