<template>
  <div v-if="exibir" class="mb-3 pt-2">
    <Alerta
        data-testid="alert-diagnostico-organizacional"
        :dispensavel="true"
        :variante="carregando ? 'info' : 'warning'"
        @dismissed="$emit('dismiss')"
    >
      <div class="d-flex align-items-start gap-2">
        <BSpinner v-if="carregando" class="mt-1" small/>
        <i v-else class="bi bi-exclamation-triangle-fill fs-5 mt-1"></i>
        <div>
          <strong v-if="carregando">Validando informações organizacionais...</strong>
          <template v-else>
            <strong>{{ tituloDiagnostico }}</strong>
            <div v-if="mensagemApoio" class="mt-1">{{ mensagemApoio }}</div>
            <div v-for="grupo in gruposFormatados" :key="grupo.tipo" class="mt-3">
              <strong>{{ grupo.rotulo }}:</strong>
              <ul class="mb-0 mt-1 ps-3">
                <li v-for="(ocorrencia, indice) in grupo.ocorrencias" :key="`${grupo.tipo}-${indice}`">
                  <template v-if="ocorrencia.siglaUnidade && ocorrencia.linkUnidade">
                    <RouterLink
                        :to="ocorrencia.linkUnidade"
                        :data-testid="`link-unidade-sem-responsavel-${indice}`"
                    >
                      {{ ocorrencia.texto }}
                    </RouterLink>
                  </template>
                  <template v-else>
                    {{ ocorrencia.texto }}
                  </template>
                </li>
              </ul>
            </div>
          </template>
        </div>
      </div>
    </Alerta>
  </div>
</template>

<script lang="ts" setup>
import {BSpinner} from "bootstrap-vue-next";
import Alerta from "@/components/comum/Alerta.vue";
import {computed} from "vue";
import {RouterLink} from "vue-router";

interface GrupoDiagnostico {
  tipo: string;
  quantidadeOcorrencias: number;
  ocorrencias?: string[];
}

interface UnidadeSemResponsavel {
  codigo: number | null;
  sigla: string;
}

interface OcorrenciaFormatada {
  texto: string;
  siglaUnidade?: string;
  linkUnidade?: string;
}

interface GrupoDiagnosticoFormatado {
  tipo: string;
  rotulo: string;
  ocorrencias: OcorrenciaFormatada[];
}

const props = defineProps<{
  carregando?: boolean;
  exibir: boolean;
  resumo: string;
  grupos: GrupoDiagnostico[];
  unidadesSemResponsavel?: UnidadeSemResponsavel[];
}>();

defineEmits<{
  dismiss: [];
}>();

const unidadesSemResponsavel = computed(() => props.unidadesSemResponsavel ?? []);
const tituloDiagnostico = computed(() =>
    unidadesSemResponsavel.value.length > 0
        ? "Há inconsistências nos dados organizacionais"
        : "Foram encontradas inconsistências nos dados organizacionais"
);
const mensagemApoio = computed(() =>
    props.resumo && props.resumo !== tituloDiagnostico.value ? props.resumo : ""
);
const gruposFormatados = computed<GrupoDiagnosticoFormatado[]>(() =>
    props.grupos.map((grupo) => ({
      tipo: grupo.tipo,
      rotulo: obterRotuloGrupo(grupo.tipo),
      ocorrencias: formatarOcorrenciasGrupo(grupo.tipo, grupo.ocorrencias ?? [], unidadesSemResponsavel.value),
    }))
);

function obterRotuloGrupo(tipo: string): string {
  switch (tipo) {
    case "Unidade sem responsável":
      return "Unidades sem titular ou responsável";
    case "Usuario sem e-mail na VW_USUARIO":
      return "Usuários sem e-mail";
    default:
      return tipo;
  }
}

function formatarOcorrenciasGrupo(
    tipo: string,
    ocorrencias: string[],
    unidadesSemResponsavelAtuais: UnidadeSemResponsavel[],
): OcorrenciaFormatada[] {
  switch (tipo) {
    case "Unidade sem responsável":
      return unidadesSemResponsavelAtuais.map((unidade) => ({
        texto: unidade.sigla,
        siglaUnidade: unidade.sigla,
        linkUnidade: unidade.codigo !== null ? `/unidade/${unidade.codigo}` : undefined,
      }));
    case "Usuario sem e-mail na VW_USUARIO":
      return ocorrencias.map(formatarUsuarioSemEmail);
    default:
      return ocorrencias.map((ocorrencia) => ({texto: ocorrencia}));
  }
}

function formatarUsuarioSemEmail(ocorrencia: string): OcorrenciaFormatada {
  const nome = extrairCampo(ocorrencia, "nome");
  const sigla = extrairCampo(ocorrencia, "sigla");
  if (nome && sigla) {
    return {texto: `${nome} (${sigla})`};
  }
  return {texto: ocorrencia};
}

function extrairCampo(ocorrencia: string, chave: string): string | null {
  const correspondencia = new RegExp(`${chave}=([^,]+?)(?:,\\s|$)`).exec(ocorrencia);
  return correspondencia?.[1]?.trim() || null;
}
</script>

<style scoped>
a {
  color: #0056b3;
  text-decoration: underline;
}

a:hover {
  color: #004085;
}

:global([data-bs-theme="dark"] [data-testid="alert-diagnostico-organizacional"] a),
:global([data-bs-theme="dark"] [data-testid="alert-diagnostico-organizacional"] a strong),
:global([data-bs-theme="dark"] [data-testid="alert-diagnostico-organizacional"] strong) {
  color: #fde68a;
}

:global([data-bs-theme="dark"] [data-testid="alert-diagnostico-organizacional"] a:hover),
:global([data-bs-theme="dark"] [data-testid="alert-diagnostico-organizacional"] a:hover strong) {
  color: #fef3c7;
}
</style>
