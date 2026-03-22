# Legacy source review

This note reviews the uploaded legacy security and OAuth-related classes and lists the most important production-grade changes that should be made before reusing them.

## Main findings

### 1. Microsoft OAuth client should not build a new Feign client per call

`MicrosoftOAuth2Client` constructs a new Feign client inside `getOAuthClient()` for each request. That is workable, but it is not ideal for production because you lose central connection pooling, timeouts, resilience, and shared observability. The `state` is also plain `userId:domain`, which should be signed and time-bound instead of being directly trusted round-trip.

### 2. OAuth callback flow needs stronger state validation and cleaner error handling

`OAuth2Controller` accepts callback parameters, derives `settingId` from `state`, exchanges the authorization code, then redirects. The controller logs and swallows `IOException`, and it returns `200 ok` even after redirect-style flows. A production rewrite should use signed state, nonce or server-side state storage, explicit failure redirect handling, and consistent HTTP responses.

### 3. Username/password provider leaks too much intent and returns empty authorities

`CustomDomainUsernamePasswordAuthenticationProvider` authenticates against a repository, but returns a token with empty authorities. In a production security model, roles or authorities should be populated explicitly. The current exception approach also makes the provider tightly coupled to login formatting rules.

### 4. Authorization filter mixes authorization and business-result concerns

`CommandHandlerAuthorizationFilter` converts missing authentication and authorization failures into business `Result.failure(...)` values. In modern Spring applications, that usually becomes harder to reason about than throwing explicit authentication / access-denied exceptions and letting a centralized security layer translate them consistently.

### 5. Authentication service is empty

`AuthenticationService` is currently an empty service and should either be removed or implemented with a real responsibility.

### 6. Authentication entry point and controller advice should be separated

`AuthenticationEntryPointAdvice` implements `AuthenticationEntryPoint` and also acts as `@ControllerAdvice`. Splitting those concerns is cleaner and easier to test.

## Recommended migration direction

- use Spring Security filter chains instead of ad hoc auth flow wiring where possible
- use signed JWT or opaque session state, not raw trust in callback state
- store OAuth state server-side or sign and expire it
- centralize error translation with `ProblemDetail`
- separate browser CSRF/session model from stateless bearer-token API model
- move bot and path-abuse filtering toward reverse proxy / WAF first, then keep app-level telemetry and fallback throttling
