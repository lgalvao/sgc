import {computed} from 'vue';
import {usePerfilStore} from '../stores/perfil';
import {useServidoresStore} from '../stores/servidores';
import {useUnidadesStore} from '../stores/unidades';

// Função auxiliar para achatar a hierarquia de unidades
function flattenUnidades(unidades) {
  let flat = [];
  unidades.forEach(u => {
    flat.push(u);
    if (u.filhas && u.filhas.length > 0) {
      flat = flat.concat(flattenUnidades(u.filhas));
    }
  });
  return flat;
}

// Função para determinar o perfil de um servidor
function getPerfil(servidor, unidadesFlat) {
  const unidade = unidadesFlat.find(u => u.titular === servidor.id);
  if (unidade) {
    if (unidade.sigla === 'SEDOC') return 'ADMIN';
    return unidade.filhas && unidade.filhas.length > 0 ? 'GESTOR' : 'CHEFE';
  }
  return 'SERVIDOR';
}

export function usePerfil() {
  const perfilStore = usePerfilStore();
  const servidoresStore = useServidoresStore();
  const unidadesStore = useUnidadesStore();

  const unidadesFlat = computed(() => flattenUnidades(unidadesStore.unidades));

  const servidoresComPerfil = computed(() => {
    return servidoresStore.servidores.map(s => ({
      ...s,
      perfil: getPerfil(s, unidadesFlat.value)
    }));
  });

  const servidorLogado = computed(() => {
    return servidoresComPerfil.value.find(s => s.id === perfilStore.servidorId);
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