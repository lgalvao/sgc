#!/usr/bin/env node
const path = require('node:path');
const {spawnSync} = require('node:child_process');

const COMANDOS = {
    cobertura: {
        analisar: 'cobertura-analisar.cjs',
        priorizar: 'cobertura-priorizar.cjs',
        complexidade: 'cobertura-complexidade.cjs',
        lacunas: 'cobertura-lacunas.cjs',
        plano: 'cobertura-plano.cjs',
        verificar: 'cobertura-verificar.cjs',
        jornada: 'cobertura-jornada.cjs'
    },
    testes: {
        analisar: 'testes-analisar.cjs',
        priorizar: 'testes-priorizar.cjs',
        'gerar-stub': 'testes-gerar-stub.cjs'
    },
    java: {
        'corrigir-fqn': 'java-corrigir-fqn.cjs',
        'auditar-null': 'java-auditar-null.cjs',
        'instalar-certificados': 'java-instalar-certificados.cjs'
    }
};

function imprimirAjuda() {
    console.log(`Uso: node backend/etc/scripts/sgc.cjs <grupo> <acao> [args...]

Grupos e acoes:
  cobertura analisar              Analise tabular da cobertura
  cobertura priorizar             Ranking resumido das prioridades de cobertura
  cobertura complexidade          Ranking de complexidade pelo CSV do JaCoCo
  cobertura lacunas               Gera JSON estruturado de lacunas
  cobertura plano                 Gera plano detalhado para 100% de cobertura
  cobertura verificar             Consulta cobertura global e por classe
  cobertura jornada               Executa o fluxo completo de cobertura
  testes analisar                 Detecta classes sem testes e gera Markdown/JSON
  testes priorizar                Prioriza backlog de testes a partir de JSON/Markdown
  testes gerar-stub               Gera CoverageTest inicial para uma classe
  java corrigir-fqn               Substitui FQNs por imports em arquivos Java
  java auditar-null               Audita verificacoes de null no backend
  java instalar-certificados      Importa certificados locais no cacerts

Exemplos:
  node backend/etc/scripts/sgc.cjs cobertura verificar --missed
  node backend/etc/scripts/sgc.cjs testes analisar --dir backend --output analise-testes.md --output-json analise-testes.json
  node backend/etc/scripts/sgc.cjs java corrigir-fqn --dry-run`);
}

function main() {
    const [grupo, acao, ...args] = process.argv.slice(2);
    if (!grupo || grupo === '--help' || grupo === '-h' || grupo === 'help') {
        imprimirAjuda();
        process.exit(0);
    }

    const script = COMANDOS[grupo]?.[acao];
    if (!script) {
        console.error(`Comando desconhecido: ${[grupo, acao].filter(Boolean).join(' ')}\n`);
        imprimirAjuda();
        process.exit(1);
    }

    const resultado = spawnSync('node', [path.join(__dirname, script), ...args], {
        stdio: 'inherit'
    });

    if (resultado.error) {
        console.error(`Erro ao executar ${grupo} ${acao}: ${resultado.error.message}`);
        process.exit(1);
    }

    process.exit(resultado.status ?? 0);
}

main();
