package org.example.service;

import co.elastic.clients.json.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Product;
import org.example.repository.ElasticSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Service
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MyService {

    @Autowired
    private final ElasticSearchRepository esr;
    @Autowired
    private final ElasticsearchOperations elasticOps;

    // Simple Search
    public Map<String, Object> simpleSearch(String keyword) {
        Criteria criteria = new Criteria("name").matches(keyword);
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<Product> hits = elasticOps.search(query, Product.class);
        return buildResponse(hits);
    }

    // Faceted Search with multiple brands, colors, price and rating ranges
    public Map<String, Object> facetedSearch(String keyword,
                                             String category,
                                             List<String> brands,
                                             List<String> colors,
                                             Double priceMin,
                                             Double priceMax,
                                             Double ratingMin,
                                             Double ratingMax) {

        Criteria criteria = new Criteria();

        if (keyword != null && !keyword.isEmpty()) {
            criteria = criteria.and(new Criteria("name").matches(keyword));
        }

        if (category != null && !category.isEmpty()) {
            criteria = criteria.and(new Criteria("category").is(category));
        }

        if (brands != null && !brands.isEmpty()) {
            Criteria brandCriteria = new Criteria();
            for (String b : brands) {
                brandCriteria = brandCriteria.or(new Criteria("brand").is(b));
            }
            criteria = criteria.and(brandCriteria);
        }

        if (colors != null && !colors.isEmpty()) {
            Criteria colorCriteria = new Criteria();
            for (String c : colors) {
                colorCriteria = colorCriteria.or(new Criteria("color").is(c));
            }
            criteria = criteria.and(colorCriteria);
        }

        if (priceMin != null || priceMax != null) {
            Criteria priceCriteria = new Criteria("price");
            if (priceMin != null) priceCriteria = priceCriteria.greaterThanEqual(priceMin);
            if (priceMax != null) priceCriteria = priceCriteria.lessThanEqual(priceMax);
            criteria = criteria.and(priceCriteria);
        }

        if (ratingMin != null || ratingMax != null) {
            Criteria ratingCriteria = new Criteria("rating");
            if (ratingMin != null) ratingCriteria = ratingCriteria.greaterThanEqual(ratingMin);
            if (ratingMax != null) ratingCriteria = ratingCriteria.lessThanEqual(ratingMax);
            criteria = criteria.and(ratingCriteria);
        }

        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<Product> hits = elasticOps.search(query, Product.class);

        return buildResponse(hits);
    }

    // Synonym Search
    public Map<String, Object> synonymSearch(String keyword) {
        Criteria criteria = new Criteria("name").matches(keyword)
                .or(new Criteria("synonyms").matches(keyword));

        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<Product> hits = elasticOps.search(query, Product.class);

        return buildResponse(hits);
    }

    // Enhanced Search
    // Enhanced Search (best search)
    public Map<String, Object> enhancedSearch(String keyword, String category, List<String> brands, List<String> colors,
                                              Double priceMin, Double priceMax, Double ratingMin, Double ratingMax,
                                              int limit, int offset) {

        Criteria criteria = new Criteria();

        if (keyword != null && !keyword.isEmpty()) {
            criteria = criteria.and(new Criteria("name").matches(keyword)
                    .or(new Criteria("description").matches(keyword))
                    .or(new Criteria("synonyms").matches(keyword)));
        }

        if (category != null && !category.isEmpty()) criteria = criteria.and(new Criteria("category").is(category));

        if (brands != null && !brands.isEmpty()) criteria = criteria.and(new Criteria("brand").in(brands));

        if (colors != null && !colors.isEmpty()) criteria = criteria.and(new Criteria("color").in(colors));

        if (priceMin != null || priceMax != null) {
            Criteria price = new Criteria("price");
            if (priceMin != null) price = price.greaterThanEqual(priceMin);
            if (priceMax != null) price = price.lessThanEqual(priceMax);
            criteria = criteria.and(price);
        }

        if (ratingMin != null || ratingMax != null) {
            Criteria rating = new Criteria("rating");
            if (ratingMin != null) rating = rating.greaterThanEqual(ratingMin);
            if (ratingMax != null) rating = rating.lessThanEqual(ratingMax);
            criteria = criteria.and(rating);
        }

        CriteriaQuery query = new CriteriaQuery(criteria).setPageable(PageRequest.of(offset, limit));
        SearchHits<Product> hits = elasticOps.search(query, Product.class);

        return buildResponse(hits);
    }


    // Add Product
    public Product addProduct(Product product) {
        return esr.save(product);
    }

    // Response Builder
    private Map<String, Object> buildResponse(SearchHits<Product> hits) {
        Map<String, Object> response = new HashMap<>();
        response.put("total", hits.getTotalHits());
        response.put("products", hits.stream().map(SearchHit::getContent).toList());
        return response;
    }
}
