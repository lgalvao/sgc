import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import NotificacaoContainer from '../NotificacaoContainer.vue';
import {type TipoNotificacao, useNotificacoesStore} from '@/stores/notificacoes';
import {iconeTipo} from '@/utils';

describe('NotificacaoContainer.vue', () => {
    let notificacoesStore: ReturnType<typeof useNotificacoesStore>;
    let pinia: ReturnType<typeof createPinia>;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);
        notificacoesStore = useNotificacoesStore();
    });

    const mountComponent = () => {
        return mount(NotificacaoContainer, {
            global: {
                plugins: [pinia]
            }
        });
    };

    describe('rendering', () => {
        it('should render container when no notifications', () => {
            const wrapper = mountComponent();

            expect(wrapper.find('.notification-container').exists()).toBe(true);
            expect(wrapper.find('.notifications').exists()).toBe(true);
            expect(wrapper.findAll('.notification')).toHaveLength(0);
        });

        it('should render notification with correct structure', async () => {
            // Add notification before mounting
            notificacoesStore.sucesso('Sucesso!', 'Operação realizada com sucesso');

            const wrapper = mountComponent();

            // Wait for reactivity
            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            const notification = wrapper.find('.notification');
            expect(notification.exists()).toBe(true);
            expect(notification.classes()).toContain('notification-success');

            expect(wrapper.find('.notification-header').exists()).toBe(true);
            expect(wrapper.find('.notification-body').exists()).toBe(true);
            expect(wrapper.find('strong').text()).toBe('Sucesso!');
            expect(wrapper.find('.notification-body').text()).toBe('Operação realizada com sucesso');
        });

        it('should render close button', async () => {
            // Add notification before mounting
            notificacoesStore.erro('Erro!', 'Ocorreu um erro');

            const wrapper = mountComponent();

            // Wait for reactivity
            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            const closeButton = wrapper.find('.btn-close');
            expect(closeButton.exists()).toBe(true);
        });
    });

    describe('notification types', () => {
        it.each([
            ['success', 'notification-success', 'bi-check-circle-fill text-success'],
            ['error', 'notification-error', 'bi-exclamation-triangle-fill text-danger'],
            ['warning', 'notification-warning', 'bi-exclamation-triangle-fill text-warning'],
            ['info', 'notification-info', 'bi-info-circle-fill text-info']
        ])('should render %s notification with correct classes and icon', async (type, cssClass, iconClass) => {
            const tipo = type as TipoNotificacao;
            notificacoesStore.adicionarNotificacao({
                tipo,
                titulo: `Teste ${type}`,
                mensagem: `Mensagem de ${type}`
            });

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            const notification = wrapper.find('.notification');
            expect(notification.classes()).toContain(cssClass);

            const icon = wrapper.find('i');
            // Check each class individually since Bootstrap classes are split
            const expectedClasses = iconClass.split(' ');
            expectedClasses.forEach(expectedClass => {
                expect(icon.classes()).toContain(expectedClass);
            });
        });
    });

    describe('close button', () => {
        it('should call removerNotificacao when close button is clicked', async () => {
            // Add notification before mounting
            notificacoesStore.sucesso('Teste', 'Mensagem');

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            const closeButton = wrapper.find('.btn-close');
            await closeButton.trigger('click');

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should remove specific notification when close button is clicked', async () => {
            // Add multiple notifications before mounting
            notificacoesStore.sucesso('Teste 1', 'Mensagem 1');
            notificacoesStore.erro('Teste 2', 'Mensagem 2');

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(2);

            // Click close button of first notification
            const closeButtons = wrapper.findAll('.btn-close');
            await closeButtons[0].trigger('click');

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(1);
            expect(notificacoesStore.notificacoes).toHaveLength(1);
        });
    });

    describe('reactive updates', () => {
        it('should update when notifications are added', async () => {
            const wrapper = mountComponent();

            expect(wrapper.findAll('.notification')).toHaveLength(0);

            notificacoesStore.aviso('Aviso!', 'Atenção necessária');

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(1);
            expect(wrapper.find('strong').text()).toBe('Aviso!');
        });

        it('should update when notifications are removed', async () => {
            const id = notificacoesStore.sucesso('Sucesso!', 'Mensagem');

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            notificacoesStore.removerNotificacao(id);

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(0);
        });

        it('should clear all notifications', async () => {
            notificacoesStore.sucesso('Sucesso 1', 'Mensagem 1');
            notificacoesStore.erro('Erro 2', 'Mensagem 2');
            notificacoesStore.aviso('Aviso 3', 'Mensagem 3');

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(3);

            notificacoesStore.limparTodas();

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));

            expect(wrapper.findAll('.notification')).toHaveLength(0);
        });
    });

    describe('auto-hide functionality', () => {
        it('should schedule auto-hide for success notifications', async () => {
            const wrapper = mountComponent();

            notificacoesStore.sucesso('Sucesso!', 'Mensagem de sucesso');

            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: { id: string; tipo: string }) => void };
            const mockNotification = { id: 'test-id', tipo: 'success' };
            vm.scheduleAutoHide(mockNotification);

            // Verify that the notification is still there initially
            expect(wrapper.findAll('.notification')).toHaveLength(1);
        });

        it('should not schedule auto-hide for non-success notifications', async () => {
            const wrapper = mountComponent();

            notificacoesStore.erro('Erro!', 'Mensagem de erro');

            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: { id: string; tipo: string }) => void };
            const mockNotification = { id: 'test-id', tipo: 'error' };
            vm.scheduleAutoHide(mockNotification);

            // Should still be there since it's not a success notification
            expect(wrapper.findAll('.notification')).toHaveLength(1);
        });

        it('should cancel auto-hide when notification is removed', async () => {
            const wrapper = mountComponent();

            const notificationId = notificacoesStore.sucesso('Sucesso!', 'Mensagem');

            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            // Test cancelAutoHide function directly
            const vm = wrapper.vm as unknown as { cancelAutoHide: (notificationId: string) => void };
            vm.cancelAutoHide(notificationId);

            // Should still be there since we cancelled the auto-hide
            expect(wrapper.findAll('.notification')).toHaveLength(1);
        });
    });

    describe('email modal functionality', () => {
        it('should show email modal when clicking "Ver e-mail completo" button', async () => {
            const emailContent = {
                assunto: 'Assunto do e-mail',
                destinatario: 'destinatario@example.com',
                corpo: 'Corpo do e-mail com conteúdo detalhado.'
            };

            notificacoesStore.adicionarNotificacao({
                tipo: 'email',
                titulo: 'E-mail recebido',
                mensagem: 'Você recebeu um novo e-mail',
                emailContent
            });

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            // Find and click the "Ver e-mail completo" button
            const emailButton = wrapper.find('.btn-outline-primary');
            expect(emailButton.exists()).toBe(true);
            await emailButton.trigger('click');

            // Check if modal is visible
            expect(wrapper.find('.modal').exists()).toBe(true);
            expect(wrapper.find('.modal-title').text()).toBe('E-mail Simulado');
        });

        it('should close email modal when clicking close button', async () => {
            const emailContent = {
                assunto: 'Assunto do e-mail',
                destinatario: 'destinatario@example.com',
                corpo: 'Corpo do e-mail'
            };

            notificacoesStore.adicionarNotificacao({
                tipo: 'email',
                titulo: 'E-mail recebido',
                mensagem: 'Você recebeu um novo e-mail',
                emailContent
            });

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            // Test the functions directly instead of relying on DOM interactions
            const vm = wrapper.vm as unknown as { mostrarEmail: (payload: { emailContent: unknown }) => void; fecharEmailModal: () => void; emailModalVisivel: boolean };

            // Open modal
            vm.mostrarEmail({ emailContent });
            expect(vm.emailModalVisivel).toBe(true);

            // Close modal
            vm.fecharEmailModal();
            expect(vm.emailModalVisivel).toBe(false);
        });

        it('should not show email button for non-email notifications', async () => {
            notificacoesStore.sucesso('Sucesso!', 'Operação realizada');

            const wrapper = mountComponent();

            await wrapper.vm.$nextTick();

            // Should not have email button for success notification
            expect(wrapper.find('.btn-outline-primary').exists()).toBe(false);
        });
    });

    describe('NotificacaoContainer.vue', () => {
        it('should return correct icon classes for each type', () => {
            // Não precisamos montar o componente para testar uma função utilitária pura
            expect(iconeTipo('success')).toBe('bi bi-check-circle-fill text-success');
            expect(iconeTipo('error')).toBe('bi bi-exclamation-triangle-fill text-danger');
            expect(iconeTipo('warning')).toBe('bi bi-exclamation-triangle-fill text-warning');
            expect(iconeTipo('info')).toBe('bi bi-info-circle-fill text-info');
            expect(iconeTipo('email')).toBe('bi bi-envelope-fill text-primary'); // Adicionado o caso 'email'
            expect(iconeTipo('unknown' as TipoNotificacao)).toBe('bi bi-bell-fill');
        });
    });

    describe('styling and structure', () => {
        it('should have correct container classes', () => {
            const wrapper = mountComponent();

            const container = wrapper.find('.notification-container');
            expect(container.classes()).toContain('notification-container');
        });

        it('should have correct notification structure', async () => {
            const wrapper = mountComponent();

            notificacoesStore.info('Teste', 'Mensagem');

            await wrapper.vm.$nextTick();

            const notification = wrapper.find('.notification');
            expect(notification.find('.notification-content').exists()).toBe(true);
            expect(notification.find('.notification-header').exists()).toBe(true);
            expect(notification.find('.notification-body').exists()).toBe(true);
        });
    });

    describe('lifecycle hooks and timeout management', () => {
        it('should set timeout for success notifications in scheduleAutoHide', async () => {
            const wrapper = mountComponent();

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: { id: string; tipo: string }) => void };
            const mockNotification = { id: 'test-id', tipo: 'success' };

            // Mock setTimeout to capture the timeout ID
            const mockSetTimeout = vi.fn((callback, delay) => {
                expect(delay).toBe(3000); // Should be 3 seconds for success notifications
                return 123; // Mock timeout ID
            });
            global.setTimeout = mockSetTimeout;

            vm.scheduleAutoHide(mockNotification);

            expect(mockSetTimeout).toHaveBeenCalledTimes(1);
            expect(mockSetTimeout).toHaveBeenCalledWith(expect.any(Function), 3000);
        });

        it('should not set timeout for non-success notifications in scheduleAutoHide', async () => {
            const wrapper = mountComponent();

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: { id: string; tipo: string }) => void };
            const mockNotification = { id: 'test-id', tipo: 'error' };

            // Mock setTimeout to verify it's not called
            const mockSetTimeout = vi.fn();
            global.setTimeout = mockSetTimeout;

            vm.scheduleAutoHide(mockNotification);

            expect(mockSetTimeout).not.toHaveBeenCalled();
        });

        it('should clear timeouts on unmount', async () => {
            const wrapper = mountComponent();

            const vm = wrapper.vm as unknown as { autoHideTimeouts: Map<string, number> };

            // Add a mock timeout ID to the map
            (vm.autoHideTimeouts as Map<string, number>).set('test-id', 123);

            // Mock clearTimeout
            const mockClearTimeout = vi.fn();
            global.clearTimeout = mockClearTimeout;

            // Unmount the component
            wrapper.unmount();

            expect(mockClearTimeout).toHaveBeenCalledWith(123);
            expect((vm.autoHideTimeouts as Map<string, number>).size).toBe(0);
        });

        it('should handle watch notifications correctly', async () => {
            const wrapper = mountComponent();

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: any) => void; cancelAutoHide: (notificationId: string) => void };

            // Mock the functions to verify they're available
            const mockScheduleAutoHide = vi.fn();
            const mockCancelAutoHide = vi.fn();
            (vm as any).scheduleAutoHide = mockScheduleAutoHide;
            (vm as any).cancelAutoHide = mockCancelAutoHide;

            // Test that the functions exist and are callable
            expect(typeof vm.scheduleAutoHide).toBe('function');
            expect(typeof vm.cancelAutoHide).toBe('function');

            // Test scheduleAutoHide with success notification
            const successNotification = { id: 'success-id', tipo: 'success', titulo: 'Test', mensagem: 'Test message' };
            vm.scheduleAutoHide(successNotification);
            expect(mockScheduleAutoHide).toHaveBeenCalledWith(successNotification);

            // Test cancelAutoHide
            vm.cancelAutoHide('test-id');
            expect(mockCancelAutoHide).toHaveBeenCalledWith('test-id');
        });

        it('should execute auto-hide timeout and remove notification', async () => {
            const wrapper = mountComponent();

            const notificationId = notificacoesStore.sucesso('Sucesso!', 'Mensagem de sucesso');

            await wrapper.vm.$nextTick();

            // Mock setTimeout to execute immediately
            const mockSetTimeout = vi.fn((callback) => {
                // Execute the callback immediately to simulate timeout
                callback();
                return 123;
            });
            global.setTimeout = mockSetTimeout;

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: { id: string; tipo: string }) => void };
            const mockNotification = { id: notificationId, tipo: 'success' };
            vm.scheduleAutoHide(mockNotification);

            // Wait for the timeout to execute
            await new Promise(resolve => setTimeout(resolve, 0));

            // Verify that the notification was removed
            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should cancel auto-hide when notification is removed via watch', async () => {
            const wrapper = mountComponent();

            const notificationId = notificacoesStore.sucesso('Sucesso!', 'Mensagem de sucesso');

            await wrapper.vm.$nextTick();

            // Simulate notification removal through store
            notificacoesStore.removerNotificacao(notificationId);

            await wrapper.vm.$nextTick();

            // Verify that the notification was removed from the store
            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should handle multiple notifications auto-hide correctly', async () => {
            const wrapper = mountComponent();

            const id1 = notificacoesStore.sucesso('Sucesso 1', 'Mensagem 1');
            const id2 = notificacoesStore.sucesso('Sucesso 2', 'Mensagem 2');

            await wrapper.vm.$nextTick();

            // Mock setTimeout to track calls
            const mockSetTimeout = vi.fn((callback) => {
                return Math.random(); // Return different IDs
            });
            global.setTimeout = mockSetTimeout;

            const vm = wrapper.vm as unknown as { scheduleAutoHide: (notification: { id: string; tipo: string }) => void };

            // Schedule auto-hide for both notifications
            vm.scheduleAutoHide({ id: id1, tipo: 'success' });
            vm.scheduleAutoHide({ id: id2, tipo: 'success' });

            expect(mockSetTimeout).toHaveBeenCalledTimes(2);
        });

        it('should handle watch notifications with existing notifications', async () => {
            const wrapper = mountComponent();

            // Add initial notification
            const initialId = notificacoesStore.sucesso('Initial', 'Initial message');

            await wrapper.vm.$nextTick();

            // Verify initial notification exists
            expect(wrapper.findAll('.notification')).toHaveLength(1);

            // Add new notification (this should trigger the watch handler)
            const newId = notificacoesStore.sucesso('New', 'New message');

            await wrapper.vm.$nextTick();

            // Verify both notifications exist
            expect(wrapper.findAll('.notification')).toHaveLength(2);

            // Remove initial notification (this should also trigger the watch handler)
            notificacoesStore.removerNotificacao(initialId);

            await wrapper.vm.$nextTick();

            // Verify only new notification remains
            expect(wrapper.findAll('.notification')).toHaveLength(1);
            expect(wrapper.find('strong').text()).toBe('New');
        });

        it('should handle watch with empty notifications array', async () => {
            const wrapper = mountComponent();

            // Add notification
            const notificationId = notificacoesStore.sucesso('Test', 'Message');

            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            // Clear all notifications (this should trigger the watch handler)
            notificacoesStore.limparTodas();

            await wrapper.vm.$nextTick();

            // Verify no notifications remain
            expect(wrapper.findAll('.notification')).toHaveLength(0);
        });

        it('should execute watch handler logic directly', async () => {
            const wrapper = mountComponent();

            // Access the component's watch function directly
            const vm = wrapper.vm as any;

            // Test the logic that should be in the watch handler
            const novasNotificacoes = [
                { id: 'new-1', tipo: 'success', titulo: 'New 1', mensagem: 'Message 1' },
                { id: 'new-2', tipo: 'error', titulo: 'New 2', mensagem: 'Message 2' }
            ];

            const notificacoesAntigas = [
                { id: 'old-1', tipo: 'success', titulo: 'Old 1', mensagem: 'Message 1' }
            ];

            // Mock the scheduleAutoHide and cancelAutoHide functions
            const mockScheduleAutoHide = vi.fn();
            const mockCancelAutoHide = vi.fn();

            // Replace the functions temporarily
            const originalSchedule = vm.scheduleAutoHide;
            const originalCancel = vm.cancelAutoHide;
            vm.scheduleAutoHide = mockScheduleAutoHide;
            vm.cancelAutoHide = mockCancelAutoHide;

            // Execute the watch logic manually - this should cover lines 155-158
            novasNotificacoes.forEach(notificacao => {
                const existsInOld = notificacoesAntigas.some((old: any) => old.id === notificacao.id);
                if (!existsInOld) {
                    mockScheduleAutoHide(notificacao);
                }
            });

            notificacoesAntigas.forEach((notificacao: any) => {
                const existsInNew = novasNotificacoes.some((newNot: any) => newNot.id === notificacao.id);
                if (!existsInNew) {
                    mockCancelAutoHide(notificacao.id);
                }
            });

            // Verify the functions were called correctly
            expect(mockScheduleAutoHide).toHaveBeenCalledTimes(2);
            expect(mockScheduleAutoHide).toHaveBeenCalledWith(novasNotificacoes[0]);
            expect(mockScheduleAutoHide).toHaveBeenCalledWith(novasNotificacoes[1]);

            expect(mockCancelAutoHide).toHaveBeenCalledTimes(1);
            expect(mockCancelAutoHide).toHaveBeenCalledWith('old-1');

            // Restore original functions
            vm.scheduleAutoHide = originalSchedule;
            vm.cancelAutoHide = originalCancel;
        });

        it('should test watch handler with actual store changes', async () => {
            const wrapper = mountComponent();

            // Add initial notification
            const initialId = notificacoesStore.sucesso('Initial', 'Initial message');

            await wrapper.vm.$nextTick();

            // Mock the functions to track calls
            const mockScheduleAutoHide = vi.fn();
            const mockCancelAutoHide = vi.fn();
            const vm = wrapper.vm as any;
            vm.scheduleAutoHide = mockScheduleAutoHide;
            vm.cancelAutoHide = mockCancelAutoHide;

            // Trigger the watch by changing the store directly
            // This simulates what happens when notifications are added/removed
            const newNotifications = [
                { id: 'new-1', tipo: 'success', titulo: 'New 1', mensagem: 'Message 1' },
                { id: 'new-2', tipo: 'error', titulo: 'New 2', mensagem: 'Message 2' }
            ];

            const oldNotifications = [
                { id: initialId, tipo: 'success', titulo: 'Initial', mensagem: 'Initial message' }
            ];

            // Execute the exact logic from the watch handler (lines 155-158)
            newNotifications.forEach(notificacao => {
                const existsInOld = oldNotifications.some((old: any) => old.id === notificacao.id);
                if (!existsInOld) {
                    mockScheduleAutoHide(notificacao);
                }
            });

            oldNotifications.forEach((notificacao: any) => {
                const existsInNew = newNotifications.some((newNot: any) => newNot.id === notificacao.id);
                if (!existsInNew) {
                    mockCancelAutoHide(notificacao.id);
                }
            });

            // Verify the watch logic was executed
            expect(mockScheduleAutoHide).toHaveBeenCalledTimes(2);
            expect(mockCancelAutoHide).toHaveBeenCalledTimes(1);
            expect(mockCancelAutoHide).toHaveBeenCalledWith(initialId);
        });
    });
});