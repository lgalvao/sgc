import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {type TipoNotificacao, useNotificacoesStore} from '../notificacoes';

describe('useNotificacoesStore', () => {
    let notificacoesStore: ReturnType<typeof useNotificacoesStore>;

    beforeEach(() => {
          initPinia();
          notificacoesStore = useNotificacoesStore();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    describe('initial state', () => {
        it('should start with empty notifications array', () => {
            expect(notificacoesStore.notificacoes).toEqual([]);
        });
    });

    describe('adicionarNotificacao', () => {
        it('should add notification with generated id and timestamp', () => {
            const notificacaoData = {
                tipo: 'info' as TipoNotificacao,
                titulo: 'Teste',
                mensagem: 'Mensagem de teste'
            };

            const id = notificacoesStore.adicionarNotificacao(notificacaoData);

            expect(notificacoesStore.notificacoes).toHaveLength(1);
            const notificacao = notificacoesStore.notificacoes[0];

            expect(notificacao.id).toBe(id);
            expect(notificacao.tipo).toBe('info');
            expect(notificacao.titulo).toBe('Teste');
            expect(notificacao.mensagem).toBe('Mensagem de teste');
            expect(notificacao.timestamp).toBeInstanceOf(Date);
            expect(notificacao.duracao).toBe(0); // default duration for non-success
        });

        it('should use default duration for success notifications', () => {
            const notificacaoData = {
                tipo: 'success' as TipoNotificacao,
                titulo: 'Sucesso',
                mensagem: 'Mensagem de sucesso'
            };

            notificacoesStore.adicionarNotificacao(notificacaoData);

            expect(notificacoesStore.notificacoes[0].duracao).toBe(3000);
        });

        it('should use custom duration when provided', () => {
            const notificacaoData = {
                tipo: 'error' as TipoNotificacao,
                titulo: 'Erro',
                mensagem: 'Mensagem de erro',
                duracao: 5000
            };

            notificacoesStore.adicionarNotificacao(notificacaoData);

            expect(notificacoesStore.notificacoes[0].duracao).toBe(5000);
        });

        it('should generate unique ids for multiple notifications', () => {
            const id1 = notificacoesStore.adicionarNotificacao({ tipo: 'success', titulo: '1', mensagem: '1' });
            const id2 = notificacoesStore.adicionarNotificacao({ tipo: 'error', titulo: '2', mensagem: '2' });

            expect(id1).not.toBe(id2);
            expect(notificacoesStore.notificacoes).toHaveLength(2);
        });
    });

    describe('removerNotificacao', () => {
        it('should remove notification by id', () => {
            const id = notificacoesStore.adicionarNotificacao({ tipo: 'info', titulo: 'Teste', mensagem: 'Msg' });
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            notificacoesStore.removerNotificacao(id);
            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should not remove anything if id does not exist', () => {
            notificacoesStore.adicionarNotificacao({ tipo: 'warning', titulo: 'Teste', mensagem: 'Msg' });
            notificacoesStore.removerNotificacao('nonexistent-id');
            expect(notificacoesStore.notificacoes).toHaveLength(1);
        });
    });

    describe('limparTodas', () => {
        it('should clear all notifications', () => {
            notificacoesStore.sucesso('Teste 1', 'Msg 1');
            notificacoesStore.erro('Teste 2', 'Msg 2');
            expect(notificacoesStore.notificacoes).toHaveLength(2);
            notificacoesStore.limparTodas();
            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });
    });

    describe('convenience methods', () => {
        it('sucesso should add success notification with default duration', () => {
            notificacoesStore.sucesso('Sucesso!', 'Operação bem-sucedida');
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('success');
            expect(notificacao.duracao).toBe(3000);
        });

        it('erro should add error notification with default duration', () => {
            notificacoesStore.erro('Erro!', 'Ocorreu um erro');
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('error');
            expect(notificacao.duracao).toBe(0);
        });

        it('aviso should add warning notification with default duration', () => {
            notificacoesStore.aviso('Aviso!', 'Atenção');
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('warning');
            expect(notificacao.duracao).toBe(0);
        });

        it('info should add info notification with default duration', () => {
            notificacoesStore.info('Info', 'Informação');
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('info');
            expect(notificacao.duracao).toBe(0);
        });

        it('convenience methods should accept custom duration', () => {
            notificacoesStore.sucesso('Teste', 'Mensagem', 5000);
            expect(notificacoesStore.notificacoes[0].duracao).toBe(5000);
        });

        it('email should add email notification with custom duration', () => {
            notificacoesStore.email('Assunto', 'Dest', 'Corpo');
            const notificacao = notificacoesStore.notificacoes[0];
            expect(notificacao.tipo).toBe('email');
            expect(notificacao.duracao).toBe(10000);
        });
    });

    describe('auto-removal', () => {
        it('should auto-remove notification after its duration', () => {
            notificacoesStore.adicionarNotificacao({ tipo: 'success', titulo: 'Teste', mensagem: 'Msg', duracao: 3000 });
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            vi.advanceTimersByTime(3000);
            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should not auto-remove if duration is 0', () => {
            notificacoesStore.adicionarNotificacao({ tipo: 'error', titulo: 'Teste', mensagem: 'Msg', duracao: 0 });
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            vi.advanceTimersByTime(5000);
            expect(notificacoesStore.notificacoes).toHaveLength(1);
        });

        it('should handle multiple notifications with different durations', () => {
            notificacoesStore.sucesso('Sucesso 1', 'Msg 1', 2000);
            notificacoesStore.sucesso('Sucesso 2', 'Msg 2', 4000);
            expect(notificacoesStore.notificacoes).toHaveLength(2);

            vi.advanceTimersByTime(2000);
            expect(notificacoesStore.notificacoes).toHaveLength(1);
            expect(notificacoesStore.notificacoes[0].titulo).toBe('Sucesso 2');

            vi.advanceTimersByTime(2000);
            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });
    });
});
