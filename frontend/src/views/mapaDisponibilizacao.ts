import {computed, type ComputedRef, ref, type Ref} from "vue";
import type {Competencia, DisponibilizarMapaRequest, MapaCompleto} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa";
import logger from "@/utils/logger";
import {type ErroNormalizado, normalizarErro} from "@/utils/apiError";

const {mapa: TEXTOS_MAPA} = TEXTOS;

type DependenciasMapaDisponibilizacao = {
    competencias: ComputedRef<Competencia[]>;
    existeCompetenciaSemAtividade: ComputedRef<boolean>;
    atividadesSemCompetencia: ComputedRef<{ codigo: number }[]>;
    mostrarModalDisponibilizar: Ref<boolean>;
    limparErros: () => void;
    obterCodigoSubprocessoObrigatorio: () => number;
    disponibilizarMapaFluxo: (codigoSubprocesso: number, request: DisponibilizarMapaRequest) => Promise<void>;
    concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
    aplicarErroNormalizado: (error: ReturnType<typeof normalizarErro> | null) => void;
};

export interface SincronizarMapaContextoParams {
    mapaAtualizado: MapaCompleto | null | undefined;
    codigoSubprocesso: number | null;
    sincronizarMapa: (codigo: number, mapa: MapaCompleto) => void;
    mapaContextoAtual: Ref<{ detalhes: { codigo: number }; mapa: MapaCompleto } | null>;
}

type ResultadoChecklistDisponibilizacao =
    | {tipo: "pode-disponibilizar"}
    | {tipo: "erro-validacao"; mensagem: string};

type EstadoMapaDisponibilizacao = ReturnType<typeof criarEstado>;

function criarEstado() {
    return {
        notificacaoDisponibilizacao: ref(""),
        erroValidacaoMapa: ref(""),
        erroValidacaoMapaTick: ref(0),
        loadingDisponibilizacao: ref(false),
    };
}

function limparErroValidacaoMapa(estado: EstadoMapaDisponibilizacao, erroMapa?: Ref<ErroNormalizado | null>) {
    estado.erroValidacaoMapa.value = "";
    estado.erroValidacaoMapaTick.value += 1;
    if (erroMapa) {
        erroMapa.value = null;
    }
}

function validarChecklistDisponibilizacao(dependencias: DependenciasMapaDisponibilizacao): ResultadoChecklistDisponibilizacao {
    if (dependencias.competencias.value.length === 0) {
        return {tipo: "erro-validacao", mensagem: TEXTOS_MAPA.ERRO_MAPA_SEM_COMPETENCIAS};
    }
    if (dependencias.existeCompetenciaSemAtividade.value) {
        return {tipo: "erro-validacao", mensagem: TEXTOS_MAPA.ERRO_COMPETENCIA_SEM_ATIVIDADE};
    }
    if (dependencias.atividadesSemCompetencia.value.length > 0) {
        return {tipo: "erro-validacao", mensagem: TEXTOS_MAPA.ERRO_ATIVIDADES_SEM_COMPETENCIA};
    }
    return {tipo: "pode-disponibilizar"};
}

export function useMapaDisponibilizacao(dependencias: DependenciasMapaDisponibilizacao) {
    const {
        notificacaoDisponibilizacao,
        erroValidacaoMapa,
        erroValidacaoMapaTick,
        loadingDisponibilizacao,
    } = criarEstado();

    async function disponibilizarMapa(request: DisponibilizarMapaRequest) {
        if (loadingDisponibilizacao.value) {
            return;
        }

        const codigoSubprocesso = dependencias.obterCodigoSubprocessoObrigatorio();
        loadingDisponibilizacao.value = true;
        try {
            await dependencias.disponibilizarMapaFluxo(codigoSubprocesso, request);
            await dependencias.concluirAcaoPainel(TEXTOS_SUCESSO_MAPA.MAPA_DISPONIBILIZADO, () => {
                dependencias.mostrarModalDisponibilizar.value = false;
                notificacaoDisponibilizacao.value = "";
                dependencias.limparErros();
            });
        } catch (error) {
            logger.error(error);
            dependencias.aplicarErroNormalizado(normalizarErro(error));
        } finally {
            loadingDisponibilizacao.value = false;
        }
    }

    return {
        podeConfirmarDisponibilizacao: computed(() => validarChecklistDisponibilizacao(dependencias).tipo === "pode-disponibilizar"),
        erroValidacaoMapa,
        erroValidacaoMapaTick,
        loadingDisponibilizacao,
        notificacaoDisponibilizacao,
        abrirModalDisponibilizar: () => {
            erroValidacaoMapa.value = "";
            const checklist = validarChecklistDisponibilizacao(dependencias);
            if (checklist.tipo === "erro-validacao") {
                erroValidacaoMapa.value = checklist.mensagem;
                erroValidacaoMapaTick.value += 1;
                return;
            }
            dependencias.mostrarModalDisponibilizar.value = true;
            dependencias.limparErros();
        },
        fecharModalDisponibilizar: () => {
            dependencias.mostrarModalDisponibilizar.value = false;
            notificacaoDisponibilizacao.value = "";
            dependencias.limparErros();
        },
        disponibilizarMapa,
        limparErroMapa: (erroMapa?: Ref<ErroNormalizado | null>) => limparErroValidacaoMapa({
            notificacaoDisponibilizacao,
            erroValidacaoMapa,
            erroValidacaoMapaTick,
            loadingDisponibilizacao,
        }, erroMapa),
        sincronizarMapaContexto: ({
            mapaAtualizado,
            codigoSubprocesso,
            sincronizarMapa,
            mapaContextoAtual,
        }: SincronizarMapaContextoParams) => {
            if (!mapaAtualizado || !codigoSubprocesso) {
                return;
            }
            sincronizarMapa(codigoSubprocesso, mapaAtualizado);
            if (mapaContextoAtual.value?.detalhes.codigo === codigoSubprocesso) {
                mapaContextoAtual.value.mapa = mapaAtualizado;
            }
        },
    };
}
