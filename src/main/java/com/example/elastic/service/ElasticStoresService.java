package com.example.elastic.service;

import com.example.elastic.domain.entity.ElasticStores;
import com.example.elastic.domain.request.ElasticInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ElasticStoresService {

    ElasticStores saveStore(ElasticInformation elasticInformation);

    Page<ElasticStores> findByKeyword(Pageable pageable,String keyword);

    Page<ElasticStores> findByStoreName(Pageable pageable,String storeName);

    Page<ElasticStores> findByMenuName(Pageable pageable,String menuName);

    void deleteByCustomerId(Long customerId);

}
