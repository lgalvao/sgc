import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ProcessoDiagnosticoAlert from "../ProcessoDiagnosticoAlert.vue";

describe("ProcessoDiagnosticoAlert.vue", () => {
    const stubs = {
        Alerta: {
            template: '<div class="alert"><slot /><button class="close" @click="$emit(\'dismissed\')">x</button></div>',
            props: ['mensagem', 'variante', 'dispensavel']
        },
        BSpinner: {template: '<div class="spinner" />'},
        RouterLink: {template: '<a :href="to"><slot /></a>', props: ['to']}
    };

    it("não renderiza nada se exibir for false", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: false,
                resumo: "",
                grupos: []
            },
            global: {stubs}
        });
        expect(wrapper.find(".alert").exists()).toBe(false);
    });

    it("renderiza spinner quando carregando for true", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                carregando: true,
                resumo: "",
                grupos: []
            },
            global: {stubs}
        });
        expect(wrapper.find(".spinner").exists()).toBe(true);
        expect(wrapper.text()).toContain("Validando informações organizacionais...");
    });

    it("renderiza lista de unidades sem responsável (singular)", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [],
                unidadesSemResponsavel: [{codigo: 1, sigla: "UNIT1"}]
            },
            global: {stubs}
        });
        const text = wrapper.text().replace(/\s+/g, ' ');
        expect(text).toContain("A unidade UNIT1 está atualmente sem responsável.");
        expect(wrapper.find("a").attributes("href")).toBe("/unidade/1");
    });

    it("renderiza lista de unidades sem responsável (plural)", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [],
                unidadesSemResponsavel: [
                    {codigo: 1, sigla: "UNIT1"},
                    {codigo: 2, sigla: "UNIT2"}
                ]
            },
            global: {stubs}
        });
        const text = wrapper.text().replace(/\s+/g, ' ');
        expect(text).toContain("As unidades UNIT1 e UNIT2 estão atualmente sem responsável.");
    });

    it("renderiza lista de unidades sem responsável (plural com 3+)", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [],
                unidadesSemResponsavel: [
                    {codigo: 1, sigla: "UNIT1"},
                    {codigo: 2, sigla: "UNIT2"},
                    {codigo: 3, sigla: "UNIT3"}
                ]
            },
            global: {stubs}
        });
        const text = wrapper.text().replace(/\s+/g, ' ');
        expect(text).toContain("As unidades UNIT1, UNIT2, e UNIT3 estão atualmente sem responsável.");
    });

    it("renderiza unidade sem código como texto em vez de link", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [],
                unidadesSemResponsavel: [{codigo: null, sigla: "UNIT_NULL"}]
            },
            global: {stubs}
        });
        expect(wrapper.text()).toContain("UNIT_NULL");
        expect(wrapper.find("a").exists()).toBe(false);
    });

    it("renderiza resumo e grupos quando não há unidades sem responsável", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "Resumo do diagnóstico",
                grupos: [{tipo: "Erro Tipo A", quantidadeOcorrencias: 5}],
                unidadesSemResponsavel: []
            },
            global: {stubs}
        });
        expect(wrapper.text()).toContain("Há unidades sem responsável atual.");
        expect(wrapper.text()).toContain("Resumo do diagnóstico");
        expect(wrapper.text()).toContain("Erro Tipo A: 5 ocorrência(s)");
    });

    it("emite dismiss quando o alerta é fechado", async () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: []
            },
            global: {stubs}
        });
        await wrapper.find(".close").trigger("click");
        expect(wrapper.emitted("dismiss")).toBeTruthy();
    });
});
