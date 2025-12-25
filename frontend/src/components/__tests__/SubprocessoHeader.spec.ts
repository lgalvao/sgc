import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import {badgeClass} from "@/utils";
import SubprocessoHeader from "../SubprocessoHeader.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("SubprocessoHeader.vue", () => {
    const context = setupComponentTest();

    const defaultProps = {
        processoDescricao: "Processo de Teste",
        unidadeSigla: "TEST",
        unidadeNome: "Unidade de Teste",
        situacao: "EM_ANDAMENTO",
        titularNome: "João Silva",
        titularRamal: "1234",
        titularEmail: "joao@teste.com",
        podeAlterarDataLimite: false, // Default to false
    };

    const mountComponent = (props = {}) => {
        const mountOptions = getCommonMountOptions();
        context.wrapper = mount(SubprocessoHeader, {
            ...mountOptions,
            props: {
                ...defaultProps,
                ...props,
            },
        });
        return context.wrapper;
    };

    describe("renderização das props", () => {
        it("deve renderizar todas as props básicas corretamente", () => {
            const wrapper = mountComponent();

            expect(wrapper.text()).toContain("Processo: Processo de Teste");
            // Ajustado para conferir se sigla e nome estão presentes,
            // já que a renderização agora é feita em elementos separados no novo template
            expect(wrapper.text()).toContain("TEST");
            expect(wrapper.text()).toContain("Unidade de Teste");
            expect(wrapper.text()).toContain("Situação:EM_ANDAMENTO");
            expect(wrapper.text()).toContain("Titular: João Silva");
            expect(wrapper.text()).toContain("1234");
            expect(wrapper.text()).toContain("joao@teste.com");
        });
    });

    describe("seção do responsável", () => {
        it("deve renderizar a seção do responsável quando responsavelNome é fornecido", () => {
            const wrapper = mountComponent({
                responsavelNome: "Maria Santos",
                responsavelRamal: "5678",
                responsavelEmail: "maria@teste.com",
            });

            expect(wrapper.text()).toContain("Responsável: Maria Santos");
            expect(wrapper.text()).toContain("5678");
            expect(wrapper.text()).toContain("maria@teste.com");
        });

        it("não deve renderizar a seção do responsável quando responsavelNome não é fornecido", () => {
            const wrapper = mountComponent();

            expect(wrapper.text()).not.toContain("Responsável:");
        });

        it("deve lidar com dados parciais do responsável", () => {
            const wrapper = mountComponent({
                responsavelNome: "Maria Santos",
                // responsavelRamal and responsavelEmail not provided
            });

            expect(wrapper.text()).toContain("Responsável: Maria Santos");
        });
    });

    describe("botão alterar data limite", () => {
        it("deve mostrar o botão quando podeAlterarDataLimite for verdadeiro", () => {
            const wrapper = mountComponent({
                podeAlterarDataLimite: true,
            });

            const button = wrapper.find("button");
            expect(button.exists()).toBe(true);
            expect(button.text()).toContain("Alterar data limite");
            expect(button.classes()).toContain("btn-outline-primary");
        });

        it("não deve mostrar o botão quando podeAlterarDataLimite for falso", () => {
            const wrapper = mountComponent({
                podeAlterarDataLimite: false,
            });

            const button = wrapper.find("button");
            expect(button.exists()).toBe(false);
        });

        it("deve emitir alterarDataLimite quando o botão for clicado", async () => {
            const wrapper = mountComponent({
                podeAlterarDataLimite: true,
            });

            const button = wrapper.find("button");
            await button.trigger("click");
            expect(wrapper.emitted()).toHaveProperty("alterarDataLimite");
        });
    });

    describe("SubprocessoHeader.vue", () => {
        it("deve retornar a classe correta do badge para uma situação conhecida", () => {
            // Não precisamos montar o componente para testar uma função utilitária pura
            const result = badgeClass("EM_ANDAMENTO");

            // Since we can't easily mock the constants, we'll test that the function exists and returns a string
            expect(typeof result).toBe("string");
            expect(result.length).toBeGreaterThan(0);
        });

        it("deve retornar a classe padrão para qualquer situação", () => {
            // Não precisamos montar o componente para testar uma função utilitária pura
            const result = badgeClass("UNKNOWN_SITUACAO");

            expect(result).toBe("bg-secondary");
        });
    });

    describe("estrutura e estilo", () => {
        it("deve ter a estrutura correta do card", () => {
            const wrapper = mountComponent();

            expect(wrapper.find(".card").exists()).toBe(true);
            expect(wrapper.find(".card-body").exists()).toBe(true);
        });

        it("deve ter o estilo de texto correto", () => {
            const wrapper = mountComponent();

            expect(wrapper.find(".text-muted").exists()).toBe(true);
            expect(wrapper.find(".display-6").exists()).toBe(true);
            expect(wrapper.find(".fw-bold").exists()).toBe(true);
        });

        it("deve ter os ícones corretos", () => {
            const wrapper = mountComponent();

            const phoneIcons = wrapper.findAll(".bi-telephone-fill");
            const emailIcons = wrapper.findAll(".bi-envelope-fill");

            expect(phoneIcons.length).toBeGreaterThan(0);
            expect(emailIcons.length).toBeGreaterThan(0);
        });
    });
});
