import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import ConsensoDiagnosticoView from '../ConsensoDiagnosticoView.vue';

const backMock = vi.fn();
const pushMock = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: backMock,
        push: pushMock,
    }),
}));

const contextoData = ref<any>({
    competencias: [
        {competenciaCodigo: 10, descricao: 'Competência A'},
    ],
});
vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: contextoData,
    }),
}));

const podeCriarConsensoVal = ref(true);
vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: computed(() => podeCriarConsensoVal.value),
    }),
}));

const competenciasLocais = ref<any[]>([
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

const setPendingMock = vi.fn();
vi.mock('@/stores/toast', () => ({
    useToastStore: () => ({
        setPending: setPendingMock,
    }),
}));

describe('ConsensoDiagnosticoView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        contextoData.value = {
            competencias: [
                {competenciaCodigo: 10, descricao: 'Competência A'},
            ],
        };
        podeCriarConsensoVal.value = true;
        competenciasLocais.value = [
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
                servidorNome: 'Servidor Exemplo',
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

                },
            },
        });

        expect(wrapper.text()).toContain('Avaliação de consenso');
        expect(wrapper.text()).toContain('Servidor Exemplo - 242426');
        expect(wrapper.find('.bi-person-lines-fill').exists()).toBe(false);
        expect(wrapper.findAll('thead tr')).toHaveLength(2);
        expect(wrapper.find('th[rowspan="2"]').text()).toBe('Competência');
        expect(wrapper.find('[data-testid="consenso-final-dominio-10"]').exists()).toBe(true);

        const cabecalhosGrupo = wrapper.findAll('thead tr').at(0)?.findAll('th') ?? [];
        expect(cabecalhosGrupo.map((coluna) => coluna.text())).toEqual([
            'Competência',
            'Importância',
            'Domínio',
        ]);

        const cabecalhosSubgrupo = wrapper.findAll('thead tr').at(1)?.findAll('th') ?? [];
        expect(cabecalhosSubgrupo.map((coluna) => coluna.text())).toEqual([
            'Importância',
            'Chefe',
            'Consenso',
            'Servidor',
            'Chefe',
            'Consenso',
        ]);

        const opcoesConsensoDominio = wrapper.findAll('[data-testid="consenso-final-dominio-10"] option');
        expect(opcoesConsensoDominio.at(0)?.text()).toBe('-');
        expect(opcoesConsensoDominio.at(1)?.text()).toBe('NA');
    });

    it('renderiza como servidor, exibe BTable e aprova com sucesso', async () => {
        podeCriarConsensoVal.value = false;
        competenciasLocais.value = [
            {
                competenciaCodigo: 10,
                autoimportancia: 3,
                autodominio: 4,
                chefiaImportancia: null,
                chefiaDominio: null,
                consensoImportancia: null,
                consensoDominio: null,
            }
        ];

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
                servidorNome: 'Servidor Exemplo',
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

                },
            },
        });

        expect(wrapper.find('table.tabela-consenso').exists()).toBe(true);
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
                servidorNome: 'Servidor Exemplo',
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

    it('deve usar nome e título no subtítulo quando o servidor não for o usuário logado', () => {
        const outroServidor = '999999';
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: outroServidor,
                servidorNome: 'Outro Servidor',
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

        expect(wrapper.text()).toContain('Outro Servidor - 999999');
        expect(wrapper.text()).not.toContain('Servidor Exemplo - 999999');
    });

    it('deve exibir tabela padrao se não houver chefia importancias', () => {
        competenciasLocais.value = [
            { competenciaCodigo: 15, autoimportancia: 5, autodominio: 2 },
        ];
        podeCriarConsensoVal.value = true;
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
                servidorNome: 'Servidor Exemplo',
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
                servidorNome: 'Servidor Exemplo',
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

    it('exercita carregando, salvandoAutomaticamente e aprovando', () => {
        carregandoVal.value = true;
        salvandoAutomaticamenteVal.value = true;
        aprovandoVal.value = true;
        podeCriarConsensoVal.value = true;
        ehConsensoAprovadoVal.value = false;
        
        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div class="carregando">Carregando...</div>'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {template: '<select v-bind="$attrs" />'},
                    BSpinner: {template: '<span class="spinner"/>'},
                },
            },
        });
        
        expect(wrapper.find('.carregando').exists()).toBe(true);
        // Os spinners de salvando não renderizam se tiver carregando=true pois o template v-else oculta
    });

    it('exercita fallback da descricao e spinner aprovando', async () => {
        podeCriarConsensoVal.value = false;
        // competenciaCodigo 999 não está no mock de contexto
        competenciasLocais.value = [
            {competenciaCodigo: 999, importancia: 3, dominio: 4}
        ];
        aprovandoVal.value = true;

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BSpinner: {template: '<span class="spinner"/>'},
                    BTable: {
                        props: ['items'],
                        template: `
                          <table data-testid="tbl-servidor">
                            <tr v-for="item in items" :key="item.competenciaCodigo">
                              <td>{{ item.descricao }}</td>
                            </tr>
                          </table>
                        `,
                    },
                },
            },
        });

        // Test fallback for unknown competencia
        expect(wrapper.text()).toContain('Competência 999');
        // Test spinner in aprovando
        expect(wrapper.find('[data-testid="btn-aprovar-consenso"]').find('.spinner').exists()).toBe(true);
    });

    it('exercita valores numericos NaN no select', async () => {
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
                    CarregamentoPagina: {template: '<div />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {
                        template: '<select />',
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        // Trigger the update:modelValue directly on the select stub with NaN number
        const select = wrapper.findComponent('[data-testid="consenso-chefia-importancia-10"]');
        (select as any).vm.$emit('update:modelValue', NaN);
        
        expect(atualizarNotaDetalhadaMock).toHaveBeenLastCalledWith(10, {
            origem: 'chefia',
            campo: 'importancia',
            valor: null,
        });
        
        (wrapper.findComponent('[data-testid="consenso-chefia-importancia-10"]') as any).vm.$emit('update:modelValue', '');
    });

    it('exercita salvandoAutomaticamente renderizado', () => {
        carregandoVal.value = false;
        salvandoAutomaticamenteVal.value = true;
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
                    CarregamentoPagina: {template: '<div />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BFormSelect: {template: '<select v-bind="$attrs" />'},
                    BSpinner: {template: '<span class="spinner"/>'},
                },
            },
        });
        
        expect(wrapper.find('.spinner').exists()).toBe(true);
    });

    it('deve redirecionar para a tela de subprocesso ao aprovar consenso se processoCodigo estiver presente', async () => {
        podeCriarConsensoVal.value = false;
        competenciasLocais.value = [
            {
                competenciaCodigo: 10,
                autoimportancia: 3,
                autodominio: 4,
                chefiaImportancia: null,
                chefiaDominio: null,
                consensoImportancia: null,
                consensoDominio: null,
            }
        ];

        contextoData.value = {
            processoCodigo: 99,
            competencias: [
                {competenciaCodigo: 10, descricao: 'Competência A'},
            ],
        };

        const wrapper = mount(ConsensoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
                servidorNome: 'Servidor Exemplo',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div />'},
                    AppAlert: {template: '<div />'},
                    BAlert: {template: '<div><slot /></div>'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BCard: {template: '<section v-bind="$attrs"><slot /></section>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        const btnAprovar = wrapper.get('[data-testid="btn-aprovar-consenso"]');
        aprovarConsensoMock.mockResolvedValue(undefined);
        await btnAprovar.trigger('click');
        expect(aprovarConsensoMock).toHaveBeenCalled();
        expect(setPendingMock).toHaveBeenCalledWith('Avaliação de consenso aprovada');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {
                codProcesso: '99',
                siglaUnidade: 'ASSESSORIA_12',
            },
            query: {
                codSubprocesso: '400',
            },
        });
    });
});
