import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {useProcessosStore} from "@/stores/processos";
import * as subprocessoService from "@/services/subprocessoService";
import {type Atividade} from "@/types/tipos";
import ImportarAtividadesModal from "../ImportarAtividadesModal.vue";

vi.mock("@/services/subprocessoService", () => ({
    importarAtividades: vi.fn(),
    listarAtividadesParaImportacao: vi.fn(),
}));

const stubs = {
    BModal: {
        props: ['modelValue', 'title'],
        template: '<div v-if="modelValue" data-testid="modal"><h1>{{ title }}</h1><slot /><slot name="footer" /></div>'
    },
    BAlert: {template: '<div><slot /></div>'},
    BFormSelect: {
        props: ['modelValue', 'options'],
        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot /><option v-for="opt in options" :key="opt.codigo || opt.codUnidade" :value="opt.codigo || opt.codUnidade">{{ opt.descricao || opt.sigla }}</option></select>'
    },
    BFormSelectOption: {template: '<option><slot /></option>'},
    BFormCheckbox: {
        props: ['modelValue', 'value'],
        template: '<div>Checkbox <slot /></div>'
    },
    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
    BSpinner: {template: '<div></div>'},
};

describe("ImportarAtividadesModal.vue", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function createWrapper(props = {}) {
        return mount(ImportarAtividadesModal, {
            global: {
                plugins: [createTestingPinia({
                    stubActions: false,
                    initialState: {
                        processos: {
                            processosParaImportacao: [
                                {codigo: 1, descricao: "Processo 1"}
                            ]
                        }
                    }
                })],
                stubs
            },
            props: {
                mostrar: true,
                codSubprocessoDestino: 123,
                ...props
            }
        });
    }

    it("carrega processos ao abrir", async () => {
        const wrapper = createWrapper();
        const store = useProcessosStore();
        store.buscarProcessosParaImportacao = vi.fn();

        await wrapper.setProps({mostrar: false});
        await wrapper.setProps({mostrar: true});

        expect(store.buscarProcessosParaImportacao).toHaveBeenCalled();
    });

    it("carrega unidades ao selecionar processo", async () => {
        const wrapper = createWrapper();
        const store = useProcessosStore();
        const mockUnidades = [{codUnidade: 10, sigla: "U1", codSubprocesso: 100}];
        (store.buscarUnidadesParaImportacao as any).mockResolvedValue(mockUnidades);

        const select = wrapper.find('select#processo-select');
        await select.setValue("1");
        await flushPromises();

        expect(store.buscarUnidadesParaImportacao).toHaveBeenCalledWith(1);
        expect((wrapper.vm as any).unidadesParticipantes).toEqual(mockUnidades);
    });

    it("carrega atividades ao selecionar unidade", async () => {
        const wrapper = createWrapper();
        const store = useProcessosStore();
        (store.buscarUnidadesParaImportacao as any).mockResolvedValue([{codUnidade: 10, sigla: "U1", codSubprocesso: 100}]);

        await wrapper.find('select#processo-select').setValue("1");
        await flushPromises();

        const mockAtividades: Atividade[] = [{codigo: 50, descricao: "Ativ 50", conhecimentos: []}];
        vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue(mockAtividades);

        await wrapper.find('select#unidade-select').setValue("10");
        await flushPromises();

        expect(subprocessoService.listarAtividadesParaImportacao).toHaveBeenCalledWith(100);
        expect((wrapper.vm as any).atividadesParaImportar).toEqual(mockAtividades);
    });

    it("executa importação com sucesso", async () => {
        const wrapper = createWrapper();
        const store = useProcessosStore();
        (store.buscarUnidadesParaImportacao as any).mockResolvedValue([{codUnidade: 10, sigla: "U1", codSubprocesso: 100}]);

        await wrapper.find('select#processo-select').setValue("1");
        await flushPromises();

        const mockAtividades: Atividade[] = [{codigo: 50, descricao: "Ativ 50", conhecimentos: []}];
        vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue(mockAtividades);
        await wrapper.find('select#unidade-select').setValue("10");
        await flushPromises();

        // Seleciona atividade diretamente no ref para evitar problemas com stub de BFormCheckbox
        (wrapper.vm as any).atividadesSelecionadas = [mockAtividades[0]];
        await flushPromises();

        vi.mocked(subprocessoService.importarAtividades).mockResolvedValue(true as any);

        await wrapper.find('[data-testid="btn-importar"]').trigger("click");
        await flushPromises();

        expect(subprocessoService.importarAtividades).toHaveBeenCalledWith(123, 100, [50]);
        expect(wrapper.emitted("importar")).toBeTruthy();
        expect(wrapper.emitted("fechar")).toBeTruthy();
    });

    it("emite fechar ao cancelar", async () => {
        const wrapper = createWrapper();
        await wrapper.find('[data-testid="importar-atividades-modal__btn-modal-cancelar"]').trigger("click");
        expect(wrapper.emitted("fechar")).toBeTruthy();
    });
});
