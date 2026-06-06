import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import ConsensoDiagnosticoView from '../ConsensoDiagnosticoView.vue';

const backMock = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: backMock,
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

const podeCriarConsensoVal = ref(true);
vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: computed(() => podeCriarConsensoVal.value),
    }),
}));

const competenciasLocais = ref<any[]>([]);
const competenciasDetalhadasLocais = ref<any[]>([
    {
        competenciaCodigo: 10,
        autoimportancia: 3,
        autodominio: 4,
        chefiaImportancia: 3,
        chefiaDominio: 4,
        consensoImportancia: 3,
        consensoDominio: 4,
    },
]);
const situacaoServidorVal = ref('CONSENSO_CRIADO');
const ehConsensoAprovadoVal = ref(false);
const carregandoVal = ref(false);
const salvandoAutomaticamenteVal = ref(false);
const aprovandoVal = ref(false);
const erroAprovarVal = ref<any>(null);
const atualizarNotaDetalhadaMock = vi.fn();
const aprovarConsensoMock = vi.fn();

vi.mock('@/composables/useConsensoDiagnostico', () => ({
    useConsensoDiagnostico: () => ({
        competenciasLocais,
        competenciasDetalhadasLocais,
        situacaoServidor: situacaoServidorVal,
        ehConsensoAprovado: computed(() => ehConsensoAprovadoVal.value),
        carregando: computed(() => carregandoVal.value),
        salvandoAutomaticamente: salvandoAutomaticamenteVal,
        aprovando: computed(() => aprovandoVal.value),
        erroAprovar: erroAprovarVal,
        atualizarNotaDetalhada: atualizarNotaDetalhadaMock,
        aprovarConsenso: aprovarConsensoMock,
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '242426',
        usuarioNome: 'Servidor Exemplo',
    }),
}));

describe('ConsensoDiagnosticoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        podeCriarConsensoVal.value = true;
        competenciasLocais.value = [];
        competenciasDetalhadasLocais.value = [
            {
                competenciaCodigo: 10,
                autoimportancia: 3,
                autodominio: 4,
                chefiaImportancia: 3,
                chefiaDominio: 4,
                consensoImportancia: 3,
                consensoDominio: 4,
            },
        ];
        situacaoServidorVal.value = 'CONSENSO_CRIADO';
        ehConsensoAprovadoVal.value = false;
        carregandoVal.value = false;
        salvandoAutomaticamenteVal.value = false;
        aprovandoVal.value = false;
        erroAprovarVal.value = null;
    });

    it('simplifica o cabeçalho e a tabela de consenso', () => {
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {
                        props: ['options', 'modelValue'],
                        template: `
                          <select v-bind="$attrs">
                            <option
                              v-for="opcao in options"
                              :key="String(opcao.value)"
                              :value="opcao.value ?? ''"
                            >
                              {{ opcao.text }}
                            </option>
                          </select>
                        `,
                    },
                    BSpinner: {template: '<span />'},
                    BTable: {
                        template: '<table data-testid="tbl-consenso"><slot name="cell(autoimportancia)" :item="{ autoimportancia: 3 }" /><slot name="cell(autodominio)" :item="{ autodominio: 4 }" /><slot name="cell(chefiaImportancia)" :item="{ competenciaCodigo: 10, chefiaImportancia: 3 }" /><slot name="cell(chefiaDominio)" :item="{ competenciaCodigo: 10, chefiaDominio: 4 }" /><slot name="cell(consensoImportancia)" :item="{ competenciaCodigo: 10, consensoImportancia: 3 }" /><slot name="cell(consensoDominio)" :item="{ competenciaCodigo: 10, consensoDominio: 4 }" /></table>',
                    },
                },
            },
        });

        expect(wrapper.text()).toContain('Avaliação de Consenso');
        expect(wrapper.text()).toContain('Servidor Exemplo');
        expect(wrapper.text()).not.toContain('Servidor:');
        expect(wrapper.text()).not.toContain('Escala:');
        expect(wrapper.find('.bi-person-lines-fill').exists()).toBe(false);
        expect(wrapper.findAll('thead tr')).toHaveLength(2);
        expect(wrapper.find('th[rowspan="2"]').text()).toBe('Competência');
        expect(wrapper.find('[data-testid="consenso-final-dominio-10"]').exists()).toBe(true);

        const cabecalhosGrupo = wrapper.findAll('thead tr').at(0)?.findAll('th') ?? [];
        expect(cabecalhosGrupo.map((coluna) => coluna.text())).toEqual([
            'Competência',
            'Servidor',
            'Chefia',
            'Consenso',
        ]);

        const opcoesConsensoDominio = wrapper.findAll('[data-testid="consenso-final-dominio-10"] option');
        expect(opcoesConsensoDominio.at(0)?.text()).toBe('-');
        expect(opcoesConsensoDominio.at(1)?.text()).toBe('NA');
    });

    it('renderiza como servidor, exibe BTable e aprova com sucesso', async () => {
        podeCriarConsensoVal.value = false;
        competenciasLocais.value = [
            {competenciaCodigo: 10, importancia: 3, dominio: 4}
        ];

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BSpinner: {template: '<span />'},
                    BTable: {
                        props: ['items'],
                        template: `
                          <table data-testid="tbl-servidor">
                            <tr v-for="item in items" :key="item.competenciaCodigo">
                              <td>{{ item.descricao }}</td>
                              <td><slot name="cell(importancia)" :item="item" /></td>
                              <td><slot name="cell(dominio)" :item="item" /></td>
                            </tr>
                          </table>
                        `,
                    },
                },
            },
        });

        expect(wrapper.find('[data-testid="tbl-servidor"]').exists()).toBe(true);
        expect(wrapper.text()).toContain('Competência A');

        const btnAprovar = wrapper.get('[data-testid="btn-aprovar-consenso"]');
        aprovarConsensoMock.mockResolvedValue(undefined);
        await btnAprovar.trigger('click');
        expect(aprovarConsensoMock).toHaveBeenCalled();
        expect(backMock).toHaveBeenCalled();
    });

    it('exibe alerta de erro quando falha aprovar consenso', async () => {
        podeCriarConsensoVal.value = false;
        competenciasLocais.value = [
            {competenciaCodigo: 10, importancia: 3, dominio: 4}
        ];
        const erro = new Error('Falha ao salvar consenso');
        erroAprovarVal.value = erro;
        aprovarConsensoMock.mockRejectedValue(erro);

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BSpinner: {template: '<span />'},
                    BTable: {template: '<div />'},
                },
            },
        });

        const btnAprovar = wrapper.get('[data-testid="btn-aprovar-consenso"]');
        await btnAprovar.trigger('click');
        expect(wrapper.find('.app-alert').text()).toContain('Falha ao salvar consenso');
    });

    it('exercita normalizarValorNota com ramificacoes', async () => {
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<input type="text" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        const input = wrapper.find('[data-testid="consenso-chefia-importancia-10"]');

        await input.setValue(5);
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'importancia',
            valor: 5,
        });

        await input.setValue('');
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'importancia',
            valor: null,
        });

        await input.setValue('abc');
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'importancia',
            valor: null,
        });
    });

    it('deve exibir o alerta de consenso aprovado e notas estáticas quando ehConsensoAprovado for verdadeiro', () => {
        ehConsensoAprovadoVal.value = true;
        podeCriarConsensoVal.value = true;
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div class="alert-consenso"><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {template: '<select />'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        expect(wrapper.find('.alert-consenso').exists()).toBe(true);
        expect(wrapper.find('.alert-consenso').text()).toContain('A avaliação de consenso deste servidor já foi aprovada');
        expect(wrapper.find('select').exists()).toBe(false);
    });

    it('deve usar o servidorTitulo como subtítulo quando o servidor não for o usuário logado', () => {
        const outroServidor = '999999';
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: outroServidor,
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {template: '<select />'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        expect(wrapper.text()).toContain(outroServidor);
        expect(wrapper.text()).not.toContain('Servidor Exemplo');
    });

    it('deve utilizar competenciasLocais caso competenciasDetalhadasLocais esteja vazio', () => {
        competenciasDetalhadasLocais.value = [];
        competenciasLocais.value = [
            { competenciaCodigo: 15, importancia: 5, dominio: 2 },
        ];
        podeCriarConsensoVal.value = true;
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {template: '<select />'},
                    BSpinner: {template: '<span />'},
                },
            },
        });
        expect(wrapper.text()).toContain('Competência 15');
    });

    it('deve exibir erro padrão de salvamento se erroAprovar não contiver mensagem', async () => {
        podeCriarConsensoVal.value = false;
        competenciasLocais.value = [
            {competenciaCodigo: 10, importancia: 3, dominio: 4}
        ];
        erroAprovarVal.value = {};
        aprovarConsensoMock.mockRejectedValue(new Error('Erro interno genérico'));

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BSpinner: {template: '<span />'},
                    BTable: {template: '<div />'},
                },
            },
        });

        const btnAprovar = wrapper.get('[data-testid="btn-aprovar-consenso"]');
        await btnAprovar.trigger('click');
        expect(wrapper.find('.app-alert').text()).toContain('Não foi possível salvar. Tente novamente.');
    });

    it('deve exercitar as notas de chefiaDominio, consensoImportancia, consensoDominio e botões de voltar', async () => {
        podeCriarConsensoVal.value = true;
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<input type="text" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        // 1. Chefia Dominio
        const inputChefiaDominio = wrapper.find('[data-testid="consenso-chefia-dominio-10"]');
        await inputChefiaDominio.setValue(4);
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'dominio',
            valor: 4,
        });

        // 2. Consenso Importancia
        const inputConsensoImportancia = wrapper.find('[data-testid="consenso-final-importancia-10"]');
        await inputConsensoImportancia.setValue(3);
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'consenso',
            campo: 'importancia',
            valor: 3,
        });

        // 3. Consenso Dominio
        const inputConsensoDominio = wrapper.find('[data-testid="consenso-final-dominio-10"]');
        await inputConsensoDominio.setValue(2);
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'consenso',
            campo: 'dominio',
            valor: 2,
        });

        // 4. Botão de voltar
        const btnVoltar = wrapper.find('button[variant="outline-secondary"]');
        await btnVoltar.trigger('click');
        expect(backMock).toHaveBeenCalled();
    });

    it('deve limpar erroMensagem ao dispensar o alerta', async () => {
        podeCriarConsensoVal.value = false;
        competenciasLocais.value = [
            {competenciaCodigo: 10, importancia: 3, dominio: 4}
        ];
        erroAprovarVal.value = new Error('Erro de teste');
        aprovarConsensoMock.mockRejectedValue(erroAprovarVal.value);

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {
                        props: ['mensagem'],
                        emits: ['dismissed'],
                        template: '<div class="app-alert">{{ mensagem }}<button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>',
                    },
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BSpinner: {template: '<span />'},
                    BTable: {template: '<div />'},
                },
            },
        });

        const btnAprovar = wrapper.get('[data-testid="btn-aprovar-consenso"]');
        await btnAprovar.trigger('click');
        expect(wrapper.find('.app-alert').exists()).toBe(true);

        await wrapper.get('[data-testid="btn-dismiss-alert"]').trigger('click');
        expect(wrapper.find('.app-alert').exists()).toBe(false);
    });
});

