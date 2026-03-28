#!/usr/bin/env node
const path = require('node:path');
const {spawnSync} = require('node:child_process');

const COMANDOS = {
    'cobertura:detalhar': 'cobertura-analisar.cjs',
    'cobertura:prioridades': 'cobertura-priorizar.cjs',
    'cobertura:complexidade': 'cobertura-complexidade.cjs',
    'cobertura:lacunas': 'cobertura-lacunas.cjs',
    'cobertura:plano': 'cobertura-plano.cjs',
    'cobertura:verificar': 'cobertura-verificar.cjs',
    'cobertura:jornada': 'cobertura-jornada.cjs',
    'testes:analisar': 'testes-analisar.cjs',
    'testes:priorizar': 'testes-priorizar.cjs',
    'testes:stub': 'testes-gerar-stub.cjs',
    'java:corrigir-fqn': 'java-corrigir-fqn.cjs',
    'java:auditar-null': 'java-auditar-null.cjs',
    'java:instalar-certificados': 'java-instalar-certificados.cjs'
};

function imprimirAjuda() {
    console.log(`Uso: node backend/etc/scripts/sgc.cjs <comando> [args...]

Comandos:
  cobertura:detalhar      Analise tabular da cobertura
  cobertura:prioridades   Ranking resumido das prioridades de cobertura
  cobertura:complexidade  Ranking de complexidade pelo CSV do JaCoCo
  cobertura:lacunas       Gera JSON estruturado de lacunas
  cobertura:plano         Gera plano detalhado para 100% de cobertura
  cobertura:verificar     Consulta cobertura global e por classe
  cobertura:jornada       Executa o fluxo completo de cobertura
  testes:analisar         Detecta classes sem testes e gera Markdown/JSON
  testes:priorizar        Prioriza backlog de testes a partir de JSON/Markdown
  testes:stub             Gera CoverageTest inicial para uma classe
  java:corrigir-fqn       Substitui FQNs por imports em arquivos Java
  java:auditar-null       Audita verificacoes de null no backend
  java:instalar-certificados Importa certificados locais no cacerts

Exemplos:
  node backend/etc/scripts/sgc.cjs cobertura:verificar --missed
  node backend/etc/scripts/sgc.cjs testes:analisar --dir backend --output analise-testes.md --output-json analise-testes.json
  node backend/etc/scripts/sgc.cjs java:corrigir-fqn --dry-run`);
}

function main() {
    const [comando, ...args] = process.argv.slice(2);
    if (!comando || comando === '--help' || comando === '-h' || comando === 'help') {
        imprimirAjuda();
        process.exit(0);
    }

    const script = COMANDOS[comando];
    if (!script) {
        console.error(`Comando desconhecido: ${comando}\n`);
        imprimirAjuda();
        process.exit(1);
    }

    const resultado = spawnSync('node', [path.join(__dirname, script), ...args], {
        stdio: 'inherit'
    });

    if (resultado.error) {
        console.error(`Erro ao executar ${comando}: ${resultado.error.message}`);
        process.exit(1);
    }

    process.exit(resultado.status ?? 0);
}

main();
