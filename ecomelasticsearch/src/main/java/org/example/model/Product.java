package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "products")
public class Product {

    @Id
    private int sku;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String brand;

    @Field(type = FieldType.Text)
    private String category;

    @Field(type = FieldType.Text)
    private String subcategory;

    @Field(type = FieldType.Text)
    private String price;

    @Field(type = FieldType.Text)
    private String rating;

    @Field(type = FieldType.Text)
    private String stock;

    @Field(type = FieldType.Text)
    private String color;

    @Field(type = FieldType.Keyword)
    private List<String> colorVariants;

    @Field(type = FieldType.Keyword)
    private List<String> materials;

    @Field(type = FieldType.Text)
    private List<String> synonyms;

}
