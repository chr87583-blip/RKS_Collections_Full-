package com.rks.collections.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String price;
    private String sizesAvailable;
    private String pattern;
    private List<String> imageUrls;
    private String videoUrl;
    private String aiDescription; // AI-generated description
}
