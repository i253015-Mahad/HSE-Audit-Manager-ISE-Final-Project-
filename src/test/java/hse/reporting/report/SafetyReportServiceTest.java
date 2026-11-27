package hse.reporting.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import hse.reporting.common.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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
    void rejectsViolationsFromAnotherIncident() {
        ViolationDraft draft = new ViolationDraft("Fall from height", "OTHER", "Working at height",
            "OTHER", "High", "Barricaded area", DATE.plusDays(1), null, null);

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
    void submittedReportIsPendingApprovalWithNoProgressStatus() {
        SafetyReport report = service.createReport(1L, "inspector@hse.test", DATE, LocalTime.of(9, 30),
            "Site A", IncidentType.ACCIDENT, List.of(draft("Fall from height")), LocalDateTime.now());

        assertEquals(SubmissionStatus.PENDING_APPROVAL, report.getSubmissionStatus());
        assertNull(report.getProgressStatus());
        assertEquals(1, report.getViolations().size());
    }

    private ViolationDraft draft(String description) {
        return new ViolationDraft(description, "OTHER", "Working at height",
            "OTHER", "High", "Barricaded area", null, null, null);
    }
}
