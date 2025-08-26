import {computed} from 'vue';
import {usePerfilStore} from '@/stores/perfil';
import {useServidoresStore} from '@/stores/servidores';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicaoTemporaria';
import {Perfil, Unidade} from '@/types/tipos';

// Função auxiliar para achatar a hierarquia de unidades
function flattenUnidades(unidades: Unidade[]): Unidade[] {
    let flat: Unidade[] = [];
    unidades.forEach((u: Unidade) => {
        flat.push(u);
        if (u.filhas && u.filhas.length > 0) flat = flat.concat(flattenUnidades(u.filhas));
    });
    return flat;
}

// Função para determinar o perfil para uma unidade específica
function getPerfilDaUnidade(unidade: Unidade): string {
    if (unidade.sigla === 'SEDOC') return 'ADMIN';
    if (unidade.tipo === 'INTERMEDIARIA') return 'GESTOR';
    if (unidade.tipo === 'OPERACIONAL' || unidade.tipo === 'INTEROPERACIONAL') return 'CHEFE';
    return 'SERVIDOR';
}

export function usePerfil() {
    const perfilStore = usePerfilStore();
    const servidoresStore = useServidoresStore();
    const unidadesStore = useUnidadesStore();
    const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();

    const unidadesFlat = computed<Unidade[]>(() => flattenUnidades(unidadesStore.unidades));

    const getPerfisDoServidor = (idServidor: number) => {
        const servidor = servidoresStore.getServidorById(idServidor);
        if (!servidor) return [];

        const pares: { perfil: string, unidade: string }[] = [];

        // 1. Adiciona perfis de titular (chefe/gestor/admin)
        const unidadesChefiadas = unidadesFlat.value.filter(u => u.idServidorTitular === idServidor);
        unidadesChefiadas.forEach(unidade => {
            pares.push({perfil: getPerfilDaUnidade(unidade), unidade: unidade.sigla});
        });

        // 2. Adiciona perfis de atribuições temporárias
        const atribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorServidor(idServidor);
        atribuicoes.forEach(atrb => {
            const unidadeAtribuicao = unidadesStore.pesquisarUnidade(atrb.unidade);
            if (unidadeAtribuicao) {
                const perfilAtribuicao = unidadeAtribuicao.tipo === 'INTERMEDIARIA' ? Perfil.GESTOR : Perfil.CHEFE;
                pares.push({perfil: perfilAtribuicao, unidade: atrb.unidade});
            }
        });

        // 3. Adiciona o perfil SERVIDOR para a unidade de lotação principal,
        // SOMENTE SE essa unidade não tiver já um perfil de titular ou CHEFE por atribuição.
        const unidadePrincipal = unidadesStore.pesquisarUnidade(servidor.unidade);
        if (unidadePrincipal) {
            const isOperacional = unidadePrincipal.tipo === 'OPERACIONAL' || unidadePrincipal.tipo === 'INTEROPERACIONAL';
            // Verifica se a unidade principal já foi adicionada com um perfil diferente de SERVIDOR
            const hasNonServidorProfileForPrincipalUnit = pares.some(
                p => p.unidade === unidadePrincipal.sigla && p.perfil !== Perfil.SERVIDOR
            );

            if (isOperacional && !hasNonServidorProfileForPrincipalUnit) {
                pares.push({perfil: Perfil.SERVIDOR, unidade: unidadePrincipal.sigla});
            }
        }

        // 4. Remove duplicatas exatas (mesmo perfil, mesma unidade)
        return pares.filter((par, index, self) =>
                index === self.findIndex((p) => (
                    p.perfil === par.perfil && p.unidade === par.unidade
                ))
        );
    };

    const servidorLogado = computed(() => {
        const servidor = servidoresStore.getServidorById(perfilStore.servidorId);
        if (!servidor) return null;
        return {
            ...servidor,
            perfil: perfilStore.perfilSelecionado,
            unidade: perfilStore.unidadeSelecionada,
        };
    });

    const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);
    const unidadeSelecionada = computed(() => perfilStore.unidadeSelecionada);

    return {
        servidorLogado,
        perfilSelecionado,
        unidadeSelecionada,
        getPerfisDoServidor,
    };
}
