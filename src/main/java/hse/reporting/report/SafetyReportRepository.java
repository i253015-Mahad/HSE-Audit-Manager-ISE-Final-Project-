package hse.reporting.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

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
}
