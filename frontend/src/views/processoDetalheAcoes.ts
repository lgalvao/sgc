import {useAcoesBlocoProcesso} from "@/views/useAcoesBlocoProcesso";
import {useFinalizacaoProcesso} from "@/views/useFinalizacaoProcesso";
import type {DependenciasProcessoAcoes} from "@/views/processoDetalheTipos";

export function useProcessoAcoes(dependencias: DependenciasProcessoAcoes) {
    return {
        ...useAcoesBlocoProcesso(dependencias),
        ...useFinalizacaoProcesso(dependencias),
    };
}
