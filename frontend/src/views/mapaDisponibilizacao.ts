import {computed, type ComputedRef, ref, type Ref} from "vue";
import type {Competencia, DisponibilizarMapaRequest, MapaCompleto} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_MAPA} from "@/constants/textos-mapa";
import logger from "@/utils/logger";
import {normalizarErro} from "@/utils/apiError";

const {mapa: TEXTOS_MAPA} = TEXTOS;

type DependenciasMapaDisponibilizacao = {
    competencias: ComputedRef<Competencia[]>;
    existeCompetenciaSemAtividade: ComputedRef<boolean>;
    atividadesSemCompetencia: ComputedRef<{ codigo: number }[]>;
    mostrarModalDisponibilizar: Ref<boolean>;
    limparErros: () => void;
    executarComSubprocesso: (callback: (codigoSubprocesso: number) => Promise<void>) => Promise<void>;
    disponibilizarMapaFluxo: (codigoSubprocesso: number, request: DisponibilizarMapaRequest) => Promise<void>;
    concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
    aplicarErroNormalizado: (error: ReturnType<typeof normalizarErro> | null) => void;
};

export interface SincronizarMapaContextoParams {
    mapaAtualizado: MapaCompleto | null | undefined;
    codigoSubprocesso: number | null;
    definirMapaCompleto: (codigo: number, mapa: MapaCompleto) => void;
    mapaContextoAtual: Ref<{ detalhes: { codigo: number }; mapa: MapaCompleto } | null>;
}

type EstadoMapaDisponibilizacao = ReturnType<typeof criarEstado>;

type ResultadoChecklistDisponibilizacao =
    | {tipo: "pode-disponibilizar"}
    | {tipo: "erro-validacao"; mensagem: string};

function criarEstado() {
    return {
        notificacaoDisponibilizacao: ref(""),
        erroValidacaoMapa: ref(""),
        erroValidacaoMapaTick: ref(0),
        loadingDisponibilizacao: ref(false),
    };
}

function limparErroValidacaoMapa(estado: EstadoMapaDisponibilizacao, erroMapa?: Ref<string | null>) {
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

async function disponibilizarMapa(
    dependencias: DependenciasMapaDisponibilizacao,
    estado: EstadoMapaDisponibilizacao,
    request: DisponibilizarMapaRequest
) {
    if (estado.loadingDisponibilizacao.value) {
        return;
    }

    await dependencias.executarComSubprocesso(async (codigoSubprocesso) => {
        estado.loadingDisponibilizacao.value = true;
        try {
            await dependencias.disponibilizarMapaFluxo(codigoSubprocesso, request);
            await dependencias.concluirAcaoPainel(TEXTOS_SUCESSO_MAPA.MAPA_DISPONIBILIZADO, () => {
                dependencias.mostrarModalDisponibilizar.value = false;
                estado.notificacaoDisponibilizacao.value = "";
                dependencias.limparErros();
            });
        } catch (error) {
            logger.error(error);
            dependencias.aplicarErroNormalizado(normalizarErro(error));
        } finally {
            estado.loadingDisponibilizacao.value = false;
        }
    });
}

export function useMapaDisponibilizacao(dependencias: DependenciasMapaDisponibilizacao) {
    const estado = criarEstado();

    return {
        podeConfirmarDisponibilizacao: computed(() => validarChecklistDisponibilizacao(dependencias).tipo === "pode-disponibilizar"),
        erroValidacaoMapa: estado.erroValidacaoMapa,
        erroValidacaoMapaTick: estado.erroValidacaoMapaTick,
        loadingDisponibilizacao: estado.loadingDisponibilizacao,
        notificacaoDisponibilizacao: estado.notificacaoDisponibilizacao,
        abrirModalDisponibilizar: () => {
            estado.erroValidacaoMapa.value = "";
            const checklist = validarChecklistDisponibilizacao(dependencias);
            if (checklist.tipo === "erro-validacao") {
                estado.erroValidacaoMapa.value = checklist.mensagem;
                estado.erroValidacaoMapaTick.value += 1;
                return;
            }
            dependencias.mostrarModalDisponibilizar.value = true;
            dependencias.limparErros();
        },
        fecharModalDisponibilizar: () => {
            dependencias.mostrarModalDisponibilizar.value = false;
            estado.notificacaoDisponibilizacao.value = "";
            dependencias.limparErros();
        },
        disponibilizarMapa: (request: DisponibilizarMapaRequest) => disponibilizarMapa(dependencias, estado, request),
        limparErroMapa: (erroMapa?: Ref<string | null>) => limparErroValidacaoMapa(estado, erroMapa),
        sincronizarMapaContexto: ({
            mapaAtualizado,
            codigoSubprocesso,
            definirMapaCompleto,
            mapaContextoAtual,
        }: SincronizarMapaContextoParams) => {
            if (!mapaAtualizado || !codigoSubprocesso) {
                return;
            }
            definirMapaCompleto(codigoSubprocesso, mapaAtualizado);
            if (mapaContextoAtual.value?.detalhes.codigo === codigoSubprocesso) {
                mapaContextoAtual.value.mapa = mapaAtualizado;
            }
        },
    };
}
