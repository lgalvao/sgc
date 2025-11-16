module.exports = {
  entry: ["frontend/src/main.ts"],
  ignorePatterns: [
    "**/*.test.*",
    "**/*.spec.*",
    "**/__tests__/**"
  ],
  alias: {
    "@": "frontend/src"
  }
};
