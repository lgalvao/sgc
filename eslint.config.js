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
  // Main configuration for Vue, TS, JS files
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
      // Spread recommended rules here
      ...tseslint.configs.recommended.rules,
      ...pluginVue.configs['flat/recommended'].rules,
      // Custom overrides
      "vue/multi-word-component-names": "off",
      "@typescript-eslint/no-explicit-any": "warn",
      "@typescript-eslint/no-unused-vars": [
        "warn",
        {
          "argsIgnorePattern": "^_",
          "varsIgnorePattern": "^_",
          "caughtErrorsIgnorePattern": "^_"
        }
      ],
    },
  }
];