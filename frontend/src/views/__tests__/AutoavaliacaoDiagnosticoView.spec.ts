import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import AutoavaliacaoDiagnosticoView from '../AutoavaliacaoDiagnosticoView.vue';

const backMock = vi.fn();
const pushMock = vi.fn();
const concluirAutoavaliacaoMock = vi.fn();
const impossibilitarAvaliacaoMock = vi.fn();
const atualizarNotaMock = vi.fn();
const setPendingMock = vi.fn();

const podeCriarConsenso = ref(false);
const situacaoServidor = ref<'AUTOAVALIACAO_NAO_INICIADA' | 'AUTOAVALIACAO_CONCLUIDA' | 'CONSENSO_APROVADO'>('AUTOAVALIACAO_NAO_INICIADA');
const salvandoAutomaticamente = ref(false);
const carregando = ref(false);
const concluindo = ref(false);
const podeEditar = ref(true);
const podeConcluirAutoavaliacao = ref(true);
const habilitarConcluirAutoavaliacao = ref(true);
const itensEquipe = ref([
    {
        servidorTitulo: '242426',
        servidorNome: 'Duff McKagan',
        situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
        podeManterConsenso: true,
        podeImpossibilitar: true,
        podePermitirAvaliacao: false,
    },
    {
        servidorTitulo: '242427',
        servidorNome: 'Slash',
        situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA',
        podeManterConsenso: true,
        podeImpossibilitar: false,
        podePermitirAvaliacao: true,
    },
]);
const competenciasLocaisVal = ref<any[]>([
    {competenciaCodigo: 10, competenciaDescricao: 'Competência A', importancia: 2, dominio: 1},
]);

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: pushMock,
        back: backMock,
    }),
}));

const contextData = ref<any>({
    processoCodigo: 400,
    unidadeSigla: 'ASSESSORIA_12',
    unidadeNome: 'Assessoria 12',
    competencias: [
        {competenciaCodigo: 10, descricao: 'Competência A'},
    ],
});

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: contextData,
    }),
}));

const queryContextoEdicaoDataVal = ref<any>({
    mapa: {
        competencias: [
            {
                codigo: 10,
                atividades: [
                    {
                        codigo: 100,
                        descricao: 'Atividade A',
                        conhecimentos: [
                            {codigo: 1, descricao: 'Conhecimento A'},
                        ],
                    },
                ],
            },
        ],
    },
});

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        queryContextoEdicao: {
            data: queryContextoEdicaoDataVal,
        },
        podeCriarConsenso: computed(() => podeCriarConsenso.value),
    }),
}));

vi.mock('@/composables/useAutoavaliacaoDiagnostico', () => ({
    useAutoavaliacaoDiagnostico: () => ({
        competenciasLocais: competenciasLocaisVal,
        situacaoServidor,
        carregando,
        salvandoAutomaticamente,
        concluindo,
        podeEditar,
        podeConcluirAutoavaliacao,
        habilitarConcluirAutoavaliacao,
        atualizarNota: atualizarNotaMock,
        concluirAutoavaliacao: concluirAutoavaliacaoMock,
    }),
}));

vi.mock('@/composables/useEquipeDiagnostico', () => ({
    useEquipeDiagnostico: () => ({
        itens: itensEquipe,
        pendentes: computed(() => itensEquipe.value.length),
    }),
}));

vi.mock('@/composables/useFluxoDiagnostico', () => ({
    useFluxoDiagnostico: () => ({
        impossibilitando: ref(false),
        impossibilitarAvaliacao: impossibilitarAvaliacaoMock,
    }),
}));

vi.mock('@/stores/toast', () => ({
    useToastStore: () => ({
        setPending: setPendingMock,
    }),
}));

function montar() {
    return mount(AutoavaliacaoDiagnosticoView, {
        props: {
            codSubprocesso: 400,
            siglaUnidade: 'ASSESSORIA_12',
        },
        global: {
            stubs: {
                LayoutPadrao: {template: '<div><slot /></div>'},
                PageHeader: {
                    props: ['title', 'subtitle'],
                    template: '<div><h1>{{ title }}</h1><p v-if="subtitle">{{ subtitle }}</p><slot name="alerta" /><slot /><slot name="actions" /></div>',
                },
                CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                Alerta: {
                    props: ['mensagem'],
                    emits: ['dismissed'],
                    template: '<div class="app-alert" v-bind="$attrs">{{ mensagem }}<button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>',
                },
                DiagnosticoFluxoModais: {
                    props: ['modalConcluirAberto', 'modalImpossibilitarAberto', 'testIdConfirmarConcluir', 'testIdConfirmarImpossibilitar', 'justificativaImpossibilidade', 'feedbackJustificativaImpossibilidade', 'tituloConcluir', 'mensagemConcluir', 'botaoConcluir'],
                    emits: ['confirmarConcluir', 'confirmarImpossibilitar', 'update:justificativaImpossibilidade', 'update:modalConcluirAberto', 'update:modalImpossibilitarAberto'],
                    template: `
                      <div>
                        <button
                          v-if="modalConcluirAberto"
                          :data-testid="testIdConfirmarConcluir"
                          @click="$emit('confirmarConcluir')"
                        >
                          Confirmar
                        </button>
                        <div v-if="modalImpossibilitarAberto">
                          <textarea
                            :value="justificativaImpossibilidade"
                            @input="$emit('update:justificativaImpossibilidade', $event.target.value)"
                          />
                          <div v-if="feedbackJustificativaImpossibilidade">{{ feedbackJustificativaImpossibilidade }}</div>
                          <button
                            :data-testid="testIdConfirmarImpossibilitar"
                            @click="$emit('confirmarImpossibilitar')"
                          >
                            Confirmar
                          </button>
                        </div>
                      </div>
                    `,
                },
                BAlert: {template: '<div><slot /></div>'},
                BBadge: {template: '<span><slot /></span>'},
                BButton: {
                    emits: ['click'],
                    template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="$attrs.disabled" @click="$emit(\'click\')"><slot /></button>',
                },
                BCard: {template: '<section><slot /></section>'},
                BCardHeader: {template: '<header><slot /></header>'},
                BSpinner: {template: '<span class="spinner" />'},
                BFormText: {template: '<small><slot /></small>'},
                BFormTextarea: {
                    props: ['modelValue'],
                    emits: ['update:modelValue'],
                    template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                },
                BFormSelect: {
                    props: ['modelValue'],
                    emits: ['update:modelValue'],
                    template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option value="">-</option><option value="2">2</option><option value="5">5</option></select>',
                },
                BListGroup: {template: '<div><slot /></div>'},
                BListGroupItem: {template: '<div><slot /></div>'},
                BCollapse: {template: '<div><slot /></div>'},
                BTable: {
                    props: ['items'],
                    template: `
                      <div>
                        <div v-for="item in items" :key="item.competenciaCodigo">
                          <slot name="cell(descricao)" :item="item" />
                          <slot name="cell(importancia)" :item="item" />
                          <slot name="cell(dominio)" :item="item" />
                        </div>
                      </div>
                    `,
                },
            },
        },
    });
}

describe('AutoavaliacaoDiagnosticoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        podeCriarConsenso.value = false;
        situacaoServidor.value = 'AUTOAVALIACAO_NAO_INICIADA';
        salvandoAutomaticamente.value = false;
        carregando.value = false;
        concluindo.value = false;
        podeEditar.value = true;
        podeConcluirAutoavaliacao.value = true;
        habilitarConcluirAutoavaliacao.value = true;
        contextData.value = {
            processoCodigo: 400,
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
            competencias: [{competenciaCodigo: 10, descricao: 'Competência A'}],
        };
        competenciasLocaisVal.value = [
            {competenciaCodigo: 10, competenciaDescricao: 'Competência A', importancia: 2, dominio: 1},
        ];
        itensEquipe.value = [
            {
                servidorTitulo: '242426',
                servidorNome: 'Duff McKagan',
                situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
                podeManterConsenso: true,
                podeImpossibilitar: true,
                podePermitirAvaliacao: false,
            },
            {
                servidorTitulo: '242427',
                servidorNome: 'Slash',
                situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA',
                podeManterConsenso: true,
                podeImpossibilitar: false,
                podePermitirAvaliacao: true,
            },
        ];
    });

    it('conclui a autoavaliação pelo botão no cabeçalho', async () => {
        concluirAutoavaliacaoMock.mockResolvedValue(undefined);
        salvandoAutomaticamente.value = true;
        const wrapper = montar();

        expect(wrapper.text()).toContain('ASSESSORIA_12 - Assessoria 12');
        expect(wrapper.find('.cursor-salvando').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-concluir-autoavaliacao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="toggle-atividades-10"]').exists()).toBe(true);

        await wrapper.get('[data-testid="btn-concluir-autoavaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir"]').trigger('click');

        expect(concluirAutoavaliacaoMock).toHaveBeenCalledTimes(1);
        expect(setPendingMock).toHaveBeenCalledWith('Autoavaliação concluída', 'success');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {
                codProcesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            query: {
                codSubprocesso: '400',
            },
        });
    });

    it('oculta a conclusão para chefia e exibe ações da equipe', async () => {
        podeCriarConsenso.value = true;
        const wrapper = montar();

        expect(wrapper.find('[data-testid="btn-concluir-autoavaliacao"]').exists()).toBe(false);
        expect(wrapper.text()).toContain('Equipe');

        await wrapper.get('[data-testid="btn-consenso-242426"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'ConsensoDiagnostico',
            params: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
        });

        expect(wrapper.find('[data-testid="btn-impossibilitar-242426"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-impossibilitar-242427"]').exists()).toBe(false);
    });

    it('valida justificativa obrigatória e registra impossibilidade', async () => {
        podeCriarConsenso.value = true;
        impossibilitarAvaliacaoMock.mockResolvedValue(undefined);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-impossibilitar-242426"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');
        expect(wrapper.text()).toContain('A justificativa é obrigatória.');

        await wrapper.get('textarea').setValue('Servidor afastado.');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');

        expect(impossibilitarAvaliacaoMock).toHaveBeenCalledWith('242426', 'Servidor afastado.');
    });

    it('mantém leitura quando o backend não habilita edição e mostra alerta final', async () => {
        podeEditar.value = false;
        podeConcluirAutoavaliacao.value = true;
        habilitarConcluirAutoavaliacao.value = false;
        situacaoServidor.value = 'CONSENSO_APROVADO';
        competenciasLocaisVal.value = [
            {competenciaCodigo: 10, competenciaDescricao: 'Competência A', importancia: 0, dominio: null},
        ];
        const wrapper = montar();

        expect(wrapper.find('select').exists()).toBe(false);
        expect(wrapper.text()).toContain('NA');
        expect(wrapper.text()).toContain('-');
        expect(wrapper.text()).toContain('A avaliação de consenso já foi aprovada.');
    });

    it('normaliza valores das notas antes de atualizar', async () => {
        const wrapper = montar();
        const select = wrapper.findComponent('[data-testid="autoavaliacao-importancia-10"]') as any;

        await select.vm.$emit('update:modelValue', null);
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);

        await select.vm.$emit('update:modelValue', '5');
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', 5);

        await select.vm.$emit('update:modelValue', Number.NaN);
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);
    });

    it('usa o botão voltar do cabeçalho', async () => {
        const wrapper = montar();
        const botaoVoltar = wrapper.findAll('button').find((botao) => botao.text().includes('Voltar'));

        await botaoVoltar?.trigger('click');
        expect(backMock).toHaveBeenCalled();
    });
});
