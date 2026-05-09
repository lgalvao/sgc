import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {useMapas} from '@/composables/useMapas';
import MapaView from '@/views/MapaView.vue';
import * as useFluxoMapaModule from '@/composables/useFluxoMapa';
import * as subprocessoService from '@/services/subprocessoService';
import type {ContextoEdicaoSubprocesso} from '@/types/tipos';

type MapaViewVm = {
    codigoSubprocesso: number | null;
    mostrarModalImpacto: boolean;
    mostrarModalDisponibilizar: boolean;
    fieldErrors: Record<string, string | undefined>;
    existeCompetenciaSemAtividade: boolean;
    aplicarErroNormalizado: (error: { erros?: Array<{ campo?: string; mensagem?: string }> } | null) => void;
    abrirModalImpacto: () => Promise<void>;
    fecharModalImpacto: () => void;
    removerAtividadeAssociada: (codigoCompetencia: number, codigoAtividade: number) => Promise<void> | void;
    disponibilizarMapa: (payload: Record<string, unknown>) => Promise<void>;
    fecharModalDisponibilizar: () => void;
};

vi.mock('@/composables/useFluxoMapa', () => ({useFluxoMapa: vi.fn()}));
vi.mock('@/services/subprocessoService', () => ({
    verificarImpactosMapa: vi.fn(),
    obterMapaCompleto: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
}));
const subprocessoStoreMock = {
    contextoEdicao: null as ContextoEdicaoSubprocesso | null,
    erroIntegracaoContexto: null as { message: string } | null,
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
    limparContextoAtual: vi.fn(),
    invalidar: vi.fn(),
};
vi.mock('@/stores/subprocesso', () => ({useSubprocessoStore: () => subprocessoStoreMock}));

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
        erro: null as { message: string } | null,
        adicionarCompetencia: vi.fn(),
        atualizarCompetencia: vi.fn(),
        removerCompetencia: vi.fn(),
        removerAtividadeDaCompetencia: vi.fn(),
        disponibilizarMapa: vi.fn(),
    };

    beforeEach(() => {
        vi.clearAllMocks();
        subprocessoStoreMock.contextoEdicao = null;
        subprocessoStoreMock.erroIntegracaoContexto = null;
        subprocessoStoreMock.garantirContextoEdicao.mockResolvedValue(null);
        subprocessoStoreMock.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);
        fluxoMapaMock.erro = null;
        fluxoMapaMock.adicionarCompetencia = vi.fn();
        fluxoMapaMock.atualizarCompetencia = vi.fn();
        fluxoMapaMock.removerCompetencia = vi.fn();
        fluxoMapaMock.removerAtividadeDaCompetencia = vi.fn();
        fluxoMapaMock.disponibilizarMapa = vi.fn();
        vi.mocked(useFluxoMapaModule.useFluxoMapa).mockReturnValue(fluxoMapaMock as unknown as ReturnType<typeof useFluxoMapaModule.useFluxoMapa>);
    });

    it('removerAtividadeAssociada does nothing if competency not found', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: false,
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
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
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
            stubActions: false,
            initialState: {
                subprocessos: {subprocessoDetalhe: null}, // ensures codSubprocesso setup might fail or return null
                mapas: {mapaCompleto: {competencias: []}}
            }
        });

        subprocessoStoreMock.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        await wrapper.vm.$nextTick(); // Wait for mount

        const vm = wrapper.vm as unknown as MapaViewVm;
        await vm.abrirModalImpacto();

        expect(subprocessoService.verificarImpactosMapa).not.toHaveBeenCalled();
    });

    it('abrirModalImpacto calls store when codSubprocesso is present', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: false,
            initialState: {
                mapas: {mapaCompleto: {competencias: []}}
            }
        });

        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        vi.mocked(subprocessoService.verificarImpactosMapa).mockResolvedValue({} as any);
        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codigoSubprocesso = 456;

        await vm.abrirModalImpacto();
        await new Promise(r => setTimeout(r, 0));

        expect(subprocessoService.verificarImpactosMapa).toHaveBeenCalledWith(456);
        expect(vm.mostrarModalImpacto).toBe(true);
    });

    it('removerAtividadeAssociada updates store if competency is found', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: false,
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
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });
        const mapas = useMapas();
        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codigoSubprocesso = 456;
        mapas.definirMapaCompleto(456, {
            competencias: [{codigo: 1, descricao: 'Comp 1', atividades: [{codigo: 10}, {codigo: 20}]}]
        } as unknown as NonNullable<typeof mapas.mapaCompleto.value>);

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa();

        await vm.removerAtividadeAssociada(1, 10);

        expect(fluxoMapa.removerAtividadeDaCompetencia).toHaveBeenCalledWith(456, 1, 10);
    });

    it('desabilita disponibilizacao quando existir competencia sem atividade', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: false,
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
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        const mapas = useMapas();
        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codigoSubprocesso = 456;
        mapas.definirMapaCompleto(456, {
            competencias: [{codigo: 1, descricao: 'Comp 1', atividades: []}]
        } as unknown as NonNullable<typeof mapas.mapaCompleto.value>);

        await wrapper.vm.$nextTick();

        expect(vm.existeCompetenciaSemAtividade).toBe(true);
    });

    it('fecharModalImpacto closes the modal', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn, stubActions: false});
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.mostrarModalImpacto = true;
        vm.fecharModalImpacto();

        expect(vm.mostrarModalImpacto).toBe(false);
    });

    it('aplicarErroNormalizado sincroniza campo atividadesCodigos para atividades', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn, stubActions: false});
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.aplicarErroNormalizado({erros: [{campo: 'atividadesCodigos', mensagem: 'Erro em atividade'}]});
        expect(vm.fieldErrors.atividades).toBe('Erro em atividade');
    });

    it('disponibilizarMapa falha com invariante quando codigoSubprocesso nao foi carregado', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn, stubActions: false});
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.codigoSubprocesso = null;
        await expect(vm.disponibilizarMapa({})).rejects.toThrow('Invariante violada: codigoSubprocesso não carregado');

        const fluxoMapa = useFluxoMapaModule.useFluxoMapa();
        expect(fluxoMapa.disponibilizarMapa).not.toHaveBeenCalled();
    });

    it('fecharModalDisponibilizar clears state', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn, stubActions: false});
        const wrapper = mount(MapaView, {
            global: {
                plugins: [pinia],
                stubs: commonStubs
            },
            props: {
                codProcesso: 1,
                sigla: "TESTE"
            }
        });

        const vm = wrapper.vm as unknown as MapaViewVm;
        vm.mostrarModalDisponibilizar = true;
        vm.fecharModalDisponibilizar();

        expect(vm.mostrarModalDisponibilizar).toBe(false);
    });
});
