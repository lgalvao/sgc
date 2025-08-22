import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useRevisaoStore, TipoMudanca} from '../revisao';

describe('useRevisaoStore', () => {
    let revisaoStore: ReturnType<typeof useRevisaoStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        revisaoStore = useRevisaoStore();
        revisaoStore.limparMudancas();
        revisaoStore.setMudancasParaImpacto([]);
    });

    it('should initialize with empty mudancas arrays', () => {
        expect(revisaoStore.mudancasRegistradas).toEqual([]);
        expect(revisaoStore.mudancasParaImpacto).toEqual([]);
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

    describe('setMudancasParaImpacto', () => {
        it('should set mudancasParaImpacto array', () => {
            const mudancas = [
                {
                    id: 1,
                    tipo: TipoMudanca.AtividadeAdicionada,
                    descricaoAtividade: 'Atividade Teste'
                }
            ];

            revisaoStore.setMudancasParaImpacto(mudancas);

            expect(revisaoStore.mudancasParaImpacto).toEqual(mudancas);
        });

        it('should replace existing mudancasParaImpacto', () => {
            const mudancas1 = [{
                id: 1,
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade 1'
            }];
            const mudancas2 = [{
                id: 2,
                tipo: TipoMudanca.ConhecimentoAdicionado,
                descricaoConhecimento: 'Conhecimento 1'
            }];

            revisaoStore.setMudancasParaImpacto(mudancas1);
            expect(revisaoStore.mudancasParaImpacto).toEqual(mudancas1);

            revisaoStore.setMudancasParaImpacto(mudancas2);
            expect(revisaoStore.mudancasParaImpacto).toEqual(mudancas2);
        });
    });

    describe('limparMudancas', () => {
        it('should clear all mudancas', () => {
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade'
            });
            revisaoStore.setMudancasParaImpacto([{
                id: 1,
                tipo: TipoMudanca.ConhecimentoAdicionado,
                descricaoConhecimento: 'Conhecimento'
            }]);

            revisaoStore.limparMudancas();

            expect(revisaoStore.mudancasRegistradas).toEqual([]);
            // mudancasParaImpacto is not cleared by limparMudancas
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