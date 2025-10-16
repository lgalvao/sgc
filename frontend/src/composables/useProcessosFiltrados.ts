import {computed, Ref, ref} from 'vue';
import {storeToRefs} from 'pinia';
import {usePerfilStore} from '@/stores/perfil';
import {useProcessosStore} from '@/stores/processos';
import { useSubprocessosStore } from '@/stores/subprocessos';
import {useUnidadesStore} from '@/stores/unidades';
import {Perfil, Processo, SituacaoProcesso} from '@/types/tipos';

export function useProcessosFiltrados(filterBySituacaoFinalizado: Ref<boolean> = ref(false)) {
  const perfil = usePerfilStore();
  const processosStore = useProcessosStore();
  const subprocessosStore = useSubprocessosStore();
  const unidadesStore = useUnidadesStore();

  const { processos } = storeToRefs(processosStore);

  const processosFiltrados = computed<Processo[]>(() => {
    const unidadeUsuario = perfil.unidadeSelecionada;
    const perfilUsuario = perfil.perfilSelecionado;

    let processosBase = processos.value as Processo[];

    if (filterBySituacaoFinalizado.value) {
      processosBase = processosBase.filter(p => p.situacao === SituacaoProcesso.FINALIZADO);
    }

    // Se o perfil for ADMIN, retorna todos os processos (base ou finalizados)
    if (perfilUsuario === Perfil.ADMIN) {
      return processosBase;
    }
    
    // Excluir processos "Criado" para perfis não-ADMIN (CDU-02)
    processosBase = processosBase.filter(p => p.situacao !== SituacaoProcesso.CRIADO);
    
    // Para GESTOR, CHEFE, SERVIDOR, filtra pela unidade
    if (unidadeUsuario) {
      let unidadesParaFiltrar: string[] = [];

      if (perfilUsuario === Perfil.GESTOR) {
        // Para GESTOR, inclui a unidade atual e todas as subordinadas
        unidadesParaFiltrar = unidadesStore.getUnidadesSubordinadas(unidadeUsuario);
      } else if (perfilUsuario === Perfil.CHEFE || perfilUsuario === Perfil.SERVIDOR) {
        // Para CHEFE e SERVIDOR, apenas a unidade atual
        unidadesParaFiltrar = [unidadeUsuario];
      }

      return processosBase.filter(p => {
        const unidadesDoProcesso = subprocessosStore.getUnidadesDoProcesso(p.id).map(pu => pu.unidade);
        // Verifica se alguma unidade do processo está na lista de unidades do usuário
        return unidadesDoProcesso.some(unidadeProcesso => unidadesParaFiltrar.includes(unidadeProcesso));
      });
    }
    // Se não houver unidade selecionada e não for ADMIN, retorna todos os processos base
    return processosBase;
  });

  return { processosFiltrados };
}