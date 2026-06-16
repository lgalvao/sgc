export type ContextoSessaoSubprocesso = readonly [string, string, string];

function lerStorageSeguro(storage: Storage | undefined, chave: string): string | null {
    if (!storage) {
        return null;
    }

    try {
        return storage.getItem(chave);
    } catch {
        return null;
    }
}

export function criarContextoSessaoSubprocessoAtual(): ContextoSessaoSubprocesso {
    const usuarioCodigo = lerStorageSeguro(globalThis.sessionStorage, "usuarioCodigo") ?? "anon";
    const perfilSelecionado = lerStorageSeguro(globalThis.localStorage, "perfilSelecionado") ?? "sem-perfil";
    const unidadeSelecionada = lerStorageSeguro(globalThis.localStorage, "unidadeSelecionada") ?? "sem-unidade";

    return [usuarioCodigo, perfilSelecionado, unidadeSelecionada] as const;
}

export function serializarContextoSessaoSubprocesso(contextoSessao: ContextoSessaoSubprocesso): string {
    return contextoSessao.join("|");
}
