import type {Ref} from "vue";
import type {
    ContextoCadastroAtividadesSubprocesso,
    ContextoEdicaoSubprocesso,
    PermissoesSubprocesso,
    SituacaoSubprocesso,
} from "@/types/tipos";
import {criarContextoSessaoSubprocessoAtual, serializarContextoSessaoSubprocesso} from "@/stores/subprocesso/contextoSessao";

export type ContextoSubprocesso = ContextoEdicaoSubprocesso | ContextoCadastroAtividadesSubprocesso;

export type AtualizacaoStatusLocal = {
    codigo: number;
    situacao: SituacaoSubprocesso;
    permissoes?: PermissoesSubprocesso;
};

export type RegistrarContextoParams<T extends ContextoSubprocesso> = {
    contextoRef: Ref<T | null>;
    contextoInvalidoRef: Ref<boolean>;
    contextoSessaoRef: Ref<string | null>;
    contexto: T;
    limparErroIntegracao: () => void;
};

export function registrarContexto<T extends ContextoSubprocesso>({
    contextoRef,
    contextoInvalidoRef,
    contextoSessaoRef,
    contexto,
    limparErroIntegracao,
}: RegistrarContextoParams<T>): void {
    contextoRef.value = contexto;
    contextoInvalidoRef.value = false;
    contextoSessaoRef.value = serializarContextoSessaoSubprocesso(criarContextoSessaoSubprocessoAtual());
    limparErroIntegracao();
}

export function atualizarDetalhesContexto<T extends ContextoSubprocesso>(
    contextoRef: Ref<T | null>,
    contextoInvalidoRef: Ref<boolean>,
    status: AtualizacaoStatusLocal,
): void {
    if (contextoRef.value?.detalhes.codigo !== status.codigo) {
        return;
    }

    contextoRef.value.detalhes.situacao = status.situacao;
    contextoInvalidoRef.value = false;
    if (status.permissoes) {
        contextoRef.value.detalhes.permissoes = status.permissoes;
    }
}

export function dadosValidos<T extends ContextoSubprocesso>(
    contextoRef: Ref<T | null>,
    contextoInvalidoRef: Ref<boolean>,
    contextoSessaoRef: Ref<string | null>,
    codigoSubprocesso: number,
): boolean {
    return contextoRef.value?.detalhes.codigo === codigoSubprocesso
        && !contextoInvalidoRef.value
        && contextoSessaoRef.value === serializarContextoSessaoSubprocesso(criarContextoSessaoSubprocessoAtual());
}
