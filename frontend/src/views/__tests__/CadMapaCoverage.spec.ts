import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {useMapas} from '@/composables/useMapas';
import MapaView from '@/views/MapaView.vue';
import * as useFluxoMapaModule from '@/composables/useFluxoMapa';
import * as useSubprocessosModule from '@/composables/useSubprocessos';

vi.mock('@/composables/useSubprocessos', () => ({useSubprocessos: vi.fn()}));
vi.mock('@/composables/useFluxoMapa', () => ({useFluxoMapa: vi.fn()}));

vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    useRoute: vi.fn(() => ({params: {codProcesso: '1', siglaUnidade: 'TEST'}})),
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
        subprocessoDetalhe: null as any,
        buscarSubprocessoPorProcessoEUnidade: vi.fn(),
        buscarContextoEdicao: vi.fn(),
        buscarSubprocessoDetalhe: vi.fn(),
        atualizarStatusLocal: vi.fn(),
        lastError: null as any,
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
        erro: null as any,
        lastError: null as any,
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
        vi.mocked(useSubprocessosModule.useSubprocessos).mockReturnValue(subprocessosMock as any);
        fluxoMapaMock.erro = null;
        fluxoMapaMock.lastError = null;
        fluxoMapaMock.clearError = vi.fn();
        fluxoMapaMock.adicionarCompetencia = vi.fn();
        fluxoMapaMock.atualizarCompetencia = vi.fn();
        fluxoMapaMock.removerCompetencia = vi.fn();
        fluxoMapaMock.disponibilizarMapa = vi.fn();
        vi.mocked(useFluxoMapaModule.useFluxoMapa).mockReturnValue(fluxoMapaMock as any);
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
        } as any;

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa() as any;

        await (wrapper.vm as any).removerAtividadeAssociada(999, 10);

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

        await (wrapper.vm as any).abrirModalImpacto();

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
        (wrapper.vm as any).codSubprocesso = 456;

        await (wrapper.vm as any).abrirModalImpacto();

        expect(mapas.buscarImpactoMapa).toHaveBeenCalledWith(456);
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);
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
        } as any;

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa() as any;
        (wrapper.vm as any).codSubprocesso = 456;

        await (wrapper.vm as any).removerAtividadeAssociada(1, 10);

        expect(fluxoMapa.atualizarCompetencia).toHaveBeenCalledWith(456, 1, {
            descricao: 'Comp 1',
            codigosAtividades: [20]
        });
    });

    it('fecharModalImpacto closes the modal', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        (wrapper.vm as any).mostrarModalImpacto = true;
        (wrapper.vm as any).fecharModalImpacto();

        expect((wrapper.vm as any).mostrarModalImpacto).toBe(false);
    });

    it('handleErrors covers codigosAtividades branch', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const store = {
            lastError: {
                subErrors: [{field: 'codigosAtividades', message: 'Erro ID'}]
            }
        };

        await (wrapper.vm as any).handleErrors(store);
        expect((wrapper.vm as any).fieldErrors.atividades).toBe('Erro ID');
    });

    it('disponibilizarMapa returns early if codSubprocesso is null', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        (wrapper.vm as any).codSubprocesso = null;
        await (wrapper.vm as any).disponibilizarMapa({});

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa() as any;
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

        (wrapper.vm as any).mostrarModalDisponibilizar = true;
        (wrapper.vm as any).fecharModalDisponibilizar();

        expect((wrapper.vm as any).mostrarModalDisponibilizar).toBe(false);
    });
});
