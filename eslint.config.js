import globals from "globals";
import pluginJs from "@eslint/js";
import eslintConfigPrettier from "eslint-config-prettier";

export default [
    {
        ignores: [
            "**/node_modules/**",
            "**/dist/**",
            "frontend/**",
            "backend/**",
            ".gradle/**",
            "build/**",
            "**/*.ts"
        ],
    },
    pluginJs.configs.recommended,
    {
        files: ["toolkit/**/*.js", "toolkit/**/*.mjs", "*.config.js", "e2e/**/*.js", "summarize-lint.js"],
        languageOptions: {
            globals: {
                ...globals.node,
            },
        },
    },
    {
        files: ["toolkit/**/*.js", "toolkit/**/*.mjs", "*.config.js", "summarize-lint.js"],
        rules: {
            "complexity": "off",
            "max-params": "off",
            "max-depth": "off",
            "max-nested-callbacks": "off",
            "max-lines": "off",
            "max-lines-per-function": "off",
            "max-statements": "off",
        },
    },
    {
        files: ["toolkit/**/*.cjs"],
        languageOptions: {
            sourceType: "commonjs",
            globals: {
                ...globals.node,
            },
        },
    },
    {
        files: ["toolkit/qa-dashboard/**/*.js"],
        languageOptions: {
            globals: {
                ...globals.browser,
            },
        },
    },
    eslintConfigPrettier,
];
