import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as subprocessoService from "@/services/subprocessoService";
import * as analiseService from "@/services/analiseService";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useMapasStore} from "@/stores/mapas";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadastroView from "../CadastroView.vue";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BBadge: {template: '<span><slot /></span>'},
    BAlert: {template: '<div><slot /></div>', props: ['modelValue']},
    EmptyState: {template: '<div><slot /></div>'},
    LoadingButton: {
        props: ['loading', 'disabled'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">{{ loading ? "Loading..." : "Action" }}</button>'
    },
    CadAtividadeForm: {template: '<div></div>', expose: ['inputRef']},
    AtividadeItem: {template: '<div></div>', props: ['atividade']},
    ImportarAtividadesModal: {template: '<div></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {template: '<div></div>', props: ['mostrar']},
    HistoricoAnaliseModal: {template: '<div></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"></div>', props: ['modelValue']},
};

describe("CadastroView coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue(123 as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {codigo: 123, situacao: "MAPEAMENTO_CADASTRO_EM_ANDAMENTO", tipoProcesso: "MAPEAMENTO"},
            permissoes: {podeEditarCadastro: true, podeDisponibilizarCadastro: true, podeVisualizarImpacto: true},
            atividadesDisponiveis: [],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    function createWrapper() {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarCadastro: ref(true),
            podeDisponibilizarCadastro: ref(true),
            podeVisualizarImpacto: ref(true),
        } as any);

        const pinia = createTestingPinia({
            stubActions: true,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: {
                        codigo: 123,
                        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                        tipoProcesso: TipoProcesso.MAPEAMENTO
                    }
                }
            }
        });

        const store = useSubprocessosStore(pinia);
        (store.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        (store.buscarContextoEdicao as any).mockResolvedValue({
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, tipoProcesso: TipoProcesso.MAPEAMENTO},
            permissoes: {podeEditarCadastro: true, podeDisponibilizarCadastro: true, podeVisualizarImpacto: true},
            atividadesDisponiveis: [],
            unidade: {sigla: "TESTE", nome: "Teste"}
        });

        return mount(CadastroView, {
            global: {
                plugins: [pinia],
                stubs
            },
            props: {
                codProcesso: "1",
                sigla: "TESTE"
            }
        });
    }

    it("cobre ramos de erro e fluxos alternativos", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        // Cobre handleAdicionarAtividade com erro
        const store = useSubprocessosStore();
        const mapasStore = useMapasStore();

        mapasStore.mapaCompleto = { codigo: 100 } as any;

        // Simula falha ao adicionar
        await (wrapper.vm as any).handleAdicionarAtividade();

        // Cobre erroGlobal
        (wrapper.vm as any).erroGlobal = "Erro Global";
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("Erro Global");

        // Cobre remoção cancelada
        (wrapper.vm as any).atividades = [{codigo: 1, descricao: "A1"}];
        (wrapper.vm as any).removerAtividade(0);
        expect((wrapper.vm as any).mostrarModalConfirmacaoRemocao).toBe(true);

        // Cobre badgeVariant default
        expect((wrapper.vm as any).badgeVariant("INVALIDO")).toBe("secondary");

        // Cobre disponibilizar com erro de situacao
        store.$patch({
            subprocessoDetalhe: {
                ...store.subprocessoDetalhe,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO
            } as any
        });
        await (wrapper.vm as any).disponibilizarCadastro();

        // Cobre disponibilizar com erros de validação
        store.$patch({
            subprocessoDetalhe: {
                ...store.subprocessoDetalhe,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
            } as any
        });
        (store.validarCadastro as any).mockResolvedValue({
            valido: false,
            erros: [{mensagem: "Erro genérico"}, {atividadeCodigo: 1, mensagem: "Erro na atividade"}]
        });
        await (wrapper.vm as any).disponibilizarCadastro();
        await flushPromises();
        expect((wrapper.vm as any).erroGlobal).toBe("Erro genérico");
    });

    it("cobre funções complementares e modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codSubprocesso = 123;
        
        // setAtividadeRef
        const el = document.createElement("div");
        vm.setAtividadeRef(1, el);
        expect(vm.atividadeRefs.get(1)).toBe(el);
        
        // scrollParaPrimeiroErro
        vm.errosValidacao = [{ atividadeCodigo: 1, mensagem: "Erro" }];
        el.scrollIntoView = vi.fn();
        vm.scrollParaPrimeiroErro();
        expect(el.scrollIntoView).toHaveBeenCalled();

        // confirmarRemocao (conhecimento)
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({} as any);
        vm.atividades = [{codigo: 1, descricao: "A1"}];
        vm.dadosRemocao = {tipo: "conhecimento", index: 0, conhecimentoCodigo: 2};
        await vm.confirmarRemocao();
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(false);

        // salvarEdicaoAtividade
        await vm.salvarEdicaoAtividade(1, "Nova Desc");

        // adicionarConhecimento
        await vm.adicionarConhecimento(0, "Novo Conhecimento");

        // removerConhecimento
        vm.removerConhecimento(0, 2);
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(true);
        
        // salvarEdicaoConhecimento
        await vm.salvarEdicaoConhecimento(1, 2, "Desc Atualizada");
        
        // handleImportAtividades
        await vm.handleImportAtividades("Aviso");
        expect(vm.mostrarModalImportar).toBe(false);
        
        // confirmarDisponibilizacao (Revisao)
        const store = useSubprocessosStore();
        store.$patch({
            subprocessoDetalhe: {
                tipoProcesso: TipoProcesso.REVISAO
            } as any
        });
        store.disponibilizarRevisaoCadastro = vi.fn().mockResolvedValue(true);
        await vm.confirmarDisponibilizacao();
        expect(vm.mostrarModalConfirmacao).toBe(false);

        // confirmarDisponibilizacao (Mapeamento)
        store.$patch({
            subprocessoDetalhe: {
                tipoProcesso: TipoProcesso.MAPEAMENTO
            } as any
        });
        store.disponibilizarCadastro = vi.fn().mockResolvedValue(true);
        await vm.confirmarDisponibilizacao();

        // abrirModalHistorico
        await vm.abrirModalHistorico();
        expect(vm.mostrarModalHistorico).toBe(true);

        // abrir/fechar ModalImpacto
        const mapasStore = useMapasStore();
        mapasStore.buscarImpactoMapa = vi.fn().mockResolvedValue(null);
        vm.abrirModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(true);
        vm.fecharModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(false);

        // Sincronizar contexto null
        vm.sincronizarEstadoInicialContexto(null);
    });
});
