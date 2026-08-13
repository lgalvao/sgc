import {describe, expect, test} from "vitest";
import {lerNumero, lerOpcao, validarArgumentos} from "../lib/cli-opcoes.js";

describe("Leitura de opções da CLI", () => {
    test("aceita opção separada e opção por atribuição", () => {
        expect(lerOpcao(["--base", "/tmp/projeto"], "--base", undefined)).toBe("/tmp/projeto");
        expect(lerOpcao(["--base=/tmp/projeto"], "--base", undefined)).toBe("/tmp/projeto");
    });

    test("preserva padrão quando a opção não foi informada", () => {
        expect(lerNumero([], "--limite", 20, {minimo: 0})).toBe(20);
        expect(lerNumero([], "--meta", undefined, {inteiro: false})).toBeUndefined();
    });

    test("valida inteiros, decimais e limites declarados", () => {
        expect(lerNumero(["--limite", "3"], "--limite", 20, {minimo: 0})).toBe(3);
        expect(lerNumero(["--meta=87.5"], "--meta", 0, {inteiro: false, minimo: 0, maximo: 100})).toBe(87.5);

        expect(() => lerNumero(["--limite", "3.5"], "--limite", 20)).toThrow("número inteiro");
        expect(() => lerNumero(["--limite", "3abc"], "--limite", 20)).toThrow("número inteiro");
        expect(() => lerNumero(["--limite", "-1"], "--limite", 20, {minimo: 0})).toThrow("maior ou igual");
        expect(() => lerNumero(["--meta", "101"], "--meta", 0, {inteiro: false, maximo: 100})).toThrow("menor ou igual");
    });

    test("falha quando uma opção exige valor", () => {
        expect(() => lerOpcao(["--base"], "--base", undefined)).toThrow("Informe um valor");
        expect(() => lerNumero(["--limite", "--json"], "--limite", 20)).toThrow("Informe um valor");
    });

    test("valida opções e normaliza a forma com atribuição", () => {
        expect(validarArgumentos(
            ["--base=/tmp/projeto", "--json", "--diretorio", "frontend", "--diretorio=backend"],
            {
                opcoesComValor: ["--base", "--diretorio"],
                opcoesBooleanas: ["--json"],
                minimoPosicionais: 0,
                maximoPosicionais: 0
            }
        )).toEqual(["--base", "/tmp/projeto", "--json", "--diretorio", "frontend", "--diretorio", "backend"]);
    });

    test("rejeita opção desconhecida, valor ausente e booleano com valor", () => {
        const esquema = {
            opcoesComValor: ["--base"],
            opcoesBooleanas: ["--json"],
            minimoPosicionais: 0,
            maximoPosicionais: 0
        } as const;

        expect(() => validarArgumentos(["--inexistente"], esquema)).toThrow("Opção desconhecida");
        expect(() => validarArgumentos(["--base", "--json"], esquema)).toThrow("Informe um valor");
        expect(() => validarArgumentos(["--json=true"], esquema)).toThrow("não recebe valor");
    });

    test("valida a quantidade de posicionais declarada pelo comando", () => {
        const esquema = {
            opcoesComValor: ["--base"],
            opcoesBooleanas: [],
            minimoPosicionais: 1,
            maximoPosicionais: 1
        } as const;

        expect(validarArgumentos(["1.2.3", "--base=/tmp/projeto"], esquema)).toEqual(["1.2.3", "--base", "/tmp/projeto"]);
        expect(() => validarArgumentos([], esquema)).toThrow("esperado 1");
        expect(() => validarArgumentos(["1.2.3", "extra"], esquema)).toThrow("recebido 2");
    });
});
