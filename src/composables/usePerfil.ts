import {computed} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {useServidoresStore} from '@/stores/servidores';
import {useUnidadesStore} from '@/stores/unidades';
import type {Servidor, Unidade} from '@/types/tipos';

// Função auxiliar para achatar a hierarquia de unidades
function flattenUnidades(unidades: Unidade[]): Unidade[] {
  let flat: Unidade[] = [];
  unidades.forEach((u: Unidade) => {
    flat.push(u);
    if (u.filhas && u.filhas.length > 0) {
      flat = flat.concat(flattenUnidades(u.filhas));
    }
  });
  return flat;
}

// Função para determinar o perfil de um servidor
function getPerfil(servidor: Servidor, unidadesFlat: Unidade[]): string {
  const unidade = unidadesFlat.find(u => u.titular === servidor.id);
  if (unidade) {
    if (unidade.sigla === 'SEDOC') return 'ADMIN';

    // Regras para CHEFE e GESTOR baseadas no tipo da unidade
    if (unidade.tipo === 'INTERMEDIARIA') {
      return 'GESTOR';
    } else if (unidade.tipo === 'OPERACIONAL' || unidade.tipo === 'INTEROPERACIONAL') {
      return 'CHEFE';
    }
  }
  return 'SERVIDOR';
}

export function usePerfil() {
  const perfilStore = usePerfilStore();
  const servidoresStore = useServidoresStore();
  const unidadesStore = useUnidadesStore();

  const unidadesFlat = computed<Unidade[]>(() => flattenUnidades(unidadesStore.unidades));

  const servidoresComPerfil = computed(() => {
    return servidoresStore.servidores.map((s: Servidor) => ({
      ...s,
      perfil: getPerfil(s, unidadesFlat.value)
    }));
  });

  const servidorLogado = computed(() => {
    return servidoresComPerfil.value.find((s: Servidor & { perfil: string }) => s.id === perfilStore.servidorId);
  });

  const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);
  const unidadeSelecionada = computed(() => perfilStore.unidadeSelecionada);

  return {
    servidorLogado,
    servidoresComPerfil,
    perfilSelecionado,
    unidadeSelecionada,
  };
}