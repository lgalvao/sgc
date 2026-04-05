const fs = require("node:fs");
const path = require("node:path");

const COVERAGE_PATH = path.join(__dirname, "../../../frontend/coverage/coverage-final.json");
const SMELLS_PATH = path.join(__dirname, "../../../etc/qualidade/codigo-cheiros/latest/ultimo-snapshot.json");

function lerArgs(args) {
    const minCobertura = Number.parseFloat(
        args.find((arg) => arg.startsWith("--min-cobertura="))?.split("=")[1] ?? "95"
    );
    const maxBranches = Number.parseInt(
        args.find((arg) => arg.startsWith("--max-branches="))?.split("=")[1] ?? "12",
        10
    );
    const limite = Number.parseInt(
        args.find((arg) => arg.startsWith("--limite="))?.split("=")[1] ?? "20",
        10
    );

    return {
        ajuda: args.includes("--help") || args.includes("-h"),
        json: args.includes("--json"),
        minCobertura,
        maxBranches,
        limite
    };
}

function imprimirAjuda() {
    console.log(`
Uso:
  node etc/scripts/sgc.js frontend cobertura priorizar-defensivos [opcoes]

Opcoes:
  --min-cobertura=<n>     Cobertura minima de statements para considerar suspeito (padrao: 95)
  --max-branches=<n>      Maximo de branches nao cobertos para priorizar arquivo pequeno/residual (padrao: 12)
  --limite=<n>            Quantidade maxima de arquivos exibidos (padrao: 20)
  --json                  Emite resultado estruturado em JSON
  --help, -h              Exibe esta ajuda
`.trim());
}

function normalizarCaminho(caminhoArquivo) {
    let relativo = caminhoArquivo.replace(/\\/g, "/");
    if (relativo.includes("frontend/src")) {
        relativo = relativo.substring(relativo.indexOf("frontend/src"));
    }
    return relativo;
}

function ignorarArquivo(relativo) {
    return relativo.includes("node_modules")
        || relativo.includes(".spec.ts")
        || relativo.includes(".test.ts")
        || relativo.includes(".stories.ts");
}

function lerCobertura() {
    if (!fs.existsSync(COVERAGE_PATH)) {
        throw new Error("coverage-final.json não encontrado. Execute `npm run coverage:unit --prefix frontend`.");
    }

    return JSON.parse(fs.readFileSync(COVERAGE_PATH, "utf8"));
}

function lerSmells() {
    if (!fs.existsSync(SMELLS_PATH)) {
        return null;
    }

    return JSON.parse(fs.readFileSync(SMELLS_PATH, "utf8"));
}

function contarCoberturaMapa(mapa, execucoes) {
    const total = Object.keys(mapa ?? {}).length;
    let cobertos = 0;

    for (const chave of Object.keys(execucoes ?? {})) {
        const valor = execucoes[chave];
        if (Array.isArray(valor)) {
            if (valor.some((item) => item > 0)) {
                cobertos += 1;
            }
            continue;
        }

        if (valor > 0) {
            cobertos += 1;
        }
    }

    return {total, cobertos, naoCobertos: Math.max(total - cobertos, 0)};
}

function gerarLinhasNaoCobertas(statementMap, execucoes) {
    const linhas = new Set();

    for (const chave of Object.keys(execucoes ?? {})) {
        if (execucoes[chave] !== 0) {
            continue;
        }

        const faixa = statementMap?.[chave];
        if (!faixa) {
            continue;
        }

        for (let linha = faixa.start.line; linha <= faixa.end.line; linha += 1) {
            linhas.add(linha);
        }
    }

    return Array.from(linhas).sort((a, b) => a - b);
}

function indexarHotspots(smells) {
    const indice = new Map();
    for (const hotspot of smells?.hotspots ?? []) {
        indice.set(hotspot.arquivo, hotspot);
    }
    return indice;
}

function calcularScore(candidato, hotspot) {
    const bonusCheiro = hotspot ? Object.values(hotspot.categorias ?? {}).reduce((soma, valor) => soma + Number(valor), 0) : 0;
    return Number(
        (
            (candidato.statements.pct * 0.6)
            + (candidato.branches.naoCobertos * 3)
            + (bonusCheiro * 0.8)
        ).toFixed(2)
    );
}

function resumirCheiros(hotspot) {
    if (!hotspot) {
        return [];
    }

    return Object.entries(hotspot.categorias ?? {})
        .sort((a, b) => b[1] - a[1])
        .map(([categoria, valor]) => `${categoria}:${valor}`);
}

function analisarCobertura(coverage, smells, opcoes) {
    const hotspots = indexarHotspots(smells);
    const candidatos = [];

    for (const caminhoArquivo of Object.keys(coverage)) {
        const relativo = normalizarCaminho(caminhoArquivo);
        if (ignorarArquivo(relativo)) {
            continue;
        }

        const item = coverage[caminhoArquivo];
        const statements = contarCoberturaMapa(item.statementMap, item.s);
        const branches = contarCoberturaMapa(item.branchMap, item.b);
        const funcoes = contarCoberturaMapa(item.fnMap, item.f);
        const statementsPct = statements.total === 0 ? 100 : (statements.cobertos / statements.total) * 100;

        if (statementsPct < opcoes.minCobertura) {
            continue;
        }

        if (branches.naoCobertos === 0 || branches.naoCobertos > opcoes.maxBranches) {
            continue;
        }

        const hotspot = hotspots.get(relativo);
        candidatos.push({
            arquivo: relativo,
            statements: {
                ...statements,
                pct: Number(statementsPct.toFixed(2))
            },
            branches,
            funcoes,
            linhasNaoCobertas: gerarLinhasNaoCobertas(item.statementMap, item.s).slice(0, 12),
            cheiros: resumirCheiros(hotspot),
            score: calcularScore({statements: {pct: statementsPct}, branches}, hotspot)
        });
    }

    return candidatos
        .sort((a, b) => b.score - a.score || a.branches.naoCobertos - b.branches.naoCobertos || a.arquivo.localeCompare(b.arquivo))
        .slice(0, opcoes.limite);
}

function imprimirHumano(candidatos, opcoes) {
    console.log(`Cobertura alta com branches residuais: min=${opcoes.minCobertura}% maxBranches=${opcoes.maxBranches}`);
    console.log("");
    console.log("Arquivo | Statements | Branches nao cobertos | Funcoes nao cobertas | Linhas sem cobertura | Cheiros");
    console.log("--- | --- | --- | --- | --- | ---");

    for (const candidato of candidatos) {
        console.log(
            `${candidato.arquivo} | ${candidato.statements.pct}% | ${candidato.branches.naoCobertos}/${candidato.branches.total} | ${candidato.funcoes.naoCobertos}/${candidato.funcoes.total} | ${candidato.linhasNaoCobertas.join(", ")} | ${candidato.cheiros.join(", ")}`
        );
    }
}

function main() {
    const opcoes = lerArgs(process.argv.slice(2));
    if (opcoes.ajuda) {
        imprimirAjuda();
        process.exit(0);
    }

    const coverage = lerCobertura();
    const smells = lerSmells();
    const candidatos = analisarCobertura(coverage, smells, opcoes);
    const resultado = {
        status: "ok",
        coveragePath: COVERAGE_PATH,
        smellsPath: fs.existsSync(SMELLS_PATH) ? SMELLS_PATH : null,
        filtros: {
            minCobertura: opcoes.minCobertura,
            maxBranches: opcoes.maxBranches,
            limite: opcoes.limite
        },
        totalCandidatos: candidatos.length,
        candidatos
    };

    if (opcoes.json) {
        console.log(JSON.stringify(resultado, null, 2));
        return;
    }

    imprimirHumano(candidatos, opcoes);
}

try {
    main();
} catch (erro) {
    console.error(`Erro: ${erro.message}`);
    process.exit(1);
}
