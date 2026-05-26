import type {Ref} from "vue";
import type {VarianteAlerta} from "@/composables/useNotification";
import type {AcaoBlocoProcesso, Processo} from "@/types/tipos";

export type ModalAcaoBlocoRef = {
    abrir: () => void;
    fechar: () => void;
    setProcessando: (valor: boolean) => void;
    setErro: (mensagem: string) => void;
};

export type DependenciasProcessoAcoes = {
    codProcesso: number;
    processo: Ref<Processo | null>;
    carregarContextoCompleto: () => Promise<Processo | null | undefined>;
    limparErro: () => void;
    registrarErro: (error: unknown) => string;
    notify: (mensagem: string, variant?: VarianteAlerta) => void;
};

export type DadosAcaoBloco = {
    ids: number[];
    dataLimite?: string;
};

export type EstadoAcoesBlocoProcesso = {
    modalBlocoRef: Ref<ModalAcaoBlocoRef | null>;
    acaoBlocoAtual: Ref<AcaoBlocoProcesso | null>;
    processandoAcaoBloco: Ref<boolean>;
};
