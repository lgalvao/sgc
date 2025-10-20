import {computed, Ref, ref} from 'vue';
import {storeToRefs} from 'pinia';
import {usePerfilStore} from '@/stores/perfil';
import {useProcessosStore} from '@/stores/processos';
import {Perfil, type ProcessoResumo} from '@/types/tipos';

export function useProcessosFiltrados(filterBySituacaoFinalizado: Ref<boolean> = ref(false)) {
    const perfil = usePerfilStore();
    const processosStore = useProcessosStore();

    const {processosPainel} = storeToRefs(processosStore);

    const processosFiltrados = computed<ProcessoResumo[]>(() => {
        const perfilUsuario = perfil.perfilSelecionado;

        let processosBase = processosPainel.value;
        if (filterBySituacaoFinalizado.value) {
            processosBase = processosBase.filter(p => p.situacao === 'FINALIZADO');
        }
        if (perfilUsuario !== Perfil.ADMIN) {
            processosBase = processosBase.filter(p => p.situacao !== 'CRIADO');
        }

        return processosBase;
    });

    return {processosFiltrados};
}