import pc from "picocolors";

function escrever(texto = ""): void {
    process.stdout.write(texto);
}

function escreverLinha(texto = ""): void {
    escrever(`${texto}\n`);
}

function imprimirJson(dados: unknown): void {
    escreverLinha(JSON.stringify(dados, null, 2));
}

function imprimirCabecalho(titulo: string, descricao?: string): void {
    escreverLinha(pc.bold(pc.cyan(titulo)));
    if (descricao) {
        escreverLinha(descricao);
    }
}

function formatarStatus(status: string): string {
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
