import {formatarDataHoraBR} from "@/utils";

export type VarianteTipoFeedback = "danger" | "primary" | "info" | "success" | "secondary";

const CONFIG_TIPO_FEEDBACK: Record<string, { rotulo: string; variante: VarianteTipoFeedback; icone: string }> = {
  BUG: {rotulo: "Bug", variante: "danger", icone: "bi-bug"},
  SUGESTAO: {rotulo: "Sugestão", variante: "primary", icone: "bi-lightbulb"},
  QUESTAO: {rotulo: "Questão", variante: "info", icone: "bi-question-circle"},
  ELOGIO: {rotulo: "Elogio", variante: "success", icone: "bi-emoji-smile"},
};
const ICONE_TIPO_PADRAO = "bi-chat-left-text";
const CHAVES_IGNORAR_BASE = ["rotaNome", "fusoHorario", "usuarioNome", "usuarioCodigo", "dataHora"] as const;
const MAPA_TRADUCOES_METADADOS: Record<string, string> = {
  tituloPagina: "Título da página",
  idioma: "Idioma",
  userAgent: "Navegador",
};

function normalizarTipoChave(tipo: string): string {
  return tipo.toUpperCase();
}

function obterConfiguracaoTipo(tipo: string) {
  return CONFIG_TIPO_FEEDBACK[normalizarTipoChave(tipo)];
}

function capitalizarTipo(chave: string): string {
  return chave.charAt(0).toUpperCase() + chave.slice(1).toLowerCase();
}

export function formatarTipo(tipo: string): string {
  const chave = normalizarTipoChave(tipo);
  return obterConfiguracaoTipo(chave)?.rotulo ?? capitalizarTipo(chave);
}

export function obterVarianteTipo(tipo: string): VarianteTipoFeedback {
  return obterConfiguracaoTipo(tipo)?.variante ?? "secondary";
}

export function obterIconeTipo(tipo: string): string {
  return obterConfiguracaoTipo(tipo)?.icone ?? ICONE_TIPO_PADRAO;
}

export function resumirNota(nota: string): string {
  if (!nota) return "";
  const doc = new DOMParser().parseFromString(nota, "text/html");
  const textoLimpo = (doc.body.textContent ?? "").replaceAll(/\s+/g, " ").trim();

  if (textoLimpo.length <= 120) {
    return textoLimpo;
  }
  return `${textoLimpo.slice(0, 117)}...`;
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

export function formatarMetadados(json?: string | null): Record<string, unknown> {
  if (!json) return {};
  try {
    const raw = JSON.parse(json);
    const filtrado: Record<string, unknown> = {};
    const chavesIgnorar = new Set<string>(CHAVES_IGNORAR_BASE);

    compactarRota(raw, filtrado, chavesIgnorar);
    compactarAcesso(raw, filtrado, chavesIgnorar);
    compactarResolucao(raw, filtrado, chavesIgnorar);

    Object.entries(raw).forEach(([chave, valor]) => {
      if (chavesIgnorar.has(chave)) return;
      const label = MAPA_TRADUCOES_METADADOS[chave] || (chave.charAt(0).toUpperCase() + chave.slice(1));
      filtrado[label] = formatarValorMetadado(chave, valor);
    });

    return filtrado;
  } catch {
    return {erro: "JSON inválido", valor: json};
  }
}

function compactarRota(raw: Record<string, unknown>, filtrado: Record<string, unknown>, chavesIgnorar: Set<string>) {
  const rotaCaminho = typeof raw.rotaCaminho === "string" ? raw.rotaCaminho : "";
  if (!rotaCaminho) {
    return;
  }

  let rotaCompleta = rotaCaminho;
  const rotaQuery = typeof raw.rotaQuery === "string" ? raw.rotaQuery : "";
  if (rotaQuery && rotaQuery !== "{}" && rotaQuery !== "null") {
    try {
      const query = JSON.parse(rotaQuery);
      const params = new URLSearchParams(query).toString();
      if (params) rotaCompleta += `?${params}`;
    } catch {
      // Se falhar o parse da query, ignora e usa só o caminho.
    }
  }

  filtrado["Rota"] = rotaCompleta;
  chavesIgnorar.add("rotaCaminho");
  chavesIgnorar.add("rotaQuery");
}

function compactarAcesso(raw: Record<string, unknown>, filtrado: Record<string, unknown>, chavesIgnorar: Set<string>) {
  const perfilAtivo = typeof raw.perfilAtivo === "string" ? raw.perfilAtivo : "";
  const unidadeAtiva = typeof raw.unidadeAtiva === "string" ? raw.unidadeAtiva : "";
  if (!perfilAtivo && !unidadeAtiva) {
    return;
  }

  if (perfilAtivo && unidadeAtiva) {
    filtrado["Acesso"] = `${perfilAtivo} - ${unidadeAtiva}`;
  } else if (perfilAtivo) {
    filtrado["Acesso"] = perfilAtivo;
  } else {
    filtrado["Acesso"] = unidadeAtiva;
  }
  chavesIgnorar.add("perfilAtivo");
  chavesIgnorar.add("unidadeAtiva");
}

function compactarResolucao(raw: Record<string, unknown>, filtrado: Record<string, unknown>, chavesIgnorar: Set<string>) {
  const larguraTela = raw.larguraTela;
  const alturaTela = raw.alturaTela;
  if (!larguraTela || !alturaTela) {
    return;
  }

  filtrado["Resolução"] = `${larguraTela}x${alturaTela}`;
  chavesIgnorar.add("larguraTela");
  chavesIgnorar.add("alturaTela");
}

function formatarValorMetadado(chave: string, valor: unknown): unknown {
  if (chave === "userAgent") {
    return extrairNavegadorAmigavel(typeof valor === "string" ? valor : "");
  }

  if (typeof valor === "string" && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(valor)) {
    try {
      return formatarDataHoraBR(valor);
    } catch {
      return valor;
    }
  }

  return valor;
}
