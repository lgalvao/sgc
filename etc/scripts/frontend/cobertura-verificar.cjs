const fs = require("node:fs");
const path = require("node:path");

const CAMINHO_PADRAO_RELATORIO = path.join(__dirname, "../../../frontend/coverage/coverage-final.json");

function imprimirAjuda() {
    console.log(`
Uso:
  node etc/scripts/sgc.js frontend cobertura verificar [opcoes]

Opcoes:
  --min=<n>                Limiar minimo de cobertura por arquivo (padrao: 80)
  --json                   Emite resultado estruturado em JSON
  --coverage-path=<caminho> Sobrescreve o caminho do coverage-final.json
  --help, -h               Exibe esta ajuda
`.trim());
}

function lerOpcoes(args) {
    const minimo = args.find((arg) => arg.startsWith("--min="))?.split("=")[1] ?? "80";
    const caminhoRelatorio = args.find((arg) => arg.startsWith("--coverage-path="))?.split("=")[1] ?? CAMINHO_PADRAO_RELATORIO;
    return {
        ajuda: args.includes("--help") || args.includes("-h"),
        json: args.includes("--json"),
        minimo: Number.parseFloat(minimo),
        caminhoRelatorio
    };
}

function normalizarCaminhoArquivo(caminhoArquivo) {
    let caminhoRelativo = caminhoArquivo.replace(/\\/g, "/");
    if (caminhoRelativo.includes("frontend/src")) {
        caminhoRelativo = caminhoRelativo.substring(caminhoRelativo.indexOf("frontend/src"));
    } else if (caminhoRelativo.includes("src")) {
        caminhoRelativo = caminhoRelativo.substring(caminhoRelativo.indexOf("src"));
    }
    return caminhoRelativo;
}

function deveIgnorarArquivo(caminhoRelativo) {
    return caminhoRelativo.includes("node_modules")
        || caminhoRelativo.includes(".spec.ts")
        || caminhoRelativo.includes(".test.ts");
}

function gerarResumoCobertura(cobertura) {
    const resumo = [];
    for (const caminhoArquivo in cobertura) {
        const coberturaArquivo = cobertura[caminhoArquivo];
        const statementMap = coberturaArquivo.statementMap;
        const statements = coberturaArquivo.s;
        const totalStatements = Object.keys(statementMap).length;
        let statementsCobertos = 0;

        for (const chave in statements) {
            if (statements[chave] > 0) {
                statementsCobertos++;
            }
        }

        const percentual = totalStatements === 0 ? 100 : (statementsCobertos / totalStatements) * 100;
        const caminhoRelativo = normalizarCaminhoArquivo(caminhoArquivo);

        if (!deveIgnorarArquivo(caminhoRelativo)) {
            resumo.push({
                arquivo: caminhoRelativo,
                coberturaPercentual: Number.parseFloat(percentual.toFixed(2)),
                statementsTotal: totalStatements,
                statementsCobertos
            });
        }
    }

    resumo.sort((a, b) => a.coberturaPercentual - b.coberturaPercentual);
    return resumo;
}

function imprimirResumoHumano(caminhoRelatorio, abaixoDoLimite, minimo) {
    console.log(`Lendo relatório de cobertura de: ${caminhoRelatorio}`);
    console.log("\nArquivo | Cobertura % | Statements (Coberto/Total)");
    console.log("--- | --- | ---");

    abaixoDoLimite.forEach((item) => {
        console.log(`${item.arquivo} | ${item.coberturaPercentual}% | ${item.statementsCobertos}/${item.statementsTotal}`);
    });

    console.log(`\nEncontrados ${abaixoDoLimite.length} arquivos com < ${minimo}% de cobertura.`);
}

function imprimirErro(mensagem, json) {
    if (json) {
        console.log(JSON.stringify({status: "erro", mensagem}, null, 2));
        return;
    }

    console.error(`Erro: ${mensagem}`);
}

function main() {
    const args = process.argv.slice(2);
    const opcoes = lerOpcoes(args);

    if (opcoes.ajuda) {
        imprimirAjuda();
        process.exit(0);
    }

    if (Number.isNaN(opcoes.minimo)) {
        imprimirErro("Valor invalido para --min. Use um numero.", opcoes.json);
        process.exit(1);
    }

    if (!fs.existsSync(opcoes.caminhoRelatorio)) {
        imprimirErro(`coverage-final.json não encontrado em ${opcoes.caminhoRelatorio}.`, opcoes.json);
        if (!opcoes.json) {
            console.error("Execute 'npm run coverage:unit' no diretório frontend primeiro.");
        }
        process.exit(1);
    }

    try {
        const conteudo = fs.readFileSync(opcoes.caminhoRelatorio, "utf8");
        const cobertura = JSON.parse(conteudo);
        const resumo = gerarResumoCobertura(cobertura);
        const abaixoDoLimite = resumo.filter((item) => item.coberturaPercentual < opcoes.minimo);
        const resultado = {
            status: "ok",
            caminhoRelatorio: opcoes.caminhoRelatorio,
            minimo: opcoes.minimo,
            totalArquivos: resumo.length,
            totalAbaixoDoLimite: abaixoDoLimite.length,
            arquivosAbaixoDoLimite: abaixoDoLimite
        };

        if (opcoes.json) {
            console.log(JSON.stringify(resultado, null, 2));
            return;
        }

        imprimirResumoHumano(opcoes.caminhoRelatorio, abaixoDoLimite, opcoes.minimo);
    } catch (erro) {
        imprimirErro(`Falha ao processar cobertura: ${erro.message}`, opcoes.json);
        process.exit(1);
    }
}

main();
