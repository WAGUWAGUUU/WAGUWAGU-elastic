package com.example.elastic.controller;

import com.example.elastic.domain.entity.ElasticStores;
import com.example.elastic.domain.request.ElasticInformation;
import com.example.elastic.service.ElasticAnalyzeService;
import com.example.elastic.service.ElasticStoresService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v2/elastic")
public class ElasticStoresController {

    private final ElasticStoresService elasticStoresService;
    private final ElasticAnalyzeService elasticAnalyzeService;

    // Endpoint to create a new store
    @PostMapping("/")
    public ElasticStores createStore(@RequestBody ElasticInformation elasticInformation) {
        ElasticStores elasticStores = elasticStoresService.saveStore(elasticInformation);
        return elasticStores;
    }

    // Endpoint to analyze entity
    @PostMapping("/analyzeEntity")
    public Mono<List<Map<String, Object>>> analyzeEntity(@RequestBody ElasticInformation elasticInformation ) {
        ElasticStores elasticStores = ElasticStores.toEntity(elasticInformation);
        return elasticAnalyzeService.analyzeEntity(elasticStores);
    }

    // Endpoint to find stores by store name
    @GetMapping("/search/store")
    public Page<ElasticStores> findByStoreName(@RequestParam("storeName")String storeName,@PageableDefault Pageable pageable){
        return elasticStoresService.findByStoreName(pageable,storeName);
    }

    // Endpoint to find stores by menu name
    @GetMapping("/search/menu")
    public Page<ElasticStores> findByMenuName(@RequestParam("menuName")String menuName,@PageableDefault Pageable pageable){
        return elasticStoresService.findByMenuName(pageable,menuName);
    }

    // Endpoint to find stores by keyword
    @GetMapping("/")
    public Page<ElasticStores> findByKeyword(@RequestParam("keyword") String keyword, @PageableDefault Pageable pageable){
        Page<ElasticStores> byKeyword = elasticStoresService.findByKeyword(pageable, keyword);
        return byKeyword;
    }

    // Endpoint to delete a store by customer ID
    @DeleteMapping("/{id}")
    public void deleteByCustomerId(@PathVariable Long id) {
        elasticStoresService.deleteByCustomerId(id);
    }

}