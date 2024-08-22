package com.example.elastic.domain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import com.example.elastic.domain.entity.ElasticStores;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@EnableElasticsearchRepositories
@EnableElasticsearchAuditing
@Configuration
public class ElasticConfig extends ReactiveElasticsearchConfiguration {

    @Value("${spring.elasticsearch.rest.uris}")
    private String elasticsearchUris;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", ""))
                .build();
    }

    @Bean
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(
                Arrays.asList(new ElasticStoresToMap(), new MapToElasticStores()));
    }

    @WritingConverter
    static class ElasticStoresToMap implements Converter<ElasticStores, Map<String, Object>> {

        @Override
        public Map<String, Object> convert(ElasticStores elasticStores) {
            LinkedHashMap<String, Object> target = new LinkedHashMap<>();
            target.put("storeId", elasticStores.getStoreId());
            target.put("customerId", elasticStores.getCustomerId());
            target.put("storeName", elasticStores.getStoreName());
            target.put("storeIntroduction", elasticStores.getStoreIntroduction());
            target.put("menuName", elasticStores.getMenuName());
            target.put("menuIntroduction", elasticStores.getMenuIntroduction());
            return target;
        }
    }

    @ReadingConverter
    static class MapToElasticStores implements Converter<Map<String, Object>, ElasticStores> {
        @Override
        public ElasticStores convert(Map<String, Object> source) {
            return ElasticStores.builder()
                    .storeId(source.containsKey("storeId") ? ((Number) source.get("storeId")).longValue() : null)
                    .customerId(source.containsKey("customerId") ? ((Number) source.get("customerId")).longValue() : null)
                    .storeName((String) source.get("storeName"))
                    .storeIntroduction((String) source.get("storeIntroduction"))
                    .menuName((String) source.get("menuName"))
                    .menuIntroduction((String) source.get("menuIntroduction"))
                    .build();
        }
    }
}
