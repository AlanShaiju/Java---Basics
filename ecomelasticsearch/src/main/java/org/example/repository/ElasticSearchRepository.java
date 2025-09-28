package org.example.repository;

import org.example.model.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticSearchRepository extends ElasticsearchRepository<Product, Integer> {
}
