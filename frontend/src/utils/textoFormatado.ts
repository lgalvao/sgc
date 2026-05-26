export const LIMITE_PADRAO_TEXTO_FORMATADO = 500;

export function extrairTextoPlanoHtml(html: string | null | undefined): string {
    if (!html) {
        return "";
    }

    const doc = new DOMParser().parseFromString(html, "text/html");
    return (doc.body.textContent || "")
        .replaceAll(/\u00a0/g, " ")
        .replaceAll(/\s+/g, " ")
        .trim();
}

function htmlTemConteudo(html: string | null | undefined): boolean {
    return extrairTextoPlanoHtml(html).length > 0;
}

export function normalizarHtmlEditor(html: string | null | undefined): string {
    if (!htmlTemConteudo(html)) {
        return "";
    }

    return html?.trim() || "";
}

export function obterComprimentoHtmlFormatado(html: string | null | undefined): number {
    return normalizarHtmlEditor(html).length;
}
