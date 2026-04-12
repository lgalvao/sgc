import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {useMapas} from '@/composables/useMapas';
import MapaView from '@/views/MapaView.vue';
import * as useFluxoMapaModule from '@/composables/useFluxoMapa';
import * as useSubprocessosModule from '@/composables/useSubprocessos';

type MapaViewVm = {
    codSubprocesso: number | null;
    mostrarModalImpacto: boolean;
    mostrarModalDisponibilizar: boolean;
    fieldErrors: Record<string, string | undefined>;
    existeCompetenciaSemAtividade: boolean;
    podeConfirmarDisponibilizacao: boolean;
    abrirModalImpacto: () => Promise<void>;
    fecharModalImpacto: () => void;
    removerAtividadeAssociada: (codigoCompetencia: number, codigoAtividade: number) => Promise<void> | void;
    handleErrors: (store: {lastError?: {subErrors?: Array<{field?: string; message?: string}>}}) => Promise<void>;
    disponibilizarMapa: (payload: Record<string, unknown>) => Promise<void>;
    fecharModalDisponibilizar: () => void;
};

vi.mock('@/composables/useSubprocessos', () => ({useSubprocessos: vi.fn()}));
vi.mock('@/composables/useFluxoMapa', () => ({useFluxoMapa: vi.fn()}));

vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    useRoute: vi.fn(() => ({params: {codProcesso: '1', siglaUnidade: 'TEST'}, query: {}})),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: vi.fn(),
        replace: vi.fn()
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe('MapaView Coverage', () => {
    const subprocessosMock = {
        subprocessoDetalhe: null as null | {codigo: number},
        buscarSubprocessoPorProcessoEUnidade: vi.fn(),
        buscarContextoEdicao: vi.fn(),
        buscarSubprocessoDetalhe: vi.fn(),
        atualizarStatusLocal: vi.fn(),
        lastError: null as {message: string} | null,
        clearError: vi.fn(),
    };

    const commonStubs = {
        PageHeader: {template: '<div><slot /><slot name="actions" /></div>'},
        BButton: {template: '<button />'},
        BContainer: {template: '<div><slot /></div>'},
        LoadingButton: {template: '<button />'},
        EmptyState: {template: '<div><slot /></div>'},
        CompetenciaCard: {template: '<div />'},
        CriarCompetenciaModal: {template: '<div />'},
        DisponibilizarMapaModal: {template: '<div />'},
        ModalConfirmacao: {template: '<div />'},
        ImpactoMapaModal: {template: '<div />'},
        BAlert: {template: '<div />'}
    };
    const fluxoMapaMock = {
        erro: null as {message: string} | null,
        lastError: null as {message?: string; subErrors?: Array<{field?: string; message?: string}>} | null,
        clearError: vi.fn(),
        adicionarCompetencia: vi.fn(),
        atualizarCompetencia: vi.fn(),
        removerCompetencia: vi.fn(),
        disponibilizarMapa: vi.fn(),
    };

    beforeEach(() => {
        vi.clearAllMocks();
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.lastError = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);
        subprocessosMock.buscarContextoEdicao.mockResolvedValue(null);
        vi.mocked(useSubprocessosModule.useSubprocessos).mockReturnValue(subprocessosMock as unknown as ReturnType<typeof useSubprocessosModule.useSubprocessos>);
        fluxoMapaMock.erro = null;
        fluxoMapaMock.lastError = null;
        fluxoMapaMock.clearError = vi.fn();
        fluxoMapaMock.adicionarCompetencia = vi.fn();
        fluxoMapaMock.atualizarCompetencia = vi.fn();
        fluxoMapaMock.removerCompetencia = vi.fn();
        fluxoMapaMock.disponibilizarMapa = vi.fn();
        vi.mocked(useFluxoMapaModule.useFluxoMapa).mockReturnValue(fluxoMapaMock as unknown as ReturnType<typeof useFluxoMapaModule.useFluxoMapa>);
    });

    it('removerAtividadeAssociada does nothing if competency not found', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                mapas: {
                    mapaCompleto: {
                        competencias: [{codigo: 1, descricao: 'Comp 1', atividades: [{codigo: 10}]}]
                    }
                }
            }
        });

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });
        const mapas = useMapas();
        mapas.mapaCompleto.value = {
            competencias: [{codigo: 1, descricao: 'Comp 1', atividades: [{codigo: 10}]}]
        } as unknown as typeof mapas.mapaCompleto.value;

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa();
        const vm = wrapper.vm as unknown as MapaViewVm;

        await vm.removerAtividadeAssociada(999, 10);

        expect(fluxoMapa.atualizarCompetencia).not.toHaveBeenCalled();
    });

    it('abrirModalImpacto does nothing if codSubprocesso is missing', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {subprocessoDetalhe: null}, // ensures codSubprocesso setup might fail or return null
                mapas: {mapaCompleto: {competencias: []}}
            }
        });

        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(null);

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const mapas = useMapas();
        mapas.buscarImpactoMapa = vi.fn().mockResolvedValue(undefined);

        await wrapper.vm.$nextTick(); // Wait for mount

        const vm = wrapper.vm as unknown as MapaViewVm;
        await vm.abrirModalImpacto();

        expect(mapas.buscarImpactoMapa).not.toHaveBeenCalled();
    });

    it('abrirModalImpacto calls store when codSubprocesso is present', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                mapas: {mapaCompleto: {competencias: []}}
            }
        });

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const mapas = useMapas();
        mapas.buscarImpactoMapa = vi.fn().mockResolvedValue(undefined);
        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codSubprocesso = 456;

        await vm.abrirModalImpacto();

        expect(mapas.buscarImpactoMapa).toHaveBeenCalledWith(456);
        expect(vm.mostrarModalImpacto).toBe(true);
    });

    it('removerAtividadeAssociada updates store if competency is found', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                mapas: {
                    mapaCompleto: {
                        competencias: [{codigo: 1, descricao: 'Comp 1', atividades: [{codigo: 10}, {codigo: 20}]}]
                    }
                }
            }
        });

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });
        const mapas = useMapas();
        mapas.mapaCompleto.value = {
            competencias: [{codigo: 1, descricao: 'Comp 1', atividades: [{codigo: 10}, {codigo: 20}]}]
        } as unknown as typeof mapas.mapaCompleto.value;

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa();
        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codSubprocesso = 456;

        await vm.removerAtividadeAssociada(1, 10);

        expect(fluxoMapa.atualizarCompetencia).toHaveBeenCalledWith(456, 1, {
            descricao: 'Comp 1',
            atividadesIds: [20]
        });
    });

    it('desabilita disponibilizacao quando existir competencia sem atividade', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                mapas: {
                    mapaCompleto: {
                        competencias: [{codigo: 1, descricao: 'Comp 1', atividades: []}]
                    }
                }
            }
        });

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const mapas = useMapas();
        mapas.mapaCompleto.value = {
            competencias: [{codigo: 1, descricao: 'Comp 1', atividades: []}]
        } as unknown as typeof mapas.mapaCompleto.value;

        await wrapper.vm.$nextTick();
        const vm = wrapper.vm as unknown as MapaViewVm;

        expect(vm.existeCompetenciaSemAtividade).toBe(true);
        expect(vm.podeConfirmarDisponibilizacao).toBe(false);
    });

    it('fecharModalImpacto closes the modal', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.mostrarModalImpacto = true;
        vm.fecharModalImpacto();

        expect(vm.mostrarModalImpacto).toBe(false);
    });

    it('handleErrors covers activitiesIds branch', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const store = {
            lastError: {
                subErrors: [{field: 'atividadesIds', message: 'Erro ID'}]
            }
        };

        const vm = wrapper.vm as unknown as MapaViewVm;
        await vm.handleErrors(store);
        expect(vm.fieldErrors.atividades).toBe('Erro ID');
    });

    it('disponibilizarMapa returns early if codSubprocesso is null', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codSubprocesso = null;
        await vm.disponibilizarMapa({});

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa();
        expect(fluxoMapa.disponibilizarMapa).not.toHaveBeenCalled();
    });

    it('fecharModalDisponibilizar clears state', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.mostrarModalDisponibilizar = true;
        vm.fecharModalDisponibilizar();

        expect(vm.mostrarModalDisponibilizar).toBe(false);
    });
});
