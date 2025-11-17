// noinspection NpmUsedModulesInstalled

import globals from "globals";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
import vueParser from "vue-eslint-parser";

export default [
    {
        ignores: [
            "dist/**",
            "node_modules/**",
            "**/*.spec.js",
            "vitest.setup.js",
            "modelo/**",
            "target/**",
            "__mocks__/**"
        ],
    },
    ...tseslint.configs.recommended,
    ...pluginVue.configs['flat/recommended'],
    {
        files: ["**/*.{js,ts,vue}"],
        plugins: {
            '@typescript-eslint': tseslint.plugin,
            vue: pluginVue,
        },
        languageOptions: {
            globals: {
                ...globals.browser,
                ...globals.node,
            },
            parser: vueParser, // Use vue-parser for all files
            parserOptions: {
                parser: tseslint.parser, // Use ts-parser for <script> blocks
                sourceType: "module",
            },
        },
        rules: {
            "@typescript-eslint/no-explicit-any": "off",
        },
    },
    {
        files: ["tests/vue-specific-setup.ts"],
        rules: {
            "@typescript-eslint/no-explicit-any": "off",
        },
    },
    {
        files: ["src/**/*.{ts,tsx,vue}"],
        rules: {
            "no-restricted-imports": [
                "warn",
                {
                    "patterns": [
                        "**/*.test.*",
                        "**/*.spec.*",
                        "**/__tests__/**"
                    ]
                }
            ]
        }
    }
];