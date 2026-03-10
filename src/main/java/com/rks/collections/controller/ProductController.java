package com.rks.collections.controller;

import com.rks.collections.model.Product;
import com.rks.collections.service.AiAssistantService;
import com.rks.collections.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private AiAssistantService aiService;

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable String id) {
        return productService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /** Serve images/videos — reads from classpath inside JAR via InputStream */
    @GetMapping("/{id}/media/{filename}")
    public ResponseEntity<byte[]> getMedia(@PathVariable String id,
                                            @PathVariable String filename) throws IOException {
        Resource resource = productService.getMediaResource(id, filename);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String mime = URLConnection.guessContentTypeFromName(filename);
        if (mime == null) mime = "application/octet-stream";

        byte[] data = resource.getInputStream().readAllBytes();

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mime))
            .contentLength(data.length)
            .body(data);
    }

    @GetMapping("/{id}/ai-description")
    public ResponseEntity<?> getAiDescription(@PathVariable String id) {
        Optional<Product> p = productService.getById(id);
        if (p.isEmpty()) return ResponseEntity.notFound().build();
        String desc = aiService.generateProductDescription(p.get());
        return ResponseEntity.ok(Map.of("description", desc));
    }

    @PostMapping("/reload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reload() {
        productService.reload();
        return ResponseEntity.ok(Map.of(
            "message", "Reloaded",
            "count", productService.getAllProducts().size()
        ));
    }
}
