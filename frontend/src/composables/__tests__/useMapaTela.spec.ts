import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useMapaTela} from '../useMapaTela';
import {ref} from 'vue';

// Mocks de dependências
vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: vi.fn(),
    }),
}));

const notifyMock = vi.fn();
vi.mock('@/composables/useNotification', () => ({
    useNotification: () => ({ notify: notifyMock }),
}));

const registrarPendenteMock = vi.fn();
vi.mock('@/composables/useToast', () => ({
    useToast: () => ({
        registrarPendente: registrarPendenteMock,
        exibirSucesso: vi.fn(),
        exibirErro: vi.fn(),
        exibirToast: vi.fn(),
        exibirPendente: vi.fn(),
    }),
}));

const contextoEdicaoRef = ref<any>(null);
const erroIntegracaoContextoRef = ref<any>(null);
vi.mock('@/stores/subprocesso', () => ({
    useSubprocessoStore: () => ({
        contextoEdicao: contextoEdicaoRef.value,
        get erroIntegracaoContexto() { return erroIntegracaoContextoRef.value; },
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        perfilSelecionado: 'SERVIDOR',
    }),
}));

vi.mock('@/composables/useInvalidacaoNavegacao', () => ({
    useInvalidacaoNavegacao: () => ({
        atualizarFluxoMapa: vi.fn(),
    }),
}));

const carregarContextoInicialMock = vi.fn().mockResolvedValue(true);
const codigoSubprocessoRef = ref<number | null>(null);
vi.mock('@/composables/useMapaOrquestracao', () => ({
    useMapaOrquestracao: () => ({
        carregandoInicial: ref(false),
        codigoSubprocesso: codigoSubprocessoRef,
        unidade: ref(null),
        carregarContextoInicial: carregarContextoInicialMock,
    }),
}));

const mapaCompletoRef = ref<any>({ atividades: [], competencias: [] });
vi.mock('@/composables/useMapas', () => ({
    useMapas: () => ({
        mapaCompleto: mapaCompletoRef,
        impactoMapa: ref(null),
        sincronizarMapa: vi.fn(),
        carregarImpacto: vi.fn(),
    }),
}));

vi.mock('@/composables/useAcesso', () => ({
    useAcesso: () => ({
        acaoPrincipalMapa: ref({ mostrar: true, habilitar: true, rotuloBotao: 'Homologar' }),
    }),
}));

vi.mock('@/composables/useFluxoMapa', () => ({
    useFluxoMapa: () => ({
        carregando: ref(false),
    }),
}));

vi.mock('@/composables/useFormErrors', () => ({
    useFormErrors: () => ({
        erros: ref({}),
        aplicarErroNormalizado: vi.fn(),
        limparErros: vi.fn(),
    }),
}));

vi.mock('@/composables/useMapaCompetenciasMutacoes', () => ({
    useMapaCompetenciasMutacoes: () => ({}),
}));

const { useMapaDisponibilizacaoArgs } = vi.hoisted(() => ({
    useMapaDisponibilizacaoArgs: { value: null as any }
}));

vi.mock('@/views/mapaDisponibilizacao', () => ({
    useMapaDisponibilizacao: (args: any) => {
        useMapaDisponibilizacaoArgs.value = args;
        return {
            erroValidacaoMapa: ref(null),
            abrirModalDisponibilizar: vi.fn(),
            fecharModalDisponibilizar: vi.fn(),
            disponibilizarMapa: vi.fn(),
            limparErroMapa: vi.fn(),
            sincronizarMapaContexto: vi.fn(),
        };
    },
}));

vi.mock('@/utils/apiError', () => ({
    normalizarErro: (err: any) => {
        if (err.message === 'Network Error') return { tipo: 'inesperado', mensagem: 'Erro de rede', status: 0 };
        if (err.message === 'Inesperado 500') return { tipo: 'inesperado', mensagem: 'Erro Servidor', status: 500 };
        return { tipo: 'erro', mensagem: err.message || 'Erro' };
    },
}));

vi.mock('@/services/relatoriosService', () => ({
    relatoriosService: {
        downloadRelatorioMapaAtualPdf: vi.fn(),
        downloadRelatorioMapaAtualCsv: vi.fn(),
    },
}));

describe('useMapaTela', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        codigoSubprocessoRef.value = 123;
        contextoEdicaoRef.value = null;
        erroIntegracaoContextoRef.value = null;
        carregarContextoInicialMock.mockResolvedValue(true);
        mapaCompletoRef.value = { atividades: [], competencias: [] };
        useMapaDisponibilizacaoArgs.value = null;
    });

    it('notificar erro de exportacao com fallback', async () => {
        const { exportarMapaAtualPdf } = useMapaTela({ codProcesso: 1, sigla: 'TEST' }) as any;
        const { relatoriosService } = await import('@/services/relatoriosService');
        vi.mocked(relatoriosService.downloadRelatorioMapaAtualPdf).mockRejectedValueOnce(new Error('Network Error'));
        
        await exportarMapaAtualPdf();
        
        expect(notifyMock).toHaveBeenCalledWith('Erro ao exportar PDF', 'danger');
    });

    it('notificar erro de exportacao com erro inesperado e status', async () => {
        const { exportarMapaAtualCsv } = useMapaTela({ codProcesso: 1, sigla: 'TEST' }) as any;
        const { relatoriosService } = await import('@/services/relatoriosService');
        vi.mocked(relatoriosService.downloadRelatorioMapaAtualCsv).mockRejectedValueOnce(new Error('Inesperado 500'));
        
        await exportarMapaAtualCsv();
        
        expect(notifyMock).toHaveBeenCalledWith('Erro Servidor', 'danger');
    });

    it('notificar erro de exportacao com mensagem do erro', async () => {
        const { exportarMapaAtualPdf } = useMapaTela({ codProcesso: 1, sigla: 'TEST' }) as any;
        const { relatoriosService } = await import('@/services/relatoriosService');
        vi.mocked(relatoriosService.downloadRelatorioMapaAtualPdf).mockRejectedValueOnce(new Error('Erro especifico'));
        
        await exportarMapaAtualPdf();
        
        expect(notifyMock).toHaveBeenCalledWith('Erro especifico', 'danger');
    });

    it('deve notificar erro se carregarContextoInicial falhar', async () => {
        carregarContextoInicialMock.mockResolvedValueOnce(false);
        
        // Simula montagem do componente sem mockar o onMounted globalmente,
        // apenas avaliando a função que foi passada para onMounted.
        let mountedCb: any = null;
        vi.doMock('vue', async (importOriginal) => {
            const actual = await importOriginal<any>();
            return {
                ...actual,
                onMounted: (cb: any) => { mountedCb = cb; }
            };
        });
        
        // Reimport the module so the new mock is used
        const { useMapaTela: reloadedUseMapaTela } = await import('../useMapaTela');
        reloadedUseMapaTela({ codProcesso: 1, sigla: 'TEST' });
        
        if (mountedCb) {
            await mountedCb();
            expect(notifyMock).toHaveBeenCalledWith('Falha grave ao resolver subprocesso para o mapa. A ocorrência deve ser auditada.', 'danger');
        }
        
        vi.doUnmock('vue');
    });

    it('computa atividadesSemCompetencia corretamente', () => {
        useMapaTela({ codProcesso: 1, sigla: 'TEST' });
        
        mapaCompletoRef.value = {
            atividades: [{ codigo: 1 }, { codigo: 2 }],
            competencias: [{ atividades: [{ codigo: 1 }] }]
        };
        
        expect(useMapaDisponibilizacaoArgs.value.atividadesSemCompetencia.value).toEqual([{ codigo: 2 }]);
    });

    it('computa atividadesSemCompetencia vazio se nao houver atividades', () => {
        useMapaTela({ codProcesso: 1, sigla: 'TEST' });
        
        mapaCompletoRef.value = {
            atividades: [],
            competencias: []
        };
        
        expect(useMapaDisponibilizacaoArgs.value.atividadesSemCompetencia.value).toEqual([]);
    });

    it('computa existeCompetenciaSemAtividade corretamente', () => {
        useMapaTela({ codProcesso: 1, sigla: 'TEST' });
        
        mapaCompletoRef.value = {
            atividades: [],
            competencias: [{ atividades: [] }]
        };
        
        expect(useMapaDisponibilizacaoArgs.value.existeCompetenciaSemAtividade.value).toBe(true);
    });
});
