import pc from "picocolors";

function escrever(texto = "") {
    process.stdout.write(texto);
}

function escreverLinha(texto = "") {
    escrever(`${texto}\n`);
}

function imprimirJson(dados) {
    escreverLinha(JSON.stringify(dados, null, 2));
}

function imprimirCabecalho(titulo, descricao) {
    escreverLinha(pc.bold(pc.cyan(titulo)));
    if (descricao) {
        escreverLinha(descricao);
    }
}

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
