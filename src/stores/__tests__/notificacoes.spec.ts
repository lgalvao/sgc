import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {type TipoNotificacao, useNotificacoesStore} from '../notificacoes';

describe('useNotificacoesStore', () => {
    let notificacoesStore: ReturnType<typeof useNotificacoesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        notificacoesStore = useNotificacoesStore();
    });

    afterEach(() => {
        vi.clearAllTimers();
    });

    describe('initial state', () => {
        it('should start with empty notifications array', () => {
            expect(notificacoesStore.notificacoes).toEqual([]);
        });
    });

    describe('adicionarNotificacao', () => {
        it('should add notification with generated id and timestamp', () => {
            const notificacaoData = {
                tipo: 'success' as TipoNotificacao,
                titulo: 'Teste',
                mensagem: 'Mensagem de teste'
            };

            const id = notificacoesStore.adicionarNotificacao(notificacaoData);

            expect(notificacoesStore.notificacoes).toHaveLength(1);
            const notificacao = notificacoesStore.notificacoes[0];

            expect(notificacao.id).toBe(id);
            expect(notificacao.tipo).toBe('success');
            expect(notificacao.titulo).toBe('Teste');
            expect(notificacao.mensagem).toBe('Mensagem de teste');
            expect(notificacao.timestamp).toBeInstanceOf(Date);
            expect(notificacao.duracao).toBe(5000); // default duration
        });

        it('should use custom duration when provided', () => {
            const notificacaoData = {
                tipo: 'error' as TipoNotificacao,
                titulo: 'Erro',
                mensagem: 'Mensagem de erro',
                duracao: 3000
            };

            notificacoesStore.adicionarNotificacao(notificacaoData);

            expect(notificacoesStore.notificacoes[0].duracao).toBe(3000);
        });

        it('should generate unique ids for multiple notifications', () => {
            const notificacao1 = notificacoesStore.adicionarNotificacao({
                tipo: 'success',
                titulo: 'Teste 1',
                mensagem: 'Mensagem 1'
            });

            const notificacao2 = notificacoesStore.adicionarNotificacao({
                tipo: 'error',
                titulo: 'Teste 2',
                mensagem: 'Mensagem 2'
            });

            expect(notificacao1).not.toBe(notificacao2);
            expect(notificacoesStore.notificacoes).toHaveLength(2);
        });
    });

    describe('removerNotificacao', () => {
        it('should remove notification by id', () => {
            const id = notificacoesStore.adicionarNotificacao({
                tipo: 'info',
                titulo: 'Teste',
                mensagem: 'Mensagem'
            });

            expect(notificacoesStore.notificacoes).toHaveLength(1);

            notificacoesStore.removerNotificacao(id);

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should not remove anything if id does not exist', () => {
            notificacoesStore.adicionarNotificacao({
                tipo: 'warning',
                titulo: 'Teste',
                mensagem: 'Mensagem'
            });

            notificacoesStore.removerNotificacao('nonexistent-id');

            expect(notificacoesStore.notificacoes).toHaveLength(1);
        });
    });

    describe('limparTodas', () => {
        it('should clear all notifications', () => {
            notificacoesStore.adicionarNotificacao({
                tipo: 'success',
                titulo: 'Teste 1',
                mensagem: 'Mensagem 1'
            });

            notificacoesStore.adicionarNotificacao({
                tipo: 'error',
                titulo: 'Teste 2',
                mensagem: 'Mensagem 2'
            });

            expect(notificacoesStore.notificacoes).toHaveLength(2);

            notificacoesStore.limparTodas();

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });
    });

    describe('convenience methods', () => {
        it('sucesso should add success notification', () => {
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('success');
            expect(notificacao.titulo).toBe('Sucesso!');
            expect(notificacao.mensagem).toBe('Operação realizada com sucesso');
        });

        it('erro should add error notification', () => {
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('error');
            expect(notificacao.titulo).toBe('Erro!');
            expect(notificacao.mensagem).toBe('Ocorreu um erro');
        });

        it('aviso should add warning notification', () => {
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('warning');
            expect(notificacao.titulo).toBe('Aviso!');
            expect(notificacao.mensagem).toBe('Atenção necessária');
        });

        it('info should add info notification', () => {
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('info');
            expect(notificacao.titulo).toBe('Informação');
            expect(notificacao.mensagem).toBe('Informação importante');
        });

        it('convenience methods should accept custom duration', () => {
            notificacoesStore.sucesso('Teste', 'Mensagem', 3000);

            expect(notificacoesStore.notificacoes[0].duracao).toBe(3000);
        });
    });

    describe('auto-removal', () => {
        beforeEach(() => {
            vi.useFakeTimers();
        });

        afterEach(() => {
            vi.restoreAllMocks();
        });

        it('should auto-remove notification after default duration', () => {
            notificacoesStore.adicionarNotificacao({
                tipo: 'success',
                titulo: 'Teste',
                mensagem: 'Mensagem',
                duracao: 5000
            });

            expect(notificacoesStore.notificacoes).toHaveLength(1);

            vi.advanceTimersByTime(5000);

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should auto-remove notification after custom duration', () => {
            notificacoesStore.adicionarNotificacao({
                tipo: 'error',
                titulo: 'Teste',
                mensagem: 'Mensagem',
                duracao: 3000
            });

            expect(notificacoesStore.notificacoes).toHaveLength(1);

            vi.advanceTimersByTime(3000);

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should not auto-remove if duration is 0', () => {
            notificacoesStore.adicionarNotificacao({
                tipo: 'info',
                titulo: 'Teste',
                mensagem: 'Mensagem',
                duracao: 0
            });

            expect(notificacoesStore.notificacoes).toHaveLength(1);

            vi.advanceTimersByTime(10000);

            expect(notificacoesStore.notificacoes).toHaveLength(1);
        });

        it('should not auto-remove if duration is undefined', () => {
            notificacoesStore.adicionarNotificacao({
                tipo: 'warning',
                titulo: 'Teste',
                mensagem: 'Mensagem'
                // duracao not provided, should default to 5000
            });

            expect(notificacoesStore.notificacoes).toHaveLength(1);

            vi.advanceTimersByTime(5000);

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });
    });
});