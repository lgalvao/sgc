import {computed, ref, watch, type Ref} from "vue";
import {useOrganizacaoStore} from "@/stores/organizacao";
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
    const organizacaoStore = useOrganizacaoStore();
    const unidadesReferencia = ref<Unidade[]>([]);
    const carregandoUnidadesReferencia = ref(false);
    const unidadesReferenciaCarregadas = ref(false);
    const carregandoDiagnosticoOrganizacional = computed(() => organizacaoStore.carregando);
    const erroDiagnosticoOrganizacional = computed(() => organizacaoStore.erroDiagnostico);
    const diagnosticoOrganizacional = computed(() => organizacaoStore.diagnostico);
    const gruposDiagnostico = computed(() => diagnosticoOrganizacional.value?.grupos ?? []);
    const resumoDiagnostico = computed(() =>
        erroDiagnosticoOrganizacional.value
        ?? diagnosticoOrganizacional.value?.resumo
        ?? ""
    );
    const unidadesSemResponsavel = computed(() => {
        const grupo = diagnosticoOrganizacional.value?.grupos.find((item) => item.tipo === "Unidade sem responsável");
        if (!grupo || grupo.ocorrencias.length === 0) {
            return [];
        }

        return grupo.ocorrencias
            .map(extrairSiglaUnidade)
            .filter((sigla): sigla is string => Boolean(sigla))
            .map((sigla) => ({
                sigla,
                codigo: buscarCodigoUnidadePorSigla(unidades.value, sigla)
                    ?? buscarCodigoUnidadePorSigla(unidadesReferencia.value, sigla),
            }));
    });
    const alertaDiagnosticoDispensado = ref(false);
    const exibirAlertaDiagnostico = computed(() =>
        mostrarDiagnosticoOrganizacional.value
        && (
            carregandoDiagnosticoOrganizacional.value
            || !!erroDiagnosticoOrganizacional.value
            || diagnosticoOrganizacional.value?.possuiViolacoes === true
        )
        && !alertaDiagnosticoDispensado.value
    );

    function dispensarAlertaDiagnostico() {
        alertaDiagnosticoDispensado.value = true;
    }

    watch(
        [() => mostrarDiagnosticoOrganizacional.value, gruposDiagnostico, unidades],
        async () => {
            if (!mostrarDiagnosticoOrganizacional.value || unidadesReferenciaCarregadas.value || carregandoUnidadesReferencia.value) {
                return;
            }

            const grupo = gruposDiagnostico.value.find((item) => item.tipo === "Unidade sem responsável");
            if (!grupo || grupo.ocorrencias.length === 0) {
                return;
            }

            const existemLinksPendentes = grupo.ocorrencias
                .map(extrairSiglaUnidade)
                .filter((sigla): sigla is string => Boolean(sigla))
                .some((sigla) => buscarCodigoUnidadePorSigla(unidades.value, sigla) === null);

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
        diagnosticoOrganizacional,
        gruposDiagnostico,
        resumoDiagnostico,
        unidadesSemResponsavel,
        exibirAlertaDiagnostico,
        dispensarAlertaDiagnostico,
        alertaDiagnosticoDispensado,
    };
}

function extrairSiglaUnidade(ocorrencia: string): string | null {
    if (!ocorrencia) {
        return null;
    }

    const correspondencia = new RegExp(/^sigla=([^,]+?)(?:,\s|$)/).exec(ocorrencia);
    return correspondencia?.[1]?.trim() || null;
}

function buscarCodigoUnidadePorSigla(unidadesOrigem: Unidade[], sigla: string): number | null {
    for (const unidade of unidadesOrigem) {
        if (unidade.sigla === sigla) {
            return unidade.codigo;
        }

        const codigoFilha = buscarCodigoUnidadePorSigla(unidade.filhas ?? [], sigla);
        if (codigoFilha !== null) {
            return codigoFilha;
        }
    }

    return null;
}
