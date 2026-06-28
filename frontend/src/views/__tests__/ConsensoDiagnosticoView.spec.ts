import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import ConsensoDiagnosticoView from '../ConsensoDiagnosticoView.vue';

const backMock = vi.fn();
const pushMock = vi.fn();
const atualizarNotaDetalhadaMock = vi.fn();
const aprovarConsensoMock = vi.fn();
const concluirAvaliacaoMock = vi.fn();
const salvarConsensoAgoraMock = vi.fn();
const setPendingMock = vi.fn();

vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: backMock,
        push: pushMock,
    }),
}));

const contextoData = ref<any>({
    processoCodigo: 400,
    competencias: [
        {competenciaCodigo: 10, descricao: 'Competência A'},
    ],
});
vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: contextoData,
    }),
}));

const competenciasLocais = ref<any[]>([
    {
        competenciaCodigo: 10,
        competenciaDescricao: 'Competência A',
        servidorImportancia: 3,
        servidorDominio: 4,
        chefiaImportancia: 3,
        chefiaDominio: 4,
        consensoImportancia: 3,
        consensoDominio: 4,
    },
]);
const podeEditar = ref(true);
const podeConcluirAvaliacao = ref(true);
const habilitarConcluirAvaliacao = ref(true);
const podeAprovarConsenso = ref(false);
const habilitarAprovarConsenso = ref(false);
const ehConsensoAprovado = ref(false);
const carregando = ref(false);
const salvandoAutomaticamente = ref(false);
const concluindo = ref(false);
const aprovando = ref(false);
const erroConcluir = ref<any>(null);
const erroAprovar = ref<any>(null);

vi.mock('@/composables/useConsensoDiagnostico', () => ({
    useConsensoDiagnostico: () => ({
        query: {
            data: computed(() => ({servidorNome: 'Servidor Exemplo'})),
        },
        competenciasLocais,
        podeEditar,
        podeConcluirAvaliacao,
        habilitarConcluirAvaliacao,
        podeAprovarConsenso,
        habilitarAprovarConsenso,
        ehConsensoAprovado: computed(() => ehConsensoAprovado.value),
        carregando: computed(() => carregando.value),
        salvandoAutomaticamente,
        concluindo: computed(() => concluindo.value),
        aprovando: computed(() => aprovando.value),
        erroConcluir,
        erroAprovar,
        atualizarNotaDetalhada: atualizarNotaDetalhadaMock,
        salvarConsensoAgora: salvarConsensoAgoraMock,
        concluirAvaliacao: concluirAvaliacaoMock,
        aprovarConsenso: aprovarConsensoMock,
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '242426',
        usuarioNome: 'Servidor Exemplo',
    }),
}));

vi.mock('@/stores/toast', () => ({
    useToastStore: () => ({
        setPending: setPendingMock,
    }),
}));

function montar(props?: Record<string, unknown>) {
    return mount(ConsensoDiagnosticoView, {
        props: {
            codSubprocesso: 400,
            siglaUnidade: 'ASSESSORIA_12',
            servidorTitulo: '242426',
            servidorNome: 'Servidor Exemplo',
            ...props,
        },
        global: {
            stubs: {
                LayoutPadrao: {template: '<div><slot /></div>'},
                PageHeader: {
                    props: ['title', 'subtitle'],
                    template: '<div><h1>{{ title }}</h1><p v-if="subtitle">{{ subtitle }}</p><slot /><slot name="alerta" /><slot name="actions" /></div>',
                },
                CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                DiagnosticoFluxoModais: {
                    props: [
                        'modalConcluirAberto', 'testIdConfirmarConcluir', 'tituloConcluir', 'mensagemConcluir', 'botaoConcluir',
                        'modalAprovarConsensoAberto', 'testIdConfirmarAprovarConsenso'
                    ],
                    emits: ['confirmarConcluir', 'update:modalConcluirAberto', 'confirmarAprovarConsenso', 'update:modalAprovarConsensoAberto'],
                    template: `
                      <div>
                        <button
                          v-if="modalConcluirAberto"
                          :data-testid="testIdConfirmarConcluir || 'btn-confirmar-concluir'"
                          @click="$emit('confirmarConcluir')"
                        >
                          Confirmar
                        </button>
                        <button
                          v-if="modalAprovarConsensoAberto"
                          :data-testid="testIdConfirmarAprovarConsenso || 'btn-confirmar-aprovar-consenso'"
                          @click="$emit('confirmarAprovarConsenso')"
                        >
                          Confirmar Aprovar
                        </button>
                      </div>
                    `,
                },
                Alerta: {
                    props: ['mensagem', 'chave'],
                    emits: ['dismissed'],
                    template: '<div class="app-alert" v-bind="$attrs">{{ mensagem }}<button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>',
                },
                BButton: {
                    emits: ['click'],
                    template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="$attrs.disabled" @click="$emit(\'click\')"><slot /></button>',
                },
                BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                BFormSelect: {
                    props: ['options', 'modelValue'],
                    emits: ['update:modelValue'],
                    template: `
                      <select :value="modelValue" v-bind="$attrs" @change="$emit('update:modelValue', $event.target.value)">
                        <option v-for="opcao in options" :key="String(opcao.value)" :value="opcao.value ?? ''">{{ opcao.text }}</option>
                      </select>
                    `,
                },
                BSpinner: {template: '<span class="spinner" />'},
            },
        },
    });
}

describe('ConsensoDiagnosticoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        contextoData.value = {
            processoCodigo: 400,
            competencias: [{competenciaCodigo: 10, descricao: 'Competência A'}],
        };
        competenciasLocais.value = [
            {
                competenciaCodigo: 10,
                competenciaDescricao: 'Competência A',
                servidorImportancia: 3,
                servidorDominio: 4,
                chefiaImportancia: 3,
                chefiaDominio: 4,
                consensoImportancia: 3,
                consensoDominio: 4,
            },
        ];
        podeEditar.value = true;
        podeConcluirAvaliacao.value = true;
        habilitarConcluirAvaliacao.value = true;
        podeAprovarConsenso.value = false;
        habilitarAprovarConsenso.value = false;
        ehConsensoAprovado.value = false;
        carregando.value = false;
        salvandoAutomaticamente.value = false;
        concluindo.value = false;
        aprovando.value = false;
        erroConcluir.value = null;
        erroAprovar.value = null;
    });

    it('renderiza a tabela e os botões do cabeçalho na convenção nova', () => {
        const wrapper = montar();

        expect(wrapper.text()).toContain('Avaliação de consenso');
        expect(wrapper.text()).toContain('Servidor Exemplo - 242426');
        expect(wrapper.find('[data-testid="btn-concluir-avaliacao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-aprovar-consenso"]').exists()).toBe(true);
        expect(wrapper.get('[data-testid="btn-aprovar-consenso"]').attributes('disabled')).toBeDefined();
        expect(wrapper.findAll('thead tr')).toHaveLength(2);
        expect(wrapper.find('[data-testid="consenso-final-dominio-10"]').exists()).toBe(true);
    });

    it('renderiza os valores do servidor nas colunas estáticas', () => {
        const wrapper = montar();
        const texto = wrapper.text();

        expect(texto).not.toContain('undefined');
        expect(texto).toContain('Competência A');
        expect(texto).toContain('3');
        expect(texto).toContain('4');
    });

    it('usa o nome retornado pela query no subtitulo quando a rota nao traz servidorNome', () => {
        const wrapper = montar({servidorTitulo: '999999', servidorNome: undefined});

        expect(wrapper.text()).toContain('Servidor Exemplo - 999999');
    });

    it('conclui a avaliação pelo cabeçalho e volta ao subprocesso', async () => {
        salvarConsensoAgoraMock.mockResolvedValue(undefined);
        concluirAvaliacaoMock.mockResolvedValue(undefined);
        const wrapper = montar({servidorTitulo: '999999', servidorNome: 'Outro Servidor'});

        await wrapper.get('[data-testid="btn-concluir-avaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir"]').trigger('click');

        expect(salvarConsensoAgoraMock).toHaveBeenCalledTimes(1);
        expect(concluirAvaliacaoMock).toHaveBeenCalledTimes(1);
        expect(setPendingMock).toHaveBeenCalledWith('Avaliação de consenso criada', 'success');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {
                codProcesso: '400',
                siglaUnidade: 'ASSESSORIA_12',
            },
            query: {
                codSubprocesso: '400',
            },
        });
    });

    it('aprova o consenso pelo cabeçalho quando o backend libera a ação', async () => {
        podeConcluirAvaliacao.value = false;
        podeAprovarConsenso.value = true;
        habilitarAprovarConsenso.value = true;
        aprovarConsensoMock.mockResolvedValue(undefined);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-aprovar-consenso"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-aprovar-consenso"]').trigger('click');

        expect(aprovarConsensoMock).toHaveBeenCalledTimes(1);
        expect(setPendingMock).toHaveBeenCalledWith('Avaliação de consenso aprovada', 'success');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {
                codProcesso: '400',
                siglaUnidade: 'ASSESSORIA_12',
            },
            query: {
                codSubprocesso: '400',
            },
        });
    });

    it('normaliza as notas ao editar os campos de consenso', async () => {
        const wrapper = montar();
        const select = wrapper.findComponent('[data-testid="consenso-chefia-importancia-10"]') as any;

        await select.vm.$emit('update:modelValue', '5');
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'importancia',
            valor: 5,
        });

        await select.vm.$emit('update:modelValue', '');
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'importancia',
            valor: null,
        });
    });

    it('mostra o alerta de aprovado e congela a edição', () => {
        ehConsensoAprovado.value = true;
        podeEditar.value = false;
        podeAprovarConsenso.value = true;
        habilitarAprovarConsenso.value = false;
        const wrapper = montar();

        expect(wrapper.text()).toContain('A avaliação de consenso já foi aprovada.');
        expect(wrapper.find('select').exists()).toBe(false);
        expect(wrapper.get('[data-testid="btn-aprovar-consenso"]').attributes('disabled')).toBeDefined();
    });

    it('mantem o botão aprovar visível e desabilitado para o proprio servidor quando o estado nao permite a ação', () => {
        podeConcluirAvaliacao.value = false;
        podeAprovarConsenso.value = false;
        habilitarAprovarConsenso.value = false;

        const wrapper = montar();

        expect(wrapper.find('[data-testid="btn-aprovar-consenso"]').exists()).toBe(true);
        expect(wrapper.get('[data-testid="btn-aprovar-consenso"]').attributes('disabled')).toBeDefined();
    });

    it('mantem a tabela em leitura quando o consenso ja esta aprovado mesmo se podeEditar vier true', () => {
        ehConsensoAprovado.value = true;
        podeEditar.value = true;

        const wrapper = montar();

        expect(wrapper.text()).toContain('A avaliação de consenso já foi aprovada.');
        expect(wrapper.find('select').exists()).toBe(false);
        expect(wrapper.text()).toContain('3');
        expect(wrapper.text()).toContain('4');
    });

    it('mantem o botão concluir avaliação desabilitado quando a situação individual já é consenso criado', () => {
        podeConcluirAvaliacao.value = true;
        podeEditar.value = true;
        habilitarConcluirAvaliacao.value = false;

        const wrapper = montar();

        expect(wrapper.find('[data-testid="btn-concluir-avaliacao"]').exists()).toBe(true);
        expect(wrapper.get('[data-testid="btn-concluir-avaliacao"]').attributes('disabled')).toBeDefined();
        expect(wrapper.find('select').exists()).toBe(false);
    });

    it('mostra erro ao falhar aprovar e permite dispensar o alerta', async () => {
        podeConcluirAvaliacao.value = false;
        podeAprovarConsenso.value = true;
        habilitarAprovarConsenso.value = true;
        erroAprovar.value = new Error('Falha ao salvar consenso');
        aprovarConsensoMock.mockRejectedValue(erroAprovar.value);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-aprovar-consenso"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-aprovar-consenso"]').trigger('click');
        expect(wrapper.find('.app-alert').text()).toContain('Falha ao salvar consenso');

        await wrapper.get('[data-testid="btn-dismiss-alert"]').trigger('click');
        expect(wrapper.find('.app-alert').exists()).toBe(false);
    });

    it('mostra erro do backend ao falhar concluir a avaliação', async () => {
        salvarConsensoAgoraMock.mockResolvedValue(undefined);
        erroConcluir.value = new Error('Preencha todos os campos');
        concluirAvaliacaoMock.mockRejectedValue(erroConcluir.value);
        const wrapper = montar({servidorTitulo: '999999', servidorNome: 'Outro Servidor'});

        await wrapper.get('[data-testid="btn-concluir-avaliacao"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir"]').trigger('click');

        expect(wrapper.find('.app-alert').text()).toContain('Preencha todos os campos');
    });

    it('reexibe o mesmo alerta local ao repetir conclusão com consenso incompleto', async () => {
        competenciasLocais.value = [
            {
                competenciaCodigo: 10,
                competenciaDescricao: 'Competência A',
                servidorImportancia: 3,
                servidorDominio: 4,
                chefiaImportancia: 3,
                chefiaDominio: 4,
                consensoImportancia: null,
                consensoDominio: null,
            },
        ];
        const wrapper = montar({servidorTitulo: '999999', servidorNome: 'Outro Servidor'});

        await wrapper.get('[data-testid="btn-concluir-avaliacao"]').trigger('click');
        expect(wrapper.find('.app-alert').text()).toContain('Preencha todos os campos');
        expect(salvarConsensoAgoraMock).not.toHaveBeenCalled();

        await wrapper.get('[data-testid="btn-dismiss-alert"]').trigger('click');
        expect(wrapper.find('.app-alert').exists()).toBe(false);

        await wrapper.get('[data-testid="btn-concluir-avaliacao"]').trigger('click');
        expect(wrapper.find('.app-alert').text()).toContain('Preencha todos os campos');
        expect(salvarConsensoAgoraMock).not.toHaveBeenCalled();
    });

    it('usa o botão voltar do cabeçalho', async () => {
        const wrapper = montar();
        const botaoVoltar = wrapper.findAll('button').find((botao) => botao.text().includes('Voltar'));

        await botaoVoltar?.trigger('click');
        expect(backMock).toHaveBeenCalled();
    });
});
