import {computed, onMounted, reactive, ref, toRefs, unref} from "vue";
import {useRouter} from "vue-router";
import {useAcesso} from "@/composables/acesso";
import {useFluxoMapa} from "@/composables/useFluxoMapa";
import {useFormErrors} from "@/composables/useFormErrors";
import {useMapaCompetenciasMutacoes} from "@/composables/useMapaCompetenciasMutacoes";
import {useImpactoMapaModal} from "@/composables/useImpactoMapaModal";
import {useMapas} from "@/composables/useMapas";
import {useNotification} from "@/composables/useNotification";
import {useToastStore} from "@/stores/toast";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {usePerfilStore} from "@/stores/perfil";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {listarAnalisesValidacao} from "@/services/analiseService";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useMapaOrquestracao} from "@/composables/useMapaOrquestracao";
import {useMapaSugestoes} from "@/composables/useMapaSugestoes";
import {useMapaAnaliseFluxo} from "@/views/mapaAnaliseFluxo";
import {useMapaDisponibilizacao} from "@/views/mapaDisponibilizacao";
import {normalizarErro} from "@/utils/apiError";
import {logger} from "@/utils";
import type {Analise, MapaCompleto} from "@/types/tipos";
import {Perfil} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {relatoriosService} from "@/services/relatoriosService";

interface MapaTelaProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

export function useMapaTela(props: MapaTelaProps) {
    const router = useRouter();
    const fluxoMapa = useFluxoMapa();
    const carregandoFluxoMapa = computed(() => unref(fluxoMapa.carregando));
    const {notify} = useNotification();
    const toastStore = useToastStore();
    const subprocessoStore = useSubprocessoStore();
    const perfilStore = usePerfilStore();
    const {atualizarFluxoMapa} = useInvalidacaoNavegacao();
    const subprocesso = computed(() => subprocessoStore.contextoEdicao?.detalhes ?? null);

    const {
        podeVisualizarImpacto,
        podeApresentarSugestoes,
        podeEditarMapa,
        mostrarValidarMapa,
        mostrarApresentarSugestoes,
        mostrarDisponibilizarMapa,
        mostrarDevolverMapa,
        habilitarApresentarSugestoes,
        habilitarDisponibilizarMapa,
        habilitarEditarMapa,
        habilitarValidarMapa,
        podeVerSugestoes,
        habilitarDevolverMapa,
        acaoPrincipalMapa
    } = useAcesso(subprocesso);

    const usarMenuAcoesMapa = computed(() => {
        return mostrarApresentarSugestoes.value
            || mostrarValidarMapa.value
            || mostrarDevolverMapa.value
            || Boolean(acaoPrincipalMapa.value?.mostrar)
            || mostrarDisponibilizarMapa.value;
    });

    const esconderEdicaoMapaParaAdmin = computed(() =>
        perfilStore.perfilSelecionado === Perfil.ADMIN
        && podeEditarMapa.value
        && !habilitarEditarMapa.value
    );
    const modoSomenteLeitura = computed(() => !podeEditarMapa.value || esconderEdicaoMapaParaAdmin.value);
    const mostrarAcaoPrincipalMapa = computed(() => Boolean(acaoPrincipalMapa.value?.mostrar));
    const habilitarAcaoPrincipalMapa = computed(() => !!acaoPrincipalMapa.value?.habilitar);
    const rotuloAcaoPrincipalMapa = computed(() => acaoPrincipalMapa.value?.rotuloBotao ?? TEXTOS.mapa.LABEL_HOMOLOGAR);
    const mapaAtual = computed(() => mapasStore.mapaCompleto.value);

    const {
        carregandoInicial,
        codigoSubprocesso,
        unidade,
        carregarContextoInicial,
    } = useMapaOrquestracao(props);

    const mostrarExportacaoMapa = computed(() => perfilStore.perfilSelecionado === Perfil.CHEFE && Boolean(codigoSubprocesso.value));
    const loadingExportacaoPdf = ref(false);
    const loadingExportacaoCsv = ref(false);

    const mapasStore = useMapas(codigoSubprocesso);
    const {impactoMapa: impactos, erro: erroMapa} = mapasStore;

    const atividades = computed(() => mapaAtual.value?.atividades ?? []);
    const competencias = computed(() => mapaAtual.value?.competencias ?? []);
    const mapaSomenteLeitura = mapaAtual;

    const estadoModais = reactive({
        mostrarModalAceitar: false,
        mostrarModalValidar: false,
        mostrarModalDevolucao: false,
        mostrarModalHistorico: false,
        mostrarModalDisponibilizar: false,
    });
    const {
        mostrarModalAceitar,
        mostrarModalValidar,
        mostrarModalDevolucao,
        mostrarModalHistorico,
        mostrarModalDisponibilizar,
    } = toRefs(estadoModais);

    const observacaoDevolucao = ref("");
    const analisesCadastro = ref<Analise[]>([]);
    const historicoAnalise = computed(() => analisesCadastro.value);

    const {
        validarSubmissao,
        resetarValidacao,
        deveExibirErro,
        focarPrimeiroErroInvalido
    } = useValidacaoFormulario();

    async function concluirAcaoPainel(mensagem: string, fecharModal: () => void) {
        fecharModal();
        toastStore.setPending(mensagem);
        const codigoAtual = codigoSubprocesso.value;
        void atualizarFluxoMapa(codigoAtual === null ? undefined : codigoAtual);
        await router.push({name: "Painel"});
    }

    const {
        sugestoes,
        sugestoesVisualizacao,
        loadingSugestoesVisualizacao,
        loadingSugestoesEnvio,
        mostrarModalSugestoes,
        mostrarModalVerSugestoes,
        verSugestoes,
        fecharModalVerSugestoes,
        abrirModalSugestoes,

        confirmarSugestoes,
    } = useMapaSugestoes({
        obterCodigoSubprocessoObrigatorio,
        notify,
        concluirAcaoPainel,
        validarSubmissao,
        focarPrimeiroErroInvalido,
        resetarValidacao,
    });

    const mensagemErroDevolucao = computed(() => {
        return deveExibirErro(!observacaoDevolucao.value.trim()) ? "A justificativa é obrigatória para a devolução." : "";
    });
    const mensagemErroSugestoes = computed(() => {
        return deveExibirErro(!sugestoes.value.trim()) ? "As sugestões são obrigatórias." : "";
    });

    const {
        mostrarModalImpacto,
        loadingImpacto,
        abrirModalImpacto,
        fecharModalImpacto,
    } = useImpactoMapaModal(codigoSubprocesso, (codigo) => mapasStore.carregarImpacto(codigo));

    const {
        abrirModalAceitar,
        fecharModalAceitar,
        abrirModalValidar,
        abrirModalDevolucao,
        confirmarValidacao,
        confirmarAceitacao,
        confirmarDevolucao,
        fecharModalHistorico,
        verHistorico,
    } = useMapaAnaliseFluxo({
        obterCodigoSubprocessoObrigatorio,
        acaoPrincipalMapa,
        mostrarModalAceitar,
        mostrarModalValidar,
        mostrarModalDevolucao,
        mostrarModalHistorico,
        observacaoDevolucao,
        analisesCadastro,
        resetarValidacao,
        validarSubmissao,
        focarPrimeiroErroInvalido,
        concluirAcaoPainel,
        notify,
        listarAnalisesCadastro: listarAnalisesValidacao,
        validarMapa: fluxoMapa.validarMapa,
        homologarMapa: fluxoMapa.homologarMapa,
        aceitarMapa: fluxoMapa.aceitarMapa,
        devolverMapa: fluxoMapa.devolverMapa,
    });

    function obterCodigoSubprocessoObrigatorio(): number {
        const codSubp = codigoSubprocesso.value;
        if (!codSubp) throw new Error("Invariante violada: codigoSubprocesso não carregado");
        return codSubp;
    }

    function obterMensagemErroIntegracaoContexto() {
        return subprocessoStore.erroIntegracaoContexto?.mensagem
            ?? "Falha grave ao resolver subprocesso para o mapa. A ocorrência deve ser auditada.";
    }

    function obterMensagemErroExportacao(error: unknown, mensagemPadrao: string) {
        const erroNormalizado = normalizarErro(error);
        if (erroNormalizado.tipo === "inesperado" && !erroNormalizado.status) {
            return mensagemPadrao;
        }
        return erroNormalizado.mensagem || mensagemPadrao;
    }

    function notificarErro(error: unknown, mensagemPadrao: string) {
        notify(obterMensagemErroExportacao(error, mensagemPadrao), "danger");
    }

    async function executarExportacao({
        estadoLoading,
        mensagemPadrao,
        descricaoLog,
        acao,
    }: {
        estadoLoading: {value: boolean};
        mensagemPadrao: string;
        descricaoLog: string;
        acao: () => Promise<void>;
    }) {
        estadoLoading.value = true;
        try {
            await acao();
        } catch (error) {
            logger.error(descricaoLog, error);
            notificarErro(error, mensagemPadrao);
        } finally {
            estadoLoading.value = false;
        }
    }

    async function exportarMapaAtualPdf() {
        await executarExportacao({
            estadoLoading: loadingExportacaoPdf,
            mensagemPadrao: TEXTOS_RELATORIOS.ERRO_EXPORTAR,
            descricaoLog: "Erro ao exportar PDF do mapa atual:",
            acao: () => relatoriosService.downloadRelatorioMapaAtualPdf(obterCodigoSubprocessoObrigatorio()),
        });
    }

    async function exportarMapaAtualCsv() {
        await executarExportacao({
            estadoLoading: loadingExportacaoCsv,
            mensagemPadrao: TEXTOS_RELATORIOS.ERRO_EXPORTAR_CSV,
            descricaoLog: "Erro ao exportar CSV do mapa atual:",
            acao: () => relatoriosService.downloadRelatorioMapaAtualCsv(obterCodigoSubprocessoObrigatorio()),
        });
    }

    const contextoEdicaoAtual = computed(() => subprocessoStore.contextoEdicao);
    const sincronizarMapaStore = (mapaAtualizado: MapaCompleto | null | undefined) =>
        sincronizarMapaContexto({
            mapaAtualizado,
            codigoSubprocesso: codigoSubprocesso.value,
            sincronizarMapa: mapasStore.sincronizarMapa,
            mapaContextoAtual: contextoEdicaoAtual,
        });

    const codigosAtividadesAssociadas = computed(() => {
        return new Set(
            competencias.value.flatMap((competencia) =>
                competencia.atividades.map((atividade) => atividade.codigo)
            )
        );
    });
    const atividadesSemCompetencia = computed(() => {
        if (atividades.value.length === 0) return [];
        return atividades.value.filter((atividade) => !codigosAtividadesAssociadas.value.has(atividade.codigo));
    });

    const existeCompetenciaSemAtividade = computed(() => {
        return competencias.value.some((competencia) => competencia.atividades.length === 0);
    });

    const {erros: fieldErrors, aplicarErroNormalizado: aplicarErroNormalizadoBase, limparErros} = useFormErrors([
        'descricao',
        'atividades',
        'atividadesCodigos',
        'dataLimite',
        'observacoes',
        'generic'
    ]);

    function sincronizarErrosAtividades() {
        if (fieldErrors.value.atividadesCodigos) {
            fieldErrors.value.atividades = fieldErrors.value.atividadesCodigos;
        }
    }

    function aplicarErroNormalizado(error: ReturnType<typeof normalizarErro> | null) {
        aplicarErroNormalizadoBase(error);
        sincronizarErrosAtividades();
    }

    const {
        competenciaSendoEditada,
        mostrarModalCriarNovaCompetencia,
        mostrarModalExcluirCompetencia,
        competenciaParaExcluir,
        loadingCompetencia,
        loadingExclusao,
        abrirModalCriarLimpo,
        fecharModalCriarNovaCompetencia,
        iniciarEdicaoCompetencia,
        adicionarCompetenciaEFecharModal,
        excluirCompetencia,
        confirmarExclusaoCompetencia,
        removerAtividadeAssociada,
    } = useMapaCompetenciasMutacoes({
        obterCodigoSubprocessoObrigatorio,
        competencias,
        fluxoMapa,
        notify,
        limparErros,
        aplicarErroNormalizado,
        sincronizarMapa: sincronizarMapaStore,
    });

    const {
        erroValidacaoMapa,
        erroValidacaoMapaTick,
        loadingDisponibilizacao,
        notificacaoDisponibilizacao,
        abrirModalDisponibilizar,
        fecharModalDisponibilizar,
        disponibilizarMapa,
        limparErroMapa,
        sincronizarMapaContexto,
    } = useMapaDisponibilizacao({
        competencias,
        existeCompetenciaSemAtividade,
        atividadesSemCompetencia,
        mostrarModalDisponibilizar,
        limparErros,
        obterCodigoSubprocessoObrigatorio,
        disponibilizarMapaFluxo: fluxoMapa.disponibilizarMapa,
        concluirAcaoPainel,
        aplicarErroNormalizado,
    });

    const erroMapaExibido = computed(() => erroValidacaoMapa.value || erroMapa.value?.mensagem || "");

    function dispensarErroMapa() {
        limparErroMapa(erroMapa);
    }

    onMounted(async () => {
        const sucesso = await carregarContextoInicial();
        if (!sucesso) {
            notificarErro(
                new Error(obterMensagemErroIntegracaoContexto()),
                "Falha ao carregar o contexto do mapa.",
            );
        }
    });

    return {
        carregandoInicial,
        carregandoFluxoMapa,
        codigoSubprocesso,
        unidade,
        subprocesso,
        podeVisualizarImpacto,
        podeApresentarSugestoes,
        podeEditarMapa,
        mostrarValidarMapa,
        mostrarApresentarSugestoes,
        mostrarDisponibilizarMapa,
        mostrarDevolverMapa,
        habilitarApresentarSugestoes,
        habilitarDisponibilizarMapa,
        habilitarEditarMapa,
        habilitarValidarMapa,
        podeVerSugestoes,
        habilitarDevolverMapa,
        acaoPrincipalMapa,
        usarMenuAcoesMapa,
        esconderEdicaoMapaParaAdmin,
        modoSomenteLeitura,
        mostrarAcaoPrincipalMapa,
        habilitarAcaoPrincipalMapa,
        rotuloAcaoPrincipalMapa,
        mostrarExportacaoMapa,
        loadingExportacaoPdf,
        loadingExportacaoCsv,
        impactos,
        atividades,
        competencias,
        mapaSomenteLeitura,
        mostrarModalAceitar,
        mostrarModalValidar,
        mostrarModalDevolucao,
        mostrarModalHistorico,
        mostrarModalDisponibilizar,
        observacaoDevolucao,
        analisesCadastro,
        historicoAnalise,
        sugestoes,
        sugestoesVisualizacao,
        loadingSugestoesVisualizacao,
        loadingSugestoesEnvio,
        mostrarModalSugestoes,
        mostrarModalVerSugestoes,
        mensagemErroDevolucao,
        mensagemErroSugestoes,
        mostrarModalImpacto,
        loadingImpacto,
        loadingCompetencia,
        loadingExclusao,
        loadingDisponibilizacao,
        notificacaoDisponibilizacao,
        fieldErrors,
        erroValidacaoMapa,
        erroValidacaoMapaTick,
        erroMapaExibido,
        competenciaSendoEditada,
        mostrarModalCriarNovaCompetencia,
        mostrarModalExcluirCompetencia,
        competenciaParaExcluir,
        existeCompetenciaSemAtividade,
        aplicarErroNormalizado,
        abrirModalAceitar,
        abrirModalDevolucao,
        abrirModalDisponibilizar,
        verHistorico,
        abrirModalImpacto,
        abrirModalSugestoes,
        abrirModalValidar,
        exportarMapaAtualCsv,
        exportarMapaAtualPdf,
        verSugestoes,
        dispensarErroMapa,
        abrirModalCriarLimpo,
        iniciarEdicaoCompetencia,
        excluirCompetencia,
        removerAtividadeAssociada,
        disponibilizarMapa,
        confirmarAceitacao,
        confirmarDevolucao,
        confirmarExclusaoCompetencia,
        confirmarSugestoes,
        confirmarValidacao,
        fecharModalAceitar,
        fecharModalCriarNovaCompetencia,
        fecharModalDisponibilizar,
        fecharModalHistorico,
        fecharModalImpacto,
        fecharModalVerSugestoes,
        adicionarCompetenciaEFecharModal,
    };
}
