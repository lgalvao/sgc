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
                plugins: [pinia],
                stubs: {
                    TransitionGroup: {
                        template: '<div><slot /></div>',
                        name: 'TransitionGroup'
                    }
                }
            }
        });
    };

    describe('rendering', () => {
        it('should render container when no notifications', () => {
            const wrapper = mountComponent();
            expect(wrapper.find('.notification-container').exists()).toBe(true);
            expect(wrapper.findAll('.notification')).toHaveLength(0);
        });

        it('should render a notification', async () => {
            notificacoesStore.sucesso('Sucesso!', 'Operação bem-sucedida');
            const wrapper = mountComponent();
            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(1);
            const notification = wrapper.find('.notification');
            expect(notification.classes()).toContain('notification-success');
            expect(notification.text()).toContain('Sucesso!');
            expect(notification.text()).toContain('Operação bem-sucedida');
        });

        it('should render multiple notifications', async () => {
            notificacoesStore.sucesso('Sucesso', 'S');
            notificacoesStore.erro('Erro', 'E');
            const wrapper = mountComponent();
            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(2);
        });
    });

    describe('interaction', () => {
        it('should remove notification when close button is clicked', async () => {
            notificacoesStore.erro('Erro', 'E');
            const wrapper = mountComponent();
            await wrapper.vm.$nextTick();

            expect(wrapper.findAll('.notification')).toHaveLength(1);

            await wrapper.find('.btn-close').trigger('click');
            await wrapper.vm.$nextTick();

            expect(notificacoesStore.notificacoes).toHaveLength(0);
        });

        it('should show email modal when button is clicked', async () => {
            notificacoesStore.email('Assunto', 'Dest', 'Corpo');
            const wrapper = mountComponent();
            await wrapper.vm.$nextTick();

            await wrapper.find('.btn-outline-primary').trigger('click');
            await wrapper.vm.$nextTick();

            expect(wrapper.find('.modal').exists()).toBe(true);
            expect(wrapper.text()).toContain('E-mail Simulado');
        });
    });

    describe('utility functions', () => {
        it('should return correct icon for each type', () => {
            expect(iconeTipo('success')).toContain('bi-check-circle-fill');
            expect(iconeTipo('error')).toContain('bi-exclamation-triangle-fill');
            expect(iconeTipo('warning')).toContain('bi-exclamation-triangle-fill');
            expect(iconeTipo('info')).toContain('bi-info-circle-fill');
            expect(iconeTipo('email')).toContain('bi-envelope-fill');
            expect(iconeTipo('unknown' as TipoNotificacao)).toContain('bi-bell-fill');
        });
    });
});