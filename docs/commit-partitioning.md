# Production-style commit partitioning

## 1. bootstrap: initialize reporting service build and local runtime
Files:
- settings.gradle
- build.gradle
- gradle.properties
- gradlew
- gradlew.bat
- gradle/wrapper/gradle-wrapper.properties
- .gitignore
- docker-compose.yml
- .github/workflows/ci.yml
- README.md

## 2. feat(domain): add report definition and execution persistence
Files:
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/domain/ReportDefinition.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/domain/ReportExecution.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/domain/ReportStatus.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/domain/ReportType.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/repository/ReportDefinitionRepository.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/repository/ReportExecutionRepository.java
- src/main/resources/db/migration/V1__init_reporting_schema.sql

## 3. feat(api): add report CRUD and execution endpoints
Files:
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/CreateReportRequest.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/RunReportRequest.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/ReportDefinitionResponse.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/ReportExecutionResponse.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/controller/ReportController.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/service/ReportMapper.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/service/ReportService.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/exception/DuplicateReportCodeException.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/exception/ReportNotFoundException.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/exception/ReportArtifactNotFoundException.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/web/ApiExceptionHandler.java

## 4. feat(security): add JWT auth, browser CSRF controls, and suspicious traffic filters
Files:
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/config/SecurityConfig.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/config/SecurityProperties.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/config/SecurityTelemetryHealthIndicator.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/controller/AuthController.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/controller/PortalSecurityController.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/AuthTokenRequest.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/AuthTokenResponse.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/security/BrowserOriginValidationFilter.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/security/ClientIpResolver.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/security/JwtService.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/security/SecurityTelemetry.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/security/SuspiciousRequestService.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/security/SuspiciousTrafficFilter.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/service/LocalUserAuthenticationService.java
- src/main/resources/application.yml
- docs/legacy-security-review.md

## 5. feat(events): publish report lifecycle events to Kafka
Files:
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/kafka/ReportEventPublisher.java

## 6. feat(reporting): add real report generation pipeline and downloadable CSV artifacts
Files:
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/domain/ReportArtifact.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/repository/ReportArtifactRepository.java
- src/main/resources/db/migration/V2__add_report_artifact.sql
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/ReportCriteria.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/ReportDataRow.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/ReportGenerationResult.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/ReportCriteriaParser.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/ReportDataGenerator.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/CsvReportRenderer.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/report/ReportGenerationService.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/controller/ReportController.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/service/ReportMapper.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/service/ReportService.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/dto/ReportExecutionResponse.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/exception/ReportArtifactNotFoundException.java
- src/main/java/com/github/dimitryivaniuta/gateway/reporting/web/ApiExceptionHandler.java

## 7. test(unit): cover auth, security filters, and report pipeline services
Files:
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/controller/AuthControllerTest.java
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/security/BrowserOriginValidationFilterTest.java
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/security/JwtServiceTest.java
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/security/SuspiciousRequestServiceTest.java
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/service/ReportServiceTest.java
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/service/ReportGenerationServiceTest.java

## 8. test(integration): add Testcontainers end-to-end report flow
Files:
- src/test/java/com/github/dimitryivaniuta/gateway/reporting/integration/ReportFlowIntegrationTest.java

## 9. ops(edge): add reverse proxy and WAF sample configurations
Files:
- nginx/security-edge.conf
- apache/security-edge.conf

## 10. ops(k8s): add Kubernetes manifests and Helm chart
Files:
- k8s/namespace.yaml
- k8s/configmap.yaml
- k8s/secret.example.yaml
- k8s/deployment.yaml
- k8s/service.yaml
- k8s/ingress.yaml
- k8s/hpa.yaml
- k8s/pdb.yaml
- k8s/networkpolicy.yaml
- k8s/serviceaccount.yaml
- charts/banking-reporting-microservice/Chart.yaml
- charts/banking-reporting-microservice/values.yaml
- charts/banking-reporting-microservice/templates/_helpers.tpl
- charts/banking-reporting-microservice/templates/configmap.yaml
- charts/banking-reporting-microservice/templates/secret.yaml
- charts/banking-reporting-microservice/templates/deployment.yaml
- charts/banking-reporting-microservice/templates/service.yaml
- charts/banking-reporting-microservice/templates/ingress.yaml
- charts/banking-reporting-microservice/templates/serviceaccount.yaml
- charts/banking-reporting-microservice/templates/hpa.yaml
