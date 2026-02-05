import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import ImportarAtividadesModal from "../ImportarAtividadesModal.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {useProcessosStore} from "@/stores/processos";
import {useAtividadesStore} from "@/stores/atividades";

describe("ImportarAtividadesModal Coverage", () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("exibe mensagem quando não há processos finalizados", async () => {
        context.wrapper = mount(ImportarAtividadesModal, {
            ...getCommonMountOptions({
                processos: { processosFinalizados: [] }
            }),
            props: { mostrar: true, codSubprocessoDestino: 999 },
        });

        expect(context.wrapper.text()).toContain("Nenhum processo disponível para importação");
    });

    it("importar retorna antecipadamente se condições não atendidas", async () => {
        context.wrapper = mount(ImportarAtividadesModal, {
            ...getCommonMountOptions({
                processos: {
                    processosFinalizados: [{ codigo: 1 }],
                    processoDetalhe: { unidades: [{ codUnidade: 10 }] }
                }
            }),
            props: { mostrar: true, codSubprocessoDestino: 999 },
        });

        const vm = context.wrapper.vm as any;

        // No unit selected
        vm.unidadeSelecionadaId = null;
        await vm.importar();
        expect(vm.erroImportacao).toBeNull();

        // Unit selected but no activities selected
        vm.unidadeSelecionada = { codUnidade: 10, codSubprocesso: 100 };
        vm.atividadesSelecionadas = [];
        await vm.importar();
        expect(vm.erroImportacao).toBe("Selecione ao menos uma atividade para importar.");
    });

    it("reseta modal ao mostrar", async () => {
        const pinia = (getCommonMountOptions({}) as any).global.plugins[0];
        const processosStore = useProcessosStore(pinia);

        const wrapper = mount(ImportarAtividadesModal, {
            props: { mostrar: false, codSubprocessoDestino: 999 },
            global: { plugins: [pinia] }
        });

        const spy = vi.spyOn(processosStore, 'buscarProcessosFinalizados');

        await wrapper.setProps({ mostrar: true });

        expect(spy).toHaveBeenCalled();
        expect((wrapper.vm as any).processoSelecionadoId).toBeNull();
    });

    it("lida com seleção de processo e unidade nulos", async () => {
        context.wrapper = mount(ImportarAtividadesModal, {
            ...getCommonMountOptions({
                processos: { processosFinalizados: [{ codigo: 1 }] }
            }),
            props: { mostrar: true, codSubprocessoDestino: 999 },
        });

        const vm = context.wrapper.vm as any;

        vm.processoSelecionadoId = 1;
        await flushPromises();
        expect(vm.processoSelecionado).not.toBeNull();

        vm.processoSelecionadoId = null;
        await flushPromises();
        expect(vm.processoSelecionado).toBeNull();
        expect(vm.unidadesParticipantes).toHaveLength(0);

        vm.unidadeSelecionadaId = 10;
        await flushPromises();
        // Even if not in list, it might try to find it
        vm.unidadeSelecionadaId = null;
        await flushPromises();
        expect(vm.unidadeSelecionada).toBeNull();
    });

    it("selecionarUnidade preenche atividades para importar", async () => {
        const mockAtividades = [{ codigo: 1, descricao: 'A1' }];
        const pinia = (getCommonMountOptions({}) as any).global.plugins[0];
        const atividadesStore = useAtividadesStore(pinia);
        atividadesStore.obterAtividadesPorSubprocesso = vi.fn().mockReturnValue(mockAtividades);

        const wrapper = mount(ImportarAtividadesModal, {
            props: { mostrar: true, codSubprocessoDestino: 999 },
            global: { plugins: [pinia] }
        });

        await (wrapper.vm as any).selecionarUnidade({ codSubprocesso: 100 });
        expect((wrapper.vm as any).atividadesParaImportar).toEqual(mockAtividades);

        await (wrapper.vm as any).selecionarUnidade(null);
        expect((wrapper.vm as any).atividadesParaImportar).toEqual([]);
    });

    it("executa importação com sucesso e triggers watchers", async () => {
        const pinia = (getCommonMountOptions({}) as any).global.plugins[0];
        const processosStore = useProcessosStore(pinia);
        const atividadesStore = useAtividadesStore(pinia);

        const mockProcesso = { codigo: 1, descricao: 'P1' };
        const mockUnidade = { codUnidade: 10, sigla: 'U1', codSubprocesso: 100 };
        const mockAtividade = { codigo: 1, descricao: 'A1' };

        processosStore.processosFinalizados = [mockProcesso] as any;
        processosStore.processoDetalhe = { unidades: [mockUnidade] } as any;
        atividadesStore.obterAtividadesPorSubprocesso = vi.fn().mockReturnValue([mockAtividade]);
        (atividadesStore.importarAtividades as any).mockResolvedValue(undefined);

        const wrapper = mount(ImportarAtividadesModal, {
            props: { mostrar: true, codSubprocessoDestino: 999 },
            global: { plugins: [pinia] }
        });

        const vm = wrapper.vm as any;

        // Trigger processoSelecionadoId watch via select
        const selects = wrapper.findAll('select');
        await selects[0].setValue(1);
        await flushPromises();
        expect(vm.processoSelecionado).toEqual(mockProcesso);

        // Trigger unidadeSelecionadaId watch via select
        await selects[1].setValue(10);
        await flushPromises();
        expect(vm.unidadeSelecionada).toEqual(mockUnidade);

        // Select activity via checkbox
        const checkbox = wrapper.find('input[type="checkbox"]');
        await checkbox.setValue(true);
        await flushPromises();
        expect(vm.atividadesSelecionadas).toHaveLength(1);

        await vm.importar();
        await flushPromises();

        expect(atividadesStore.importarAtividades).toHaveBeenCalledWith(999, 100, [1]);
        expect(wrapper.emitted('importar')).toBeTruthy();
        expect(wrapper.emitted('fechar')).toBeTruthy();
    });

    it("emite fechar ao chamar fechar()", async () => {
        const wrapper = mount(ImportarAtividadesModal, {
            ...getCommonMountOptions({}),
            props: { mostrar: true, codSubprocessoDestino: 999 },
        });

        await (wrapper.vm as any).fechar();
        expect(wrapper.emitted('fechar')).toBeTruthy();
    });
});
