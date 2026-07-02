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
                grupos: [{
                    tipo: "Unidade sem responsável",
                    quantidadeOcorrencias: 1,
                    ocorrencias: ["sigla=UNIT1"]
                }],
                unidadesSemResponsavel: [{codigo: 1, sigla: "UNIT1"}]
            },
            global: {stubs}
        });
        const text = wrapper.text().replace(/\s+/g, ' ');
        expect(text).toContain("Há inconsistências nos dados organizacionais");
        expect(text).toContain("Unidades sem titular ou responsável:");
        expect(text).toContain("UNIT1");
        expect(wrapper.find("a").attributes("href")).toBe("/unidade/1");
    });

    it("renderiza lista de unidades sem responsável (plural)", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [{
                    tipo: "Unidade sem responsável",
                    quantidadeOcorrencias: 2,
                    ocorrencias: ["sigla=UNIT1", "sigla=UNIT2"]
                }],
                unidadesSemResponsavel: [
                    {codigo: 1, sigla: "UNIT1"},
                    {codigo: 2, sigla: "UNIT2"}
                ]
            },
            global: {stubs}
        });
        const text = wrapper.text().replace(/\s+/g, ' ');
        expect(text).toContain("UNIT1");
        expect(text).toContain("UNIT2");
    });

    it("renderiza lista de unidades sem responsável (plural com 3+)", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [{
                    tipo: "Unidade sem responsável",
                    quantidadeOcorrencias: 3,
                    ocorrencias: ["sigla=UNIT1", "sigla=UNIT2", "sigla=UNIT3"]
                }],
                unidadesSemResponsavel: [
                    {codigo: 1, sigla: "UNIT1"},
                    {codigo: 2, sigla: "UNIT2"},
                    {codigo: 3, sigla: "UNIT3"}
                ]
            },
            global: {stubs}
        });
        const itens = wrapper.findAll("li").map((item) => item.text());
        expect(itens).toEqual(["UNIT1", "UNIT2", "UNIT3"]);
    });

    it("renderiza unidade sem código como texto em vez de link", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "",
                grupos: [{
                    tipo: "Unidade sem responsável",
                    quantidadeOcorrencias: 1,
                    ocorrencias: ["sigla=UNIT_NULL"]
                }],
                unidadesSemResponsavel: [{codigo: null, sigla: "UNIT_NULL"}]
            },
            global: {stubs}
        });
        expect(wrapper.text()).toContain("UNIT_NULL");
        expect(wrapper.find("a").exists()).toBe(false);
    });

    it("renderiza grupos formatados quando não há unidades sem responsável", () => {
        const wrapper = mount(ProcessoDiagnosticoAlert, {
            props: {
                exibir: true,
                resumo: "Foram encontradas inconsistências nos dados organizacionais.",
                grupos: [
                    {
                        tipo: "Usuario sem e-mail na VW_USUARIO",
                        quantidadeOcorrencias: 2,
                        ocorrencias: [
                            "sigla=117ª Z.E., titulo=1, nome=TEREZA CRISTINA DE MEDEIROS",
                            "sigla=SGP, titulo=2, nome=ELISIE MARIA JUNQUEIRA AYRES ROCHA"
                        ]
                    }
                ],
                unidadesSemResponsavel: []
            },
            global: {stubs}
        });
        const texto = wrapper.text().replace(/\s+/g, " ");
        const ocorrenciasMensagem = texto.match(/Foram encontradas inconsistências nos dados organizacionais/g) ?? [];
        expect(ocorrenciasMensagem).toHaveLength(1);
        expect(texto).toContain("Usuários sem e-mail:");
        expect(texto).toContain("TEREZA CRISTINA DE MEDEIROS (117ª Z.E.)");
        expect(texto).toContain("ELISIE MARIA JUNQUEIRA AYRES ROCHA (SGP)");
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
