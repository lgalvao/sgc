import {describe, expect, it, vi, beforeEach} from 'vitest';
import {useMapaTela} from '../useMapaTela';
import {ref, reactive} from 'vue';

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

vi.mock('@/stores/toast', () => ({
    useToastStore: () => ({
        setPending: vi.fn(),
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

vi.mock('@/composables/useMapas', () => ({
    useMapas: () => ({
        mapaCompleto: ref(null),
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

vi.mock('@/utils/apiError', () => ({
    normalizarErro: (err: any) => {
        if (err.message === 'Network Error') return { tipo: 'inesperado', mensagem: 'Erro de rede', status: 0 };
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
    });

    it('notificar erro de exportacao com fallback', async () => {
        const { exportarMapaAtualPdf } = useMapaTela({ codProcesso: 1, sigla: 'TEST' }) as any;
        const { relatoriosService } = await import('@/services/relatoriosService');
        vi.mocked(relatoriosService.downloadRelatorioMapaAtualPdf).mockRejectedValueOnce(new Error('Network Error'));
        
        await exportarMapaAtualPdf();
        
        expect(notifyMock).toHaveBeenCalledWith('Erro ao exportar PDF', 'danger');
    });

    it('notificar erro de exportacao com mensagem do erro', async () => {
        const { exportarMapaAtualPdf } = useMapaTela({ codProcesso: 1, sigla: 'TEST' }) as any;
        const { relatoriosService } = await import('@/services/relatoriosService');
        vi.mocked(relatoriosService.downloadRelatorioMapaAtualPdf).mockRejectedValueOnce(new Error('Erro especifico'));
        
        await exportarMapaAtualPdf();
        
        expect(notifyMock).toHaveBeenCalledWith('Erro especifico', 'danger');
    });
});
