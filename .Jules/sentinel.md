# Sentinel's Journal

## 2025-12-11 - Committed Production Configuration
**Vulnerability:** Hardcoded production database password found in `backend/src/main/resources/application-prod.yml`.
**Learning:** The project commits production configuration files to the repository, which invites the accidental commit of secrets.
**Prevention:** Ensure all sensitive keys in committed configuration files use environment variable placeholders (e.g., `${VAR}`). Consider removing production config files from version control.
