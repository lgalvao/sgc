import {computed, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import {useUnidadesStore} from "@/stores/unidades";
import {useAtribuicaoTemporariaStore} from "@/stores/atribuicoes";
import {usePerfilStore} from "@/stores/perfil";
import {useMapasStore} from "@/stores/mapas";
import {buscarUsuarioPorTitulo} from "@/services/usuarioService";
import {logger} from "@/utils";
import type {Usuario} from "@/types/tipos";

export function useUnidadeView(codUnidade: number) {
    const router = useRouter();
    const unidadesStore = useUnidadesStore();
    const atribuicaoStore = useAtribuicaoTemporariaStore();
    const perfilStore = usePerfilStore();
    const mapasStore = useMapasStore();

    const titularDetalhes = ref<Usuario | null>(null);

    const unidade = computed(() => unidadesStore.unidade);
    const mapaVigente = computed(() => mapasStore.mapaCompleto);

    const unidadeComResponsavelDinamico = computed(() => {
        if (!unidade.value) return null;

        const atribuicoes = atribuicaoStore.obterAtribuicoesPorUnidade(unidade.value.sigla);
        const agora = new Date();
        const atribuicaoAtiva = atribuicoes.find(a => {
            const inicio = new Date(a.dataInicio);
            let fim = null;
            if (a.dataTermino) {
                fim = new Date(a.dataTermino);
            } else if (a.dataFim) {
                fim = new Date(a.dataFim);
            }
            return inicio <= agora && (!fim || fim >= agora);
        });

        return {
            ...unidade.value,
            responsavel: atribuicaoAtiva ? atribuicaoAtiva.usuario : (unidade.value.responsavel || null)
        };
    });

    async function carregarDados() {
        try {
            await Promise.all([
                unidadesStore.buscarArvoreUnidade(codUnidade),
                atribuicaoStore.buscarAtribuicoes()
            ]);

            if (unidade.value?.tituloTitular) {
                titularDetalhes.value = await buscarUsuarioPorTitulo(unidade.value.tituloTitular);
            }
        } catch (error) {
            logger.error("Erro ao buscar titular:", error);
        }
    }

    function irParaCriarAtribuicao() {
        router.push({ path: `/unidade/${codUnidade}/atribuicao` });
    }

    function navegarParaUnidadeSubordinada(row: any) {
        router.push({ path: `/unidade/${row.codigo}` });
    }

    function visualizarMapa() {
        if (mapaVigente.value) {
            router.push({
                name: "SubprocessoVisMapa",
                params: {
                    codProcesso: mapaVigente.value.subprocessoCodigo,
                    siglaUnidade: unidade.value?.sigla
                }
            });
        }
    }

    onMounted(carregarDados);
    watch(() => codUnidade, carregarDados);

    return {
        unidadesStore,
        perfilStore,
        unidadeComResponsavelDinamico,
        titularDetalhes,
        mapaVigente,
        irParaCriarAtribuicao,
        navegarParaUnidadeSubordinada,
        visualizarMapa
    };
}
