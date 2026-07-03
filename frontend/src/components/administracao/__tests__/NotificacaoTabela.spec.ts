import {describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import NotificacaoTabela from "../NotificacaoTabela.vue";
import {createTestingPinia} from "@pinia/testing";
import type {Notificacao} from "@/services/notificacaoService";

// Mock das utilidades de formatação
vi.mock("@/utils/notificacaoFormatters", () => ({
    formatarAssunto: vi.fn((v) => `Assunto: ${v}`),
    formatarDestinatario: vi.fn((i) => i.destinatario),
    formatarQuando: vi.fn(() => "Hoje"),
    resumirContexto: vi.fn(() => "Resumo do contexto")
}));

describe("NotificacaoTabela.vue", () => {
    const items: Notificacao[] = [
        {
            codigo: 1,
            destinatario: "usuario@teste.com",
            processoDescricao: "Processo teste",
            unidadeOrigemSigla: "SECAO_221",
            tipoNotificacao: "PROCESSO_INICIADO",
            assunto: "Teste",
            situacao: "ENVIADO",
            dataHoraEnvio: "2023-01-01T10:00:00",
            corpoHtml: "<p>Olá</p>"
        } as any
    ];

    it("deve exibir empty state quando não houver itens", () => {
        const wrapper = mount(NotificacaoTabela, {
            props: {items: []},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        expect(wrapper.find('[data-testid="alert-notificacoes-sem-registros"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-notificacoes"]').exists()).toBe(false);
    });

    it("deve renderizar a tabela quando houver itens", () => {
        const wrapper = mount(NotificacaoTabela, {
            props: {items},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        expect(wrapper.find('[data-testid="tbl-notificacoes"]').exists()).toBe(true);
        expect(wrapper.text()).toContain("usuario@teste.com");
        expect(wrapper.text()).toContain("Processo teste");
        expect(wrapper.text()).toContain("SECAO_221");
    });

    it("deve emitir 'detalhes' ao clicar no botão de info", async () => {
        const wrapper = mount(NotificacaoTabela, {
            props: {items},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        await wrapper.find('[data-testid="btn-detalhes-1"]').trigger("click");
        expect(wrapper.emitted("detalhes")).toHaveLength(1);
        expect(wrapper.emitted("detalhes")![0]).toEqual([items[0]]);
    });

    it("deve emitir 'preview' ao clicar no botão de olho", async () => {
        const wrapper = mount(NotificacaoTabela, {
            props: {items},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        await wrapper.find('[data-testid="btn-preview-1"]').trigger("click");
        expect(wrapper.emitted("preview")).toHaveLength(1);
        expect(wrapper.emitted("preview")![0]).toEqual([items[0]]);
    });

    it("deve exibir botão de reenvio apenas para FALHA_DEFINITIVA", async () => {
        const itemsComFalha = [
            {...items[0], codigo: 2, situacao: "FALHA_DEFINITIVA"} as any
        ];
        const wrapper = mount(NotificacaoTabela, {
            props: {items: itemsComFalha},
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
            }
        });
        expect(wrapper.find('[data-testid="btn-notificacoes-reenviar-2"]').exists()).toBe(true);

        await wrapper.find('[data-testid="btn-notificacoes-reenviar-2"]').trigger("click");
        expect(wrapper.emitted("reenviar")).toHaveLength(1);
    });
});
