import {defineStore} from "pinia";
import {ref, type Ref} from "vue";
import type {Processo} from "@/types/tipos";
import {buscarContextoCompleto as serviceBuscarContextoCompleto} from "@/services/processo";
import {logger} from "@/utils";
import {normalizarErro} from "@/utils/apiError";
import {isErroCanceladoHttp} from "@/axios-setup";

type EstadoProcesso = {
    contextoCompleto: Ref<Processo | null>;
    codProcessoCarregado: Ref<number | null>;
    contextoInvalido: Ref<boolean>;
    carregamentosEmAndamento: Map<number, Promise<Processo>>;
};

function criarEstado(): EstadoProcesso {
    return {
        contextoCompleto: ref<Processo | null>(null),
        codProcessoCarregado: ref<number | null>(null),
        contextoInvalido: ref(false),
        carregamentosEmAndamento: new Map<number, Promise<Processo>>(),
    };
}

function registrarContextoCarregado(estado: EstadoProcesso, codProcesso: number, data: Processo) {
    estado.contextoCompleto.value = data;
    estado.codProcessoCarregado.value = codProcesso;
    estado.contextoInvalido.value = false;
}

function dadosValidos(estado: EstadoProcesso, codProcesso: number): boolean {
    return estado.contextoCompleto.value?.codigo === codProcesso
        && estado.codProcessoCarregado.value === codProcesso
        && !estado.contextoInvalido.value;
}

async function garantirContextoCompleto(estado: EstadoProcesso, codProcesso: number): Promise<Processo | null> {
    if (dadosValidos(estado, codProcesso)) {
        return estado.contextoCompleto.value;
    }

    const carregamentoExistente = estado.carregamentosEmAndamento.get(codProcesso);
    if (carregamentoExistente) {
        return carregamentoExistente;
    }

    const promessaCarregamento = serviceBuscarContextoCompleto(codProcesso);
    estado.carregamentosEmAndamento.set(codProcesso, promessaCarregamento);
    try {
        const data = await promessaCarregamento;
        registrarContextoCarregado(estado, codProcesso, data);
        return data;
    } catch (erro) {
        const erroNormalizado = normalizarErro(erro);
        if (isErroCanceladoHttp(erro) || erroNormalizado.codigo === "REQUEST_CANCELADA") {
            return null;
        }
        logger.error(`Erro ao buscar contexto completo do processo ${codProcesso}:`, erro);
        throw erro;
    } finally {
        estado.carregamentosEmAndamento.delete(codProcesso);
    }
}

/**
 * Dedupe de sessão para contexto completo de processo.
 *
 * O contexto completo carrega situação, permissões e ações disponíveis no momento.
 * Como esses dados mudam ao longo do workflow, não é seguro reutilizar snapshots
 * antigos entre navegações. Mantemos apenas deduplicação de requisições concorrentes
 * e o último payload recebido para consumo imediato da própria view.
 *
 * Estratégia: reaproveitar o último snapshot enquanto ele não foi invalidado.
 * A invalidação é explícita após ações de workflow ou eventos SSE. Isso evita
 * recargas repetidas ao reativar a view sem perder previsibilidade.
 */
export const useProcessoStore = defineStore("processo", () => {
    const estado = criarEstado();

    function limparContextoAtual(): void {
        estado.contextoCompleto.value = null;
        estado.codProcessoCarregado.value = null;
        estado.contextoInvalido.value = false;
    }

    function invalidar(): void {
        estado.contextoInvalido.value = true;
        estado.carregamentosEmAndamento.clear();
    }

    function resetar(): void {
        estado.carregamentosEmAndamento.clear();
        limparContextoAtual();
    }

    return {
        contextoCompleto: estado.contextoCompleto,
        codProcessoCarregado: estado.codProcessoCarregado,
        limparContextoAtual,
        invalidar,
        resetar,
        dadosValidos: (codProcesso: number) => dadosValidos(estado, codProcesso),
        garantirContextoCompleto: (codProcesso: number) => garantirContextoCompleto(estado, codProcesso),
    };
});
