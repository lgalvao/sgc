import {computed, onActivated, onMounted, ref, type Ref} from "vue";
import {useRouter} from "vue-router";
import {useNotification} from "@/composables/useNotification";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {TEXTOS} from "@/constants/textos";
import {
    type AtribuicaoTemporaria,
    atualizarAtribuicaoTemporaria,
    buscarAtribuicoesTemporariasPorUnidade,
    criarAtribuicaoTemporaria,
    removerAtribuicaoTemporaria
} from "@/services/atribuicaoTemporariaService";
import {useInvalidacaoDiagnosticoOrganizacional} from "@/composables/useDiagnosticoOrganizacionalQuery";
import type {Unidade} from "@/types/tipos";
import {obterHojeFormatado} from "@/utils/date";
import {logger} from "@/utils";
import {normalizarErro} from "@/utils/apiError";
import {useUnidadeQuery, useInvalidacaoUnidade} from "@/composables/useUnidadeQuery";

interface EstadoCampos {
    usuarioSelecionado: Ref<string | null>;
    termoUsuario: Ref<string>;
    dataInicio: Ref<string>;
    dataTermino: Ref<string>;
    justificativa: Ref<string>;
}

function criarEstadoCampos(): EstadoCampos {
    return {
        usuarioSelecionado: ref<string | null>(null),
        termoUsuario: ref(""),
        dataInicio: ref(""),
        dataTermino: ref(""),
        justificativa: ref(""),
    };
}

function resetarFormularioAtribuicao(campos: EstadoCampos, resetarValidacao: () => void) {
    campos.usuarioSelecionado.value = null;
    campos.termoUsuario.value = "";
    campos.dataInicio.value = "";
    campos.dataTermino.value = "";
    campos.justificativa.value = "";
    resetarValidacao();
}

function preencherFormularioComAtribuicaoVigente(
    atribuicaoAtual: AtribuicaoTemporaria | null,
    campos: EstadoCampos,
    resetarValidacao: () => void
) {
    if (!atribuicaoAtual) {
        resetarFormularioAtribuicao(campos, resetarValidacao);
        return;
    }

    campos.usuarioSelecionado.value = atribuicaoAtual.usuario.tituloEleitoral;
    campos.termoUsuario.value = atribuicaoAtual.usuario.nome;
    campos.dataInicio.value = atribuicaoAtual.dataInicio.slice(0, 10);
    campos.dataTermino.value = atribuicaoAtual.dataTermino.slice(0, 10);
    campos.justificativa.value = atribuicaoAtual.justificativa;
    resetarValidacao();
}

function criarMensagensErro(campos: EstadoCampos, erroUsuario: ReturnType<typeof ref<string>>, deveExibirErro: (condicao: boolean) => boolean) {
    const mensagemErroUsuario = computed(() => {
        if (erroUsuario.value) return erroUsuario.value;
        if (deveExibirErro(!campos.usuarioSelecionado.value)) {
            return TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO;
        }
        return "";
    });

    const mensagemErroDataInicio = computed(() =>
        deveExibirErro(!campos.dataInicio.value) ? "Informe a data de início." : "",
    );

    const mensagemErroDataTermino = computed(() =>
        deveExibirErro(!campos.dataTermino.value) ? "Informe a data de término." : "",
    );

    const mensagemErroJustificativa = computed(() =>
        deveExibirErro(!campos.justificativa.value.trim()) ? "Informe a justificativa." : "",
    );

    return {
        mensagemErroDataInicio,
        mensagemErroDataTermino,
        mensagemErroJustificativa,
        mensagemErroUsuario,
    };
}

function criarFormularioValido(campos: EstadoCampos) {
    return computed(() => Boolean(
        campos.usuarioSelecionado.value
        && campos.dataInicio.value
        && campos.dataTermino.value
        && campos.justificativa.value.trim()
    ));
}

function criarApresentacao(campos: EstadoCampos, atribuicoes: Ref<AtribuicaoTemporaria[]>) {
    const atribuicaoVigente = computed(() => {
        if (atribuicoes.value.length === 0) return null;
        const agora = new Date();
        const vigente = atribuicoes.value.find((atribuicao: AtribuicaoTemporaria) => {
            const dataInicioAtribuicao = new Date(atribuicao.dataInicio);
            const dataTerminoAtribuicao = new Date(atribuicao.dataTermino);
            return dataInicioAtribuicao <= agora && dataTerminoAtribuicao >= agora;
        });
        return vigente ?? atribuicoes.value[0] ?? null;
    });

    const modoEdicao = computed(() => Boolean(atribuicaoVigente.value));
    const tituloPagina = computed(() => TEXTOS.atribuicaoTemporaria.TITULO);
    const textoBotaoSalvar = computed(() =>
        modoEdicao.value ? TEXTOS.comum.BOTAO_SALVAR : TEXTOS.comum.BOTAO_CRIAR
    );
    const textoBotaoSalvando = computed(() =>
        modoEdicao.value ? TEXTOS.atribuicaoTemporaria.SALVANDO : TEXTOS.atribuicaoTemporaria.CRIANDO
    );
    const dataMinimaTermino = computed(() => campos.dataInicio.value.length > 0 ? campos.dataInicio.value : obterHojeFormatado());

    return {
        atribuicaoVigente,
        dataMinimaTermino,
        modoEdicao,
        textoBotaoSalvar,
        textoBotaoSalvando,
        tituloPagina,
    };
}

function criarFluxoCarga({
    atribuicaoVigente,
    atribuicoes,
    campos,
    carregamentoInicialConcluido,
    carregandoInicial,
    codigoUnidade,
    erroFormulario,
    erroUsuario,
    resetarValidacao,
    unidadeQuery
}: {
    atribuicaoVigente: Ref<AtribuicaoTemporaria | null>;
    atribuicoes: Ref<AtribuicaoTemporaria[]>;
    campos: EstadoCampos;
    carregamentoInicialConcluido: Ref<boolean>;
    carregandoInicial: Ref<boolean>;
    codigoUnidade: number;
    erroFormulario: Ref<string>;
    erroUsuario: Ref<string>;
    resetarValidacao: () => void;
    unidadeQuery: ReturnType<typeof useUnidadeQuery>;
}) {
    async function carregarDados() {
        carregandoInicial.value = true;
        erroUsuario.value = "";

        try {
            await unidadeQuery.refetch();
            atribuicoes.value = await buscarAtribuicoesTemporariasPorUnidade(codigoUnidade);
            preencherFormularioComAtribuicaoVigente(atribuicaoVigente.value, campos, resetarValidacao);
        } catch (error) {
            const mensagemErro = normalizarErro(error).mensagem;
            erroUsuario.value = mensagemErro;
            erroFormulario.value = mensagemErro;
            logger.error(error);
        } finally {
            carregandoInicial.value = false;
        }
    }

    onMounted(async () => {
        await carregarDados();
        carregamentoInicialConcluido.value = true;
    });

    onActivated(async () => {
        if (!carregamentoInicialConcluido.value) {
            return;
        }
        try {
            await unidadeQuery.refresh();
            atribuicoes.value = await buscarAtribuicoesTemporariasPorUnidade(codigoUnidade);
            preencherFormularioComAtribuicaoVigente(atribuicaoVigente.value, campos, resetarValidacao);
        } catch {
            // Ignorar erros de recarga em background
        }
    });

    return {carregarDados};
}

function criarFluxoMutacao({
    atribuicaoVigente,
    atribuicoes,
    campos,
    codigoUnidade,
    erroFormulario,
    erroUsuario,
    formularioValido,
    invalidarDiagnostico,
    carregando,
    modoEdicao,
    mostrarModalRemocao,
    notify,
    resetarValidacao,
    unidade,
    unidadeQuery,
    validarSubmissao,
    focarPrimeiroErroInvalido
}: {
    atribuicaoVigente: Ref<AtribuicaoTemporaria | null>;
    atribuicoes: Ref<AtribuicaoTemporaria[]>;
    campos: EstadoCampos;
    codigoUnidade: number;
    erroFormulario: Ref<string>;
    erroUsuario: Ref<string>;
    formularioValido: Ref<boolean>;
    invalidarDiagnostico: () => void;
    carregando: Ref<boolean>;
    modoEdicao: Ref<boolean>;
    mostrarModalRemocao: Ref<boolean>;
    notify: (mensagem: string, variante?: VarianteAlerta, dispensavel?: boolean) => void;
    resetarValidacao: () => void;
    unidade: Readonly<Ref<Unidade | null>>;
    unidadeQuery: ReturnType<typeof useUnidadeQuery>;
    validarSubmissao: (valido: boolean) => boolean;
    focarPrimeiroErroInvalido: () => Promise<void>;
}) {
    const {invalidarUnidade, invalidarDadosTelaUnidade} = useInvalidacaoUnidade();

    async function atualizarCachesPosMutacao() {
        void invalidarUnidade();
        void invalidarDadosTelaUnidade();
        await unidadeQuery.refetch();
        atribuicoes.value = await buscarAtribuicoesTemporariasPorUnidade(codigoUnidade);
        preencherFormularioComAtribuicaoVigente(atribuicaoVigente.value, campos, resetarValidacao);
    }

    async function salvarAtribuicao() {
        const unidadeAtual = unidade.value;
        if (!unidadeAtual) throw new Error("Invariante violada: unidade não carregada");

        erroUsuario.value = "";
        erroFormulario.value = "";

        if (!validarSubmissao(formularioValido.value)) {
            await focarPrimeiroErroInvalido();
            return;
        }

        const request = {
            tituloEleitoralUsuario: campos.usuarioSelecionado.value!,
            dataInicio: campos.dataInicio.value,
            dataTermino: campos.dataTermino.value,
            justificativa: campos.justificativa.value
        };
        const estavaEmEdicao = modoEdicao.value;

        carregando.value = true;

        try {
            if (atribuicaoVigente.value) {
                await atualizarAtribuicaoTemporaria(unidadeAtual.codigo, atribuicaoVigente.value.codigo, request);
            } else {
                await criarAtribuicaoTemporaria(unidadeAtual.codigo, request);
            }

            invalidarDiagnostico();
            await atualizarCachesPosMutacao();
            notify(
                estavaEmEdicao
                    ? TEXTOS.atribuicaoTemporaria.SUCESSO_ATUALIZACAO
                    : TEXTOS.atribuicaoTemporaria.SUCESSO,
                "success"
            );
        } catch (error) {
            logger.error(error);
            erroFormulario.value = normalizarErro(error).mensagem;
        } finally {
            carregando.value = false;
        }
    }

    async function removerAtribuicao() {
        const unidadeAtual = unidade.value;
        const atribuicaoAtual = atribuicaoVigente.value;
        if (!unidadeAtual || !atribuicaoAtual) {
            return;
        }

        erroFormulario.value = "";
        carregando.value = true;

        try {
            await removerAtribuicaoTemporaria(unidadeAtual.codigo, atribuicaoAtual.codigo);
            invalidarDiagnostico();
            await atualizarCachesPosMutacao();
            mostrarModalRemocao.value = false;
            resetarFormularioAtribuicao(campos, resetarValidacao);
            notify(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO, "success");
        } catch (error) {
            logger.error(error);
            erroFormulario.value = normalizarErro(error).mensagem;
        } finally {
            carregando.value = false;
        }
    }

    return {removerAtribuicao, salvarAtribuicao};
}

export function useAtribuicaoTemporariaTela(codigoUnidade: number) {
    const router = useRouter();
    const {notificacao, notify, clear} = useNotification();

    const unidadeQuery = useUnidadeQuery(codigoUnidade);
    const {invalidarDiagnostico} = useInvalidacaoDiagnosticoOrganizacional();
    const campos = criarEstadoCampos();
    const unidade = computed(() => unidadeQuery.data.value ?? null);
    const atribuicoes = ref<AtribuicaoTemporaria[]>([]);
    const carregando = ref(false);
    const carregandoInicial = ref(true);
    const carregamentoInicialConcluido = ref(false);
    const mostrarModalRemocao = ref(false);
    const erroUsuario = ref("");
    const erroFormulario = ref("");
    const {resetarValidacao, deveExibirErro, validarSubmissao, focarPrimeiroErroInvalido} = useValidacaoFormulario();

    const {atribuicaoVigente, dataMinimaTermino, modoEdicao, textoBotaoSalvar, textoBotaoSalvando, tituloPagina} =
        criarApresentacao(campos, atribuicoes);
    const formularioValido = criarFormularioValido(campos);
    const {
        mensagemErroDataInicio,
        mensagemErroDataTermino,
        mensagemErroJustificativa,
        mensagemErroUsuario
    } = criarMensagensErro(campos, erroUsuario, deveExibirErro);

    criarFluxoCarga({
        atribuicaoVigente,
        atribuicoes,
        campos,
        carregamentoInicialConcluido,
        carregandoInicial,
        codigoUnidade,
        erroFormulario,
        erroUsuario,
        resetarValidacao,
        unidadeQuery
    });
    const {removerAtribuicao, salvarAtribuicao} = criarFluxoMutacao({
        atribuicaoVigente,
        atribuicoes,
        campos,
        codigoUnidade,
        erroFormulario,
        erroUsuario,
        formularioValido,
        invalidarDiagnostico,
        carregando,
        modoEdicao,
        mostrarModalRemocao,
        notify,
        resetarValidacao,
        unidade,
        unidadeQuery,
        validarSubmissao,
        focarPrimeiroErroInvalido
    });

    function irParaUnidade() {
        void router.push(`/unidade/${codigoUnidade}`);
    }

    return {
        atribuicoes,
        atribuicaoVigente,
        carregandoInicial,
        clear,
        dataInicio: campos.dataInicio,
        dataMinimaTermino,
        dataTermino: campos.dataTermino,
        erroFormulario,
        formularioValido,
        irParaUnidade,
        carregando,
        justificativa: campos.justificativa,
        mensagemErroDataInicio,
        mensagemErroDataTermino,
        mensagemErroJustificativa,
        mensagemErroUsuario,
        modoEdicao,
        mostrarModalRemocao,
        notificacao,
        notify: notify as (mensagem: string, variante?: VarianteAlerta, dispensavel?: boolean) => void,
        removerAtribuicao,
        salvarAtribuicao,
        termoUsuario: campos.termoUsuario,
        textoBotaoSalvar,
        textoBotaoSalvando,
        tituloPagina,
        unidade,
        usuarioSelecionado: campos.usuarioSelecionado,
    };
}
