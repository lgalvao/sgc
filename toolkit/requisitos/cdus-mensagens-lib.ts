type MapaContagem = Record<string, number>;

function extrairDescricoes(texto: string): string[] {
    return [...texto.matchAll(/`Descrição`:\s*(?:'([^'\n]+)'|"([^"\n]+)")/g)]
        .map(match => match[1] ?? match[2]);
}

function extrairAssuntos(texto: string): string[] {
    return [...texto.matchAll(/^\s*Assunto:\s*(.+)$/gm)].map(match => match[1].trim());
}

function extrairMensagens(texto: string): string[] {
    return [...texto.matchAll(/mensagem\s+"([^"\n]+)"/g)].map(match => match[1]);
}

function extrairToasts(texto: string): string[] {
    const toasts: string[] = [];
    for (const match of texto.matchAll(/\*toast\*\s+"([^"\n]+)"|\*toast\*\s+`([^`\n]+)`/g)) {
        toasts.push(match[1] ?? match[2]);
    }
    return toasts;
}

function acumularMapa(mapa: MapaContagem, chave: string): void {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa: MapaContagem): MapaContagem {
    return Object.fromEntries(
        Object.entries(mapa).toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

export {
    acumularMapa,
    extrairAssuntos,
    extrairDescricoes,
    extrairMensagens,
    extrairToasts,
    ordenarMapa
};
