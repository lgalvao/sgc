import {readFileSync} from 'node:fs';
import {join, relative} from 'node:path';
import process from 'node:process';

const raizFrontend = process.cwd();

const arquivosAlvo = [
    'src/composables/useDiagnosticoContexto.ts',
    'src/composables/useDiagnosticoPermissoes.ts',
    'src/composables/useDiagnosticoUnidade.ts',
    'src/composables/useEquipeDiagnostico.ts',
    'src/composables/useAutoavaliacaoDiagnostico.ts',
    'src/composables/useConsensoDiagnostico.ts',
    'src/composables/useSituacaoCapacitacaoDiagnostico.ts',
    'src/views/useDiagnosticoUnidadeView.ts',
];

const padroes = [
    {
        nome: 'staleTime bruto',
        regex: /staleTime:\s*(Infinity|\d[\d_]*)/g,
        ignorar: /STALE_TIME_/,
    },
    {
        nome: 'guard inline de codSubprocesso',
        regex: /codSubprocesso\s*>\s*0/g,
        ignorar: /possuiCodSubprocessoValido|habilitarQueryDiagnostico/,
    },
    {
        nome: 'guard inline de sessao',
        regex: /!!perfilStore\.usuarioCodigo/g,
        ignorar: /possuiSessaoDiagnostico|habilitarQueryDiagnostico/,
    },
];

const achados = [];

for (const caminhoRelativo of arquivosAlvo) {
    const caminhoAbsoluto = join(raizFrontend, caminhoRelativo);
    const conteudo = readFileSync(caminhoAbsoluto, 'utf8');
    const linhas = conteudo.split(/\r?\n/);

    linhas.forEach((linha, indice) => {
        for (const padrao of padroes) {
            if (!padrao.regex.test(linha)) {
                padrao.regex.lastIndex = 0;
                continue;
            }
            padrao.regex.lastIndex = 0;

            if (padrao.ignorar.test(linha)) {
                continue;
            }

            achados.push({
                arquivo: relative(raizFrontend, caminhoAbsoluto),
                linha: indice + 1,
                tipo: padrao.nome,
                conteudo: linha.trim(),
            });
        }
    });
}

if (achados.length === 0) {
    console.log('Auditoria de cache do diagnóstico: nenhum guard ou staleTime bruto encontrado.');
    process.exit(0);
}

console.log('Auditoria de cache do diagnóstico:');
for (const achado of achados) {
    console.log(`- ${achado.arquivo}:${achado.linha} [${achado.tipo}] ${achado.conteudo}`);
}

process.exit(1);
