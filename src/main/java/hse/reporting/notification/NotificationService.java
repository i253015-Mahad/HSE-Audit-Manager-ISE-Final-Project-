package hse.reporting.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ownership boundary for rejection notifications (FR-42).
 *
 * <p>The report module never touches this package: the application layer calls
 * {@link #record} inside the same transaction that rejects the report, so the
 * two writes commit or roll back together.
 */
@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /** FR-42: notify the rejected report's Inspector, and nobody else. */
    @Transactional
    public void record(Long reportId, String inspectorEmail, String reason) {
        notifications.save(new Notification(reportId, inspectorEmail, reason, LocalDateTime.now()));
    }

    /** FR-42 / AC-12: the acting Inspector's own notifications, newest first. */
    @Transactional(readOnly = true)
    public List<Notification> forInspector(String inspectorEmail) {
        return notifications.findByInspectorEmailOrderByCreatedAtDesc(inspectorEmail);
    }
}
