import {describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import ConsensoDiagnosticoView from '../ConsensoDiagnosticoView.vue';

vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: vi.fn(),
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

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: computed(() => true),
    }),
}));

vi.mock('@/composables/useConsensoDiagnostico', () => ({
    useConsensoDiagnostico: () => ({
        competenciasLocais: ref([]),
        competenciasDetalhadasLocais: ref([
            {
                competenciaCodigo: 10,
                autoimportancia: 3,
                autodominio: 4,
                chefiaImportancia: 3,
                chefiaDominio: 4,
                consensoImportancia: 3,
                consensoDominio: 4,
            },
        ]),
        situacaoServidor: ref('CONSENSO_CRIADO'),
        ehConsensoAprovado: computed(() => false),
        carregando: computed(() => false),
        salvandoAutomaticamente: ref(false),
        autoguardado: ref(false),
        aprovando: computed(() => false),
        erroAprovar: ref(null),
        atualizarNotaDetalhada: vi.fn(),
        aprovarConsenso: vi.fn(),
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '242426',
        usuarioNome: 'Servidor Exemplo',
    }),
}));

describe('ConsensoDiagnosticoView', () => {
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
});
