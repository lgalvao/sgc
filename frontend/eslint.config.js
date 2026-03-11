import storybook from "eslint-plugin-storybook";

import globals from "globals";
import pluginJs from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
import vueParser from "vue-eslint-parser";
import eslintConfigPrettier from "eslint-config-prettier";
import pluginVueA11y from "eslint-plugin-vuejs-accessibility";

export default [
    {
        ignores: ["dist/", "node_modules/", "*.config.js"],
    }, {
        languageOptions: {
            ecmaVersion: "latest",
            sourceType: "module",
            globals: {
                ...globals.browser,
                ...globals.node,
            },
        },
    },
    {
        files: ["**/*.test.ts", "**/*.spec.ts", "**/__tests__/**"],
        rules: {
            "no-console": "off",
        },
    },
    pluginJs.configs.recommended,
    ...tseslint.configs.recommended, {
        files: ["**/*.ts", "**/*.vue"],
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
    ...pluginVue.configs["flat/recommended"], ...pluginVueA11y.configs["flat/recommended"], {
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
            "no-useless-assignment": "off",
            "vuejs-accessibility/label-has-for": [
                "error",
                {
                    components: [],
                    controlComponents: ["BFormInput", "BFormSelect", "BFormTextarea", "BFormCheckbox", "BFormRadio"],
                    required: { some: ["nesting", "id"] },
                    allowChildren: false,
                },
            ],
        },
    },
    {
        rules: {
            "no-console": ["error", {allow: ["error"]}],
        },
    },
    eslintConfigPrettier, ...storybook.configs["flat/recommended"]];
