import globals from "globals";
import pluginJs from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginPlaywright from "eslint-plugin-playwright";
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
            "*.config.js"
        ],
    },
    pluginJs.configs.recommended,
    ...tseslint.configs.recommended,
    // Configuração global para Node.js (scripts em etc/scripts e arquivos de config)
    {
        files: ["etc/scripts/**/*.js", "etc/scripts/**/*.mjs", "*.config.js", "e2e/**/*.js", "summarize-lint.js"],
        languageOptions: {
            globals: {
                ...globals.node,
            },
        },
    },
    // Configuração específica para testes Playwright
    {
        files: ["e2e/**/*.ts"],
        plugins: {
            playwright: pluginPlaywright,
        },
        rules: {
            ...pluginPlaywright.configs["flat/recommended"].rules,
            "@typescript-eslint/no-explicit-any": "off",
            "@typescript-eslint/no-unused-vars": [
                "warn",
                {
                    "argsIgnorePattern": "^_|autenticado|cleanup",
                    "varsIgnorePattern": "^_|autenticado|cleanup",
                    "caughtErrorsIgnorePattern": "^e$"
                },
            ],
            "no-console": "off",
        },
        languageOptions: {
            parser: tseslint.parser,
            parserOptions: {
                project: ["./e2e/tsconfig.json"],
                tsconfigRootDir: import.meta.dirname,
            },
            globals: {
                ...globals.node,
            },
        },
    },
    // Exceções para o arquivo de captura de telas e smoke tests
    {
        files: ["e2e/captura.spec.ts", "e2e/smoke.spec.ts"],
        rules: {
            "playwright/no-wait-for-timeout": "off",
            "playwright/no-conditional-in-test": "off",
            "playwright/expect-expect": "off",
            "@typescript-eslint/no-unused-vars": ["warn", {
                "argsIgnorePattern": "^_|autenticado|cleanup",
                "varsIgnorePattern": "^_|autenticado|cleanup",
                "caughtErrorsIgnorePattern": "^e$"
            }],
        }
    },
    eslintConfigPrettier,
];
