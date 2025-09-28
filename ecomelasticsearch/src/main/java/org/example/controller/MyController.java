package org.example.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.model.Product;
import org.example.service.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@RestController
@RequestMapping("/api/v1/products")
public class MyController {

    @Autowired
    private final MyService myService;

    // -----------------------------
    // Add Product
    // -----------------------------
    @PostMapping("/save_product")
    public Product addProduct(@RequestBody Product product) {
        return myService.addProduct(product);
    }

    // -----------------------------
    // Search Endpoints
    // -----------------------------

    // 1. Simple search
    @GetMapping("/search")
    public Map<String, Object> simpleSearch(@RequestParam String keyword) {
        return myService.simpleSearch(keyword);
    }

    // 2. Faceted search with multiple brands, colors, price range, and rating range
    @GetMapping("/faceted-search")
    public Map<String, Object> facetedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) List<String> color,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) Double ratingMin,
            @RequestParam(required = false) Double ratingMax
    ) {
        return myService.facetedSearch(keyword, category, brand, color, priceMin, priceMax, ratingMin, ratingMax);
    }

    // 3. Synonym search
    @GetMapping("/synonym-search")
    public Map<String, Object> synonymSearch(@RequestParam String keyword) {
        return myService.synonymSearch(keyword);
    }

    // 4. Enhanced search with pagination
    @GetMapping("/enhanced-search")
    public Map<String, Object> enhancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) Double ratingMin,
            @RequestParam(required = false) Double ratingMax,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return myService.enhancedSearch(keyword, category, brands, colors, priceMin, priceMax, ratingMin, ratingMax, limit, offset);
    }

}
