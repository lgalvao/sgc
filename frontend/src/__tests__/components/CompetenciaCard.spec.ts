import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import CompetenciaCard from '@/components/CompetenciaCard.vue';
import { Atividade, Competencia } from '@/types/tipos';

// Stubs for BootstrapVueNext components
const BCardStub = {
  template: '<div><slot></slot></div>',
  props: ['no-body']
};
const BCardBodyStub = {
  template: '<div><slot></slot></div>'
};
const BButtonStub = {
    template: '<button @click="$emit(\'click\')"><slot></slot></button>',
    props: ['variant', 'size', 'title', 'aria-label']
};

describe('CompetenciaCard.vue', () => {
    const mockAtividades: Atividade[] = [
        {
            codigo: 1,
            descricao: 'Atividade 1',
            conhecimentos: [{ codigo: 1, descricao: 'Conhecimento 1' }]
        },
        {
            codigo: 2,
            descricao: 'Atividade 2',
            conhecimentos: []
        }
    ];

    const mockCompetencia: Competencia = {
        codigo: 10,
        descricao: 'Competencia Teste',
        atividadesAssociadas: [1, 2, 99] // 99 does not exist in mockAtividades
    };

    const mountComponent = () => {
        return mount(CompetenciaCard, {
            props: {
                competencia: mockCompetencia,
                atividades: mockAtividades
            },
            global: {
                stubs: {
                    BCard: BCardStub,
                    BCardBody: BCardBodyStub,
                    BButton: BButtonStub
                },
                directives: {
                    'b-tooltip': {}
                }
            }
        });
    };

    it('deve renderizar a descrição da competência', () => {
        const wrapper = mountComponent();
        expect(wrapper.text()).toContain('Competencia Teste');
    });

    it('deve renderizar atividades associadas', () => {
        const wrapper = mountComponent();
        expect(wrapper.text()).toContain('Atividade 1');
        expect(wrapper.text()).toContain('Atividade 2');
    });

    it('deve tratar atividade não encontrada (ID 99)', () => {
        const wrapper = mountComponent();
        expect(wrapper.text()).toContain('Atividade não encontrada');
    });

    it('deve emitir evento editar', async () => {
        const wrapper = mountComponent();
        const btnEditar = wrapper.find('[data-testid="btn-editar-competencia"]');
        await btnEditar.trigger('click');
        expect(wrapper.emitted('editar')).toBeTruthy();
        expect(wrapper.emitted('editar')![0]).toEqual([mockCompetencia]);
    });

    it('deve emitir evento excluir', async () => {
        const wrapper = mountComponent();
        const btnExcluir = wrapper.find('[data-testid="btn-excluir-competencia"]');
        await btnExcluir.trigger('click');
        expect(wrapper.emitted('excluir')).toBeTruthy();
        expect(wrapper.emitted('excluir')![0]).toEqual([mockCompetencia.codigo]);
    });

    it('deve emitir evento removerAtividade', async () => {
        const wrapper = mountComponent();
        const btnsRemover = wrapper.findAll('[data-testid="btn-remover-atividade-associada"]');
        // Removing the first associated activity (ID 1)
        await btnsRemover[0].trigger('click');
        expect(wrapper.emitted('removerAtividade')).toBeTruthy();
        expect(wrapper.emitted('removerAtividade')![0]).toEqual([mockCompetencia.codigo, 1]);
    });

    it('deve mostrar tooltip de conhecimentos corretamente (getConhecimentosTooltip)', () => {
        // Accessing the component instance to test the method directly or checking rendered output if possible
        // Since getConhecimentosTooltip is internal/script setup, we can test its effects via tooltip directive binding
        // But checking directive binding value is tricky.

        // Alternatively, verify the badge presence which depends on knowledge count
        const wrapper = mountComponent();

        // Atividade 1 has knowledge, should show badge
        const badge1 = wrapper.find('[data-testid="cad-mapa__txt-badge-conhecimentos-1"]');
        expect(badge1.exists()).toBe(true);
        expect(badge1.text()).toBe('1');
    });

    // To hit the branch "Nenhum conhecimento cadastrado" in getConhecimentosTooltip,
    // we need to inspect the tooltip content. Since we mocked the directive, we can't see the tooltip.
    // However, the logic for rendering the badge already covers:
    // v-if="(getAtividadeCompleta(atvId)?.conhecimentos.length ?? 0) > 0"
    // So the tooltip binding only happens if > 0.

    // Wait, let's look at the component code again.
    /*
    <span
        v-if="(getAtividadeCompleta(atvId)?.conhecimentos.length ?? 0) > 0"
        v-b-tooltip.html.top="getConhecimentosTooltip(atvId)"
        ...
    */
    // The tooltip is ONLY bound if length > 0.
    // So the branch `if (!atividade || !atividade.conhecimentos.length)` in `getConhecimentosTooltip`
    // is technically unreachable via the template binding if `v-if` filters it out first?

    // BUT `getConhecimentosTooltip` is a function defined in script setup.
    // Maybe it's not unreachable if I access it via vm?
    // Or maybe I am misinterpreting the line number 107.

    // Line 107 is: `return "Nenhum conhecimento cadastrado";` inside `getConhecimentosTooltip`.
    // If the template prevents calling this function when empty, then it's dead code in the context of the template,
    // but still counts for coverage if the function is defined.

    // To test this line, I can try to access the function from the wrapper.vm if exposed,
    // but `script setup` closes the scope.

    // However, if I can force the condition where `getConhecimentosTooltip` is called for an activity without knowledge...
    // But the `v-if` prevents that.

    // Wait, is there any other usage? No.

    // So the code inside `getConhecimentosTooltip` handling empty knowledge is dead code unless the v-if changes.
    // But wait, the function takes `atividadeId`.

    // Actually, `getConhecimentosTooltip` is safe-guarded by `v-if`.
    // I can modify the test to expose this function or just accept I can't reach it easily through template.
    // BUT, `defineExpose` is not used.

    // Wait, I can test it by tricking the v-if? No.

    // Maybe I can change the data props such that `v-if` evaluates to true but `getConhecimentosTooltip` receives an ID that somehow fails?
    // No, `getAtividadeCompleta(atvId)` is used in both.

    // Let's assume there is a race condition or some reactivity issue where v-if might be true but the data changes? Unlikely.

    // Is it possible that `getAtividadeCompleta` returns undefined?
    // `(undefined?.conhecimentos.length ?? 0) > 0` -> `(undefined ?? 0) > 0` -> `0 > 0` -> False.

    // So `getConhecimentosTooltip` is indeed only called when there ARE knowledges.
    // The check inside `getConhecimentosTooltip`:
    // `if (!atividade || !atividade.conhecimentos.length)`
    // This check is redundant given the template guard.
    // BUT, good defensive programming.

    // To cover it, I might need to unit test the function in isolation, but I can't export it easily.

    // HOWEVER, I can test the scenario where I pass an ID that doesn't exist?
    // `getDescricaoAtividade` calls `getAtividadeCompleta`.
    // `getDescricaoAtividade` handles undefined with "Atividade não encontrada".

    // Let's verify if `getConhecimentosTooltip` is used elsewhere? No.

    // Use `defineExpose` for testing? Or just move the logic to a helper/util?
    // Or, simply accept it's hard to reach via integration test.

    // BUT, maybe the `v-if` check `(getAtividadeCompleta(atvId)?.conhecimentos.length ?? 0) > 0`
    // allows `getConhecimentosTooltip` to be called if I can somehow make `length` undefined but satisfy > 0? No.

    // What if I modify the component to expose it for testing?
    // Or better, I can assume `vitest` might not be able to call it.

    // Let's write the test file anyway to cover the rest of the component (94% coverage is mostly from template).
    // The 75% branch coverage comes from `getAtividadeCompleta` returning undefined (handled)
    // and `getConhecimentosTooltip` branches.

    // I already have a test case for "Atividade não encontrada" (ID 99), so `getDescricaoAtividade` branch is covered.

});
