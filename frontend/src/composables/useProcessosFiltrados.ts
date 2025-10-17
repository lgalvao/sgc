import {computed, Ref, ref} from 'vue';
import {storeToRefs} from 'pinia';
import {usePerfilStore} from '@/stores/perfil';
import {useProcessosStore} from '@/stores/processos';
import {useUnidadesStore} from '@/stores/unidades';
import {Perfil, SituacaoProcesso} from '@/types/tipos';
import {ProcessoResumo} from "@/mappers/processos";

export function useProcessosFiltrados(filterBySituacaoFinalizado: Ref<boolean> = ref(false)) {
    const perfil = usePerfilStore();
    const processosStore = useProcessosStore();
    const unidadesStore = useUnidadesStore();

    const {processosPainel} = storeToRefs(processosStore);

    const processosFiltrados = computed<ProcessoResumo[]>(() => {
        const unidadeUsuario = perfil.unidadeSelecionada;
        const perfilUsuario = perfil.perfilSelecionado;

        let processosBase = processosPainel.value;

        if (filterBySituacaoFinalizado.value) {
            processosBase = processosBase.filter(p => p.situacao === 'FINALIZADO');
        }

        // A lógica de filtragem foi simplificada, pois o backend já deve retornar os processos
        // corretos para o perfil e unidade do usuário.
        // A filtragem client-side agora apenas lida com a situação (Finalizado/Não Finalizado).

        // Excluir processos "Criado" para perfis não-ADMIN (CDU-02)
        if (perfilUsuario !== Perfil.ADMIN) {
            processosBase = processosBase.filter(p => p.situacao !== 'CRIADO');
        }

        return processosBase;
    });

    return {processosFiltrados};
}