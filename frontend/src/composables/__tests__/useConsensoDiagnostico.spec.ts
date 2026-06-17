import {describe, expect, it, vi, beforeEach} from 'vitest';
import {useConsensoDiagnostico} from '../useConsensoDiagnostico';
import {ref, nextTick} from 'vue';
import {useQuery, useMutation, useQueryCache} from '@pinia/colada';

let mutacaoSalvarOptions: any = null;
let mutacaoAprovarOptions: any = null;

// Mocks
vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn(),
    useMutation: vi.fn().mockImplementation((options: any) => {
        if (options.mutation && options.mutation.toString().includes('salvarConsenso')) {
            mutacaoSalvarOptions = options;
        } else {
            mutacaoAprovarOptions = options;
        }
        return {
            mutate: vi.fn().mockImplementation((...args: any[]) => {
                if (options.mutation) options.mutation(...args);
                if (options.onSuccess) options.onSuccess();
                if (options.onSettled) options.onSettled();
            }),
            mutateAsync: vi.fn().mockImplementation((...args: any[]) => {
                if (options.mutation) options.mutation(...args);
                if (options.onSuccess) options.onSuccess();
            }),
            isLoading: ref(false),
            error: ref(null),
        };
    }),
    useQueryCache: vi.fn(),
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
    chaveConsenso: vi.fn(() => 'chaveConsenso'),
    chaveEquipe: vi.fn(() => 'chaveEquipe'),
    chaveAutoavaliacao: vi.fn(() => 'chaveAuto'),
    criarContextoSessaoDiagnostico: vi.fn(),
}));

describe('useConsensoDiagnostico', () => {
    let invalidateQueriesMock: any;

    beforeEach(() => {
        vi.clearAllMocks();
        vi.useFakeTimers();
        mutacaoSalvarOptions = null;
        mutacaoAprovarOptions = null;
        
        invalidateQueriesMock = vi.fn();
        
        // Mock default behavior for useQuery
        vi.mocked(useQuery).mockReturnValue({
            data: ref(null),
            status: ref('pending'),
        } as any);
        
        vi.mocked(useQueryCache).mockReturnValue({
            getQueryData: vi.fn(),
            setQueryData: vi.fn(),
            invalidateQueries: invalidateQueriesMock,
        } as any);
    });

    it('deve inicializar com listas vazias', () => {
        const { competenciasLocais } = useConsensoDiagnostico(1);
        expect(competenciasLocais.value).toEqual([]);
    });

    it('deve atualizar estado local quando a query retorna dados', async () => {
        const queryData = ref<any>(null);
        vi.mocked(useQuery).mockReturnValue({
            data: queryData,
            status: ref('success'),
        } as any);

        const { competenciasLocais } = useConsensoDiagnostico(1);
        
        queryData.value = {
            competencias: [{ competenciaCodigo: 1, consensoImportancia: 3, consensoDominio: 2 }],
            situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'
        };

        await nextTick();
        
        expect(competenciasLocais.value).toHaveLength(1);
        expect(competenciasLocais.value[0].consensoImportancia).toBe(3);
    });

    it('deve disparar autosave e executar onSettled', async () => {
        const { salvarConsenso } = await import('@/services/diagnosticoService');
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1, 'servidor1');
        
        competenciasLocais.value = [{ competenciaCodigo: 1, consensoImportancia: 1, consensoDominio: 1 }] as any;
        
        atualizarNotaDetalhada(1, {origem: 'consenso', campo: 'importancia', valor: 5});
        
        vi.advanceTimersByTime(800);
        expect(salvarConsenso).toHaveBeenCalledWith(1, 'servidor1', expect.anything());
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveConsenso' }));
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveEquipe' }));
    });

    it('nao deve salvar se o consenso ja estiver aprovado', async () => {
        const { salvarConsenso } = await import('@/services/diagnosticoService');
        const queryData = ref<any>({ situacaoServidor: 'CONSENSO_APROVADO', competencias: [] });
        vi.mocked(useQuery).mockReturnValue({
            data: queryData,
            status: ref('success'),
        } as any);
        
        const { atualizarNotaDetalhada, competenciasLocais } = useConsensoDiagnostico(1, 'servidor1');
        competenciasLocais.value = [{ competenciaCodigo: 1, consensoImportancia: 1, consensoDominio: 1 }] as any;

        atualizarNotaDetalhada(1, {origem: 'consenso', campo: 'importancia', valor: 5});
        
        vi.advanceTimersByTime(800);
        expect(salvarConsenso).not.toHaveBeenCalled();
    });

    it('deve atualizar nota detalhada', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        
        competenciasLocais.value = [{
            competenciaCodigo: 1,
            consensoImportancia: 1,
            consensoDominio: 1,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'consenso', campo: 'importancia', valor: 4 });

        expect(competenciasLocais.value[0].consensoImportancia).toBe(4);
    });

    it('deve chamar mutacao de aprovacao e executar onSuccess', async () => {
        const { aprovarConsenso } = await import('@/services/diagnosticoService');
        const { aprovarConsenso: aprovar } = useConsensoDiagnostico(1);
        await aprovar();
        
        expect(aprovarConsenso).toHaveBeenCalledWith(1);
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveConsenso' }));
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveEquipe' }));
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveAuto' }));
    });

    it('deve atualizar nota detalhada (origem chefia) e autopreencher consenso se valores coincidirem', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        
        competenciasLocais.value = [{
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

        expect(competenciasLocais.value[0].consensoImportancia).toBe(5);
        expect(competenciasLocais.value[0].consensoDominio).toBe(4);
    });

    it('nao deve atualizar nota detalhada se competencia nao existir', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        competenciasLocais.value = [];
        
        atualizarNotaDetalhada(999, { origem: 'chefia', campo: 'importancia', valor: 5 });
        expect(competenciasLocais.value).toEqual([]);
    });

    it('deve gerar payload para salvar no formato detalhado', async () => {
        const { salvarConsenso } = await import('@/services/diagnosticoService');
        
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1, 'servidor1');
        
        competenciasLocais.value = [{
            competenciaCodigo: 1,
            consensoImportancia: 5,
            consensoDominio: 4,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'consenso', campo: 'importancia', valor: 5 });
        
        vi.advanceTimersByTime(800);
        
        expect(salvarConsenso).toHaveBeenCalledWith(1, 'servidor1', expect.objectContaining({
            competencias: [{ competenciaCodigo: 1, consensoImportancia: 5, consensoDominio: 4 }]
        }));
    });
});
