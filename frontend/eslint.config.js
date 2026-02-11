// eslint.config.js
import globals from "globals";
import pluginJs from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
import vueParser from "vue-eslint-parser";
import eslintConfigPrettier from "eslint-config-prettier";
import pluginVueA11y from "eslint-plugin-vuejs-accessibility";

export default [
    // 1. Ignore files
    {
        ignores: ["dist/", "node_modules/", "*.config.js"],
    },

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

    // 2b. Allow console in test files
    {
        files: ["**/*.test.ts", "**/*.spec.ts", "**/__tests__/**"],
        rules: {
            "no-console": "off",
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
    ...pluginVueA11y.configs["flat/recommended"],
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
            "vuejs-accessibility/label-has-for": [
                "error",
                {
                    components: ["Label"],
                    controlComponents: [
                        "BFormInput",
                        "BFormSelect",
                        "BFormTextarea",
                        "BFormCheckbox",
                        "BFormDatepicker",
                        "BFormSpinbutton",
                        "BFormTags"
                    ],
                    required: {
                        some: ["nesting", "id"],
                    },
                },
            ],
        },
    },

    // 6. Custom overrides
    {
        rules: {
            // Prevent console.* in production code (use logger instead)
            // Allows console.error for critical errors that should always be visible
            "no-console": ["error", { allow: ["error"] }],
        },
    },

    // 7. Prettier integration
    eslintConfigPrettier,
];
