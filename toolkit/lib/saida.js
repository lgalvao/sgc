import pc from "picocolors";

/**
 * @param {string} [texto]
 * @returns {void}
 */
function escrever(texto = "") {
    process.stdout.write(texto);
}

/**
 * @param {string} [texto]
 * @returns {void}
 */
function escreverLinha(texto = "") {
    escrever(`${texto}\n`);
}

/**
 * @param {unknown} dados
 * @returns {void}
 */
function imprimirJson(dados) {
    escreverLinha(JSON.stringify(dados, null, 2));
}

/**
 * @param {string} titulo
 * @param {string} [descricao]
 * @returns {void}
 */
function imprimirCabecalho(titulo, descricao) {
    escreverLinha(pc.bold(pc.cyan(titulo)));
    if (descricao) {
        escreverLinha(descricao);
    }
}

/**
 * @param {string} status
 * @returns {string}
 */
function formatarStatus(status) {
    if (status === "ok") {
        return pc.green("ok");
    }

    if (status === "alerta") {
        return pc.yellow("alerta");
    }

    if (status === "falha") {
        return pc.red("falha");
    }

    return status;
}

export {
    escrever,
    escreverLinha,
    formatarStatus,
    imprimirCabecalho,
    imprimirJson
};
