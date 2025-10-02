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
      // Detect usages deprecadas (ex.: zod .passthrough())
      "no-restricted-syntax": [
        "warn",
        {
          "selector": "CallExpression[callee.property.name='passthrough']",
          "message": "Uso de .passthrough() está deprecado — substitua por .catchall(z.unknown()) ou ajuste o schema de forma explícita."
        },
        {
          "selector": "MemberExpression[property.name='passthrough']",
          "message": "Referência a .passthrough está deprecada — substitua por .catchall(z.unknown())."
        }
      ],
      // Custom overrides
      "vue/multi-word-component-names": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-unused-vars": [
        "warn",
        {
          "argsIgnorePattern": "^_",
          "varsIgnorePattern": "^_",
          "caughtErrorsIgnorePattern": "^_"
        }
      ],
    },
  },
    {
        files: ["tests/vue-specific-setup.ts"],
        rules: {
            "@typescript-eslint/no-explicit-any": "off",
        },
  }
];