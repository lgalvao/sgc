import {beforeEach, describe, expect, it, vi} from 'vitest';
import {effectScope, nextTick, ref} from 'vue';
import {useEquipeDiagnostico} from '../useEquipeDiagnostico';
import * as diagnosticoService from '@/services/diagnosticoService';
import * as diagnosticoContexto from '@/composables/useDiagnosticoContexto';

const CODIGO_SUBPROCESSO_VALIDO = 456;
const CODIGO_SUBPROCESSO_INVALIDO = 0;
const CODIGO_USUARIO_VALIDO = '998877';

let queryOptions: any = null;
const mockQueryData = ref<any>(null);
const mockQueryStatus = ref<'pending' | 'success'>('success');

vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn((options: any) => {
        queryOptions = options;
        return {
            data: mockQueryData,
            status: mockQueryStatus,
        };
    }),
}));

const mockUsuarioCodigo = ref<string | undefined>(CODIGO_USUARIO_VALIDO);
vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        get usuarioCodigo() {
            return mockUsuarioCodigo.value;
        },
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterEquipe: vi.fn(),
}));

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    criarContextoSessaoDiagnostico: vi.fn(() => 'contexto-sessao-mock'),
    chaveEquipe: vi.fn((cod, ctx) => ['chave-equipe-mock', cod, ctx]),
}));

describe('useEquipeDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        queryOptions = null;
        mockQueryData.value = null;
        mockQueryStatus.value = 'success';
        mockUsuarioCodigo.value = CODIGO_USUARIO_VALIDO;
    });

    it('deve inicializar com o estado correto da query e computeds vazios por padrão', async () => {
        const scope = effectScope();
        let composable: ReturnType<typeof useEquipeDiagnostico> | undefined;

        scope.run(() => {
            composable = useEquipeDiagnostico(CODIGO_SUBPROCESSO_VALIDO);
        });
        await nextTick();

        expect(composable).toBeDefined();
        expect(composable!.itens.value).toEqual([]);
        expect(composable!.carregando.value).toBe(false);
        expect(composable!.totalServidores.value).toBe(0);
        expect(composable!.pendentes.value).toBe(0);

        scope.stop();
    });

    it('deve mapear corretamente as opções do useQuery', async () => {
        const scope = effectScope();
        scope.run(() => {
            useEquipeDiagnostico(CODIGO_SUBPROCESSO_VALIDO);
        });
        await nextTick();

        expect(queryOptions).toBeDefined();

        // Testa a chave do cache
        const chaveEsperada = ['chave-equipe-mock', CODIGO_SUBPROCESSO_VALIDO, 'contexto-sessao-mock'];
        expect(queryOptions.key()).toEqual(chaveEsperada);
        expect(diagnosticoContexto.chaveEquipe).toHaveBeenCalledWith(CODIGO_SUBPROCESSO_VALIDO, 'contexto-sessao-mock');

        // Testa a query function
        vi.mocked(diagnosticoService.obterEquipe).mockResolvedValue({ servidores: [] } as any);
        await queryOptions.query();
        expect(diagnosticoService.obterEquipe).toHaveBeenCalledWith(CODIGO_SUBPROCESSO_VALIDO);

        // Testa se a query está habilitada (perfil com código e subprocesso maior que zero)
        expect(queryOptions.enabled()).toBe(true);

        // Testa query desabilitada se código do usuário for vazio
        mockUsuarioCodigo.value = undefined;
        expect(queryOptions.enabled()).toBe(false);

        // Testa query desabilitada se subprocesso for inválido
        mockUsuarioCodigo.value = CODIGO_USUARIO_VALIDO;
        const scope2 = effectScope();
        scope2.run(() => {
            useEquipeDiagnostico(CODIGO_SUBPROCESSO_INVALIDO);
        });
        await nextTick();
        expect(queryOptions.enabled()).toBe(false);

        scope.stop();
        scope2.stop();
    });

    it('deve calcular corretamente a quantidade de servidores e pendências', async () => {
        mockQueryData.value = {
            servidores: [
                { servidorTitulo: '1', situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA' },
                { servidorTitulo: '2', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA' },
                { servidorTitulo: '3', situacaoServidor: 'CONSENSO_CRIADO' },
                { servidorTitulo: '4', situacaoServidor: 'CONSENSO_APROVADO' },
                { servidorTitulo: '5', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA' },
            ],
        };

        const scope = effectScope();
        let composable: ReturnType<typeof useEquipeDiagnostico> | undefined;
        scope.run(() => {
            composable = useEquipeDiagnostico(CODIGO_SUBPROCESSO_VALIDO);
        });
        await nextTick();

        // Total deve ser 5
        expect(composable!.totalServidores.value).toBe(5);

        // Pendentes devem ser 3 (excluindo os aprovados ou impossibilitados, isto é, 1, 2 e 3)
        expect(composable!.pendentes.value).toBe(3);

        // Deve refletir o carregando como true quando a query estiver pendente
        mockQueryStatus.value = 'pending';
        expect(composable!.carregando.value).toBe(true);

        scope.stop();
    });
});
