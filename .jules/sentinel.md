## 2024-05-23 - Hardcoded CORS Configuration
**Vulnerability:** The CORS configuration was hardcoded to `http://localhost:5173` in `ConfigSeguranca.java`.
**Learning:** Hardcoding origins in code prevents flexible deployment to different environments (e.g., dev, staging, prod) and can lead to availability issues or insecure configurations if the code is deployed as-is.
**Prevention:** Always externalize environment-specific configurations like allowed origins to property files (`application.yml`) and use environment variables for override.
