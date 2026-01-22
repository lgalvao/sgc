import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import PainelView from "@/views/PainelView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useProcessosStore} from "@/stores/processos";
import {useAlertasStore} from "@/stores/alertas";
import {useRouter} from "vue-router";

// Mock router
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

describe("PainelView.vue", () => {
    let routerPushMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
    });

    const initialState = {
        perfil: {
            perfilSelecionado: "ADMIN",
            unidadeSelecionada: 1,
            usuarioCodigo: 100,
            perfis: [] as string[]
        },
        processos: {
            processosPainel: []
        },
        alertas: {
            alertas: []
        }
    };

    const mountOptions = (customState = {}) => ({
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: {
                        perfil: {...initialState.perfil, ...(customState as any).perfil},
                        processos: {...initialState.processos, ...(customState as any).processos},
                        alertas: {...initialState.alertas, ...(customState as any).alertas},
                    },
                    stubActions: true,
                }),
            ],
            stubs: {
                BContainer: {template: '<div><slot /></div>'},
                BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
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
                TabelaAlertas: {
                    name: 'TabelaAlertas',
                    props: ['alertas'],
                    emits: ['ordenar'],
                    template: '<div data-testid="tabela-alertas"></div>'
                }
            },
        },
    });

    it("deve renderizar tabelas e títulos", () => {
        const wrapper = mount(PainelView, mountOptions());

        const headers = wrapper.findAllComponents({ name: 'PageHeader' });
        expect(headers).toHaveLength(2);
        expect(headers[0].props('title')).toBe('Processos');
        expect(headers[1].props('title')).toBe('Alertas');

        expect(wrapper.findComponent({name: 'TabelaProcessos'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'TabelaAlertas'}).exists()).toBe(true);
    });

    it("deve carregar dados ao montar se perfil estiver selecionado", async () => {
        const wrapper = mount(PainelView, mountOptions());
        const processosStore = useProcessosStore();
        const alertasStore = useAlertasStore();

        await wrapper.vm.$nextTick();

        expect(processosStore.buscarProcessosPainel).toHaveBeenCalledWith("ADMIN", 1, 0, 10);
        expect(alertasStore.buscarAlertas).toHaveBeenCalledWith(100, 1, 0, 10);
    });

    it("não deve carregar dados se perfil não estiver selecionado", async () => {
        const wrapper = mount(PainelView, mountOptions({
            perfil: {perfilSelecionado: null, unidadeSelecionada: null}
        }));
        const processosStore = useProcessosStore();
        const alertasStore = useAlertasStore();

        await wrapper.vm.$nextTick();

        expect(processosStore.buscarProcessosPainel).not.toHaveBeenCalled();
        expect(alertasStore.buscarAlertas).not.toHaveBeenCalled();
    });

    it("deve mostrar botão de criar processo apenas para admin", async () => {
        // Admin
        const wrapperAdmin = mount(PainelView, mountOptions({
            perfil: {perfis: ["ADMIN"]}
        }));
        expect(wrapperAdmin.find('[data-testid="btn-painel-criar-processo"]').exists()).toBe(true);

        // Not Admin
        const wrapperUser = mount(PainelView, mountOptions({
            perfil: {perfis: ["USER"]}
        }));
        expect(wrapperUser.find('[data-testid="btn-painel-criar-processo"]').exists()).toBe(false);
    });

    it("deve reordenar processos ao receber evento da tabela", async () => {
        const wrapper = mount(PainelView, mountOptions());
        const processosStore = useProcessosStore();

        vi.clearAllMocks(); // Limpa chamadas do onMounted

        await wrapper.findComponent({name: 'TabelaProcessos'}).vm.$emit('ordenar', 'dataCriacao');

        expect(processosStore.buscarProcessosPainel).toHaveBeenLastCalledWith(
            "ADMIN", 1, 0, 10, "dataCriacao", "asc"
        );

        await wrapper.findComponent({name: 'TabelaProcessos'}).vm.$emit('ordenar', 'dataCriacao');
        expect(processosStore.buscarProcessosPainel).toHaveBeenLastCalledWith(
            "ADMIN", 1, 0, 10, "dataCriacao", "desc"
        );
    });

    it("deve navegar ao selecionar processo", async () => {
        const wrapper = mount(PainelView, mountOptions());

        const processoMock = {codigo: 1, descricao: "Teste", linkDestino: "/processo/1"};
        await wrapper.findComponent({name: 'TabelaProcessos'}).vm.$emit('selecionar-processo', processoMock);

        expect(routerPushMock).toHaveBeenCalledWith("/processo/1");
    });

    it("deve reordenar alertas", async () => {
        const wrapper = mount(PainelView, mountOptions());
        const alertasStore = useAlertasStore();

        // Aguarda onMounted/onActivated
        await wrapper.vm.$nextTick();
        await flushPromises();

        vi.clearAllMocks(); // Limpa chamadas do onMounted

        await wrapper.findComponent({name: 'TabelaAlertas'}).vm.$emit('ordenar', 'processo');

        expect(alertasStore.buscarAlertas).toHaveBeenLastCalledWith(
            100, 1, 0, 10, "processo", "asc"
        );
    });
});
