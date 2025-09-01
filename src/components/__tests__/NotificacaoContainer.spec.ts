import {beforeEach, describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import NotificacaoContainer from '../NotificacaoContainer.vue';
import {type TipoNotificacao, useNotificacoesStore} from '@/stores/notificacoes';

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

    describe('iconeTipo function', () => {
        it('should return correct icon classes for each type', () => {
            const wrapper = mountComponent();

            // Access the component's iconeTipo method
            const vm = wrapper.vm as InstanceType<typeof NotificacaoContainer>;

            expect(vm.iconeTipo('success')).toBe('bi bi-check-circle-fill text-success');
            expect(vm.iconeTipo('error')).toBe('bi bi-exclamation-triangle-fill text-danger');
            expect(vm.iconeTipo('warning')).toBe('bi bi-exclamation-triangle-fill text-warning');
            expect(vm.iconeTipo('info')).toBe('bi bi-info-circle-fill text-info');
            expect(vm.iconeTipo('unknown' as TipoNotificacao)).toBe('bi bi-bell-fill');
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
});