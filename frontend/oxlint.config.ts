import {defineConfig} from "oxlint";

export default defineConfig({
    plugins: ["eslint", "typescript", "import", "unicorn", "promise", "vitest", "vue"],

    env: {
        browser: true,
        es2024: true,
    },

    options: {
        typeAware: true,
        reportUnusedDisableDirectives: "warn",
        respectEslintDisableDirectives: true,
    },

    categories: {
        correctness: "error",
        suspicious: "warn",
        pedantic: "off",
        style: "off",
        perf: "off",
        restriction: "off",
        nursery: "off",
    },

    rules: {
        "typescript/ban-ts-comment": [
            "error",
            {
                "ts-ignore": true,
                "ts-nocheck": true,
                "ts-expect-error": "allow-with-description",
                "ts-check": false,
                minimumDescriptionLength: 10,
            },
        ],

        "typescript/no-explicit-any": "error",
        "typescript/no-non-null-assertion": "warn",
        "vitest/expect-expect": "error",
        "vitest/no-focused-tests": "error",
        "vitest/no-disabled-tests": "warn",

        "typescript/no-unused-vars": [
            "error",
            {args: "after-used", ignoreRestSiblings: true, varsIgnorePattern: "^_"},
        ],

        "no-unreachable": "error",
        "typescript/no-floating-promises": "error",
        "typescript/await-thenable": "error",
        "promise/catch-or-return": ["error", {allowFinally: true}],
        "no-magic-numbers": "off",
        "eqeqeq": ["error", "always", {null: "ignore"}],
        "no-console": ["warn", {allow: ["warn", "error"]}],
        "no-debugger": "error",
        "no-eval": "error",
        "no-new-func": "error",
        "oxc/bad-comparison-sequence": "error",
        "oxc/bad-object-literal-comparison": "error",
        "oxc/const-comparisons": "error",
        "oxc/double-comparisons": "error",
        "oxc/bad-array-method-on-arguments": "error",
        "oxc/approx-constant": "warn",
        "import/no-cycle": "error",
        "import/no-duplicates": "error",
        "no-throw-literal": "error",
        "unicorn/error-message": "warn",
    },

    overrides: [
        {
            files: ["**/*.spec.ts", "**/*.test.ts", "**/*.spec.js", "**/*.test.js"],
            env: {vitest: true},
            rules: {
                "no-magic-numbers": "off",
                "typescript/no-floating-promises": "warn",
                "typescript/no-explicit-any": "warn",
                "typescript/no-non-null-assertion": "off",
                "vitest/require-mock-type-parameters": "off",
                "vitest/no-conditional-expect": "off",
                "typescript/unbound-method": "off",
            },
        },
        {
            files: ["vite.config.*", "vitest.config.*", "*.config.ts", "scripts/**"],
            env: {node: true},
            rules: {
                "no-console": "off",
                "typescript/no-explicit-any": "warn",
                "no-magic-numbers": "off",
            },
        },
        {
            files: ["**/*.d.ts"],
            rules: {
                "typescript/no-explicit-any": "off",
                "no-magic-numbers": "off",
            },
        },
    ],
});
