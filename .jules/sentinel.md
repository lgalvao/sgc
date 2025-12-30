## 2025-12-30 - [Fix Broken Meta-Annotation for XSS Protection]
**Vulnerability:** The `@SanitizarHtml` annotation was being ignored by Jackson during deserialization, silently disabling XSS protection.
**Learning:** In Jackson (even v3), meta-annotations must be marked with `@JacksonAnnotationsInside` (from `com.fasterxml.jackson.annotation`) to be recognized. Without this, the introspector sees the custom annotation but ignores the `@JsonDeserialize` inside it.
**Prevention:** Always verify custom Jackson annotations with a unit test that specifically asserts the expected behavior (e.g., side effects like sanitization) actually occurs.
