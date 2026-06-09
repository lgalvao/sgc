import {describe, expect, it, vi, beforeEach} from 'vitest';
import {useConsensoDiagnostico} from '../useConsensoDiagnostico';
import {ref} from 'vue';

// Mocks
vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn((options: any) => ({
        data: ref(null),
        status: ref('pending'),
        key: options.key(),
    })),
    useMutation: vi.fn(() => ({
        mutate: vi.fn(),
        mutateAsync: vi.fn(),
        isLoading: ref(false),
        error: ref(null),
    })),
    useQueryCache: vi.fn(() => ({
        setQueryData: vi.fn(),
        invalidateQueries: vi.fn(),
    })),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: 'chefia123',
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterConsenso: vi.fn(),
    obterConsensoServidor: vi.fn(),
    salvarConsenso: vi.fn(),
    aprovarConsenso: vi.fn(),
}));

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    chaveConsenso: vi.fn(),
    chaveEquipe: vi.fn(),
    chaveAutoavaliacao: vi.fn(),
    criarContextoSessaoDiagnostico: vi.fn(),
}));

describe('useConsensoDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve inicializar com listas vazias', () => {
        const { competenciasLocais, competenciasDetalhadasLocais } = useConsensoDiagnostico(1);
        expect(competenciasLocais.value).toEqual([]);
        expect(competenciasDetalhadasLocais.value).toEqual([]);
    });

    it('deve atualizar nota detalhada (origem chefia) e autopreencher consenso se valores coincidirem', () => {
        const { competenciasDetalhadasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        
        competenciasDetalhadasLocais.value = [{
            competenciaCodigo: 1,
            autoimportancia: 5,
            autodominio: 4,
            chefiaImportancia: null,
            chefiaDominio: null,
            consensoImportancia: null,
            consensoDominio: null,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'importancia', valor: 5 });
        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'dominio', valor: 4 });

        expect(competenciasDetalhadasLocais.value[0].consensoImportancia).toBe(5);
        expect(competenciasDetalhadasLocais.value[0].consensoDominio).toBe(4);
    });

    it('deve atualizar nota detalhada (origem consenso) sem alterar chefia', () => {
        const { competenciasDetalhadasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        
        competenciasDetalhadasLocais.value = [{
            competenciaCodigo: 1,
            chefiaImportancia: 3,
            consensoImportancia: null,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'consenso', campo: 'importancia', valor: 5 });

        expect(competenciasDetalhadasLocais.value[0].consensoImportancia).toBe(5);
        expect(competenciasDetalhadasLocais.value[0].chefiaImportancia).toBe(3);
    });

    it('nao deve atualizar nota detalhada se competencia nao existir', () => {
        const { competenciasDetalhadasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        competenciasDetalhadasLocais.value = [];
        
        atualizarNotaDetalhada(999, { origem: 'chefia', campo: 'importancia', valor: 5 });
        expect(competenciasDetalhadasLocais.value).toEqual([]);
    });
});
