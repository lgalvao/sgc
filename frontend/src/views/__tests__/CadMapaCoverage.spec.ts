import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import CadMapa from '@/views/CadMapa.vue';
import {useMapasStore} from '@/stores/mapas';
import {useSubprocessosStore} from '@/stores/subprocessos';

// Mock router
vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    useRoute: vi.fn(() => ({ params: { codProcesso: '1', siglaUnidade: 'TEST' } })),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: vi.fn(),
        replace: vi.fn()
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe('CadMapa Coverage', () => {
    const commonStubs = {
        PageHeader: { template: '<div><slot /><slot name="actions" /></div>' },
        BButton: { template: '<button />' },
        BContainer: { template: '<div><slot /></div>' },
        LoadingButton: { template: '<button />' },
        EmptyState: { template: '<div><slot /></div>' },
        CompetenciaCard: { template: '<div />' },
        CriarCompetenciaModal: { template: '<div />' },
        DisponibilizarMapaModal: { template: '<div />' },
        ModalConfirmacao: { template: '<div />' },
        ImpactoMapaModal: { template: '<div />' },
        BAlert: { template: '<div />' }
    };

    it('removerAtividadeAssociada does nothing if competency not found', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                mapas: {
                    mapaCompleto: {
                        competencias: [{ codigo: 1, descricao: 'Comp 1', atividadesAssociadas: [10] }]
                    }
                }
            }
        });

        const wrapper = mount(CadMapa, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const mapasStore = useMapasStore(pinia);

        // Call with invalid ID
        await (wrapper.vm as any).removerAtividadeAssociada(999, 10);

        expect(mapasStore.atualizarCompetencia).not.toHaveBeenCalled();
    });

    it('abrirModalImpacto does nothing if codSubprocesso is missing', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: { subprocessoDetalhe: null }, // ensures codSubprocesso setup might fail or return null
                mapas: { mapaCompleto: { competencias: [] } }
            }
        });

        const store = useSubprocessosStore(pinia);
        (store.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(null);

        const wrapper = mount(CadMapa, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });

        const mapasStore = useMapasStore(pinia);

        await wrapper.vm.$nextTick(); // Wait for mount

        // Trigger
        await (wrapper.vm as any).abrirModalImpacto();

        expect(mapasStore.buscarImpactoMapa).not.toHaveBeenCalled();
    });
});
