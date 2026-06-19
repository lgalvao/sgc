import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useConsensoDiagnostico} from '../useConsensoDiagnostico';
import {nextTick, ref} from 'vue';
import {useQuery, useQueryCache} from '@pinia/colada';

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

const podeCriarConsensoMock = ref(false);
const statusContextoEdicaoMock = ref<'pending' | 'success'>('success');
vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: podeCriarConsensoMock,
        queryContextoEdicao: {
            status: statusContextoEdicaoMock,
        },
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterConsenso: vi.fn(),
    obterConsensoServidor: vi.fn(),
    salvarConsenso: vi.fn(),
    concluirConsenso: vi.fn(),
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
        podeCriarConsensoMock.value = false;
        statusContextoEdicaoMock.value = 'success';
        
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

    function criarCompetenciaConsenso(overrides: Record<string, unknown> = {}) {
        return {
            competenciaCodigo: 1,
            servidorImportancia: null,
            servidorDominio: null,
            chefiaImportancia: null,
            chefiaDominio: null,
            consensoImportancia: null,
            consensoDominio: null,
            ...overrides,
        };
    }

    it('deve inicializar com listas vazias', () => {
        const { competenciasLocais } = useConsensoDiagnostico(1);
        expect(competenciasLocais.value).toEqual([]);
    });

    it('deve consultar consenso do servidor especifico quando a sessao pode criar consenso', async () => {
        const {obterConsenso, obterConsensoServidor} = await import('@/services/diagnosticoService');
        podeCriarConsensoMock.value = true;
        vi.mocked(useQuery).mockImplementation((options: any) => {
            return {
                data: ref(null),
                status: ref('success'),
            } as any;
        });

        useConsensoDiagnostico(1, 'chefia123');
        const queryOptions = vi.mocked(useQuery).mock.calls[0]?.[0] as any;
        await queryOptions.query();

        expect(obterConsensoServidor).toHaveBeenCalledWith(1, 'chefia123');
        expect(obterConsenso).not.toHaveBeenCalled();
    });

    it('deve aguardar permissoes antes de habilitar consulta com servidor na rota de consenso', () => {
        statusContextoEdicaoMock.value = 'pending';
        podeCriarConsensoMock.value = true;

        useConsensoDiagnostico(1, '242424');
        const queryOptions = vi.mocked(useQuery).mock.calls[0]?.[0] as any;

        expect(queryOptions.enabled()).toBe(false);

        statusContextoEdicaoMock.value = 'success';

        expect(queryOptions.enabled()).toBe(true);
    });

    it('deve consultar consenso do usuario logado quando a sessao nao pode criar consenso', async () => {
        const {obterConsenso, obterConsensoServidor} = await import('@/services/diagnosticoService');
        vi.mocked(useQuery).mockImplementation((options: any) => {
            return {
                data: ref(null),
                status: ref('success'),
            } as any;
        });

        useConsensoDiagnostico(1, 'chefia123');
        const queryOptions = vi.mocked(useQuery).mock.calls[0]?.[0] as any;
        await queryOptions.query();

        expect(obterConsenso).toHaveBeenCalledWith(1);
        expect(obterConsensoServidor).not.toHaveBeenCalled();
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

    it('deve autopreencher consenso na carga quando servidor e chefia ja vierem iguais e nao nulos', async () => {
        const queryData = ref<any>(null);
        vi.mocked(useQuery).mockReturnValue({
            data: queryData,
            status: ref('success'),
        } as any);

        const { competenciasLocais } = useConsensoDiagnostico(1);

        queryData.value = {
            competencias: [{
                competenciaCodigo: 1,
                servidorImportancia: 5,
                servidorDominio: 4,
                chefiaImportancia: 5,
                chefiaDominio: 4,
                consensoImportancia: null,
                consensoDominio: null,
            }],
            situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
        };

        await nextTick();

        expect(competenciasLocais.value[0].consensoImportancia).toBe(5);
        expect(competenciasLocais.value[0].consensoDominio).toBe(4);
    });

    it('deve normalizar autoavaliacao do backend para os campos do servidor', async () => {
        const queryData = ref<any>(null);
        vi.mocked(useQuery).mockReturnValue({
            data: queryData,
            status: ref('success'),
        } as any);

        const { competenciasLocais } = useConsensoDiagnostico(1);

        queryData.value = {
            competencias: [{
                competenciaCodigo: 1,
                servidorImportancia: 5,
                servidorDominio: 4,
                chefiaImportancia: null,
                chefiaDominio: null,
                consensoImportancia: null,
                consensoDominio: null,
            }],
            situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
        };

        await nextTick();

        expect(competenciasLocais.value[0].servidorImportancia).toBe(5);
        expect(competenciasLocais.value[0].servidorDominio).toBe(4);
    });

    it('deve disparar autosave e executar onSettled', async () => {
        const { salvarConsenso } = await import('@/services/diagnosticoService');
        const queryData = ref<any>({
            situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
            competencias: [],
            podeEditar: true,
            podeConcluirAvaliacao: true,
            habilitarConcluirAvaliacao: true,
            podeAprovarConsenso: false,
            habilitarAprovarConsenso: false,
        });
        vi.mocked(useQuery).mockReturnValue({
            data: queryData,
            status: ref('success'),
        } as any);
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

    it('deve chamar mutacao de conclusao e invalidar as queries relevantes', async () => {
        const { concluirConsenso } = await import('@/services/diagnosticoService');
        const { concluirAvaliacao } = useConsensoDiagnostico(1, 'servidor1');
        await concluirAvaliacao();

        expect(concluirConsenso).toHaveBeenCalledWith(1, 'servidor1');
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveConsenso' }));
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveEquipe' }));
        expect(invalidateQueriesMock).toHaveBeenCalledWith(expect.objectContaining({ key: 'chaveAuto' }));
    });

    it('deve autopreencher consenso de importancia quando servidor e chefe forem iguais e nao nulos', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);
        
        competenciasLocais.value = [{
            competenciaCodigo: 1,
            servidorImportancia: 5,
            servidorDominio: 4,
            chefiaImportancia: null,
            chefiaDominio: null,
            consensoImportancia: null,
            consensoDominio: null,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'importancia', valor: 5 });

        expect(competenciasLocais.value[0].consensoImportancia).toBe(5);
        expect(competenciasLocais.value[0].consensoDominio).toBeNull();
    });

    it('deve autopreencher consenso de dominio quando servidor e chefe forem iguais e nao nulos', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);

        competenciasLocais.value = [{
            competenciaCodigo: 1,
            servidorImportancia: 5,
            servidorDominio: 4,
            chefiaImportancia: null,
            chefiaDominio: null,
            consensoImportancia: null,
            consensoDominio: null,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'dominio', valor: 4 });

        expect(competenciasLocais.value[0].consensoDominio).toBe(4);
        expect(competenciasLocais.value[0].consensoImportancia).toBeNull();
    });

    it('deve autopreencher apenas o campo igual e manter nulo o campo sem igualdade valida', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);

        competenciasLocais.value = [{
            competenciaCodigo: 1,
            servidorImportancia: 5,
            servidorDominio: null,
            chefiaImportancia: null,
            chefiaDominio: null,
            consensoImportancia: null,
            consensoDominio: null,
        }] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'importancia', valor: 5 });
        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'dominio', valor: null });

        expect(competenciasLocais.value[0].consensoImportancia).toBe(5);
        expect(competenciasLocais.value[0].consensoDominio).toBeNull();
    });

    it('deve limpar o consenso quando a chefia alterar o campo para vazio', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);

        competenciasLocais.value = [criarCompetenciaConsenso({
            servidorImportancia: 5,
            servidorDominio: 4,
            chefiaImportancia: 5,
            chefiaDominio: 4,
            consensoImportancia: 5,
            consensoDominio: 4,
        })] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'importancia', valor: null });

        expect(competenciasLocais.value[0].chefiaImportancia).toBeNull();
        expect(competenciasLocais.value[0].consensoImportancia).toBeNull();
        expect(competenciasLocais.value[0].consensoDominio).toBe(4);
    });

    it('deve preservar consenso anterior quando a chefia divergir do servidor na importancia', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);

        competenciasLocais.value = [criarCompetenciaConsenso({
            servidorImportancia: 2,
            chefiaImportancia: 1,
            consensoImportancia: 2,
        })] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'importancia', valor: 4 });

        expect(competenciasLocais.value[0].chefiaImportancia).toBe(4);
        expect(competenciasLocais.value[0].consensoImportancia).toBe(2);
    });

    it('deve preservar consenso anterior quando a chefia divergir do servidor no dominio', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);

        competenciasLocais.value = [criarCompetenciaConsenso({
            servidorDominio: 2,
            chefiaDominio: 5,
            consensoDominio: 5,
        })] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'dominio', valor: 4 });

        expect(competenciasLocais.value[0].chefiaDominio).toBe(4);
        expect(competenciasLocais.value[0].consensoDominio).toBe(5);
    });

    it('deve preservar consenso diferente mesmo quando servidor e chefia forem iguais', () => {
        const { competenciasLocais, atualizarNotaDetalhada } = useConsensoDiagnostico(1);

        competenciasLocais.value = [criarCompetenciaConsenso({
            servidorImportancia: 1,
            chefiaImportancia: 1,
            consensoImportancia: 2,
        })] as any;

        atualizarNotaDetalhada(1, { origem: 'chefia', campo: 'importancia', valor: 1 });

        expect(competenciasLocais.value[0].consensoImportancia).toBe(2);
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
        
        competenciasLocais.value = [criarCompetenciaConsenso({
            servidorImportancia: 3,
            servidorDominio: 2,
            consensoImportancia: 5,
            consensoDominio: 4,
        })] as any;

        atualizarNotaDetalhada(1, { origem: 'consenso', campo: 'importancia', valor: 5 });
        
        vi.advanceTimersByTime(800);
        
        expect(salvarConsenso).toHaveBeenCalledWith(1, 'servidor1', expect.objectContaining({
            competencias: [expect.objectContaining({
                competenciaCodigo: 1,
                consensoImportancia: 5,
                consensoDominio: 4,
                servidorImportancia: 3,
                servidorDominio: 2,
            })]
        }));
    });
});
