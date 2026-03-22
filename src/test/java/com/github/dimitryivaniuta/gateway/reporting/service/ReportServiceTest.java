package com.github.dimitryivaniuta.gateway.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportDefinition;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportStatus;
import com.github.dimitryivaniuta.gateway.reporting.domain.ReportType;
import com.github.dimitryivaniuta.gateway.reporting.dto.CreateReportRequest;
import com.github.dimitryivaniuta.gateway.reporting.dto.RunReportRequest;
import com.github.dimitryivaniuta.gateway.reporting.exception.ReportNotFoundException;
import com.github.dimitryivaniuta.gateway.reporting.kafka.ReportEventPublisher;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportGenerationResult;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportGenerationService;
import com.github.dimitryivaniuta.gateway.reporting.report.ReportDataRow;
import com.github.dimitryivaniuta.gateway.reporting.repository.ReportArtifactRepository;
import com.github.dimitryivaniuta.gateway.reporting.repository.ReportDefinitionRepository;
import com.github.dimitryivaniuta.gateway.reporting.repository.ReportExecutionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Tests the report service business flow.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportDefinitionRepository reportDefinitionRepository;
    @Mock
    private ReportExecutionRepository reportExecutionRepository;
    @Mock
    private ReportArtifactRepository reportArtifactRepository;
    @Mock
    private ReportEventPublisher reportEventPublisher;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ReportGenerationService reportGenerationService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        reportService = new ReportService(
                reportDefinitionRepository,
                reportExecutionRepository,
                reportArtifactRepository,
                new ReportMapper(),
                reportEventPublisher,
                stringRedisTemplate,
                reportGenerationService,
                new ObjectMapper()
        );
    }

    @Test
    void shouldCreateReport() {
        CreateReportRequest request = new CreateReportRequest("RPT-001", "Daily AML", ReportType.AML, "tenant-a", "{}");
        when(reportDefinitionRepository.findByReportCode("RPT-001")).thenReturn(Optional.empty());
        when(reportDefinitionRepository.save(any(ReportDefinition.class))).thenAnswer(invocation -> {
            ReportDefinition definition = invocation.getArgument(0);
            definition.setId(11L);
            definition.setCreatedAt(OffsetDateTime.now());
            definition.setUpdatedAt(OffsetDateTime.now());
            return definition;
        });

        var response = reportService.createReport(request, "alice");

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo(ReportStatus.ACTIVE);
        verify(reportEventPublisher).publishReportCreated(any(ReportDefinition.class));
    }

    @Test
    void shouldThrowWhenReportMissing() {
        when(reportDefinitionRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReport(44L)).isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    void shouldRunReportAndGenerateArtifact() {
        ReportDefinition definition = new ReportDefinition();
        definition.setId(17L);
        definition.setReportCode("RPT-017");
        definition.setName("Monthly Settlement");
        definition.setReportType(ReportType.SETTLEMENT);
        definition.setTenantId("tenant-b");
        definition.setCriteriaJson("{}");
        definition.setStatus(ReportStatus.ACTIVE);
        definition.setCreatedBy("alice");
        definition.setCreatedAt(OffsetDateTime.now());
        definition.setUpdatedAt(OffsetDateTime.now());
        when(reportDefinitionRepository.findById(17L)).thenReturn(Optional.of(definition));
        when(reportExecutionRepository.findByReportDefinitionIdOrderByCreatedAtDesc(17L)).thenReturn(List.of());
        when(reportGenerationService.generate(definition)).thenReturn(new ReportGenerationResult(
                List.of(new ReportDataRow("B-1", "tenant-b", "RPT-017", LocalDate.now(), "CP-100", "EUR", new BigDecimal("120.00"), 15, "BOOKED")),
                "text/csv",
                "RPT-017.csv",
                "header\nrow",
                "abc123",
                new BigDecimal("120.00")
        ));

        var response = reportService.runReport(17L, new RunReportRequest("bob"));

        assertThat(response.id()).isEqualTo(17L);
        verify(reportExecutionRepository).save(any());
        verify(reportArtifactRepository).save(any());
        verify(reportEventPublisher).publishExecutionCompleted(any());
        verify(reportEventPublisher).publishArtifactGenerated(any(), any());
    }
}
