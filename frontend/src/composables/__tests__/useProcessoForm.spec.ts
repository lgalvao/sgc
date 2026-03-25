import {describe, expect, it} from "vitest";
import {useProcessoForm} from "../useProcessoForm";
import {TipoProcesso} from "@/types/tipos";
import {flushPromises} from "@vue/test-utils";
import {obterAmanhaFormatado} from "@/utils/dateUtils";

describe("useProcessoForm", () => {
    it("deve inicializar com valores vazios", () => {
        const form = useProcessoForm();
        expect(form.descricao.value).toBe("");
        expect(form.isFormInvalid.value).toBe(true);
    });

    it("deve inicializar com dados iniciais", () => {
        const amanha = obterAmanhaFormatado();
        const initialData: any = {
            descricao: "Teste",
            tipo: TipoProcesso.MAPEAMENTO,
            dataLimite: `${amanha}T00:00:00`,
            unidades: [{codUnidade: 1}]
        };
        const form = useProcessoForm(initialData);
        expect(form.descricao.value).toBe("Teste");
        expect(form.dataLimite.value).toBe(amanha);
        expect(form.unidadesSelecionadas.value).toEqual([1]);
        expect(form.isFormInvalid.value).toBe(false);
    });

    it("deve construir request de criacao", () => {
        const amanha = obterAmanhaFormatado();
        const form = useProcessoForm();
        form.descricao.value = "Novo";
        form.tipo.value = TipoProcesso.REVISAO;
        form.dataLimite.value = amanha;
        form.unidadesSelecionadas.value = [10];

        const req = form.construirCriarRequest();
        expect(req.descricao).toBe("Novo");
        expect(req.dataLimiteEtapa1).toBe(`${amanha}T00:00:00`);
    });

    it("deve construir request de atualizacao", () => {
        const amanha = obterAmanhaFormatado();
        const form = useProcessoForm();
        form.descricao.value = "Update";
        form.tipo.value = TipoProcesso.MAPEAMENTO;
        form.dataLimite.value = amanha;
        form.unidadesSelecionadas.value = [10];

        const req = form.construirAtualizarRequest(5);
        expect(req.codigo).toBe(5);
        expect(req.descricao).toBe("Update");
    });

    it("deve limpar o formulario", () => {
        const form = useProcessoForm();
        form.descricao.value = "Suja";
        form.limpar();
        expect(form.descricao.value).toBe("");
    });

    it("deve limpar erros ao observar mudanças", async () => {
        const form = useProcessoForm();
        form.fieldErrors.value.descricao = "Erro";

        form.descricao.value = "Nova Descricao";
        await flushPromises();
        expect(form.fieldErrors.value.descricao).toBe("");

        form.fieldErrors.value.tipo = "Erro Tipo";
        form.tipo.value = TipoProcesso.MAPEAMENTO;
        await flushPromises();
        expect(form.fieldErrors.value.tipo).toBe("");

        form.fieldErrors.value.dataLimite = "Erro Data";
        form.dataLimite.value = obterAmanhaFormatado();
        await flushPromises();
        expect(form.fieldErrors.value.dataLimite).toBe("");

        form.fieldErrors.value.unidades = "Erro Unidades";
        form.unidadesSelecionadas.value = [1];
        await flushPromises();
        expect(form.fieldErrors.value.unidades).toBe("");
    });

    it("não deve disparar erro de data futura se a data estiver incompleta", async () => {
        const form = useProcessoForm();
        form.dataLimite.value = "202"; // Incompleta
        await flushPromises();
        expect(form.fieldErrors.value.dataLimite).toBe("");
        expect(form.isFormInvalid.value).toBe(true); // Mas o formulário ainda é inválido
    });

    it("deve disparar erro de data futura se a data estiver completa e for passada", async () => {
        const form = useProcessoForm();
        form.dataLimite.value = "2020-01-01"; // Completa mas no passado
        await flushPromises();
        expect(form.fieldErrors.value.dataLimite).toBe("A data limite deve ser uma data futura.");
        expect(form.isFormInvalid.value).toBe(true);
    });

    it("deve habilitar o formulário apenas com data completa e futura", async () => {
        const form = useProcessoForm();
        form.descricao.value = "Teste";
        form.tipo.value = TipoProcesso.MAPEAMENTO;
        form.unidadesSelecionadas.value = [1];
        
        form.dataLimite.value = "202"; // Incompleta
        await flushPromises();
        expect(form.isFormInvalid.value).toBe(true);

        form.dataLimite.value = obterAmanhaFormatado(); // Completa e futura
        await flushPromises();
        expect(form.isFormInvalid.value).toBe(false);
    });

    it("deve construir requests com data nula", () => {
        const form = useProcessoForm();
        form.descricao.value = "X";
        form.tipo.value = TipoProcesso.REVISAO;
        form.dataLimite.value = "";

        const req1 = form.construirCriarRequest();
        expect(req1.dataLimiteEtapa1).toBeNull();

        const req2 = form.construirAtualizarRequest(1);
        expect(req2.dataLimiteEtapa1).toBeNull();
    });
});
