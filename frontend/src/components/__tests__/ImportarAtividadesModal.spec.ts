import {flushPromises, mount, type VueWrapper} from "@vue/test-utils";
import {BFormSelect} from "bootstrap-vue-next";
import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
// Mock data
import {type Atividade, type ProcessoResumo, SituacaoProcesso, TipoProcesso,} from "@/types/tipos";
import ImportarAtividadesModal from "../ImportarAtividadesModal.vue";

// Helper type for the component instance
type ImportarAtividadesModalVM = InstanceType<typeof ImportarAtividadesModal>;

const mockProcessos: ProcessoResumo[] = [
    {
        codigo: 1,
        descricao: "Processo 1",
        tipo: TipoProcesso.MAPEAMENTO,
        situacao: SituacaoProcesso.FINALIZADO,
        dataCriacao: "2021-01-01",
        dataLimite: "2021-01-01",
        unidadeCodigo: 1,
        unidadeNome: "test",
    },
];
const mockProcessoDetalhe = {
    unidades: [{codUnidade: 10, sigla: "U1", codSubprocesso: 100}],
};
const mockAtividades: Atividade[] = [
    {codigo: 1, descricao: "Atividade A", conhecimentos: []},
];

// Mock composable and stores
const mockExecute = vi.fn();
vi.mock("@/composables/useApi", () => ({
    useApi: () => ({
        execute: mockExecute,
        error: ref(null),
        isLoading: ref(false),
        clearError: vi.fn(),
    }),
}));
vi.mock("@/stores/processos", () => ({
  useProcessosStore: () => ({
    processosPainel: mockProcessos,
    processoDetalhe: mockProcessoDetalhe,
    fetchProcessosPainel: vi.fn(),
    fetchProcessoDetalhe: vi.fn(),
  }),
}));
vi.mock("@/stores/atividades", () => ({
  useAtividadesStore: () => ({
    getAtividadesPorSubprocesso: () => mockAtividades,
    fetchAtividadesParaSubprocesso: vi.fn(),
    importarAtividades: vi.fn(),
  }),
}));

describe("ImportarAtividadesModal", () => {
  let wrapper: VueWrapper<ImportarAtividadesModalVM>;

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    wrapper = mount(ImportarAtividadesModal, {
      props: { mostrar: true, codSubrocessoDestino: 999 },
    });
  });

  it('deve emitir "fechar" ao clicar em Cancelar', async () => {
      await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger("click");
      expect(wrapper.emitted("fechar")).toBeTruthy();
  });

    it("deve habilitar o botão de importação e chamar a API ao importar", async () => {
    const importButton = wrapper.find('[data-testid="btn-importar"]');
    expect((importButton.element as HTMLButtonElement).disabled).toBe(true);

    // Simulate user selecting a process and unit
    const selects = wrapper.findAllComponents(BFormSelect as any);
        await selects[0].setValue("1");
    await flushPromises();
        await selects[1].setValue("10");
    await flushPromises();

    // Find and check the checkbox for the activity
    await (wrapper.find('input[type="checkbox"]') as any).setChecked(true);

    // Now, the button should be enabled
    expect((importButton.element as HTMLButtonElement).disabled).toBe(false);

    // Simulate the import click
    mockExecute.mockResolvedValue(true);
        await importButton.trigger("click");

    // Verify the API call and emitted events
    expect(mockExecute).toHaveBeenCalledWith(
      999,
      mockProcessoDetalhe.unidades[0].codSubprocesso,
        [mockAtividades[0].codigo],
    );
        expect(wrapper.emitted("importar")).toBeTruthy();
        expect(wrapper.emitted("fechar")).toBeTruthy();
  });
});
