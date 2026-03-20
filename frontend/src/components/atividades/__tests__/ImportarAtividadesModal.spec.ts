import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as subprocessoService from "@/services/subprocessoService";
import {type Atividade} from "@/types/tipos";
import ImportarAtividadesModal from "../ImportarAtividadesModal.vue";

vi.mock("@/services/subprocessoService", () => ({
    importarAtividades: vi.fn(),
    listarAtividadesParaImportacao: vi.fn(),
}));

const processosMock = {
    processosParaImportacao: ref<any[]>([]),
    buscarProcessosParaImportacao: vi.fn(),
    buscarUnidadesParaImportacao: vi.fn(),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => processosMock
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
        processosMock.processosParaImportacao.value = [
            {codigo: 1, descricao: "Processo 1"}
        ];
    });

    function createWrapper(props = {}) {
        return mount(ImportarAtividadesModal, {
            global: {
                plugins: [createTestingPinia({
                    stubActions: false,
                    initialState: {}
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
        processosMock.buscarProcessosParaImportacao = vi.fn();

        await wrapper.setProps({mostrar: false});
        await wrapper.setProps({mostrar: true});

        expect(processosMock.buscarProcessosParaImportacao).toHaveBeenCalled();
    });

    it("carrega unidades ao selecionar processo", async () => {
        const wrapper = createWrapper();
        const mockUnidades = [{codUnidade: 10, sigla: "U1", codSubprocesso: 100}];
        (processosMock.buscarUnidadesParaImportacao as any).mockResolvedValue(mockUnidades);

        const select = wrapper.find('select#processo-select');
        await select.setValue("1");
        await flushPromises();

        expect(processosMock.buscarUnidadesParaImportacao).toHaveBeenCalledWith(1);
        expect((wrapper.vm as any).unidadesParticipantes).toEqual(mockUnidades);
    });

    it("carrega atividades ao selecionar unidade", async () => {
        const wrapper = createWrapper();
        (processosMock.buscarUnidadesParaImportacao as any).mockResolvedValue([{codUnidade: 10, sigla: "U1", codSubprocesso: 100}]);

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
        (processosMock.buscarUnidadesParaImportacao as any).mockResolvedValue([{codUnidade: 10, sigla: "U1", codSubprocesso: 100}]);

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

    it("limpa seleções de atividades ao trocar de processo", async () => {
        processosMock.processosParaImportacao.value = [
            {codigo: 1, descricao: "Processo 1"},
            {codigo: 2, descricao: "Processo 2"},
        ];
        const wrapper = createWrapper();
        const unidades1 = [{codUnidade: 10, sigla: "U1", codSubprocesso: 100}];
        (processosMock.buscarUnidadesParaImportacao as any).mockResolvedValue(unidades1);

        // Seleciona processo 1 e unidade
        await wrapper.find('select#processo-select').setValue("1");
        await flushPromises();
        const mockAtividades: Atividade[] = [{codigo: 50, descricao: "Ativ 50", conhecimentos: []}];
        vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue(mockAtividades);
        await wrapper.find('select#unidade-select').setValue("10");
        await flushPromises();

        // Simula seleção de atividade do processo 1
        (wrapper.vm as any).atividadesSelecionadas = [mockAtividades[0]];
        expect((wrapper.vm as any).atividadesSelecionadas.length).toBe(1);

        // Troca para processo 2 - seleções devem ser limpas
        (processosMock.buscarUnidadesParaImportacao as any).mockResolvedValue([{codUnidade: 20, sigla: "U2", codSubprocesso: 200}]);
        await wrapper.find('select#processo-select').setValue("2");
        await flushPromises();

        expect((wrapper.vm as any).atividadesSelecionadas.length).toBe(0);
    });

    it("limpa seleções de atividades ao trocar de unidade", async () => {
        const wrapper = createWrapper();
        const unidades = [
            {codUnidade: 10, sigla: "U1", codSubprocesso: 100},
            {codUnidade: 20, sigla: "U2", codSubprocesso: 200},
        ];
        (processosMock.buscarUnidadesParaImportacao as any).mockResolvedValue(unidades);

        await wrapper.find('select#processo-select').setValue("1");
        await flushPromises();

        // Seleciona unidade 1 e simula atividades selecionadas
        const mockAtividadesU1: Atividade[] = [{codigo: 50, descricao: "Ativ 50", conhecimentos: []}];
        vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue(mockAtividadesU1);
        await wrapper.find('select#unidade-select').setValue("10");
        await flushPromises();
        (wrapper.vm as any).atividadesSelecionadas = [mockAtividadesU1[0]];
        expect((wrapper.vm as any).atividadesSelecionadas.length).toBe(1);

        // Troca para unidade 2 - seleções devem ser limpas
        const mockAtividadesU2: Atividade[] = [{codigo: 60, descricao: "Ativ 60", conhecimentos: []}];
        vi.mocked(subprocessoService.listarAtividadesParaImportacao).mockResolvedValue(mockAtividadesU2);
        await wrapper.find('select#unidade-select').setValue("20");
        await flushPromises();

        expect((wrapper.vm as any).atividadesSelecionadas.length).toBe(0);
    });
});
