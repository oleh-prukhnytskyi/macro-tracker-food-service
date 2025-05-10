package com.olehprukhnytskyi.macrotrackerfoodservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "foods")
public class Food {

    @MongoId
    @Field(name = "_id")
    @JsonProperty("_id")
    private String id;

    @Indexed(unique = true)
    private String code;

    @Field(name = "product_name")
    @JsonProperty("product_name")
    private String productName;

    @Field(name = "generic_name")
    @JsonProperty("generic_name")
    private String genericName;

    @Field(name = "image_url")
    @JsonProperty("image_url")
    private String imageUrl;

    private String brands;

    @Field(name = "_keywords")
    @JsonProperty("_keywords")
    private List<String> keywords;

    private Nutriments nutriments;

    @Field(name = "data_hash")
    @JsonProperty("data_hash")
    @Indexed(unique = true)
    private String dataHash;
}
