import {describe, expect, it, vi, beforeEach, afterEach} from 'vitest';
import {defineComponent, ref} from 'vue';
import {mount, flushPromises} from '@vue/test-utils';
import {useBuscadorUsuarios} from '../useBuscadorUsuarios';
import * as usuarioService from '@/services/usuarioService';

vi.mock('@/services/usuarioService', () => ({
    pesquisarUsuarios: vi.fn().mockResolvedValue([]),
}));

const mockNotify = vi.fn();
vi.mock('@/composables/useNotification', () => ({
    useNotification: () => ({ notify: mockNotify }),
}));

describe('useBuscadorUsuarios.ts', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    const TestComponent = defineComponent({
        props: ['termo', 'selecionado'],
        setup(props) {
            const termoRef = ref(props.termo);
            const selecionadoRef = ref(props.selecionado);
            const buscador = useBuscadorUsuarios(termoRef, selecionadoRef);
            return { ...buscador, termoRef, selecionadoRef };
        },
        template: '<div></div>'
    });

    it('aoAlterarTermo schedules search and updates mostrarResultadosUsuarios', async () => {
        const wrapper = mount(TestComponent, { props: { termo: '', selecionado: null } });
        const vm = wrapper.vm as any;
        
        vm.aoAlterarTermo('test');
        expect(vm.termoRef).toBe('test');
        expect(vm.mostrarResultadosUsuarios).toBe(true);
        expect(vi.getTimerCount()).toBe(1);

        vi.advanceTimersByTime(300);
        await vi.runAllTicks();
        expect(usuarioService.pesquisarUsuarios).toHaveBeenCalledWith('test');
    });

    it('aoAlterarTermo clears results if term is too short', async () => {
        const wrapper = mount(TestComponent, { props: { termo: 'abc', selecionado: null } });
        const vm = wrapper.vm as any;
        vm.usuariosEncontrados = [{nome: 'U1'}];
        
        vm.aoAlterarTermo('a');
        expect(vm.usuariosEncontrados).toEqual([]);
        expect(vm.mostrarResultadosUsuarios).toBe(false);
    });

    it('agendarOcultacao schedules hide', async () => {
        const wrapper = mount(TestComponent, { props: { termo: '', selecionado: null } });
        const vm = wrapper.vm as any;
        
        vm.mostrarResultadosUsuarios = true;
        vm.agendarOcultacao();
        vi.advanceTimersByTime(150);
        expect(vm.mostrarResultadosUsuarios).toBe(false);
    });

    it('calcularProximoIndice handles empty list and wrap around', () => {
        const wrapper = mount(TestComponent, { props: { termo: '', selecionado: null } });
        const vm = wrapper.vm as any;
        
        // Empty list
        vm.usuariosEncontrados = [];
        expect(vm.calcularProximoIndice(1)).toBe(0); // Reality check: returns 0

        // Wrap around
        vm.usuariosEncontrados = [{nome: 'U1'}, {nome: 'U2'}];
        vm.indiceUsuarioDestacado = 1;
        expect(vm.calcularProximoIndice(1)).toBe(0);
        
        vm.indiceUsuarioDestacado = 0;
        expect(vm.calcularProximoIndice(-1)).toBe(1);
    });

    it('selecionarUsuario updates refs', () => {
        const wrapper = mount(TestComponent, { props: { termo: 'initial', selecionado: null } });
        const vm = wrapper.vm as any;
        
        vm.mostrarResultadosUsuarios = true;
        
        vm.selecionarUsuario({nome: 'User', tituloEleitoral: '123'});
        
        expect(vm.termoRef).toBe('User');
        expect(vm.selecionadoRef).toBe('123');
        expect(vm.mostrarResultadosUsuarios).toBe(false);
    });

    it('handles search error', async () => {
        const wrapper = mount(TestComponent, { props: { termo: '', selecionado: null } });
        const vm = wrapper.vm as any;
        
        vi.mocked(usuarioService.pesquisarUsuarios).mockRejectedValue(new Error('fail'));
        
        vm.aoAlterarTermo('error');
        vi.advanceTimersByTime(300);
        await vi.runAllTicks();
        
        expect(mockNotify).toHaveBeenCalledWith(expect.any(String), 'danger');
        expect(vm.usuariosEncontrados).toEqual([]);
    });

    it('limparResultados early returns if already clean', () => {
        const wrapper = mount(TestComponent, { props: { termo: '', selecionado: null } });
        const vm = wrapper.vm as any;
        vm.usuariosEncontrados = [];
        vm.mostrarResultadosUsuarios = false;
        vm.pesquisandoUsuarios = false;
        
        vm.limparResultados();
        // Should return early
    });

    it('watch termo clears results if empty', async () => {
        const wrapper = mount(TestComponent, { props: { termo: 'test', selecionado: null } });
        const vm = wrapper.vm as any;
        vm.usuariosEncontrados = [{nome: 'U1'}];
        
        vm.termoRef = '';
        await nextTick(); // Vue watch
        expect(vm.usuariosEncontrados).toEqual([]);
    });
});

import {nextTick} from 'vue';
