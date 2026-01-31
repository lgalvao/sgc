import { computed } from "vue";
import { useAtividadesStore } from "@/stores/atividades";
import { useUnidadesStore } from "@/stores/unidades";
import { useProcessosStore } from "@/stores/processos";
import { usePerfilStore } from "@/stores/perfil";
import { useAnalisesStore } from "@/stores/analises";
import { Perfil, SituacaoSubprocesso, TipoProcesso, type Unidade } from "@/types/tipos";

export function useVisAtividadesState(props: { codProcesso: number | string; sigla: string }) {
    const atividadesStore = useAtividadesStore();
    const unidadesStore = useUnidadesStore();
    const processosStore = useProcessosStore();
    const perfilStore = usePerfilStore();
    const analisesStore = useAnalisesStore();

    const unidadeId = computed(() => props.sigla);
    const codProcesso = computed(() => Number(props.codProcesso));

    const unidade = computed(() => {
        function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | undefined {
            for (const u of unidades) {
                if (u.sigla === sigla) return u;
                if (u.filhas && u.filhas.length) {
                    const encontrada = buscarUnidade(u.filhas, sigla);
                    if (encontrada) return encontrada;
                }
            }
        }
        return buscarUnidade(unidadesStore.unidades as Unidade[], unidadeId.value);
    });

    const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value);
    const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ""));
    const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

    const subprocesso = computed(() => {
        if (!processosStore.processoDetalhe) return null;
        return processosStore.processoDetalhe.unidades.find((u) => u.sigla === unidadeId.value);
    });

    const isHomologacao = computed(() => {
        if (!subprocesso.value) return false;
        const { situacaoSubprocesso } = subprocesso.value;
        const perfil = perfilSelecionado.value;
        return (
            perfil === Perfil.ADMIN &&
            (situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO ||
                situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
                situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
        );
    });

    const podeVerImpacto = computed(() => {
        if (!subprocesso.value || !perfilSelecionado.value) return false;
        const perfil = perfilSelecionado.value;
        const podeVer = perfil === Perfil.GESTOR || perfil === Perfil.ADMIN;
        const situacaoCorreta =
            subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
            subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
        return podeVer && situacaoCorreta;
    });

    const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

    const atividades = computed(() => {
        if (codSubprocesso.value === undefined) return [];
        return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value) || [];
    });

    const processoAtual = computed(() => processosStore.processoDetalhe);
    const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

    const historicoAnalises = computed(() => {
        if (!codSubprocesso.value) return [];
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
    });

    return {
        atividadesStore,
        processosStore,
        perfilStore,
        analisesStore,
        codProcesso,
        unidadeId,
        unidade,
        siglaUnidade,
        nomeUnidade,
        perfilSelecionado,
        subprocesso,
        isHomologacao,
        podeVerImpacto,
        codSubprocesso,
        atividades,
        isRevisao,
        historicoAnalises,
        Perfil,
    };
}
