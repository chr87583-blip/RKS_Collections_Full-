package com.rks.collections.service;

import com.rks.collections.model.Product;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
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
            Resource dir = resolver.getResource("classpath:products/");
            if (!dir.exists()) return;

            File[] folders = dir.getFile().listFiles(File::isDirectory);
            if (folders == null) return;
            Arrays.sort(folders);

            for (File folder : folders) {
                Product p = parseFolder(folder);
                if (p != null) products.add(p);
            }
            System.out.println("✅ Loaded " + products.size() + " products");
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
    }

    private Product parseFolder(File folder) {
        String id = folder.getName();
        File txtFile = new File(folder, id + ".txt");

        String name = id, price = "0", sizes = "", pattern = "";

        if (txtFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String lower = line.toLowerCase();
                    if (lower.startsWith("name:"))            name    = line.substring(5).trim();
                    else if (lower.startsWith("price:"))      price   = line.substring(6).trim();
                    else if (lower.startsWith("sizes available:")) sizes = line.substring(16).trim();
                    else if (lower.startsWith("pattern:"))    pattern = line.substring(8).trim();
                }
            } catch (IOException e) { System.err.println("Error reading " + txtFile); }
        }

        List<String> imageUrls = new ArrayList<>();
        String videoUrl = null;

        File[] files = folder.listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                String fname = f.getName().toLowerCase();
                String url = "/api/products/" + id + "/media/" + f.getName();
                if (IMAGE_EXT.stream().anyMatch(fname::endsWith)) imageUrls.add(url);
                else if (VIDEO_EXT.stream().anyMatch(fname::endsWith)) videoUrl = url;
            }
        }
        Collections.sort(imageUrls);

        return new Product(id, name, price, sizes, pattern, imageUrls, videoUrl, null);
    }

    public List<Product> getAllProducts() { return products; }

    public Optional<Product> getById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public void reload() { loadProducts(); }
}
