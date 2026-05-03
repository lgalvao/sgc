# SGC — UAT Feedback Widget: Feature Specification

**Version:** 1.0  
**Status:** Ready for implementation  
**Scope:** UAT builds only — excluded from production at build time  
**Stack:** Vue 3 / TypeScript / Vite / BootstrapVueNext (frontend) · Spring Boot / Oracle (backend)

-----

## 1. Overview

A persistent floating button is available during UAT builds, allowing any logged-in user to submit contextual feedback about any page in the system. When activated, the widget captures a screenshot of the current view (before the modal opens), collects a user note and a severity tag, enriches the submission with automatic contextual metadata, and posts the result to a dedicated backend endpoint.

The feature must be **entirely absent from production builds** — not hidden, not disabled at runtime, but structurally excluded from the compiled output.

-----

## 2. Activation Strategy

### 2.1 Frontend — Vite Build-Time Flag

**File:** `.env.uat`

```
VITE_FEEDBACK_WIDGET=true
```

**File:** `.env.production`

```
# VITE_FEEDBACK_WIDGET is intentionally absent
```

The widget is registered in the application only when the flag is present and true. Because `import.meta.env.VITE_FEEDBACK_WIDGET` is resolved at compile time, Rollup’s tree-shaker will drop the entire import graph (component + `html2canvas`) from the production bundle.

**File:** `src/main.ts` (or the root layout component)

```ts
if (import.meta.env.VITE_FEEDBACK_WIDGET === 'true') {
  const { default: FeedbackWidget } = await import('@/components/feedback/FeedbackWidget.vue')
  app.component('FeedbackWidget', FeedbackWidget)
}
```

**Root layout template** (`App.vue` or the authenticated shell):

```html
<FeedbackWidget v-if="$options.components?.FeedbackWidget" />
```

Alternatively, mount the component directly in the dynamic import block using a portal/teleport to `body`.

### 2.2 Backend — Spring Profile Guard

```java
@RestController
@Profile("uat")
@RequestMapping("/api/feedback")
public class FeedbackController { ... }
```

With `spring.profiles.active=production`, Spring does not instantiate this bean. The endpoint does not exist and returns 404 for any probe.

**Active profile is set in deployment configuration, not in source code.**

### 2.3 Build Pipeline Convention

|Environment|Vite mode          |Spring profile|
|-----------|-------------------|--------------|
|UAT        |`--mode uat`       |`uat`         |
|Production |`--mode production`|`production`  |

CI must enforce that `--mode production` is always used for releases to the production environment.

-----

## 3. Frontend Specification

### 3.1 File Structure

```
src/
  components/
    feedback/
      FeedbackWidget.vue      # Root component — floating button + modal
      FeedbackModal.vue       # Modal content — form fields
      FeedbackButton.vue      # The persistent floating trigger button
  composables/
    useFeedback.ts            # Screenshot capture, payload assembly, submission
  types/
    feedback.ts               # TypeScript interfaces
```

### 3.2 FeedbackButton.vue

A fixed-position circular button, always rendered on top of all content.

**Visual requirements:**

- Position: `fixed`, `bottom: 1.5rem`, `right: 1.5rem`
- `z-index`: above all modals and overlays (e.g., `9999`)
- Appearance: circular icon button, 48×48px, using a speech-bubble or flag icon from the project’s icon library
- Tooltip on hover: `"Enviar feedback"`
- Must not interfere with page scrolling or interactive elements beneath it

**Behavior:**

- On click: immediately trigger screenshot capture (see §3.4), then open the modal
- While submission is in progress: display a spinner in place of the icon; button is disabled
- On success: briefly show a checkmark icon (1.5 s), then revert to normal
- On error: briefly show an error icon (2 s), then revert to normal; modal remains open

### 3.3 FeedbackModal.vue

A `<BModal>` (BootstrapVueNext) opened programmatically. It must not appear in the screenshot.

**Form fields:**

|Field               |Type               |Required|Notes                                                                  |
|--------------------|-------------------|--------|-----------------------------------------------------------------------|
|`note`              |`<BFormTextarea>`  |Yes     |Min 10 chars. Label: “Descreva o problema ou sugestão”. Rows: 4.       |
|`type`              |`<BFormRadioGroup>`|Yes     |Options: `bug` · `suggestion` · `question` · `praise`. Default: `bug`. |
|`screenshotIncluded`|Read-only indicator|—       |Shows thumbnail of captured screenshot with a “Remover” link to opt out|

**Actions:**

- `Enviar` — submits the form (disabled if `note` is empty or < 10 chars)
- `Cancelar` — closes the modal, discards the capture

**Screenshot thumbnail:**

- Displayed inside the modal as a small preview (max 200px wide)
- Clicking it opens the full image in a new browser tab
- A “Remover captura” link allows the user to exclude the screenshot from the submission

### 3.4 Screenshot Capture (`useFeedback.ts`)

**Dependency:** `html2canvas` (install: `npm install html2canvas`)

**Capture sequence (critical ordering):**

1. User clicks the floating button
1. `captureScreenshot()` is called **before** any modal state changes
1. `html2canvas(document.body, { useCORS: true, allowTaint: false, scale: window.devicePixelRatio })` is called
1. The resulting canvas is converted to a `Blob` via `canvas.toBlob('image/webp', 0.85)`
1. The blob is stored in a `ref<Blob | null>` inside the composable
1. Only after the blob is stored does the modal open
1. If capture fails (rejection or timeout > 5 s), proceed without a screenshot and log a warning; do not block the user from submitting

**Why this order matters:** opening the modal first would capture the overlay instead of the underlying page.

### 3.5 Metadata Assembly

All metadata is assembled automatically at submission time. The user provides only `note` and `type`.

```ts
// src/types/feedback.ts

export interface FeedbackMetadata {
  // Session
  userId: string           // From auth store
  userName: string         // Display name
  userEmail: string        // If available in session
  
  // Navigation
  routeName: string        // Current Vue Router route name
  routePath: string        // Full path including params
  routeQuery: string       // Serialized query string (JSON)
  pageTitle: string        // document.title
  
  // SGC context
  activeProfile: string | null   // Active unit/profile — from app store
  activeYear: string | null      // Active year/period — from app store
  
  // Environment
  timestamp: string        // ISO 8601, client timezone
  timezoneOffset: number   // Minutes offset from UTC
  userAgent: string        // navigator.userAgent
  screenWidth: number      // window.innerWidth
  screenHeight: number     // window.innerHeight
  locale: string           // navigator.language
}

export interface FeedbackPayload {
  type: 'bug' | 'suggestion' | 'question' | 'praise'
  note: string
  metadata: FeedbackMetadata
  // Screenshot is sent as a separate multipart field
}
```

**Store fields to read:**  
The composable must read the currently active profile/unit and year from whichever Pinia store SGC uses for session/navigation context. The exact store and field names must be verified against the actual codebase before implementation.

### 3.6 Submission

```ts
async function submitFeedback(
  payload: FeedbackPayload,
  screenshot: Blob | null
): Promise<void> {
  const form = new FormData()
  form.append('data', JSON.stringify(payload))
  if (screenshot) {
    form.append('screenshot', screenshot, 'screenshot.webp')
  }

  await axios.post('/api/feedback', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

**Error handling:**

- Network or server errors: display a `useToastStore` error toast (use SGC’s existing notification pattern), keep the modal open
- Success: display a `useToastStore` success toast, close the modal, clear the stored screenshot

-----

## 4. Backend Specification

### 4.1 File Structure

```
src/main/java/.../feedback/
  FeedbackController.java
  FeedbackService.java
  FeedbackRecord.java        # JPA entity
  FeedbackRepository.java    # Spring Data JPA
  FeedbackMetadata.java      # Embedded JSON value object (or @Embeddable)
  FeedbackType.java          # Enum: BUG, SUGGESTION, QUESTION, PRAISE
```

### 4.2 API Endpoint

**`POST /api/feedback`**

- Content-Type: `multipart/form-data`
- Authentication: required (existing session/JWT — same as all other endpoints)
- Profile guard: `@Profile("uat")`

**Request parts:**

|Part name   |Type           |Required|Description                 |
|------------|---------------|--------|----------------------------|
|`data`      |`String` (JSON)|Yes     |Serialized `FeedbackPayload`|
|`screenshot`|`MultipartFile`|No      |WebP image, max 5 MB        |

**Response — success (`201 Created`):**

```json
{
  "id": "uuid-v4",
  "createdAt": "2025-10-15T14:32:00-03:00"
}
```

**Response — validation error (`400 Bad Request`):**

```json
{
  "error": "VALIDATION_ERROR",
  "message": "note must not be blank"
}
```

**Response — payload too large (`413`):** triggered by Spring’s `multipart.max-file-size` configuration.

### 4.3 Data Model

```java
@Entity
@Table(name = "SGC_FEEDBACK")   // Prefix consistent with SGC naming conventions
public class FeedbackRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType type;

    @Column(nullable = false, length = 2000)
    private String note;

    // Metadata stored as a JSON string in a CLOB column
    // (avoids schema proliferation; metadata is read-only reference data)
    @Column(columnDefinition = "CLOB")
    private String metadataJson;

    // Screenshot stored as filesystem path, not as a BLOB
    // (avoids bloating the Oracle tablespace)
    @Column(length = 500)
    private String screenshotPath;

    @Column(nullable = false)
    private String submittedByUserId;

    @Column(nullable = false)
    private String submittedByUserName;

    @Column(nullable = false)
    private OffsetDateTime submittedAt;

    @Column(nullable = false)
    private String routePath;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;   // NEW, REVIEWED, RESOLVED, WONTFIX
}
```

**`FeedbackStatus` enum:** `NEW` · `REVIEWED` · `RESOLVED` · `WONTFIX`  
Default on creation: `NEW`.

### 4.4 Screenshot Storage

Screenshots are saved to the filesystem, not to Oracle BLOBs.

**Configuration property:**

```yaml
# application-uat.yml
sgc:
  feedback:
    screenshot-dir: /var/sgc/feedback/screenshots
    max-screenshot-size-bytes: 5242880   # 5 MB
```

**Naming convention:** `{feedbackId}_{timestamp}.webp`

The `FeedbackService` must validate that the resolved path remains within `screenshot-dir` before writing (path traversal prevention).

### 4.5 FeedbackService Responsibilities

1. Deserialize and validate the `data` JSON part
1. Validate `note` is not blank and does not exceed 2000 characters
1. Validate screenshot size if present
1. Resolve the authenticated user from the security context (do not trust the metadata — it is informational only)
1. Save the screenshot to the filesystem if present, record the path
1. Persist the `FeedbackRecord` to Oracle
1. Return the created record’s `id` and `submittedAt`

**The userId and userName stored in the record must come from the Spring Security context, not from the client-submitted metadata.** The metadata copy is kept for diagnostic purposes only.

### 4.6 Database Migration

Create a Flyway migration (or Liquibase changeset, whichever SGC uses):

```sql
-- V{next}__create_sgc_feedback.sql

CREATE TABLE SGC_FEEDBACK (
    ID              RAW(16)         NOT NULL,
    TYPE            VARCHAR2(20)    NOT NULL,
    NOTE            VARCHAR2(2000)  NOT NULL,
    METADATA_JSON   CLOB,
    SCREENSHOT_PATH VARCHAR2(500),
    SUBMITTED_BY_USER_ID   VARCHAR2(100) NOT NULL,
    SUBMITTED_BY_USER_NAME VARCHAR2(200) NOT NULL,
    SUBMITTED_AT    TIMESTAMP WITH TIME ZONE NOT NULL,
    ROUTE_PATH      VARCHAR2(500)   NOT NULL,
    STATUS          VARCHAR2(20)    DEFAULT 'NEW' NOT NULL,
    CONSTRAINT PK_SGC_FEEDBACK PRIMARY KEY (ID),
    CONSTRAINT CK_FEEDBACK_TYPE CHECK (TYPE IN ('BUG','SUGGESTION','QUESTION','PRAISE')),
    CONSTRAINT CK_FEEDBACK_STATUS CHECK (STATUS IN ('NEW','REVIEWED','RESOLVED','WONTFIX'))
);

CREATE INDEX IDX_FEEDBACK_STATUS ON SGC_FEEDBACK(STATUS);
CREATE INDEX IDX_FEEDBACK_USER ON SGC_FEEDBACK(SUBMITTED_BY_USER_ID);
CREATE INDEX IDX_FEEDBACK_DATE ON SGC_FEEDBACK(SUBMITTED_AT);
```

-----

## 5. Configuration Summary

### 5.1 Frontend (`package.json` scripts)

```json
{
  "scripts": {
    "build:uat": "vite build --mode uat",
    "build:prod": "vite build --mode production"
  }
}
```

### 5.2 Backend (`application-uat.yml`)

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB

sgc:
  feedback:
    screenshot-dir: /var/sgc/feedback/screenshots
    max-screenshot-size-bytes: 5242880
```

-----

## 6. New Dependencies

### Frontend

|Package      |Purpose                 |Install                  |
|-------------|------------------------|-------------------------|
|`html2canvas`|DOM-to-canvas screenshot|`npm install html2canvas`|

No other new dependencies. The modal uses `BModal` from BootstrapVueNext. Submission uses the existing Axios instance.

### Backend

No new dependencies. Uses Spring MVC (`@Profile`, `MultipartFile`), Spring Data JPA, and the existing Oracle driver.

-----

## 7. Exclusion Verification Checklist

Before releasing a production build, verify:

- [ ] `.env.production` does not contain `VITE_FEEDBACK_WIDGET`
- [ ] Production bundle does not contain the string `html2canvas` (check with `grep` or bundle analyzer)
- [ ] Production bundle does not contain the string `FeedbackWidget`
- [ ] Spring Boot production startup log does not list `FeedbackController` as a registered bean
- [ ] `GET /api/feedback` on the production server returns 404

-----

## 8. Acceptance Criteria

### UAT build

- AC-1: The floating feedback button is visible on every authenticated page, fixed to the bottom-right corner.
- AC-2: Clicking the button captures the current page as a WebP image before any modal appears.
- AC-3: The modal opens after capture and displays the screenshot thumbnail, note field, and type selector.
- AC-4: Removing the screenshot via “Remover captura” excludes it from the submission; the form still submits.
- AC-5: Submitting with a blank note shows an inline validation message and does not post to the server.
- AC-6: A successful submission shows a success toast and closes the modal.
- AC-7: A failed submission shows an error toast and keeps the modal open with the note intact.
- AC-8: The submitted record in the database contains the correct userId from the session (not from client metadata).
- AC-9: The screenshot file is saved to the configured directory with the correct naming convention.

### Production build

- AC-10: The floating button does not appear anywhere in the application.
- AC-11: `html2canvas` is absent from the production JavaScript bundle.
- AC-12: `POST /api/feedback` returns 404 on the production server.

-----

## 9. Out of Scope

The following are explicitly excluded from this specification:

- A UI for reviewing or managing submitted feedback (a simple database query or export is sufficient for UAT)
- Email notifications on submission
- Attachment support beyond a single screenshot
- Annotation tools on the screenshot (draw, highlight, etc.)
- Any form of user authentication change

These may be added in a future iteration if the pattern proves useful beyond UAT.