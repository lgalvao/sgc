import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ref } from 'vue';
import { useSubprocessoResolver } from '../useSubprocessoResolver';
import { useProcessosStore } from '@/stores/processos';
import { useUnidadesStore } from '@/stores/unidades';
import { initPinia } from '@/test-utils/helpers';

vi.mock('@/stores/processos');
vi.mock('@/stores/unidades');

describe('useSubprocessoResolver', () => {
    beforeEach(() => {
        initPinia();
        vi.clearAllMocks();
    });

    it('deve retornar nomeUnidade corretamente', () => {
        vi.mocked(useUnidadesStore).mockReturnValue({
            unidade: { nome: 'Unidade Teste' },
        } as any);

        vi.mocked(useProcessosStore).mockReturnValue({
            processoDetalhe: null,
        } as any);

        const { nomeUnidade } = useSubprocessoResolver(ref(1), ref('TESTE'));

        expect(nomeUnidade.value).toBe('Unidade Teste');
    });

    it('deve retornar nomeUnidade vazio se unidade for nula', () => {
        vi.mocked(useUnidadesStore).mockReturnValue({
            unidade: null,
        } as any);

        vi.mocked(useProcessosStore).mockReturnValue({
             processoDetalhe: null,
        } as any);

        const { nomeUnidade } = useSubprocessoResolver(ref(1), ref('TESTE'));

        expect(nomeUnidade.value).toBe('');
    });

    it('deve encontrar unidade recursivamente na árvore', () => {
        vi.mocked(useUnidadesStore).mockReturnValue({
            unidade: null,
        } as any);

        const mockProcessoDetalhe = {
            unidades: [
                {
                    sigla: 'PAI',
                    filhos: [
                        {
                            sigla: 'FILHO',
                            codSubprocesso: 100,
                            mapaCodigo: 200,
                        },
                    ],
                },
            ],
        };

        vi.mocked(useProcessosStore).mockReturnValue({
            processoDetalhe: mockProcessoDetalhe,
        } as any);

        const { codSubprocesso, codMapa, subprocesso } = useSubprocessoResolver(ref(1), ref('FILHO'));

        expect(codSubprocesso.value).toBe(100);
        expect(codMapa.value).toBe(200);
        expect(subprocesso.value).toEqual(mockProcessoDetalhe.unidades[0].filhos[0]);
    });

    it('deve retornar nulo se unidade não for encontrada', () => {
        vi.mocked(useUnidadesStore).mockReturnValue({
            unidade: null,
        } as any);

        const mockProcessoDetalhe = {
            unidades: [
                {
                    sigla: 'OUTRA',
                    filhos: [],
                },
            ],
        };

        vi.mocked(useProcessosStore).mockReturnValue({
            processoDetalhe: mockProcessoDetalhe,
        } as any);

        const { codSubprocesso, codMapa, subprocesso } = useSubprocessoResolver(ref(1), ref('INEXISTENTE'));

        expect(codSubprocesso.value).toBeUndefined();
        expect(codMapa.value).toBeUndefined();
        expect(subprocesso.value).toBeNull();
    });

    it('deve lidar com processoDetalhe nulo', () => {
        vi.mocked(useUnidadesStore).mockReturnValue({
            unidade: null,
        } as any);

        vi.mocked(useProcessosStore).mockReturnValue({
            processoDetalhe: null,
        } as any);

        const { codSubprocesso, codMapa, subprocesso } = useSubprocessoResolver(ref(1), ref('TESTE'));

        expect(codSubprocesso.value).toBeUndefined();
        expect(codMapa.value).toBeUndefined();
        expect(subprocesso.value).toBeNull();
    });
});
