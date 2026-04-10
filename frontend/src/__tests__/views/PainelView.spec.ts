import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import PainelView from "@/views/PainelView.vue";
import {createTestingPinia} from "@pinia/testing";
import * as painelService from "@/services/painelService";
import {useRouter} from "vue-router";

vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    createRouter: vi.fn(() => ({
        push: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

const mockPageVazia = {content: [], totalPages: 0, totalElements: 0, number: 0, size: 10, first: true, last: true, empty: true};

vi.mock("@/services/painelService", () => ({
    listarProcessos: vi.fn(),
    listarAlertas: vi.fn(),
    marcarAlertasLidos: vi.fn().mockResolvedValue(undefined),
}));

describe("PainelView.vue", () => {
    let routerPushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
        (painelService.listarAlertas as any).mockResolvedValue(mockPageVazia);
        (painelService.listarProcessos as any).mockResolvedValue(mockPageVazia);
    });

    const initialState = {
        perfil: {
            perfilSelecionado: "ADMIN",
            unidadeSelecionada: 1,
            usuarioCodigo: 100,
            perfis: [] as string[],
            permissoesSessao: {
                mostrarCriarProcesso: true
            }
        },
        processos: {
            processosPainel: []
        },
    };

    const mountOptions = (customState = {}) => ({
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        perfil: {...initialState.perfil, ...(customState as any).perfil},
                        processos: {...initialState.processos, ...(customState as any).processos},
                        painel: {alertas: [], processos: [], invalido: false, ...(customState as any).painel},
                    },
                    stubActions: true,
                }),
            ],
            stubs: {
                BContainer: {template: '<div><slot /></div>'},
                BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                PageHeader: {
                    name: 'PageHeader',
                    props: ['title'],
                    template: '<div data-testid="page-header">{{ title }} <slot name="actions" /></div>'
                },
                TabelaProcessos: {
                    name: 'TabelaProcessos',
                    props: ['processos', 'criterioOrdenacao', 'direcaoOrdenacaoAsc'],
                    emits: ['ordenar', 'selecionar-processo'],
                    template: '<div data-testid="tabela-processos"></div>'
                },
            },
        },
    });

    it("deve mostrar estado vazio de alertas sem renderizar a tabela", async () => {
        const wrapper = mount(PainelView, mountOptions());
        await flushPromises();

        const headers = wrapper.findAllComponents({name: 'PageHeader'});
        expect(headers).toHaveLength(2);
        expect(headers[0].props('title')).toBe('Processos');
        expect(headers[1].props('title')).toBe('Alertas');

        expect(wrapper.findComponent({name: 'TabelaProcessos'}).exists()).toBe(true);
        expect(wrapper.find('[data-testid="empty-state-alertas"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-alertas"]').exists()).toBe(false);
    });

    it("deve renderizar a tabela de alertas quando houver itens", async () => {
        const alertaMock = {
            codigo: 1,
            codProcesso: 10,
            unidadeOrigem: 'SEC',
            unidadeDestino: 'CGP',
            descricao: 'Alerta',
            dataHora: '2026-03-18T10:00:00',
            dataHoraLeitura: null,
            mensagem: 'Processo pendente',
            origem: 'Secretaria',
            processo: 'Processo 10',
        };
        // stubActions=true impede as actions de executar — populamos o estado diretamente
        const wrapper = mount(PainelView, mountOptions({
            painel: {alertas: [alertaMock], processos: [], invalido: false}
        }));
        await flushPromises();

        expect(wrapper.find('[data-testid="tbl-alertas"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="empty-state-alertas"]').exists()).toBe(false);
    });

    it("deve carregar dados ao montar se perfil estiver selecionado", async () => {
        const wrapper = mount(PainelView, mountOptions());

        await wrapper.vm.$nextTick();

        expect(painelService.listarProcessos).toHaveBeenCalledWith({codUnidade: 1, page: 0, size: 10});
        expect(painelService.listarAlertas).toHaveBeenCalledWith({codUnidade: 1, page: 0, size: 200, sort: "dataHora", order: "desc"});
    });

    it("não deve carregar dados se perfil não estiver selecionado", async () => {
        const wrapper = mount(PainelView, mountOptions({
            perfil: {perfilSelecionado: null, unidadeSelecionada: null}
        }));

        await wrapper.vm.$nextTick();

        expect(painelService.listarProcessos).not.toHaveBeenCalled();
        expect(painelService.listarAlertas).not.toHaveBeenCalled();
    });

    it("deve mostrar botão de criar processo apenas para admin", async () => {
        // Admin
        const wrapperAdmin = mount(PainelView, mountOptions({
            perfil: {
                perfilSelecionado: "ADMIN",
                permissoesSessao: {mostrarCriarProcesso: true}
            }
        }));
        await flushPromises();
        expect(wrapperAdmin.find('[data-testid="btn-painel-criar-processo"]').exists()).toBe(true);

        // Not admin
        const wrapperUser = mount(PainelView, mountOptions({
            perfil: {
                perfilSelecionado: "USER",
                permissoesSessao: {mostrarCriarProcesso: false}
            }
        }));
        await flushPromises();
        expect(wrapperUser.find('[data-testid="btn-painel-criar-processo"]').exists()).toBe(false);
    });

    it("deve reordenar processos localmente sem chamar o backend", async () => {
        const wrapper = mount(PainelView, mountOptions());
        await flushPromises();

        vi.clearAllMocks(); // Limpa chamadas do onMounted

        await wrapper.findComponent({name: 'TabelaProcessos'}).vm.$emit('ordenar', 'dataCriacao');

        // Ordenação é agora local — não deve chamar o backend
        expect(painelService.listarProcessos).not.toHaveBeenCalled();
    });

    it("deve navegar ao selecionar processo", async () => {
        const wrapper = mount(PainelView, mountOptions());
        await flushPromises();

        const processoMock = {codigo: 1, descricao: "Teste", linkDestino: "/processo/1"};
        await wrapper.findComponent({name: 'TabelaProcessos'}).vm.$emit('selecionar-processo', processoMock);

        expect(routerPushMock).toHaveBeenCalledWith("/processo/1");
    });

    it("deve cobrir branches de abrirDetalhesProcesso, rowClassAlerta, rowAttrAlerta", async () => {
        const wrapper = mount(PainelView, mountOptions());
        await wrapper.vm.$nextTick();

        // abrirDetalhesProcesso
        await (wrapper.vm as any).abrirDetalhesProcesso(null);
        expect(routerPushMock).not.toHaveBeenCalled();
        
        await (wrapper.vm as any).abrirDetalhesProcesso({ codigo: 1 }); // without linkDestino
        expect(routerPushMock).not.toHaveBeenCalled();

        // rowClassAlerta
        expect((wrapper.vm as any).rowClassAlerta(null)).toBe("");
        expect((wrapper.vm as any).rowClassAlerta({ dataHoraLeitura: "2023-01-01" })).toBe("");
        expect((wrapper.vm as any).rowClassAlerta({ dataHoraLeitura: null })).toBe("fw-bold");

        // rowAttrAlerta
        expect((wrapper.vm as any).rowAttrAlerta(null)).toEqual({});
        expect((wrapper.vm as any).rowAttrAlerta({ codigo: 123 })).toEqual({ 'data-testid': 'row-alerta-123' });
    });
});
