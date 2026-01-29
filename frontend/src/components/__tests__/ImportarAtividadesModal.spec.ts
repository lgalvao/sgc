import {flushPromises, mount, type VueWrapper} from "@vue/test-utils";
import {BFormSelect} from "bootstrap-vue-next";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {type Atividade, type ProcessoResumo, SituacaoProcesso, TipoProcesso} from "@/types/tipos";
import ImportarAtividadesModal from "../ImportarAtividadesModal.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {useAtividadesStore} from "@/stores/atividades";

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

const mockExecute = vi.fn();
vi.mock("@/composables/useApi", () => ({
    useApi: () => ({
        execute: mockExecute,
        error: ref(null),
        isLoading: ref(false),
        clearError: vi.fn(),
    }),
}));

describe("ImportarAtividadesModal", () => {
    const context = setupComponentTest();
    let wrapper: VueWrapper<ImportarAtividadesModalVM>;

    beforeEach(() => {
        vi.clearAllMocks();

        const mapAtividades = new Map<number, Atividade[]>();
        mapAtividades.set(100, mockAtividades);

        context.wrapper = mount(ImportarAtividadesModal, {
            ...getCommonMountOptions({
                processos: {
                    processosFinalizados: mockProcessos,
                    processoDetalhe: mockProcessoDetalhe,
                },
                atividades: {
                    atividadesPorSubprocesso: mapAtividades
                }
            }),
            props: {mostrar: true, codSubprocessoDestino: 999},
        });
        wrapper = context.wrapper as VueWrapper<ImportarAtividadesModalVM>;

        // Mock getter that was converted from computed property
        const atividadesStore = useAtividadesStore();
        atividadesStore.obterAtividadesPorSubprocesso = vi.fn((id: number) => {
            return mapAtividades.get(id) || [];
        });
    });

    it('deve emitir "fechar" ao clicar em Cancelar', async () => {
        await wrapper.find('[data-testid="importar-atividades-modal__btn-modal-cancelar"]').trigger("click");
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

    it("deve resetar o modal quando a prop 'mostrar' mudar para true", async () => {
        // Simulate selecting something first
        const selects = wrapper.findAllComponents(BFormSelect as any);
        await selects[0].setValue("1");
        await flushPromises();

        await wrapper.setProps({mostrar: false});
        await wrapper.setProps({mostrar: true});

        // Check if reset
        expect((wrapper.vm as any).processoSelecionadoId).toBeNull();
    });

    it("deve lidar com erro na importação", async () => {
        // Setup selection
        const selects = wrapper.findAllComponents(BFormSelect as any);
        await selects[0].setValue("1");
        await flushPromises();
        await selects[1].setValue("10");
        await flushPromises();
        await (wrapper.find('input[type="checkbox"]') as any).setChecked(true);

        mockExecute.mockRejectedValue(new Error("Fail"));
        const importButton = wrapper.find('[data-testid="btn-importar"]');
        await importButton.trigger("click");

        expect(mockExecute).toHaveBeenCalled();
        expect(wrapper.emitted("importar")).toBeFalsy();
    });

    it("deve limpar seleção se selecionar processo vazio", async () => {
        const selects = wrapper.findAllComponents(BFormSelect as any);
        await selects[0].setValue("1");
        await flushPromises();
        // BFormSelect/setValue might set it as string "1" even if bound to number
        // Check if wrapper.vm.processoSelecionadoId is 1 or "1" depends on implementation and bootstrap-vue-next
        // Original test expected "1". Since we mocked state with number 1, let's see.
        // If setValue sets string, v-model becomes string.
        expect((wrapper.vm as any).processoSelecionadoId).toBe("1");

        await selects[0].setValue(""); // Select placeholder/empty
        await flushPromises();
        expect((wrapper.vm as any).processoSelecionadoId).toBe("");
    });
});
