import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, nextTick, ref} from 'vue';
import {mount} from '@vue/test-utils';
import AutoavaliacaoDiagnosticoView from '../AutoavaliacaoDiagnosticoView.vue';

const backMock = vi.fn();
const pushMock = vi.fn();
const concluirAutoavaliacaoMock = vi.fn();
const aprovarConsensoMock = vi.fn();
const impossibilitarAvaliacaoMock = vi.fn();
const atualizarNotaMock = vi.fn();
const setPendingMock = vi.fn();

const podeCriarConsenso = ref(false);
const situacaoServidor = ref<'AUTOAVALIACAO_NAO_INICIADA' | 'AUTOAVALIACAO_CONCLUIDA' | 'CONSENSO_CRIADO' | 'CONSENSO_APROVADO'>('AUTOAVALIACAO_NAO_INICIADA');
const salvandoAutomaticamente = ref(false);
const erroConcluir = ref<Error | null>(null);
const erroAprovar = ref<Error | null>(null);
const itensEquipe = ref([
    {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'},
    {servidorTitulo: '242427', servidorNome: 'Slash', situacaoServidor: 'CONSENSO_CRIADO'},
]);
const competenciasLocaisVal = ref<any>([
    {competenciaCodigo: 10, importancia: 2, dominio: 1},
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
    situacaoDiagnostico: 'EM_ANDAMENTO',
    situacaoSubprocesso: 'DIAGNOSTICO_EM_ANDAMENTO',
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
        carregando: ref(false),
        salvandoAutomaticamente,
        concluindo: ref(false),
        erroConcluir,
        atualizarNota: atualizarNotaMock,
        concluirAutoavaliacao: concluirAutoavaliacaoMock,
    }),
}));

vi.mock('@/composables/useConsensoDiagnostico', () => ({
    useConsensoDiagnostico: () => ({
        aprovando: ref(false),
        erroAprovar,
        aprovarConsenso: aprovarConsensoMock,
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

describe('AutoavaliacaoDiagnosticoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        podeCriarConsenso.value = false;
        situacaoServidor.value = 'AUTOAVALIACAO_NAO_INICIADA';
        salvandoAutomaticamente.value = false;
        erroConcluir.value = null;
        erroAprovar.value = null;
        contextData.value = {
            processoCodigo: 400,
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
            situacaoDiagnostico: 'EM_ANDAMENTO',
            situacaoSubprocesso: 'DIAGNOSTICO_EM_ANDAMENTO',
            competencias: [
                {competenciaCodigo: 10, descricao: 'Competência A'},
            ],
        };
        queryContextoEdicaoDataVal.value = {
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
        };
        competenciasLocaisVal.value = [
            {competenciaCodigo: 10, importancia: 2, dominio: 1},
        ];
    });

    function montar() {
        return mount(AutoavaliacaoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    ModalConfirmacao: {
                        props: ['modelValue', 'testIdConfirmar'],
                        emits: ['update:modelValue', 'confirmar'],
                        template: '<div v-if="modelValue"><button :data-testid="testIdConfirmar" @click="$emit(\'confirmar\')">Confirmar</button></div>',
                    },
                    BAlert: {template: '<div><slot /></div>'},
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BCardHeader: {template: '<header><slot /></header>'},
                    BSpinner: {template: '<span />'},
                    BFormText: {template: '<small><slot /></small>'},
                    BFormTextarea: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    },
                    BFormSelect: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option value="0">NA</option><option value="1">1</option><option value="2">2</option></select>',
                    },
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<div><slot /></div>'},
                    BModal: {
                        props: ['modelValue'],
                        emits: ['update:modelValue', 'hide'],
                        template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
                    },
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

    it('renderiza autoavaliação do servidor, detalhes da atividade e conclusão', async () => {
        concluirAutoavaliacaoMock.mockResolvedValue(undefined);
        salvandoAutomaticamente.value = true;
        const wrapper = montar();

        expect(wrapper.text()).toContain('ASSESSORIA_12 - Assessoria 12');
        expect(wrapper.text()).toContain('Salvando');
        expect(wrapper.find('[data-testid="toggle-atividades-10"]').exists()).toBe(true);

        await wrapper.get('[data-testid="btn-concluir-autoavaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir"]').trigger('click');
        expect(concluirAutoavaliacaoMock).toHaveBeenCalledTimes(1);
        expect(setPendingMock).toHaveBeenCalledWith('Autoavaliação concluída');
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

    it('renderiza aprovação de consenso para servidor e mostra erro ao falhar', async () => {
        situacaoServidor.value = 'CONSENSO_CRIADO';
        erroAprovar.value = new Error('Falha ao aprovar');
        aprovarConsensoMock.mockRejectedValue(erroAprovar.value);
        const wrapper = montar();

        expect(wrapper.text()).toContain('O responsavel pela unidade registrou a avaliação de consenso.');
        await wrapper.get('[data-testid="btn-aprovar-consenso"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-aprovar"]').trigger('click');
        expect(wrapper.text()).toContain('Falha ao aprovar');
    });

    it('exibe alertas por situação do servidor, atualiza nota e volta pela navegação', async () => {
        situacaoServidor.value = 'AUTOAVALIACAO_NAO_INICIADA';
        const wrapper = montar();

        // 1. Em AUTOAVALIACAO_NAO_INICIADA, pode editar notas
        await wrapper.get('select').setValue('2');
        expect(atualizarNotaMock).toHaveBeenCalled();

        // Testar botão voltar
        const botaoVoltar = wrapper.findAll('button').find((botao) => botao.text().includes('Voltar'));
        await botaoVoltar!.trigger('click');
        expect(backMock).toHaveBeenCalled();

        // 2. Em AUTOAVALIACAO_CONCLUIDA, exibe o botão concluir-autoavaliacao (desabilitado)
        situacaoServidor.value = 'AUTOAVALIACAO_CONCLUIDA';
        await nextTick();
        expect(wrapper.find('[data-testid="btn-concluir-autoavaliacao"]').exists()).toBe(true);

        // 3. Em CONSENSO_APROVADO, exibe a mensagem de fluxo finalizado
        situacaoServidor.value = 'CONSENSO_APROVADO';
        await nextTick();
        expect(wrapper.text()).toContain('Avaliação de consenso aprovada. Fluxo finalizado.');
    });

    it('mostra erro ao falhar conclusão da autoavaliação', async () => {
        erroConcluir.value = new Error('Falha ao concluir');
        concluirAutoavaliacaoMock.mockRejectedValue(erroConcluir.value);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-concluir-autoavaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao concluir');
    });

    it('renderiza equipe da chefia, navega para consenso e valida impossibilidade', async () => {
        podeCriarConsenso.value = true;
        situacaoServidor.value = 'CONSENSO_APROVADO';
        impossibilitarAvaliacaoMock.mockResolvedValue(undefined);
        const wrapper = montar();

        expect(wrapper.text()).toContain('Equipe');
        expect(wrapper.find('[data-testid="btn-concluir-autoavaliacao"]').exists()).toBe(false);

        await wrapper.get('[data-testid="btn-consenso-242426"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'ConsensoDiagnostico',
            params: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
        });

        await wrapper.get('[data-testid="btn-impossibilitar-242427"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');
        expect(wrapper.text()).toContain('A justificativa é obrigatória.');

        await wrapper.get('textarea').setValue('Servidor afastado.');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');
        expect(impossibilitarAvaliacaoMock).toHaveBeenCalledWith('242427', 'Servidor afastado.');
        expect(wrapper.text()).toContain('Impossibilidade registrada');
    });

    it('exibe erro caso a impossibilidade falhe', async () => {
        podeCriarConsenso.value = true;
        situacaoServidor.value = 'CONSENSO_APROVADO';
        impossibilitarAvaliacaoMock.mockRejectedValue(new Error('Erro ao salvar no BD'));
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-impossibilitar-242427"]').trigger('click');
        await wrapper.get('textarea').setValue('Servidor afastado.');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');
        expect(wrapper.text()).toContain('Erro ao salvar no BD');
    });

    it('exercita ramos padrao de variantes e formatacao de conhecimentos vazios', async () => {
        contextData.value.situacaoSubprocesso = 'DIAGNOSTICO_FECHADO';
        itensEquipe.value = [
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'OUTRO_STATUS' as any},
        ];
        queryContextoEdicaoDataVal.value.mapa.competencias[0].atividades[0].conhecimentos = [];
        podeCriarConsenso.value = true;
        const wrapper = montar();

        await wrapper.get('[data-testid="toggle-atividades-10"]').trigger('click');
        expect(wrapper.text()).toContain('-');
    });

    it('exercita normalizarValorNota com ramificacoes em AutoavaliacaoDiagnosticoView', async () => {
        const wrapper = montar();
        const select = wrapper.findComponent('[data-testid="autoavaliacao-importancia-10"]') as any;
        
        // Emite null
        await select.vm.$emit('update:modelValue', null);
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);

        // Emite undefined
        await select.vm.$emit('update:modelValue', undefined);
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);

        // Emite string vazia
        await select.vm.$emit('update:modelValue', '');
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);

        // Emite numero valido
        await select.vm.$emit('update:modelValue', 4);
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', 4);

        // Emite NaN literal e via string
        await select.vm.$emit('update:modelValue', Number.NaN);
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);
        
        // Emite string de numero valido
        await select.vm.$emit('update:modelValue', '5');
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', 5);

        // Emite string invalida
        await select.vm.$emit('update:modelValue', 'abc');
        expect(atualizarNotaMock).toHaveBeenLastCalledWith(10, 'importancia', null);
    });

    it('exercita formatacao de notas quando readonly', async () => {
        situacaoServidor.value = 'CONSENSO_APROVADO'; // Nao pode editar
        competenciasLocaisVal.value = [
            {competenciaCodigo: 10, importancia: 0, dominio: 5},
            {competenciaCodigo: 11, importancia: null, dominio: null},
        ];
        contextData.value.competencias = [
            {competenciaCodigo: 10, descricao: 'Comp 10'},
            {competenciaCodigo: 11, descricao: 'Comp 11'},
        ];

        const wrapper = montar();
        expect(wrapper.text()).toContain('NA');
        expect(wrapper.text()).toContain('-');
        expect(wrapper.text()).toContain('5');
    });

    it('exercita tratamento de erros sem mensagem explícita e outros branches de normalizarNota', async () => {
        erroConcluir.value = {} as any;
        concluirAutoavaliacaoMock.mockRejectedValue(erroConcluir.value);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-concluir-autoavaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir"]').trigger('click');
        expect(wrapper.text()).toContain('Erro desconhecido ou não mapeado pela aplicação.');
    });

    it('exercita erro de consenso sem mensagem explicita', async () => {
        situacaoServidor.value = 'CONSENSO_CRIADO';
        erroAprovar.value = {} as any;
        aprovarConsensoMock.mockRejectedValue(erroAprovar.value);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-aprovar-consenso"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-aprovar"]').trigger('click');
        expect(wrapper.text()).toContain('Erro desconhecido ou não mapeado pela aplicação.');
    });

    it('exercita fallbacks de normalizacao e formatacao de notas e situacao de servidor', () => {
        podeCriarConsenso.value = true;
        itensEquipe.value = [
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA'},
            {servidorTitulo: '242427', servidorNome: 'Slash', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA'},
        ];
        
        const wrapper = montar();
        expect(wrapper.text()).toContain('Autoavaliação não iniciada');
        expect(wrapper.text()).toContain('Avaliação impossibilitada');
    });

    it('exercita situacoes de diagnostico concluido, consenso aprovado e conhecimentos preenchidos', async () => {
        contextData.value.situacaoSubprocesso = 'DIAGNOSTICO_CONCLUIDO';
        itensEquipe.value = [
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'CONSENSO_APROVADO'},
        ];
        queryContextoEdicaoDataVal.value.mapa.competencias[0].atividades[0].conhecimentos = [
            {codigo: 10, descricao: 'React'},
            {codigo: 11, descricao: 'Vue'},
        ];
        podeCriarConsenso.value = true;
        
        const wrapper = montar();
        
        // Simular que o toggle de detalhes da competência está aberto para renderizar a lista
        const vm = wrapper.vm as any;
        vm.detalhesCompetenciaAbertos[10] = true;
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('React, Vue');
    });
});
