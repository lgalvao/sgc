import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import {run} from 'axe-core';
import AppAlert from './AppAlert.vue';

const global = {
    stubs: {
        BAlert: {
            template: '<div data-testid="app-alert-stub"><slot /></div>',
        },
        BButton: {
            template: '<button type="button"><slot /></button>',
        },
    },
};

function montarAlerta(props: InstanceType<typeof AppAlert>['$props']) {
    const alvo = document.createElement('div');
    document.body.appendChild(alvo);

    const wrapper = mount(AppAlert, {
        props,
        global,
        attachTo: alvo,
    });

    return {wrapper, alvo};
}

describe('AppAlert A11y', () => {
    it('has no accessibility violations in default mode', async () => {
        const {wrapper, alvo} = montarAlerta({mensagem: 'Hello world'});
        const results = await run(alvo);
        expect(results).toHaveNoViolations();
        wrapper.unmount();
        alvo.remove();
    });

    it('has no accessibility violations in detailed mode', async () => {
        const {wrapper, alvo} = montarAlerta({
            notificacao: {
                resumo: 'Error',
                detalhes: ['Detail 1', 'Detail 2']
            },
        });
        const results = await run(alvo);
        expect(results).toHaveNoViolations();
        wrapper.unmount();
        alvo.remove();
    });
});
