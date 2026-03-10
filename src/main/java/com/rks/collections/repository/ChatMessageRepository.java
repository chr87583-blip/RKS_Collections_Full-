package com.rks.collections.repository;

import com.rks.collections.model.AppUser;
import com.rks.collections.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByCreatedAtDesc(AppUser user);
    List<ChatMessage> findTop20ByUserOrderByCreatedAtAsc(AppUser user);
}
