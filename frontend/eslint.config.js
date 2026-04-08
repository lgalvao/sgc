import storybook from "eslint-plugin-storybook";
import globals from "globals";
import pluginJs from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
import vueParser from "vue-eslint-parser";
import eslintConfigPrettier from "eslint-config-prettier";
import pluginVueA11y from "eslint-plugin-vuejs-accessibility";

export default [
    // 1. Ignores globais
    {
        ignores: ["dist/", "node_modules/", "*.config.js", "coverage/"],
    },
    // 2. Configuração base para todos os arquivos
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
    // 3. Configurações recomendadas
    pluginJs.configs.recommended,
    ...tseslint.configs.recommended,
    // 4. Regras para TypeScript e Vue (Geral)
    {
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
            "@typescript-eslint/no-explicit-any": "error",
            "@typescript-eslint/no-unused-vars": [
                "warn",
                {
                    argsIgnorePattern: "^_+$",
                    varsIgnorePattern: "^_+$",
                },
            ],
            "no-console": ["error", {allow: ["error"]}],
        },
    },
    // 5. Overrides para Testes e Stories (Desabilita 'any' e 'no-console')
    {
        files: [
            "**/__tests__/**",
            "**/*.test.ts",
            "**/*.spec.ts",
            "**/*.stories.ts",
            "**/test-utils/**"
        ],
        rules: {
            "no-console": "off",
            "@typescript-eslint/no-explicit-any": "off",
        },
    },
    // 6. Configuração específica para Vue
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
    // 7. Estilo e Plugins Adicionais
    eslintConfigPrettier,
    ...storybook.configs["flat/recommended"]
];
