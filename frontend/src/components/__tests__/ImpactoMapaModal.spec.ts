import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount, VueWrapper} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import ImpactoMapaModal from '../ImpactoMapaModal.vue';
import {useMapasStore} from '@/stores/mapas';
import {ImpactoMapa} from "@/types/tipos";

// Mock the full store to control its state and actions
vi.mock('@/stores/mapas', () => ({
    useMapasStore: vi.fn(() => ({
        impactoMapa: null as ImpactoMapa | null,
        fetchImpactoMapa: vi.fn(),
    })),
}));

describe('ImpactoMapaModal.vue', () => {
    let wrapper: VueWrapper<any>;
    let mapasStore: ReturnType<typeof useMapasStore>;

    const idSubprocesso = 1;

    const mountComponent = (props = {mostrar: true, idSubprocesso}) => {
        return mount(ImpactoMapaModal, {
            props,
            global: {
                plugins: [createPinia()],
            },
        });
    };

    beforeEach(() => {
        setActivePinia(createPinia());
        mapasStore = useMapasStore();
        vi.clearAllMocks();
    });

    it('should not render when "mostrar" is false', () => {
        wrapper = mountComponent({mostrar: false, idSubprocesso});
        expect(wrapper.find('.modal.show').exists()).toBe(false);
    });

    it('should render and fetch impacts when "mostrar" becomes true', async () => {
        wrapper = mountComponent({mostrar: false, idSubprocesso});
        expect(mapasStore.fetchImpactoMapa).not.toHaveBeenCalled();

        await wrapper.setProps({mostrar: true});
        await wrapper.vm.$nextTick();

        expect(wrapper.find('.modal.show').exists()).toBe(true);
        expect(mapasStore.fetchImpactoMapa).toHaveBeenCalledWith(idSubprocesso);
    });

    it('should display a loading state initially', async () => {
        wrapper = mountComponent();
        expect(wrapper.find('.spinner-border').exists()).toBe(true);
    });

    it('should display "no impact" message when temImpacto is false', async () => {
        vi.mocked(mapasStore.fetchImpactoMapa).mockImplementation(async () => {
            mapasStore.impactoMapa = {temImpacto: false, competencias: []};
        });
        wrapper = mountComponent();
        await wrapper.vm.$nextTick();
        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="msg-nenhum-impacto"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="msg-nenhum-impacto"]').text()).toContain('A revisão do cadastro não produziu nenhum impacto');
    });

    it('should display impacted competencies and their reasons', async () => {
        vi.mocked(mapasStore.fetchImpactoMapa).mockImplementation(async () => {
            mapasStore.impactoMapa = {
                temImpacto: true,
                competencias: [{
                    id: 1,
                    descricao: 'Gestão de Projetos',
                    atividadesAdicionadas: ['Análise de Requisitos'],
                    atividadesRemovidas: [],
                    conhecimentosAdicionados: ['Metodologia Ágil'],
                    conhecimentosRemovidos: []
                }]
            };
        });

        wrapper = mountComponent();
        await wrapper.vm.$nextTick();
        await wrapper.vm.$nextTick();

        const text = wrapper.text();
        expect(text).toContain('Gestão de Projetos');
        expect(text).toContain('Atividade adicionada: Análise de Requisitos');
        expect(text).toContain('Conhecimento adicionado: Metodologia Ágil');
    });

    it('should display error message on fetch failure', async () => {
        vi.mocked(mapasStore.fetchImpactoMapa).mockImplementation(async () => {
            mapasStore.impactoMapa = null;
        });

        wrapper = mountComponent();
        await wrapper.vm.$nextTick();
        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="msg-erro"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="msg-erro"]').text()).toContain('Não foi possível carregar os impactos no mapa.');
    });

    it('should emit "fechar" when the close button is clicked', async () => {
        wrapper = mountComponent();
        await wrapper.find('.btn-close').trigger('click');
        expect(wrapper.emitted('fechar')).toBeTruthy();
    });
});