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
        ignores: ["dist/", "node_modules/", "*.config.js", "coverage/", "src/types/**/*.d.ts", "**/*.d.ts"],
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

            // Regras de Qualidade de Engenharia de Software
            "complexity": ["warn", 12],
            "max-params": ["error", 3], // Limite de 3 parâmetros estrito (Regra de Negócio SGC)
            "max-depth": ["error", 4],
            "max-nested-callbacks": ["error", 3],
            "max-lines": ["warn", { max: 500, skipComments: true, skipBlankLines: true }],
            "max-lines-per-function": ["warn", { max: 140, skipComments: true, skipBlankLines: true }],
            "max-statements": ["warn", 28],
        },
    },
    // 5. Overrides para Testes, Stories e Tipagens (Desabilita regras estritas de complexidade/any)
    {
        files: [
            "**/__tests__/**",
            "**/*.test.ts",
            "**/*.spec.ts",
            "**/*.stories.ts",
            "**/test-utils/**",
            "**/*.d.ts"
        ],
        rules: {
            "no-console": "off",
            "@typescript-eslint/no-explicit-any": "off",
            "complexity": "off",
            "max-params": "off",
            "max-depth": "off",
            "max-nested-callbacks": "off",
            "max-lines": "off",
            "max-lines-per-function": "off",
            "max-statements": "off",
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
            "max-depth": ["error", 5],
            "vue/multi-word-component-names": "off",
            "no-useless-assignment": "off",
            "vuejs-accessibility/label-has-for": [
                "error",
                {
                    components: [],
                    controlComponents: ["BFormInput", "BFormSelect", "BFormTextarea", "BFormCheckbox", "BFormRadio"],
                    required: {some: ["nesting", "id"]},
                    allowChildren: false,
                },
            ],
        },
    },
    // 7. Estilo e Plugins Adicionais
    eslintConfigPrettier,
    ...storybook.configs["flat/recommended"]
];
