import {describe, expect, it} from "vitest";
import type {Unidade} from "@/types/tipos";
import {filtrarSelecionadasPorElegibilidade, removerUnidadesSemEquipe} from "@/views/processoCadastroUnidades";

function criarUnidade(codigo: number, tipo?: string, isElegivel?: boolean, filhas?: Unidade[]): Unidade {
    return {
        codigo,
        nome: `Unidade ${codigo}`,
        sigla: `U${codigo}`,
        tipo,
        isElegivel,
        filhas
    };
}

describe("processoCadastroUnidades", () => {
    it("deve remover nós SEM_EQUIPE preservando descendentes", () => {
        const arvore = [
            criarUnidade(1, "OPERACIONAL", true, [
                criarUnidade(2, "SEM_EQUIPE", false, [
                    criarUnidade(3, "OPERACIONAL", true),
                ]),
            ]),
        ];

        const resultado = removerUnidadesSemEquipe(arvore);

        expect(resultado).toHaveLength(1);
        expect(resultado[0].filhas?.map(u => u.codigo)).toEqual([3]);
    });

    it("deve filtrar selecionadas por elegibilidade da árvore", () => {
        const arvore = [
            criarUnidade(10, "OPERACIONAL", true),
            criarUnidade(11, "OPERACIONAL", false, [
                criarUnidade(12, "OPERACIONAL", true),
            ]),
        ];

        const resultado = filtrarSelecionadasPorElegibilidade([10, 11, 12, 99], arvore);

        expect(resultado).toEqual([10, 12]);
    });
});
