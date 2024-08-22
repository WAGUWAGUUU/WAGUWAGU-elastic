package com.example.elastic.domain.entity;

import com.example.elastic.domain.request.ElasticInformation;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Slf4j
@Getter
@Builder
@ToString
@Setting(settingPath = "/static/elastic/elastic-settings.json")
@Mapping(mappingPath = "/static/elastic/stores-mappings.json")
@Document(indexName = "stores_index")
public class ElasticStores {

    @Id
    @Field(name = "storeId", type = FieldType.Long)
    private Long storeId;

    @Field(name = "customerId", type = FieldType.Long)
    private Long customerId;

    @Field(name = "storeName", type = FieldType.Text)
    private String storeName;

    @Field(name = "storeIntroduction", type = FieldType.Text)
    private String storeIntroduction;

    @Field(name = "menuName", type = FieldType.Text)
    private String menuName;

    @Field(name = "menuIntroduction", type = FieldType.Text)
    private String menuIntroduction;

    public static ElasticStores toEntity(ElasticInformation elasticInformation) {
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
