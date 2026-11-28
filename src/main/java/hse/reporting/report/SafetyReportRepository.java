package hse.reporting.report;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {

    /** FR-06: observation date + location + type and identical brief description, excluding deleted reports. */
    @Query("""
        select count(v)
        from SafetyReport r join r.violations v
        where r.deleted = false
          and (:excludedReportId is null or r.id <> :excludedReportId)
          and r.observationDate = :observationDate
          and r.location = :location
          and r.observationType = :observationType
          and v.briefDescription = :briefDescription
        """)
    long countDuplicates(@Param("excludedReportId") Long excludedReportId,
                         @Param("observationDate") LocalDate observationDate,
                         @Param("location") String location,
                         @Param("observationType") IncidentType observationType,
                         @Param("briefDescription") String briefDescription);

    // ------------------------------------------------------------------
    // Role-scoped reads (FR-14, FR-15, FR-16). Deleted reports are excluded
    // everywhere (FR-28). Violations are fetched eagerly because every list
    // applies the severity filter and every detail page renders them — a lazy
    // collection here would mean N+1 queries and a lazy-init failure outside
    // an open session.
    // ------------------------------------------------------------------

    /** FR-14 / AC-14: an Inspector's own reports only. */
    @EntityGraph(attributePaths = "violations")
    List<SafetyReport> findByInspectorEmailAndDeletedFalseOrderBySubmittedAtDesc(String inspectorEmail);

    @EntityGraph(attributePaths = "violations")
    Optional<SafetyReport> findByIdAndInspectorEmailAndDeletedFalse(Long id, String inspectorEmail);

    /** FR-15 / AC-15: the reports of the projects a Manager currently handles. */
    @EntityGraph(attributePaths = "violations")
    List<SafetyReport> findByProjectIdInAndDeletedFalseOrderBySubmittedAtDesc(Collection<Long> projectIds);

    @EntityGraph(attributePaths = "violations")
    Optional<SafetyReport> findByIdAndProjectIdInAndDeletedFalse(Long id, Collection<Long> projectIds);

    /** FR-16 / AC-16: approved non-deleted reports — Pending and Rejected are never returned (FR-12). */
    @EntityGraph(attributePaths = "violations")
    List<SafetyReport> findBySubmissionStatusAndDeletedFalseOrderBySubmittedAtDesc(SubmissionStatus submissionStatus);

    @EntityGraph(attributePaths = "violations")
    Optional<SafetyReport> findByIdAndSubmissionStatusAndDeletedFalse(Long id, SubmissionStatus submissionStatus);
}
