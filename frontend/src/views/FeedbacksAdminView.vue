<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <PageHeader :title="TEXTOS.administracao.FEEDBACKS_TITULO">
        <template #actions>
          <BButton
              :disabled="carregando"
              data-testid="btn-feedbacks-atualizar"
              variant="outline-primary"
              @click="carregar"
          >
            <i aria-hidden="true" class="bi bi-arrow-clockwise"></i>
            {{ TEXTOS.administracao.FEEDBACKS_ATUALIZAR }}
          </BButton>
        </template>
      </PageHeader>

      <BAlert v-if="erro" :model-value="true" variant="danger">
        {{ erro }}
      </BAlert>

      <EmptyState
          v-else-if="feedbacks.length === 0"
          :description="TEXTOS.administracao.FEEDBACKS_SEM_REGISTROS"
          :title="TEXTOS.administracao.FEEDBACKS_TITULO"
          icon="bi-chat-left-text"
      />

      <BTable
          v-else
          :fields="campos"
          :items="feedbacks"
          hover
          responsive
          small
          striped
      >
        <template #cell(tipo)="{ item }">
          <BBadge :variant="obterVarianteTipo(item.tipo)">
            <i :class="['bi', iconesTipo[item.tipo.toUpperCase()] || 'bi-chat-left-text', 'me-1']"></i>
            {{ formatarTipo(item.tipo) }}
          </BBadge>
        </template>

        <template #cell(usuarioNome)="{ item }">
          <div class="fw-semibold">{{ item.usuarioNome }}</div>
          <div class="small text-body-secondary">{{ item.usuarioCodigo }}</div>
        </template>

        <template #cell(nota)="{ item }">
          <span class="feedback-resumo">{{ resumirNota(item.nota) }}</span>
        </template>

        <template #cell(enviadoEm)="{ item }">
          {{ formatarDataHoraBR(item.enviadoEm) }}
        </template>

        <template #cell(acoes)="{ item }">
          <BButton
              :aria-label="`Ver detalhes do feedback ${item.codigo}`"
              :data-testid="`btn-feedback-detalhes-${item.codigo}`"
              size="sm"
              variant="outline-secondary"
              @click="abrirDetalhes(item)"
          >
            <i class="bi bi-eye"></i>
          </BButton>
        </template>
      </BTable>
    </template>

    <BModal
        v-model="mostrarDetalhes"
        :title="TEXTOS.administracao.FEEDBACKS_MODAL_TITULO"
        :ok-title="TEXTOS.comum.BOTAO_FECHAR"
        ok-only
        ok-variant="secondary"
        data-testid="modal-detalhes-feedback"
        scrollable
        size="lg"
    >
      <div v-if="feedbackSelecionado" class="p-2">
        <dl class="row mb-0">
          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO }}</dt>
          <dd class="col-sm-9">
            <BBadge :variant="obterVarianteTipo(feedbackSelecionado.tipo)">
              <i :class="['bi', iconesTipo[feedbackSelecionado.tipo.toUpperCase()] || 'bi-chat-left-text', 'me-1']"></i>
              {{ formatarTipo(feedbackSelecionado.tipo) }}
            </BBadge>
          </dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO }}</dt>
          <dd class="col-sm-9">{{ feedbackSelecionado.usuarioNome }} ({{ feedbackSelecionado.usuarioCodigo }})</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA }}</dt>
          <dd class="col-sm-9 text-break"><code>{{ feedbackSelecionado.rota }}</code></dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.ENVIADO_EM }}</dt>
          <dd class="col-sm-9">{{ formatarDataHoraBR(feedbackSelecionado.enviadoEm) }}</dd>

          <dt class="col-sm-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA }}</dt>
          <dd class="col-sm-9">
            <div v-if="feedbackSelecionado.screenshotDisponivel">
              <button class="btn p-0 border-0 d-inline-block" @click="abrirImagemAmpliada(feedbackSelecionado.codigo)">
                <img
                    :src="obterUrlScreenshot(feedbackSelecionado.codigo)"
                    alt="Captura de tela"
                    class="img-fluid border rounded feedback-thumbnail shadow-sm"
                />
              </button>
            </div>
            <span v-else class="text-muted">Não disponível no servidor</span>
          </dd>

          <dt class="col-sm-3 mt-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA }}</dt>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <dd class="col-sm-9 mt-3 text-break shadow-none bg-light p-3 border rounded" v-html="feedbackSelecionado.nota"></dd>

          <dt v-if="feedbackSelecionado.metadataJson" class="col-sm-12 mt-3">{{ TEXTOS.administracao.FEEDBACKS_CAMPOS.METADADOS }}</dt>
          <dd v-if="feedbackSelecionado.metadataJson" class="col-sm-12">
            <div class="table-responsive border rounded mt-2">
              <table class="table table-sm table-striped mb-0">
                <thead class="table-light">
                  <tr>
                    <th class="ps-2">Chave</th>
                    <th>Valor</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(valor, chave) in formatarMetadados(feedbackSelecionado.metadataJson)" :key="chave">
                    <td class="small fw-semibold ps-2" style="width: 30%">{{ chave }}</td>
                    <td class="small text-break">{{ valor }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </dd>
        </dl>
      </div>
    </BModal>

    <!-- Modal de Imagem Ampliada -->
    <BModal
        v-model="mostrarImagemAmpliada"
        :title="TEXTOS.administracao.FEEDBACKS_CAMPOS.CAPTURA"
        :ok-title="TEXTOS.comum.BOTAO_FECHAR"
        ok-only
        ok-variant="secondary"
        centered
        size="xl"
        data-testid="modal-imagem-ampliada"
    >
      <div class="text-center">
        <img :src="urlImagemAmpliada" alt="Captura ampliada" class="img-fluid rounded shadow"/>
      </div>
    </BModal>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import {BAlert, BBadge, BButton, BModal, BTable} from "bootstrap-vue-next";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import {type FeedbackAdmin, listarFeedbacksAdmin, obterUrlScreenshot} from "@/services/feedbackAdminService";
import {formatarDataHoraBR} from "@/utils";
import {normalizarErro} from "@/utils/apiError";

const feedbacks = ref<FeedbackAdmin[]>([]);
const carregando = ref(true);
const erro = ref<string | null>(null);
const mostrarDetalhes = ref(false);
const feedbackSelecionado = ref<FeedbackAdmin | null>(null);
const mostrarImagemAmpliada = ref(false);
const urlImagemAmpliada = ref("");

const campos = [
  {key: "tipo", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.TIPO},
  {key: "usuarioNome", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.USUARIO},
  {key: "nota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.NOTA},
  {key: "rota", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ROTA},
  {key: "enviadoEm", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ENVIADO_EM},
  {key: "acoes", label: TEXTOS.administracao.FEEDBACKS_CAMPOS.ACOES},
];

function formatarTipo(tipo: string): string {
  const t = (tipo || "").toUpperCase();
  const tipos: Record<string, string> = {
    BUG: "Bug",
    SUGESTAO: "Sugestão",
    QUESTAO: "Questão",
    ELOGIO: "Elogio",
  };
  return tipos[t] ?? (t.charAt(0) + t.slice(1).toLowerCase());
}

function obterVarianteTipo(tipo: string): "danger" | "primary" | "info" | "success" | "secondary" {
  const t = (tipo || "").toUpperCase();
  const variantes: Record<string, "danger" | "primary" | "info" | "success" | "secondary"> = {
    BUG: "danger",
    SUGESTAO: "primary",
    QUESTAO: "info",
    ELOGIO: "success",
  };
  return variantes[t] ?? "secondary";
}

const iconesTipo: Record<string, string> = {
  BUG: "bi-bug",
  SUGESTAO: "bi-lightbulb",
  QUESTAO: "bi-question-circle",
  ELOGIO: "bi-emoji-smile",
};

function resumirNota(nota: string): string {
  if (!nota) return "";
  const doc = new DOMParser().parseFromString(nota, "text/html");
  const textoLimpo = (doc.body.textContent || "").replaceAll(/\s+/g, " ").trim();

  if (textoLimpo.length <= 120) {
    return textoLimpo;
  }
  return `${textoLimpo.slice(0, 117)}...`;
}

function abrirDetalhes(item: FeedbackAdmin) {
  feedbackSelecionado.value = item;
  mostrarDetalhes.value = true;
}

function abrirImagemAmpliada(codigo: string) {
  urlImagemAmpliada.value = obterUrlScreenshot(codigo);
  mostrarImagemAmpliada.value = true;
}

function extrairNavegadorAmigavel(ua: string): string {
  if (!ua) return "Desconhecido";
  let navegador = "Outro";
  let so = "Desconhecido";

  if (ua.includes("Firefox")) navegador = "Firefox";
  else if (ua.includes("Edg")) navegador = "Edge";
  else if (ua.includes("Chrome")) navegador = "Chrome";
  else if (ua.includes("Safari")) navegador = "Safari";

  if (ua.includes("Windows NT")) so = "Windows";
  else if (ua.includes("Android")) so = "Android";
  else if (ua.includes("iPhone") || ua.includes("iPad")) so = "iOS";
  else if (ua.includes("Macintosh")) so = "macOS";
  else if (ua.includes("Linux")) so = "Linux";

  return `${navegador} no ${so}`;
}

function formatarMetadados(json?: string | null): Record<string, unknown> {
  if (!json) return {};
  try {
    const raw = JSON.parse(json);
    const filtrado: Record<string, unknown> = {};
    const chavesIgnorar = ["rotaNome", "fusoHorario", "usuarioNome", "usuarioCodigo", "dataHora"];

    const mapaTraducoes: Record<string, string> = {
      tituloPagina: "Título da página",
      idioma: "Idioma",
      userAgent: "Navegador",
    };

    // Compactação de rota e query
    if (raw.rotaCaminho) {
      let rotaCompleta = raw.rotaCaminho;
      if (raw.rotaQuery && raw.rotaQuery !== "{}" && raw.rotaQuery !== "null") {
        try {
          const query = JSON.parse(raw.rotaQuery);
          const params = new URLSearchParams(query).toString();
          if (params) rotaCompleta += `?${params}`;
        } catch {
          // Se falhar o parse da query, ignora e usa só o caminho
        }
      }
      filtrado["Rota"] = rotaCompleta;
      chavesIgnorar.push("rotaCaminho", "rotaQuery");
    }

    // Compactação de perfil e unidade
    if (raw.perfilAtivo || raw.unidadeAtiva) {
      const perfil = raw.perfilAtivo || "-";
      const unidade = raw.unidadeAtiva || "-";
      filtrado["Acesso"] = `${perfil} - ${unidade}`;
      chavesIgnorar.push("perfilAtivo", "unidadeAtiva");
    }

    // Compactação de resolução
    if (raw.larguraTela && raw.alturaTela) {
      filtrado["Resolução"] = `${raw.larguraTela}x${raw.alturaTela}`;
      chavesIgnorar.push("larguraTela", "alturaTela");
    }

    Object.entries(raw).forEach(([chave, valor]) => {
      if (chavesIgnorar.includes(chave)) return;

      const label = mapaTraducoes[chave] || (chave.charAt(0).toUpperCase() + chave.slice(1));

      if (chave === "userAgent") {
        filtrado[label] = extrairNavegadorAmigavel(valor as string);
        return;
      }

      // Formatação de data/hora se o valor parecer um ISO string
      if (typeof valor === "string" && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(valor)) {
        try {
          filtrado[label] = formatarDataHoraBR(valor);
          return;
        } catch {
          // Mantém original se falhar
        }
      }

      filtrado[label] = valor;
    });

    return filtrado;
  } catch {
    return {erro: "JSON inválido", valor: json};
  }
}

async function carregar() {
  carregando.value = true;
  erro.value = null;
  try {
    feedbacks.value = await listarFeedbacksAdmin();
  } catch (error) {
    erro.value = normalizarErro(error).mensagem || TEXTOS.administracao.FEEDBACKS_ERRO_CARREGAR;
  } finally {
    carregando.value = false;
  }
}

onMounted(carregar);
</script>

<style scoped>
.feedback-resumo {
  display: inline-block;
  max-width: 20rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feedback-thumbnail {
  max-height: 200px;
  cursor: zoom-in;
  transition: transform 0.2s;
}

.feedback-thumbnail:hover {
  transform: scale(1.02);
}
</style>
