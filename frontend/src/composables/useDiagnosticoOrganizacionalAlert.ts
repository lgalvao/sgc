import {computed, ref, type Ref, watch} from "vue";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {buscarTodasUnidades} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";

export interface UnidadeSemResponsavel {
    codigo: number | null;
    sigla: string;
}

type EstadoDiagnosticoOrganizacionalAlert = ReturnType<typeof criarEstado>;
type DependenciasDiagnosticoOrganizacionalAlert = {
    unidades: Ref<Unidade[]>;
    mostrarDiagnosticoOrganizacional: Ref<boolean>;
    gruposDiagnostico: Ref<Array<{tipo: string; ocorrencias: string[]}>>;
};

function criarEstado() {
    return {
        unidadesReferencia: ref<Unidade[]>([]),
        carregandoUnidadesReferencia: ref(false),
        unidadesReferenciaCarregadas: ref(false),
        alertaDiagnosticoDispensado: ref(false),
    };
}

function listarSiglasUnidadesSemResponsavel(ocorrencias: string[]): string[] {
    return ocorrencias
        .map(extrairSiglaUnidade)
        .filter((sigla): sigla is string => Boolean(sigla));
}

function encontrarGrupoUnidadeSemResponsavel(grupos: Array<{tipo: string; ocorrencias: string[]}>) {
    return grupos.find((item) => item.tipo === "Unidade sem responsável") ?? null;
}

function carregarUnidadesReferenciaSeNecessario(
    estado: EstadoDiagnosticoOrganizacionalAlert,
    dependencias: DependenciasDiagnosticoOrganizacionalAlert,
) {
    watch(
        [() => dependencias.mostrarDiagnosticoOrganizacional.value, dependencias.gruposDiagnostico, dependencias.unidades],
        async () => {
            if (!dependencias.mostrarDiagnosticoOrganizacional.value || estado.unidadesReferenciaCarregadas.value || estado.carregandoUnidadesReferencia.value) {
                return;
            }

            const grupo = encontrarGrupoUnidadeSemResponsavel(dependencias.gruposDiagnostico.value);
            if (!grupo) {
                return;
            }

            const existemLinksPendentes = listarSiglasUnidadesSemResponsavel(grupo.ocorrencias)
                .some((sigla) => buscarCodigoUnidadePorSigla(dependencias.unidades.value, sigla) === null);
            if (!existemLinksPendentes) {
                return;
            }

            estado.carregandoUnidadesReferencia.value = true;
            try {
                estado.unidadesReferencia.value = await buscarTodasUnidades();
                estado.unidadesReferenciaCarregadas.value = true;
            } finally {
                estado.carregandoUnidadesReferencia.value = false;
            }
        },
        {immediate: true},
    );
}

export function useDiagnosticoOrganizacionalAlert(
    unidades: Ref<Unidade[]>,
    mostrarDiagnosticoOrganizacional: Ref<boolean>,
) {
    const organizacaoStore = useOrganizacaoStore();
    const estado = criarEstado();
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
        const grupo = encontrarGrupoUnidadeSemResponsavel(gruposDiagnostico.value);
        if (!grupo) {
            return [];
        }
        return listarSiglasUnidadesSemResponsavel(grupo.ocorrencias)
            .map((sigla) => ({
                sigla,
                codigo: buscarCodigoUnidadePorSigla(unidades.value, sigla)
                    ?? buscarCodigoUnidadePorSigla(estado.unidadesReferencia.value, sigla),
            }));
    });
    const exibirAlertaDiagnostico = computed(() =>
        mostrarDiagnosticoOrganizacional.value
        && (
            carregandoDiagnosticoOrganizacional.value
            || !!erroDiagnosticoOrganizacional.value
            || diagnosticoOrganizacional.value?.possuiViolacoes === true
        )
        && !estado.alertaDiagnosticoDispensado.value
    );

    watch(
        () => mostrarDiagnosticoOrganizacional.value,
        async (deveExibir) => {
            await organizacaoStore.garantirDiagnostico(deveExibir);
        },
        {immediate: true},
    );
    carregarUnidadesReferenciaSeNecessario(estado, {unidades, mostrarDiagnosticoOrganizacional, gruposDiagnostico});

    return {
        carregandoDiagnosticoOrganizacional,
        erroDiagnosticoOrganizacional,
        diagnosticoOrganizacional,
        gruposDiagnostico,
        resumoDiagnostico,
        unidadesSemResponsavel,
        exibirAlertaDiagnostico,
        dispensarAlertaDiagnostico: () => {
            estado.alertaDiagnosticoDispensado.value = true;
        },
        alertaDiagnosticoDispensado: estado.alertaDiagnosticoDispensado,
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
