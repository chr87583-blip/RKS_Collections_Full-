package com.rks.collections.service;

import com.rks.collections.model.AppUser;
import com.rks.collections.model.ChatMessage;
import com.rks.collections.model.Product;
import com.rks.collections.repository.ChatMessageRepository;
import com.rks.collections.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiAssistantService {

    @Autowired private ChatMessageRepository chatMsgRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ProductService productService;

    // Optional Spring AI ChatClient — injected only if OpenAI key is configured
    @Autowired(required = false)
    private org.springframework.ai.chat.ChatClient chatClient;

    private static final String SYSTEM_PROMPT =
        "You are Riya, the AI fashion assistant for RKS Collections — a premium Indian ethnic wear boutique. " +
        "You are warm, knowledgeable, and enthusiastic about fashion. Help customers choose the perfect ethnic wear, " +
        "provide size guidance, styling tips, fabric care advice, and assist with WhatsApp ordering. " +
        "About RKS Collections: Premium handcrafted ethnic wear, kurta sets, lehengas. " +
        "Sizes: S, M, L, XL, XXL. Orders via WhatsApp. Fast delivery across India. " +
        "Keep responses concise (2-3 sentences), friendly, and helpful.";

    public String chat(String username, String userMessage, String productContext) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();

        // Build context string
        String fullPrompt = SYSTEM_PROMPT;
        if (productContext != null && !productContext.isEmpty()) {
            Optional<Product> prod = productService.getById(productContext);
            if (prod.isPresent()) {
                Product p = prod.get();
                fullPrompt += " Currently viewing: " + p.getName() +
                    " priced at ₹" + p.getPrice() +
                    ", sizes " + p.getSizesAvailable() +
                    ". Details: " + p.getPattern();
            }
        }

        // Get last 10 messages for history context
        List<ChatMessage> history = chatMsgRepo.findTop20ByUserOrderByCreatedAtAsc(user);

        String aiResponse;

        if (chatClient != null) {
            // Build full prompt with history
            StringBuilder sb = new StringBuilder(fullPrompt).append("\n\nConversation history:\n");
            for (ChatMessage msg : history) {
                sb.append(msg.getRole().equals("user") ? "Customer: " : "Riya: ")
                  .append(msg.getContent()).append("\n");
            }
            sb.append("Customer: ").append(userMessage).append("\nRiya:");

            try {
                aiResponse = chatClient.call(sb.toString());
            } catch (Exception e) {
                aiResponse = getFallbackResponse(userMessage);
            }
        } else {
            aiResponse = getFallbackResponse(userMessage);
        }

        // Save conversation to DB
        chatMsgRepo.save(new ChatMessage(user, "user", userMessage));
        chatMsgRepo.save(new ChatMessage(user, "assistant", aiResponse));

        return aiResponse;
    }

    private String getFallbackResponse(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("size") || lower.contains("fit"))
            return "Our kurtas are available in S, M, L, XL and XXL. For a relaxed fit, go one size up. Would you like to order via WhatsApp for personalised sizing help? 😊";
        if (lower.contains("price") || lower.contains("cost") || lower.contains("rate"))
            return "Our collection is priced between ₹999–₹3,999. Every piece is handcrafted with premium fabric. Tap the WhatsApp button to order! ✨";
        if (lower.contains("deliver") || lower.contains("ship"))
            return "We deliver across India! Standard delivery takes 3-5 working days. Place your order via WhatsApp and we'll confirm shipping details. 🚚";
        if (lower.contains("fabric") || lower.contains("material") || lower.contains("wash"))
            return "Our kurtas are made from premium cotton, chanderi silk, and rayon. Hand wash in cold water or dry clean for best results. 🌸";
        if (lower.contains("occasion") || lower.contains("wear") || lower.contains("festival"))
            return "RKS Collections has styles for every occasion — casual everyday wear, festive celebrations, and formal events. Browse our collection and ask me for specific recommendations! 🎉";
        return "Thank you for your question! For the best assistance, please WhatsApp us directly — our team will help you choose the perfect piece from RKS Collections. 💛 To enable AI, add your OpenAI key to application.properties.";
    }

    public String generateProductDescription(Product product) {
        if (chatClient == null) {
            return product.getPattern() + " — A stunning piece from RKS Collections, crafted with care and elegance.";
        }
        String prompt = "Write a compelling 2-sentence product description for an Indian ethnic wear boutique for: " +
            "Name: " + product.getName() + ", Details: " + product.getPattern() +
            ". Make it elegant and highlight the craftsmanship.";
        try {
            return chatClient.call(prompt);
        } catch (Exception e) {
            return product.getPattern();
        }
    }

    public List<ChatMessage> getChatHistory(String username) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        return chatMsgRepo.findByUserOrderByCreatedAtDesc(user);
    }

    public void clearHistory(String username) {
        AppUser user = userRepo.findByUsername(username).orElseThrow();
        chatMsgRepo.deleteAll(chatMsgRepo.findByUserOrderByCreatedAtDesc(user));
    }
}
