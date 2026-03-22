# banking-reporting-microservice

Production-grade Spring Boot 4 banking reporting microservice for a Real Estate / Banking portal. The service demonstrates secure report creation and querying, PostgreSQL persistence, Flyway migrations, Redis-backed throttling and attack telemetry, Kafka event publishing, Actuator health checks, a real CSV report generation pipeline, and a modern browser/API authorization approach that avoids noisy legacy CSRF handling.

## Suggested GitHub repository description

Production-grade banking reporting microservice with Spring Boot 4, Java 21, PostgreSQL, Flyway, Redis, Kafka KRaft, JWT API security, browser CSRF protection, CSV report generation, Prometheus metrics, and health monitoring.

## Production-grade upgrades in this revision

- added a real report generation pipeline with criteria parsing, deterministic dataset generation, CSV rendering, artifact persistence, and download endpoint
- added `report_artifact` persistence and Kafka event publishing for generated artifacts
- added Testcontainers end-to-end integration coverage for PostgreSQL, Redis, and Kafka
- added full reverse-proxy security examples for both Nginx and Apache + ModSecurity
- added Kubernetes manifests and a Helm chart for production deployment
- added production-style commit partitioning under `docs/commit-partitioning.md`

## Why this design for CSRF and attack noise

The attached email shows a blocked POST request to a **non-existent** endpoint with a missing CSRF token. In practice, thousands of such requests are usually internet background noise, scanners, or spray-and-pray probes rather than meaningful targeted attacks.

### Professional behavior implemented here

1. **Separate browser and API security models**
   - Browser endpoints use session cookie semantics + CSRF token support.
   - API endpoints use `Authorization: Bearer` JWT and are stateless.
   - Browser writes are additionally checked with `Origin` / `Referer` validation.

2. **Do not alert by email for every blocked probe**
   - Log suspicious traffic in structured form.
   - Count offenders in Redis.
   - Expose metrics/health signals.
   - Alert only on thresholds or anomaly spikes.

3. **Rate-limit and temporarily deny abusive IPs**
   - Redis-backed limiter tracks request velocity per client IP.
   - Repeated offenders are temporarily blocked with `429`.
   - This is safer than permanently blocking single IPs, which is brittle with NAT, VPNs, clouds, and botnets.

4. **Treat unknown paths differently**
   - Unknown state-changing paths are treated as suspicious by prefix classification.
   - Avoid expensive business logic for requests that never map to a real route.

5. **Prefer edge protection first**
   - Best practice is to block high-volume garbage **before** the app: WAF / CDN / reverse proxy / load balancer rules, reputation feeds, geo/risk controls, and bot management.
   - Application-level controls remain important as a second layer.

### Should you block attacker IPs?

Temporarily, yes. Permanently, usually not as the primary strategy. Large static deny lists are rarely sufficient because the source IPs rotate constantly. The better professional approach is:

- WAF / reverse proxy filtering first
- per-IP and per-subnet throttling
- anomaly-based alerting
- low-noise application logs
- strong browser security model
- no mailbox spam for every rejected probe

## Main capabilities

- Create and query banking reports
- Run reports through a real generation pipeline
- Persist generated CSV artifacts in PostgreSQL
- Download report artifacts by execution id
- Publish report lifecycle and artifact events to Kafka
- Cache summary views in Redis
- JWT-based API authorization for `/api/**`
- CSRF-aware browser security for `/portal/**`
- Redis-backed suspicious IP throttling for all incoming requests
- Actuator health, readiness, liveness, metrics, and Prometheus scraping
- Reverse-proxy security examples for Nginx and Apache
- Kubernetes and Helm deployment assets

## Architecture

- `controller` - REST APIs for auth, browser security bootstrap, reports, and artifact download
- `service` - business logic and orchestration
- `report` - generation pipeline components: criteria parsing, row generation, rendering, metadata
- `security` - JWT issuance/validation helpers, Spring Security config, suspicious-request filters, origin validation
- `kafka` - report event publisher
- `repository` - JPA repositories
- `web` - exception handling and error translation
- `docs` - legacy source assessment and commit plan
- `nginx` / `apache` - edge hardening rules
- `k8s` / `charts` - deployment assets

## Local run

```bash
./gradlew clean test
./gradlew bootRun
```

## Default local credentials

JWT demo login endpoint:

- username: `report-admin`
- password: `changeit-admin`

Browser portal CSRF bootstrap endpoint:

- `GET /portal/security/csrf`

## Key endpoints

- `POST /auth/token`
- `GET /portal/security/csrf`
- `POST /api/reports`
- `GET /api/reports/{reportId}`
- `GET /api/reports`
- `POST /api/reports/{reportId}/run`
- `GET /api/reports/executions/{executionId}/artifact`
- `GET /actuator/health`
- `GET /actuator/prometheus`

## Operations

- See `nginx/security-edge.conf` for Nginx reverse-proxy and request-filtering rules
- See `apache/security-edge.conf` for Apache + ModSecurity profile
- See `k8s/` for raw manifests
- See `charts/banking-reporting-microservice/` for the Helm chart
- See `docs/commit-partitioning.md` for production-style commit grouping

## Postman

See `postman/Banking-Reporting-Microservice.postman_collection.json` and `postman/Banking-Reporting-Microservice.local.postman_environment.json`.
