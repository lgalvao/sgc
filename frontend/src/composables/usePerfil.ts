import {computed} from "vue";
import {useAtribuicaoTemporariaStore} from "@/stores/atribuicoes";
import {usePerfilStore} from "@/stores/perfil";
import {useUnidadesStore} from "@/stores/unidades";
import {useUsuariosStore} from "@/stores/usuarios";
import {Perfil, type PerfilUnidade, type Unidade} from "@/types/tipos";

// Função auxiliar para achatar a hierarquia de unidades
function flattenUnidades(unidades: Unidade[]): Unidade[] {
  let flat: Unidade[] = [];
  unidades.forEach((u: Unidade) => {
    flat.push(u);
      if (u.filhas && u.filhas.length > 0)
          flat = flat.concat(flattenUnidades(u.filhas));
  });
  return flat;
}

// Função para determinar o perfil para uma unidade específica
function getPerfilDaUnidade(unidade: Unidade): Perfil {
    if (unidade.sigla === "SEDOC") return Perfil.ADMIN;
    if (unidade.tipo === "INTERMEDIARIA") return Perfil.GESTOR;
    if (unidade.tipo === "OPERACIONAL" || unidade.tipo === "INTEROPERACIONAL")
        return Perfil.CHEFE;
  return Perfil.SERVIDOR;
}

export function usePerfil() {
  const perfilStore = usePerfilStore();
  const usuariosStore = useUsuariosStore();
  const unidadesStore = useUnidadesStore();
  const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();

    const unidadesFlat = computed<Unidade[]>(() =>
        flattenUnidades(unidadesStore.unidades),
    );

  const getPerfisDoServidor = (idServidor: number): PerfilUnidade[] => {
    const usuario = usuariosStore.getUsuarioById(idServidor);
    if (!usuario) return [];

    const pares: PerfilUnidade[] = [];

    // 1. Adiciona perfis de titular (chefe/gestor/admin)
      const unidadesChefiadas = unidadesFlat.value.filter(
          (u) => u.idServidorTitular === idServidor,
      );
      unidadesChefiadas.forEach((unidade) => {
          pares.push({
              perfil: getPerfilDaUnidade(unidade),
              unidade: unidade,
              siglaUnidade: unidade.sigla,
          });
    });

    // 2. Adiciona perfis de atribuições temporárias
      const atribuicoes =
          atribuicaoTemporariaStore.getAtribuicoesPorServidor(idServidor);
      atribuicoes.forEach((atrb) => {
          const unidadeAtribuicao = unidadesStore.pesquisarUnidadePorSigla(
              atrb.unidade.sigla,
          );
      if (unidadeAtribuicao) {
          const perfilAtribuicao =
              unidadeAtribuicao.tipo === "INTERMEDIARIA"
                  ? Perfil.GESTOR
                  : Perfil.CHEFE;
          pares.push({
              perfil: perfilAtribuicao,
              unidade: unidadeAtribuicao,
              siglaUnidade: unidadeAtribuicao.sigla,
          });
      }
    });

    // 3. Adiciona o perfil SERVIDOR para a unidade de lotação principal,
    // SOMENTE SE essa unidade não tiver já um perfil de titular ou CHEFE por atribuição.
      const unidadePrincipal = unidadesStore.pesquisarUnidadePorSigla(
          usuario.unidade.sigla,
      );
    if (unidadePrincipal) {
        const isOperacional =
            unidadePrincipal.tipo === "OPERACIONAL" ||
            unidadePrincipal.tipo === "INTEROPERACIONAL";
      // Verifica se a unidade principal já foi adicionada com um perfil diferente de SERVIDOR
      const hasNonServidorProfileForPrincipalUnit = pares.some(
          (p) =>
              p.unidade.sigla === unidadePrincipal.sigla &&
              p.perfil !== Perfil.SERVIDOR,
      );

      if (isOperacional && !hasNonServidorProfileForPrincipalUnit) {
          pares.push({
              perfil: Perfil.SERVIDOR,
              unidade: unidadePrincipal,
              siglaUnidade: unidadePrincipal.sigla,
          });
      }
    }

    // 4. Remove duplicatas exatas (mesmo perfil, mesma unidade)
      return pares.filter(
          (par, index, self) =>
              index ===
              self.findIndex(
                  (p) =>
                      p.perfil === par.perfil && p.unidade.sigla === par.unidade.sigla,
              ),
    );
  };

  const servidorLogado = computed(() => {
    const usuario = usuariosStore.getUsuarioById(perfilStore.servidorId);
    if (!usuario) return null;
    return {
      ...usuario,
      perfil: perfilStore.perfilSelecionado,
      unidade: perfilStore.unidadeSelecionada,
    };
  });

  const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

  const unidadeSelecionada = computed(() => {
      const unidadeObj = unidadesFlat.value.find(
          (u) => u.codigo === perfilStore.unidadeSelecionada,
      );
    return unidadeObj?.sigla || perfilStore.unidadeSelecionada;
  });

  return {
    servidorLogado,
    perfilSelecionado,
    unidadeSelecionada,
    getPerfisDoServidor,
  };
}
