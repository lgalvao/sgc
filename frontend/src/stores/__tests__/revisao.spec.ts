import {beforeEach, describe, expect, it} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {TipoMudanca, useRevisaoStore} from '../revisao';

describe('useRevisaoStore', () => {
    let revisaoStore: ReturnType<typeof useRevisaoStore>;

    beforeEach(() => {
        initPinia();
        revisaoStore = useRevisaoStore();
        revisaoStore.limparMudancas();
    });

    it('should initialize with empty mudancas arrays', () => {
        expect(revisaoStore.mudancasRegistradas).toEqual([]);
    });

    describe('registrarMudanca', () => {
        it('should add mudanca with auto-generated id', () => {
            const mudanca = {
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Nova Atividade'
            };

            revisaoStore.registrarMudanca(mudanca);

            expect(revisaoStore.mudancasRegistradas).toHaveLength(1);
            expect(revisaoStore.mudancasRegistradas[0]).toMatchObject(mudanca);
            expect(revisaoStore.mudancasRegistradas[0].id).toBeDefined();
        });

        it('should increment nextId for each mudanca', () => {
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade 1'
            });
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.ConhecimentoAdicionado,
                descricaoConhecimento: 'Conhecimento 1'
            });

            expect(revisaoStore.mudancasRegistradas).toHaveLength(2);
            expect(revisaoStore.mudancasRegistradas[0].id).toBeDefined();
            expect(revisaoStore.mudancasRegistradas[1].id).toBeDefined();
        });
    });

    describe('limparMudancas', () => {
        it('should clear all mudancas', () => {
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade'
            });

            revisaoStore.limparMudancas();

            expect(revisaoStore.mudancasRegistradas).toEqual([]);
        });
    });

    describe('TipoMudanca enum', () => {
        it('should have all expected values', () => {
            expect(TipoMudanca.AtividadeAdicionada).toBe('AtividadeAdicionada');
            expect(TipoMudanca.AtividadeRemovida).toBe('AtividadeRemovida');
            expect(TipoMudanca.AtividadeAlterada).toBe('AtividadeAlterada');
            expect(TipoMudanca.ConhecimentoAdicionado).toBe('ConhecimentoAdicionado');
            expect(TipoMudanca.ConhecimentoRemovido).toBe('ConhecimentoRemovido');
            expect(TipoMudanca.ConhecimentoAlterado).toBe('ConhecimentoAlterado');
        });
    });
});
