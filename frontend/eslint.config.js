// eslint.config.js
import globals from "globals";
import pluginJs from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
import vueParser from "vue-eslint-parser";
import eslintConfigPrettier from "eslint-config-prettier";

export default [
    // 1. Ignore files
    {
        ignores: ["dist/", "node_modules/", "*.config.js"],
    },

    // 2. Global settings for all files
    {
        languageOptions: {
            ecmaVersion: "latest",
            sourceType: "module",
            globals: {
                ...globals.browser,
                ...globals.node,
            },
        },
    },

    // 3. ESLint's recommended rules
    pluginJs.configs.recommended,

    // 4. TypeScript-specific configuration
    ...tseslint.configs.recommended,
    {
        files: ["**/*.ts", "**/*.tsx", "**/*.vue"],
        languageOptions: {
            parser: tseslint.parser,
            parserOptions: {
                project: ["./tsconfig.json"],
                tsconfigRootDir: import.meta.dirname,
                extraFileExtensions: [".vue"],
            },
        },
        rules: {
            "@typescript-eslint/no-explicit-any": "off",
            "@typescript-eslint/no-unused-vars": [
                "warn",
                {
                    argsIgnorePattern: "^_+$",
                    varsIgnorePattern: "^_+$",
                },
            ],
        },
    },

    // 5. Vue-specific configuration
    ...pluginVue.configs["flat/recommended"],
    {
        files: ["**/*.vue"],
        languageOptions: {
            parser: vueParser,
            parserOptions: {
                parser: tseslint.parser,
                project: ["./tsconfig.json"],
                tsconfigRootDir: import.meta.dirname,
                extraFileExtensions: [".vue"],
            },
        },
        rules: {
            "vue/multi-word-component-names": "off",
        },
    },

    // 6. Prettier integration
    eslintConfigPrettier,
];