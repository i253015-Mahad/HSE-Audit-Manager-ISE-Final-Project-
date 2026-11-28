package hse.reporting.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import hse.reporting.common.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafetyReportServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 27);

    @Mock
    private SafetyReportRepository reports;

    @Mock
    private ReportVersionRepository versions;

    private SafetyReportService service;

    @BeforeEach
    void setUp() {
        service = new SafetyReportService(reports, versions, new ObjectMapper());
    }

    @Test
    void rejectsReportWithNoViolations() {
        assertThrows(BusinessRuleException.class,
            () -> service.validate(DATE, "Site A", IncidentType.ACCIDENT, List.of(), null));
    }

    @Test
    void rejectsViolationWithMissingViolatorDetails() {
        ViolationDraft draft = new ViolationDraft("Fall from height", "Unsafe act", "", "Low", "",
            "Barricaded area", "", "B-123", "Labourer", "Foreman");

        assertThrows(BusinessRuleException.class,
            () -> service.validate(DATE, "Site A", IncidentType.ACCIDENT, List.of(draft), null));
    }

    @Test
    void rejectsDuplicateViolationInActiveReport() {
        when(reports.countDuplicates(isNull(), any(), any(), any(), any())).thenReturn(1L);

        assertThrows(BusinessRuleException.class,
            () -> service.validate(DATE, "Site A", IncidentType.ACCIDENT,
                List.of(draft("Fall from height")), null));
    }

    @Test
    void submittedReportSnapshotsSorDetails() {
        SafetyReport report = service.createReport(1L, "inspector@hse.test", observer(),
            "mahadwaqas28@gmail.com", DATE, LocalTime.of(9, 30), "Site A",
            IncidentType.INSPECTION_FINDING, List.of(draft("Fall from height")), LocalDateTime.now());

        assertEquals(SubmissionStatus.PENDING_APPROVAL, report.getSubmissionStatus());
        assertNull(report.getProgressStatus());
        assertEquals("Inspector I253172", report.getObserverName());
        assertEquals("I-253172", report.getObserverBadgeNo());
        assertEquals("Safety Inspector", report.getObserverTitle());
        assertEquals("HSE Department", report.getObserverCompany());
        assertEquals("mahadwaqas28@gmail.com", report.getManagerEmail());
        assertEquals(DATE, report.getObservationDate());
        assertEquals(LocalTime.of(9, 30), report.getObservationTime());
        assertEquals(IncidentType.INSPECTION_FINDING, report.getObservationType());

        Violation violation = report.getViolations().get(0);
        assertEquals("Unsafe act", violation.getObservationHazard());
        assertEquals("Low", violation.getRiskRating());
        assertEquals("Ali", violation.getViolatorName());
        assertEquals("B-123", violation.getViolatorBadgeNo());
        assertEquals("Labourer", violation.getViolatorTitle());
        assertEquals("Foreman", violation.getTaskIncharge());
    }

    @Test
    void versionSnapshotContainsSorFields() {
        when(versions.findByReportIdOrderByVersionNumberAsc(any())).thenReturn(List.of());
        SafetyReport report = service.createReport(1L, "inspector@hse.test", observer(),
            "mahadwaqas28@gmail.com", DATE, LocalTime.of(9, 30), "Site A",
            IncidentType.INSPECTION_FINDING, List.of(draft("Fall from height")), LocalDateTime.now());

        service.recordVersion(report);

        ArgumentCaptor<ReportVersion> captor = ArgumentCaptor.forClass(ReportVersion.class);
        verify(versions).save(captor.capture());
        ReportVersion snapshot = captor.getValue();
        assertEquals(1, snapshot.getVersionNumber());
        assertEquals("Inspector I253172", snapshot.getObserverName());
        assertEquals("mahadwaqas28@gmail.com", snapshot.getManagerEmail());
        assertEquals(DATE, snapshot.getObservationDate());
        assertEquals("Site A", snapshot.getLocation());
        assertTrue(snapshot.getViolationsJson().contains("Fall from height"));
        assertTrue(snapshot.getViolationsJson().contains("Ali"));
    }

    private ObserverDetails observer() {
        return new ObserverDetails("i253172@nu.edu.pk", "Inspector I253172", "I-253172",
            "Safety Inspector", "HSE Department");
    }

    private ViolationDraft draft(String description) {
        return new ViolationDraft(description, "Unsafe act", "", "Low", "", "Barricaded area",
            "Ali", "B-123", "Labourer", "Foreman");
    }
}
