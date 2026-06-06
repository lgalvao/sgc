import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import DiagnosticoUnidadeView from '../DiagnosticoUnidadeView.vue';
import {Perfil} from '@/types/comum';

const backMock = vi.fn();
const validarDiagnosticoMock = vi.fn();
const devolverDiagnosticoMock = vi.fn();
const homologarDiagnosticoMock = vi.fn();

const perfilSelecionado = ref<Perfil>(Perfil.GESTOR);
const situacaoDiagnostico = ref<'EM_ANDAMENTO' | 'CONCLUIDO' | 'VALIDADO' | 'HOMOLOGADO'>('CONCLUIDO');
const erroValidar = ref<Error | null>(null);
const erroDevolver = ref<Error | null>(null);
const erroHomologar = ref<Error | null>(null);

vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: backMock,
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        perfilSelecionado: perfilSelecionado.value,
    }),
}));

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: ref({
            competencias: [
                {competenciaCodigo: 10, descricao: 'Competência A'},
            ],
        }),
    }),
}));

vi.mock('@/composables/useMonitoramentoDiagnostico', () => ({
    useMonitoramentoDiagnostico: () => ({
        unidade: ref({
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        }),
        servidores: ref([
            {
                servidorTitulo: '242426',
                servidorNome: 'Duff McKagan',
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [
                    {competenciaCodigo: 10, importancia: 4, dominio: 2},
                    {competenciaCodigo: 11, importancia: null, dominio: null},
                ],
            },
        ]),
        ocupacoesCriticas: ref([
            {servidorTitulo: '242426', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
        ]),
        movimentacoes: ref([
            {descricao: 'Diagnóstico concluído', unidadeOrigem: 'A', unidadeDestino: 'B', dataHora: '2026-06-06 09:00'},
        ]),
        carregando: ref(false),
        situacao: situacaoDiagnostico,
        totalPendentes: ref(1),
    }),
}));

vi.mock('@/composables/useFluxoDiagnostico', () => ({
    useFluxoDiagnostico: () => ({
        validando: ref(false),
        devolvendo: ref(false),
        homologando: ref(false),
        erroValidar,
        erroDevolver,
        erroHomologar,
        validarDiagnostico: validarDiagnosticoMock,
        devolverDiagnostico: devolverDiagnosticoMock,
        homologarDiagnostico: homologarDiagnosticoMock,
    }),
}));

describe('DiagnosticoUnidadeView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        perfilSelecionado.value = Perfil.GESTOR;
        situacaoDiagnostico.value = 'CONCLUIDO';
        erroValidar.value = null;
        erroDevolver.value = null;
        erroHomologar.value = null;
    });

    function montar() {
        return mount(DiagnosticoUnidadeView, {
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
                    EmptyState: {template: '<div data-testid="empty-state" />'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BBadge: {template: '<span><slot /></span>'},
                    BCard: {template: '<section><slot /></section>'},
                    BCardHeader: {template: '<header><slot /></header>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BAccordion: {template: '<div><slot /></div>'},
                    BAccordionItem: {template: '<div><slot name="title" /><slot /></div>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<div><slot /></div>'},
                    BSpinner: {template: '<span />'},
                    BFormText: {template: '<small><slot /></small>'},
                    BFormTextarea: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    },
                    BModal: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
                    },
                    BTable: {
                        props: ['items'],
                        template: `
                          <div>
                            <div v-for="item in items" :key="item.competenciaCodigo ?? item.servidorTitulo">
                              <slot name="cell(importancia)" :item="item" />
                              <slot name="cell(dominio)" :item="item" />
                              <slot name="cell(gap)" :item="item" />
                              <slot name="cell(situacaoCapacitacao)" :item="item" />
                            </div>
                          </div>
                        `,
                    },
                },
            },
        });
    }

    it('renderiza métricas, gaps, ocupações e histórico do diagnóstico', () => {
        const wrapper = montar();

        expect(wrapper.text()).toContain('ASSESSORIA_12');
        expect(wrapper.text()).toContain('Assessoria 12');
        expect(wrapper.text()).toContain('Servidores');
        expect(wrapper.text()).toContain('Pendentes');
        expect(wrapper.text()).toContain('Ocupações Críticas');
        expect(wrapper.text()).toContain('+2');
        expect(wrapper.text()).toContain('---');
        expect(wrapper.text()).toContain('Em capacitação');
        expect(wrapper.text()).toContain('Diagnóstico concluído');
        expect(wrapper.find('[data-testid="btn-validar-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-devolver-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-homologar-diagnostico-unidade"]').exists()).toBe(false);
    });

    it('valida, devolve com justificativa obrigatória e homologa como admin', async () => {
        validarDiagnosticoMock.mockResolvedValue(undefined);
        devolverDiagnosticoMock.mockResolvedValue(undefined);
        homologarDiagnosticoMock.mockResolvedValue(undefined);
        perfilSelecionado.value = Perfil.ADMIN;
        situacaoDiagnostico.value = 'VALIDADO';

        const wrapper = montar();

        await wrapper.get('[data-testid="btn-homologar-diagnostico-unidade"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-homologar-unidade"]').trigger('click');
        expect(homologarDiagnosticoMock).toHaveBeenCalledWith(undefined);
        expect(wrapper.text()).toContain('Diagnóstico homologado');

        situacaoDiagnostico.value = 'CONCLUIDO';
        const wrapperConcluido = montar();
        await wrapperConcluido.get('[data-testid="btn-validar-diagnostico-unidade"]').trigger('click');
        let textarea = wrapperConcluido.get('textarea');
        await textarea.setValue('Observações');
        await wrapperConcluido.get('[data-testid="btn-confirmar-validar-unidade"]').trigger('click');
        expect(validarDiagnosticoMock).toHaveBeenCalledWith('Observações');

        await wrapperConcluido.get('[data-testid="btn-devolver-diagnostico-unidade"]').trigger('click');
        await wrapperConcluido.get('[data-testid="btn-confirmar-devolver-unidade"]').trigger('click');
        expect(wrapperConcluido.text()).toContain('A justificativa é obrigatória.');

        textarea = wrapperConcluido.get('textarea');
        await textarea.setValue('Ajustar consenso');
        await wrapperConcluido.get('[data-testid="btn-confirmar-devolver-unidade"]').trigger('click');
        expect(devolverDiagnosticoMock).toHaveBeenCalledWith('Ajustar consenso');
    });

    it('exibe erro quando validar falha', async () => {
        erroValidar.value = new Error('Falha ao validar');
        validarDiagnosticoMock.mockRejectedValue(erroValidar.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-validar-diagnostico-unidade"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-validar-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao validar');
    });
});
