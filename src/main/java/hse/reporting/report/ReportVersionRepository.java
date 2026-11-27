package hse.reporting.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportVersionRepository extends JpaRepository<ReportVersion, Long> {

    List<ReportVersion> findByReportIdOrderByVersionNumberAsc(Long reportId);
}
