package com.rks.collections.service;

import com.rks.collections.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ProductService {

    private List<Product> products = new ArrayList<>();

    private static final List<String> IMAGE_EXT = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final List<String> VIDEO_EXT = Arrays.asList(".mp4", ".webm", ".mov");

    @PostConstruct
    public void loadProducts() {
        products.clear();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // Get all .txt files inside products/ — works both in IDE and inside JAR
            Resource[] txtFiles = resolver.getResources("classpath:products/**/*.txt");

            if (txtFiles == null || txtFiles.length == 0) {
                System.out.println("⚠️ No product .txt files found in classpath:products/");
                return;
            }

            for (Resource txtFile : txtFiles) {
                Product p = parseTxtResource(txtFile, resolver);
                if (p != null) products.add(p);
            }

            // Sort by product name
            products.sort(Comparator.comparing(Product::getName));
            System.out.println("✅ Loaded " + products.size() + " products");

        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
    }

    private Product parseTxtResource(Resource txtFile, PathMatchingResourcePatternResolver resolver) {
        try {
            // Derive folder/product ID from filename: "sample-kurta.txt" → "sample-kurta"
            String filename = txtFile.getFilename(); // e.g. sample-kurta.txt
            if (filename == null) return null;
            String id = filename.replace(".txt", "");

            // Parse txt content
            String name = id, price = "0", sizes = "", pattern = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(txtFile.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String lower = line.toLowerCase();
                    if      (lower.startsWith("name:"))             name    = line.substring(5).trim();
                    else if (lower.startsWith("price:"))            price   = line.substring(6).trim();
                    else if (lower.startsWith("sizes available:"))  sizes   = line.substring(16).trim();
                    else if (lower.startsWith("pattern:"))          pattern = line.substring(8).trim();
                }
            }

            // Find all images and videos in same folder using classpath wildcard
            List<String> imageUrls = new ArrayList<>();
            String videoUrl = null;

            Resource[] allFiles = resolver.getResources("classpath:products/" + id + "/*");
            if (allFiles != null) {
                List<Resource> sorted = Arrays.asList(allFiles);
                sorted.sort(Comparator.comparing(r -> r.getFilename()));

                for (Resource f : sorted) {
                    String fname = f.getFilename();
                    if (fname == null || fname.endsWith(".txt")) continue;
                    String lower = fname.toLowerCase();
                    String url = "/api/products/" + id + "/media/" + fname;
                    if (IMAGE_EXT.stream().anyMatch(lower::endsWith))      imageUrls.add(url);
                    else if (VIDEO_EXT.stream().anyMatch(lower::endsWith)) videoUrl = url;
                }
            }

            return new Product(id, name, price, sizes, pattern, imageUrls, videoUrl, null);

        } catch (Exception e) {
            System.err.println("Error parsing product: " + e.getMessage());
            return null;
        }
    }

    // Serve media files — works inside JAR using InputStream
    public Resource getMediaResource(String productId, String filename) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource r = resolver.getResource("classpath:products/" + productId + "/" + filename);
            if (r.exists()) return r;
        } catch (Exception ignored) {}
        return null;
    }

    public List<Product> getAllProducts() { return products; }

    public Optional<Product> getById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public void reload() { loadProducts(); }
}
