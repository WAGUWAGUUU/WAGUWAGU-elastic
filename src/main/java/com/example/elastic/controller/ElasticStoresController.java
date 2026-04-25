package com.example.elastic.controller;

import com.example.elastic.domain.entity.ElasticStores;
import com.example.elastic.domain.request.ElasticInformation;
import com.example.elastic.domain.response.ElasticStoreSearchResult;
import com.example.elastic.service.ElasticAnalyzeService;
import com.example.elastic.service.ElasticStoresService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/elastic")
public class ElasticStoresController {

    private final ElasticStoresService elasticStoresService;
    private final ElasticAnalyzeService elasticAnalyzeService;

    @PostMapping("/")
    public ElasticStores createStore(@RequestBody ElasticInformation elasticInformation) {
        return elasticStoresService.saveStore(elasticInformation);
    }

    @PostMapping("/analyzeEntity")
    public Mono<List<Map<String, Object>>> analyzeEntity(@RequestBody ElasticInformation elasticInformation ) {
        ElasticStores elasticStores = ElasticStores.toEntity(elasticInformation);
        return elasticAnalyzeService.analyzeEntity(elasticStores);
    }

    @GetMapping("/search/store")
    public Page<ElasticStores> findByStoreName(@RequestParam("storeName")String storeName,@PageableDefault Pageable pageable){
        return elasticStoresService.findByStoreName(pageable,storeName);
    }

    @GetMapping("/search/menu")
    public Page<ElasticStores> findByMenuName(@RequestParam("menuName")String menuName,@PageableDefault Pageable pageable){
        return elasticStoresService.findByMenuName(pageable,menuName);
    }

    @GetMapping("/search/menu/relevance")
    public Page<ElasticStoreSearchResult> findMenuByRelevance(@RequestParam("menuName") String menuName,
                                                              @PageableDefault Pageable pageable) {
        return elasticStoresService.findMenuByRelevance(pageable, menuName);
    }

    @GetMapping("/")
    public Page<ElasticStores> findByKeyword(@RequestParam("keyword") String keyword, @PageableDefault Pageable pageable){
        return elasticStoresService.findByKeyword(pageable, keyword);
    }

    @DeleteMapping("/{customerId}")
    public void deleteByCustomerId(@PathVariable Long customerId) {
        elasticStoresService.deleteByCustomerId(customerId);
    }

}
