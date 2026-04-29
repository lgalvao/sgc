import {ref, type Ref} from "vue";
import type {Atividade, ContextoCadastroAtividadesSubprocesso, RespostaLocalCadastro, Unidade} from "@/types/tipos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {calcularAssinaturaCadastro} from "@/utils/formatters";
import logger from "@/utils/logger";

interface CadastroOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

export function useCadastroOrquestracao(props: CadastroOrquestracaoProps, atividades: Ref<Atividade[]>) {
    const subprocessoStore = useSubprocessoStore();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const atividadesSnapshotInicial = ref<string | null>(null);
    const unidade = ref<Unidade | null>(null);
    const codMapa = ref<number | null>(null);

    function processarRespostaLocal(response: RespostaLocalCadastro) {
        atividades.value = response.atividadesAtualizadas;
        subprocessoStore.atualizarStatusLocal({
            ...response.subprocesso,
            permissoes: response.permissoes
        });
    }

    function sincronizarEstadoInicialContexto(data: ContextoCadastroAtividadesSubprocesso) {
        processarRespostaLocal({
            subprocesso: {
                codigo: data.detalhes.codigo,
                situacao: data.detalhes.situacao,
            },
            permissoes: data.detalhes.permissoes,
            atividadesAtualizadas: data.atividadesDisponiveis,
        });

        atividadesSnapshotInicial.value = data.assinaturaCadastroReferencia ?? calcularAssinaturaCadastro(data.atividadesDisponiveis);
        unidade.value = data.unidade;
        codMapa.value = data.mapa.codigo;
    }

    async function carregarContextoInicial() {
        try {
            const data = typeof props.codSubprocesso === "number"
                ? await subprocessoStore.garantirContextoCadastroAtividades(props.codSubprocesso, false)
                : await subprocessoStore.garantirContextoCadastroAtividadesPorProcessoEUnidade(
                    Number(props.codProcesso),
                    props.sigla,
                    false,
                );

            if (!data) {
                logger.error("ERRO: Subprocesso não encontrado!");
                return false;
            }

            codigoSubprocesso.value = data.detalhes.codigo;
            sincronizarEstadoInicialContexto(data);
            return true;
        } catch (e) {
            logger.error("Erro ao carregar contexto inicial", e);
            return false;
        } finally {
            carregandoInicial.value = false;
        }
    }

    return {
        carregandoInicial,
        codigoSubprocesso,
        atividadesSnapshotInicial,
        unidade,
        codMapa,
        carregarContextoInicial,
        processarRespostaLocal,
        sincronizarEstadoInicialContexto
    };
}
