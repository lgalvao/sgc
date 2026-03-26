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
            "build/**"
        ],
    },
    pluginJs.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ["etc/scripts/**/*.js", "etc/scripts/**/*.mjs", "*.config.js", "e2e/**/*.js", "summarize-lint.js"],
        languageOptions: {
            globals: {
                ...globals.node,
            },
        },
    },
    {
        files: ["e2e/**/*.ts"],
        plugins: {
            playwright: pluginPlaywright,
        },
        rules: {
            "playwright/expect-expect": ["warn", {
                "assertFunctionNames": [
                    "validarProcessoFixture",
                    "validarProcesso",
                    "verificarToast",
                    "verificarAppAlert",
                    "verificarAlertaPainel",
                    "verificarPaginaPainel",
                    "verificarProcessoNaTabela",
                    "verificarDetalhesProcesso",
                    "verificarUnidadeParticipante",
                    "verificarDetalhesSubprocesso",
                    "verificarSituacaoSubprocesso",
                    "verificarBotaoDisponibilizar",
                    "verificarBotaoImpactoDropdown",
                    "verificarBotaoHistoricoAnalise",
                    "verificarBotaoImpactoDireto",
                    "verificarBotaoImpactoAusenteEdicao",
                    "verificarBotaoImpactoAusenteDireto",
                    "verificarOpcoesImportacao",
                    "verificarOpcoesImportacaoVazia",
                    "verificarAcoesAnaliseCadastroVisualizacao",
                    "esperarPaginaPainel",
                    "esperarPaginaCadastroProcesso",
                    "esperarPaginaDetalhesProcesso",
                    "esperarPaginaSubprocesso",
                    "aguardarProcessoNoPainel",
                    "confirmarInicioProcessoPeloDialogo"
                ]
            }],
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
    {
        files: ["e2e/captura.spec.ts", "e2e/smoke.spec.ts"],
        rules: {
            "playwright/expect-expect": "off"
        }
    },
    eslintConfigPrettier,
];
