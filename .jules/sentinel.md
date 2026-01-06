## 2025-12-30 - [Fix Broken Meta-Annotation for XSS Protection]
**Vulnerability:** The `@SanitizarHtml` annotation was being ignored by Jackson during deserialization, silently disabling XSS protection.
**Learning:** In Jackson (even v3), meta-annotations must be marked with `@JacksonAnnotationsInside` (from `com.fasterxml.jackson.annotation`) to be recognized. Without this, the introspector sees the custom annotation but ignores the `@JsonDeserialize` inside it.
**Prevention:** Always verify custom Jackson annotations with a unit test that specifically asserts the expected behavior (e.g., side effects like sanitization) actually occurs.

## 2025-02-18 - [Fix Broken Jackson Imports in Security Infrastructure]
**Vulnerability:** XSS protection was ineffective because the `@SanitizarHtml` annotation and `HtmlSanitizingDeserializer` were using incorrect `tools.jackson` imports (Jackson 3.x/alpha) mixed with `com.fasterxml.jackson` (Jackson 2.x), causing the deserializer to be ignored or fail compilation.
**Learning:** Spring Boot 3.x relies on Jackson 2.x (`com.fasterxml.jackson`). Using `tools.jackson` is not supported and leads to silent failures where annotations are ignored.
**Prevention:** Ensure all Jackson-related code uses `com.fasterxml.jackson` packages. Verify deserializers with integration tests that use the actual `ObjectMapper` configuration.
