package hse.reporting.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {

    /** FR-06: incident date + location + type and identical violation description, excluding deleted reports. */
    @Query("""
        select count(v)
        from SafetyReport r join r.violations v
        where r.deleted = false
          and (:excludedReportId is null or r.id <> :excludedReportId)
          and r.incidentDate = :incidentDate
          and r.location = :location
          and r.incidentType = :incidentType
          and v.description = :description
        """)
    long countDuplicates(@Param("excludedReportId") Long excludedReportId,
                            @Param("incidentDate") LocalDate incidentDate,
                            @Param("location") String location,
                            @Param("incidentType") IncidentType incidentType,
                            @Param("description") String description);
}
