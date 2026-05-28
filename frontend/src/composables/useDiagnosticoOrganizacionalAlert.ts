import {computed, ref, type Ref, watch} from "vue";
import {useDiagnosticoOrganizacionalQuery} from "@/composables/useDiagnosticoOrganizacionalQuery";
import {buscarTodasUnidades} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";

export interface UnidadeSemResponsavel {
    codigo: number | null;
    sigla: string;
}

export function useDiagnosticoOrganizacionalAlert(
    unidades: Ref<Unidade[]>,
    mostrarDiagnosticoOrganizacional: Ref<boolean>,
) {
    const query = useDiagnosticoOrganizacionalQuery();

    // Estado local reativo do composable
    const unidadesReferencia = ref<Unidade[]>([]);
    const carregandoUnidadesReferencia = ref(false);
    const unidadesReferenciaCarregadas = ref(false);
    const alertaDiagnosticoDispensado = ref(false);

    const carregandoDiagnosticoOrganizacional = computed(() => query.isLoading.value);
    const erroDiagnosticoOrganizacional = computed(() =>
        query.error.value ? "Não foi possível verificar as pendências organizacionais." : null
    );
    const diagnosticoOrganizacional = computed(() => query.data.value ?? null);
    const gruposDiagnostico = computed(() => {
        const grupos = diagnosticoOrganizacional.value?.grupos;
        return grupos ? grupos : [];
    });

    const resumoDiagnostico = computed(() => {
        const erro = erroDiagnosticoOrganizacional.value;
        if (erro) return erro;
        const resumo = diagnosticoOrganizacional.value?.resumo;
        return resumo ? resumo : "";
    });

    const encontrarGrupoUnidadeSemResponsavel = () => {
        return gruposDiagnostico.value.find((item) => item.tipo === "Unidade sem responsável");
    };

    const unidadesSemResponsavel = computed(() => {
        const grupo = encontrarGrupoUnidadeSemResponsavel();
        if (!grupo) {
            return [];
        }
        return listarSiglasUnidadesSemResponsavel(grupo.ocorrencias)
            .map((sigla) => ({
                sigla,
                codigo: buscarCodigoUnidadePorSigla(unidades.value, sigla)
                    || buscarCodigoUnidadePorSigla(unidadesReferencia.value, sigla),
            }));
    });

    const exibirAlertaDiagnostico = computed(() =>
        mostrarDiagnosticoOrganizacional.value
        && (
            carregandoDiagnosticoOrganizacional.value
            || !!erroDiagnosticoOrganizacional.value
            || diagnosticoOrganizacional.value?.possuiViolacoes === true
        )
        && !alertaDiagnosticoDispensado.value
    );

    // Watcher para carregar unidades de referência de forma reativa sob demanda
    watch(
        [() => mostrarDiagnosticoOrganizacional.value, gruposDiagnostico, unidades],
        async () => {
            if (!mostrarDiagnosticoOrganizacional.value || unidadesReferenciaCarregadas.value || carregandoUnidadesReferencia.value) {
                return;
            }

            const grupo = encontrarGrupoUnidadeSemResponsavel();
            if (!grupo) {
                return;
            }

            const siglas = listarSiglasUnidadesSemResponsavel(grupo.ocorrencias);
            const existemLinksPendentes = siglas.some(
                (sigla) => !buscarCodigoUnidadePorSigla(unidades.value, sigla)
            );
            if (!existemLinksPendentes) {
                return;
            }

            carregandoUnidadesReferencia.value = true;
            try {
                unidadesReferencia.value = await buscarTodasUnidades();
                unidadesReferenciaCarregadas.value = true;
            } finally {
                carregandoUnidadesReferencia.value = false;
            }
        },
        {immediate: true},
    );

    return {
        carregandoDiagnosticoOrganizacional,
        erroDiagnosticoOrganizacional,
        gruposDiagnostico,
        resumoDiagnostico,
        unidadesSemResponsavel,
        exibirAlertaDiagnostico,
        dispensarAlertaDiagnostico: () => {
            alertaDiagnosticoDispensado.value = true;
        },
    };
}

function extrairSiglaUnidade(ocorrencia: string): string | null {
    if (!ocorrencia) {
        return null;
    }
    const correspondencia = new RegExp(/^sigla=([^,]+?)(?:,\s|$)/).exec(ocorrencia);
    return correspondencia?.[1]?.trim() || null;
}

function listarSiglasUnidadesSemResponsavel(ocorrencias: string[]): string[] {
    return ocorrencias
        .map(extrairSiglaUnidade)
        .filter((sigla): sigla is string => Boolean(sigla));
}

function buscarCodigoUnidadePorSigla(unidadesOrigem: Unidade[] | undefined | null, sigla: string): number | null {
    if (!unidadesOrigem) {
        return null;
    }
    for (const unidade of unidadesOrigem) {
        if (unidade.sigla === sigla) {
            return unidade.codigo;
        }

        const codigoFilha = buscarCodigoUnidadePorSigla(unidade.filhas, sigla);
        if (codigoFilha) {
            return codigoFilha;
        }
    }
    return null;
}
