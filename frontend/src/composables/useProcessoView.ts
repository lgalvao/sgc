import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useProcessosStore} from "@/stores/processos";
import {usePerfilStore} from "@/stores/perfil";
import {useFeedbackStore} from "@/stores/feedback";
import {SituacaoSubprocesso} from "@/types/tipos";

function flattenUnidades(unidades: any[]): any[] {
    let result: any[] = [];
    for (const u of unidades) {
        result.push(u);
        if (u.filhos && u.filhos.length > 0) {
            result = result.concat(flattenUnidades(u.filhos));
        }
    }
    return result;
}

export function useProcessoView() {
    const route = useRoute();
    const router = useRouter();
    const processosStore = useProcessosStore();
    const perfilStore = usePerfilStore();
    const feedbackStore = useFeedbackStore();

    const codProcesso = Number(route.params.codProcesso || route.query.codProcesso);
    const modalBlocoRef = ref<any>(null);
    const mostrarModalFinalizacao = ref(false);
    const acaoBlocoAtual = ref<"aceitar" | "homologar" | "disponibilizar">("aceitar");

    const processo = computed(() => processosStore.processoDetalhe);
    const participantesHierarquia = computed(() => processo.value?.unidades || []);

    const podeAceitarBloco = computed(() => {
        return unidadesElegiveisPorAcao.value.aceitar.length > 0;
    });

    const podeHomologarBloco = computed(() => {
        return (processo.value?.podeHomologarCadastro || processo.value?.podeHomologarMapa || false)
            && unidadesElegiveisPorAcao.value.homologar.length > 0;
    });

    const podeDisponibilizarBloco = computed(() => {
        return (processo.value?.podeFinalizar || false)
            && unidadesElegiveisPorAcao.value.disponibilizar.length > 0;
    });

    const mostrarBotoesBloco = computed(() => {
        return podeAceitarBloco.value || podeHomologarBloco.value || podeDisponibilizarBloco.value;
    });

    const podeFinalizar = computed(() => {
        return processo.value?.podeFinalizar || false;
    });

    const unidadesElegiveisPorAcao = computed(() => {
        const unidades = flattenUnidades(participantesHierarquia.value);
        return {
            aceitar: unidades.filter(u =>
                u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
                u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
                u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
            ),
            homologar: unidades.filter(u =>
                u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
                u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
                u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
            ),
            disponibilizar: unidades.filter(u =>
                u.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
                u.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO ||
                u.situacaoSubprocesso === SituacaoSubprocesso.NAO_INICIADO
            )
        };
    });

    const unidadesElegiveis = computed(() => {
        const elegiveis = unidadesElegiveisPorAcao.value[acaoBlocoAtual.value];
        return elegiveis.map(u => ({
            codigo: u.codUnidade,
            sigla: u.sigla,
            nome: u.nome,
            situacao: u.situacaoLabel || u.situacaoSubprocesso
        }));
    });

    const idsElegiveis = computed(() => unidadesElegiveis.value.map(u => u.codigo));

    const tituloModalBloco = computed(() => {
        switch (acaoBlocoAtual.value) {
            case "aceitar": return "Aceitar em Bloco";
            case "homologar": return "Homologar em Bloco";
            case "disponibilizar": return "Disponibilizar Mapas em Bloco";
            default: return "";
        }
    });

    const textoModalBloco = computed(() => {
        switch (acaoBlocoAtual.value) {
            case "aceitar": return "Selecione as unidades para aceitar o cadastro/mapa em bloco:";
            case "homologar": return "Selecione as unidades para homologar o cadastro/mapa em bloco:";
            case "disponibilizar": return "Selecione as unidades para disponibilizar os mapas em bloco:";
            default: return "";
        }
    });

    const rotuloBotaoBloco = computed(() => {
        switch (acaoBlocoAtual.value) {
            case "aceitar": return "Aceitar Selecionados";
            case "homologar": return "Homologar Selecionados";
            case "disponibilizar": return "Disponibilizar Selecionados";
            default: return "";
        }
    });



    async function abrirDetalhesUnidade(row: any) {
        if (!row.clickable) return;

        await router.push({
            name: "Subprocesso",
            params: {
                codProcesso: codProcesso.toString(),
                siglaUnidade: row.sigla
            }
        });
    }

    function finalizarProcesso() {
        mostrarModalFinalizacao.value = true;
    }

    async function confirmarFinalizacao() {
        try {
            await processosStore.finalizarProcesso(codProcesso);
            feedbackStore.show("Sucesso", "Processo finalizado com sucesso", "success");
            await router.push("/painel");
        } catch (error: any) {
            feedbackStore.show("Erro ao finalizar", error.message || "Ocorreu um erro", "danger");
        }
    }

    function abrirModalBloco(acao: "aceitar" | "homologar" | "disponibilizar") {
        acaoBlocoAtual.value = acao;
        modalBlocoRef.value?.abrir();
    }

    async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
        try {
            modalBlocoRef.value?.setProcessando(true);
            await processosStore.executarAcaoBloco(acaoBlocoAtual.value, dados.ids, dados.dataLimite);

            feedbackStore.show("Sucesso", "Ação em bloco realizada com sucesso", "success");
            modalBlocoRef.value?.fechar();
            await processosStore.buscarContextoCompleto(codProcesso);
        } catch (error: any) {
            modalBlocoRef.value?.setErro(error.message || "Erro ao executar ação em bloco");
            modalBlocoRef.value?.setProcessando(false);
        }
    }

    onMounted(async () => {
        if (codProcesso) {
            await processosStore.buscarContextoCompleto(codProcesso);
        }
    });

    return {
        processosStore,
        perfilStore,
        feedbackStore,
        processo,
        participantesHierarquia,
        modalBlocoRef,
        mostrarModalFinalizacao,
        acaoBlocoAtual,
        unidadesElegiveis,
        idsElegiveis,
        mostrarBotoesBloco,
        podeAceitarBloco,
        podeHomologarBloco,
        podeDisponibilizarBloco,
        podeFinalizar,
        tituloModalBloco,
        textoModalBloco,
        rotuloBotaoBloco,
        abrirDetalhesUnidade,
        finalizarProcesso,
        confirmarFinalizacao,
        abrirModalBloco,
        executarAcaoBloco
    };
}
