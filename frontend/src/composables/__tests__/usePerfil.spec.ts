
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { usePerfil } from '../usePerfil';
import { usePerfilStore } from '@/stores/perfil';
import { useUsuariosStore } from '@/stores/usuarios';
import { useUnidadesStore } from '@/stores/unidades';
import { useAtribuicaoTemporariaStore } from '@/stores/atribuicoes';
import { Perfil } from '@/types/tipos';
import { initPinia } from '@/test-utils/helpers';

vi.mock('@/stores/perfil');
vi.mock('@/stores/usuarios');
vi.mock('@/stores/unidades');
vi.mock('@/stores/atribuicoes');

describe('usePerfil', () => {
    beforeEach(() => {
        initPinia();
        vi.clearAllMocks();
    });

    it('deve retornar o perfil e a unidade selecionada corretamente', () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            perfilSelecionado: Perfil.CHEFE,
            unidadeSelecionada: 123,
        } as any);

        vi.mocked(useUnidadesStore).mockReturnValue({
            unidades: [{ codigo: 123, sigla: 'TESTE' }],
        } as any);

        const { perfilSelecionado, unidadeSelecionada } = usePerfil();

        expect(perfilSelecionado.value).toBe(Perfil.CHEFE);
        expect(unidadeSelecionada.value).toBe('TESTE');
    });

    it('deve retornar o servidor logado com os dados corretos', () => {
        vi.mocked(usePerfilStore).mockReturnValue({
            servidorId: 1,
            perfilSelecionado: Perfil.GESTOR,
            unidadeSelecionada: 456,
        } as any);

        vi.mocked(useUsuariosStore).mockReturnValue({
            getUsuarioById: (id: number) => ({ id, nome: 'Usuário Teste' }),
        } as any);

        const { servidorLogado } = usePerfil();

        expect(servidorLogado.value).toEqual({
            id: 1,
            nome: 'Usuário Teste',
            perfil: Perfil.GESTOR,
            unidade: 456,
        });
    });

    it('deve calcular os perfis do servidor corretamente', () => {
        const mockUnidades = [
            { codigo: 1, sigla: 'ADMIN_UNIT', tipo: 'INTERMEDIARIA', idServidorTitular: 1 },
            { codigo: 2, sigla: 'CHEFE_UNIT', tipo: 'OPERACIONAL', idServidorTitular: 1 },
            { codigo: 3, sigla: 'SERVIDOR_UNIT', tipo: 'OPERACIONAL', idServidorTitular: 2 },
        ];

        vi.mocked(useUsuariosStore).mockReturnValue({
            getUsuarioById: (id: number) => ({ id, nome: 'Servidor', unidade: { sigla: 'SERVIDOR_UNIT' } }),
        } as any);

        vi.mocked(useUnidadesStore).mockReturnValue({
            unidades: mockUnidades,
            pesquisarUnidade: (sigla: string) => mockUnidades.find(u => u.sigla === sigla),
        } as any);

        vi.mocked(useAtribuicaoTemporariaStore).mockReturnValue({
            getAtribuicoesPorServidor: () => [],
        } as any);

        const { getPerfisDoServidor } = usePerfil();
        const perfis = getPerfisDoServidor(1);

        expect(perfis).toHaveLength(3);
        expect(perfis).toEqual(
            expect.arrayContaining([
                expect.objectContaining({ perfil: Perfil.GESTOR, siglaUnidade: 'ADMIN_UNIT' }),
                expect.objectContaining({ perfil: Perfil.CHEFE, siglaUnidade: 'CHEFE_UNIT' }),
                expect.objectContaining({ perfil: Perfil.SERVIDOR, siglaUnidade: 'SERVIDOR_UNIT' }),
            ])
        );
    });
});
