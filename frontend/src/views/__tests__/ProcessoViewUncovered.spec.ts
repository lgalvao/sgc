import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import ProcessoView from "../ProcessoDetalheView.vue";
import {createTestingPinia} from "@pinia/testing";
import * as processoService from "@/services/processo";
import {useProcessoStore} from "@/stores/processo";

const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: {params: {codProcesso: "1"}, query: {}}
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: mocks.push,
    }),
    useRoute: () => mocks.mockRoute,
}));

vi.mock("@/services/processo", () => ({
    executarAcaoEmBloco: vi.fn().mockResolvedValue({}),
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot></slot></div>'},
    AppAlert: {template: '<div></div>'},
    PageHeader: {template: '<div><slot></slot><slot name="actions"></slot></div>', props: ['title']},
    ProcessoInfo: {template: '<div></div>'},
    ProcessoSubprocessosTable: {template: '<div></div>'},
    BSpinner: {template: '<div></div>'},
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" :id="$attrs.id" @click="$emit(\'click\')"><slot /></button>'},
    BDropdown: {template: '<div><slot /></div>'},
    BDropdownItemButton: {template: '<button :data-testid="$attrs[\'data-testid\']" :id="$attrs.id" @click="$emit(\'click\')"><slot /></button>'},
    ModalAcaoBloco: {
        template: '<div></div>',
        methods: {
            abrir: vi.fn(),
            fechar: vi.fn(),
            setProcessando: vi.fn(),
            setErro: vi.fn(),
        }
    },
    ModalConfirmacao: {template: '<div></div>'},
};

describe("ProcessoDetalheView Uncovered Branches", () => {
    let pinia: any;

    beforeEach(() => {
        vi.clearAllMocks();
        pinia = createTestingPinia({
            stubActions: false,
        });
    });

    it("cobre branches de getters e executarAcaoBloco falhas", async () => {
        const store = useProcessoStore(pinia);
        store.garantirContextoCompleto = vi.fn().mockResolvedValue(null);

        const wrapper = mount(ProcessoView, {
            global: {
                plugins: [pinia],
                stubs
            }
        });
        await flushPromises();

        const vm = wrapper.vm as any;
        
        // Testar executarAcaoBloco sem processo
        await vm.executarAcaoBloco({ ids: [1], dataLimite: "2025-01-01" });
        expect(processoService.executarAcaoEmBloco).not.toHaveBeenCalled();

        // Testar executarAcaoBloco sem acaoBlocoAtual
        vm.processo = { codigo: 1, descricao: "Teste" }; // fake processo
        await vm.executarAcaoBloco({ ids: [1], dataLimite: "2025-01-01" });
        expect(processoService.executarAcaoEmBloco).not.toHaveBeenCalled();
    });

    it("cobre acaoBlocoPrincipal click (apenas 1 acao visivel)", async () => {
        const store = useProcessoStore(pinia);
        store.garantirContextoCompleto = vi.fn().mockResolvedValue({
            codigo: 1,
            descricao: "Teste",
            acoesBloco: [{codigo: 'acao-1', rotulo: 'Acao 1', habilitar: true, mostrar: true}]
        });

        const wrapper = mount(ProcessoView, {
            global: {
                plugins: [pinia],
                stubs
            }
        });
        await flushPromises();

        // Since there is only 1 acao, BButton for acaoBlocoPrincipal should be rendered
        const btn = wrapper.find('[data-testid="btn-processo-acao-1"]');
        await btn.trigger('click');
        expect((wrapper.vm as any).acaoBlocoAtual.codigo).toBe('acao-1');
    });

    it("cobre onActivated com e sem invalidação", async () => {
        const store = useProcessoStore(pinia);
        store.garantirContextoCompleto = vi.fn().mockResolvedValue({
            codigo: 1,
            descricao: "Teste",
        });

        const KeepAliveWrapper = {
            template: '<keep-alive><ProcessoView v-if="show" /></keep-alive>',
            components: { ProcessoView },
            data() { return { show: true } }
        };

        const wrapper = mount(KeepAliveWrapper, {
            global: {
                plugins: [pinia],
                stubs
            }
        });
        const vm = wrapper.vm as unknown as { show: boolean };
        await flushPromises();

        const processoView = wrapper.findComponent(ProcessoView);

        // Deactivate
        vm.show = false;
        await flushPromises();

        store.contextoCompleto = {codigo: 1, descricao: "Teste"} as any;
        store.codProcessoCarregado = 1;
        const contextoAtualizado = { codigo: 1, descricao: "Atualizado" } as any;
        store.garantirContextoCompleto = vi.fn().mockResolvedValue(contextoAtualizado);

        // Reactivate sem invalidação: não recarrega
        vm.show = true;
        await flushPromises();

        expect(store.garantirContextoCompleto).not.toHaveBeenCalled();

        // Invalida e reativa: deve recarregar
        vm.show = false;
        await flushPromises();
        store.invalidar();
        vm.show = true;
        await flushPromises();

        expect((processoView.vm as any).processo.descricao).toBe("Atualizado");
    });
});
