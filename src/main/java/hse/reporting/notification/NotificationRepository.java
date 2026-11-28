package hse.reporting.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** FR-42: newest first, for one Inspector only. */
    List<Notification> findByInspectorEmailOrderByCreatedAtDesc(String inspectorEmail);
}
