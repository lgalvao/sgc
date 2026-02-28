import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import SubprocessoHeader from "../processo/SubprocessoHeader.vue";

describe("SubprocessoHeader.vue", () => {
    const defaultProps = {
        processoDescricao: "Processo Teste",
        unidadeSigla: "TEST",
        unidadeNome: "Unidade de Teste",
        situacao: "Em Andamento",
        titularNome: "João Titular",
        titularRamal: "1234",
        titularEmail: "joao@teste.com",
        podeAlterarDataLimite: false,
    };

    const createWrapper = (props = {}) => {
        return mount(SubprocessoHeader, {
            props: {...defaultProps, ...props},
            global: {
                stubs: {
                    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<div><slot /></div>'},
                    BCardBody: {template: '<div><slot /></div>'}
                }
            }
        });
    };

    it("deve renderizar as informações básicas", () => {
        const wrapper = createWrapper();
        expect(wrapper.text()).toContain("Processo: Processo Teste");
        expect(wrapper.text()).toContain("TEST");
        expect(wrapper.text()).toContain("Unidade de Teste");
        expect(wrapper.text()).toContain("João Titular");
    });

    it("deve renderizar informações do responsável se diferente do titular", () => {
        const wrapper = createWrapper({
            responsavelNome: "Maria Responsavel",
            responsavelRamal: "5678",
            responsavelEmail: "maria@teste.com"
        });
        expect(wrapper.text()).toContain("Responsável: Maria Responsavel");
        expect(wrapper.text()).toContain("5678");
    });

    it("não deve renderizar responsável se for igual ao titular", () => {
        const wrapper = createWrapper({
            responsavelNome: "João Titular",
            titularNome: "João Titular"
        });
        expect(wrapper.text()).not.toContain("Responsável:");
    });

    it("deve emitir 'alterarDataLimite' ao clicar no botão", async () => {
        const wrapper = createWrapper({podeAlterarDataLimite: true});
        const btn = wrapper.find('[data-testid="btn-alterar-data-limite"]');
        expect(btn.exists()).toBe(true);
        await btn.trigger("click");
        expect(wrapper.emitted("alterarDataLimite")).toBeTruthy();
    });

    it("deve emitir 'reabrirCadastro' ao clicar no botão", async () => {
        const wrapper = createWrapper({podeReabrirCadastro: true});
        const btn = wrapper.find('[data-testid="btn-reabrir-cadastro"]');
        expect(btn.exists()).toBe(true);
        await btn.trigger("click");
        expect(wrapper.emitted("reabrirCadastro")).toBeTruthy();
    });

    it("deve emitir 'reabrirRevisao' ao clicar no botão", async () => {
        const wrapper = createWrapper({podeReabrirRevisao: true});
        const btn = wrapper.find('[data-testid="btn-reabrir-revisao"]');
        expect(btn.exists()).toBe(true);
        await btn.trigger("click");
        expect(wrapper.emitted("reabrirRevisao")).toBeTruthy();
    });

    it("deve emitir 'enviarLembrete' ao clicar no botão", async () => {
        const wrapper = createWrapper({podeEnviarLembrete: true});
        const btn = wrapper.find('[data-testid="btn-enviar-lembrete"]');
        expect(btn.exists()).toBe(true);
        await btn.trigger("click");
        expect(wrapper.emitted("enviarLembrete")).toBeTruthy();
    });
});
