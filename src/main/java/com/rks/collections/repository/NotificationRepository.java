package com.rks.collections.repository;

import com.rks.collections.model.AppUser;
import com.rks.collections.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get notifications for a specific user OR broadcast notifications
    @Query("SELECT n FROM Notification n WHERE n.isBroadcast = true OR n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findForUser(@Param("user") AppUser user);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.isBroadcast = true OR n.user = :user) AND n.isRead = false")
    long countUnreadForUser(@Param("user") AppUser user);

    List<Notification> findByIsBroadcastTrueOrderByCreatedAtDesc();

    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findByUser(@Param("user") AppUser user);
}
