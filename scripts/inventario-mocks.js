import fs from 'fs/promises';
import path from 'path';

/**
 * Inventário de mocks em src/mocks/
 * - Lista arquivos JSON
 * - Para cada arquivo que contenha um array de objetos, coleta as chaves observadas e alguns exemplos de valores
 * - Gera relatório em scripts/relatorios/inventario-mocks.json
 *
 * Uso:
 *   node scripts/inventario-mocks.js
 */

const ROOT = process.cwd();
const MOCKS_DIR = path.join(ROOT, 'src', 'mocks');
const REPORTS_DIR = path.join(ROOT, 'scripts', 'relatorios');
const REPORT_FILE = path.join(REPORTS_DIR, 'inventario-mocks.json');

async function garantirDiretorioRelatorios() {
  try {
    await fs.mkdir(REPORTS_DIR, { recursive: true });
  } catch (err) {
    console.error('Erro ao criar diretório de relatórios:', err);
    throw err;
  }
}

function coletarChavesEExemplos(arr, limiteExemplos = 3) {
  const chaves = new Set();
  const exemplos = [];
  for (const item of arr) {
    if (item && typeof item === 'object' && !Array.isArray(item)) {
      Object.keys(item).forEach(k => chaves.add(k));
      if (exemplos.length < limiteExemplos) exemplos.push(item);
    }
  }
  return {
    chaves: Array.from(chaves).sort(),
    exemplos
  };
}

async function inventariarMocks() {
  await garantirDiretorioRelatorios();

  let arquivos;
  try {
    arquivos = await fs.readdir(MOCKS_DIR);
  } catch (err) {
    console.error(`Não foi possível listar ${MOCKS_DIR}:`, err);
    process.exit(1);
  }

  const arquivosJson = arquivos.filter(f => f.endsWith('.json'));
  const relatorio = {
    geradoEm: new Date().toISOString(),
    raiz: MOCKS_DIR,
    arquivos: {}
  };

  for (const arquivo of arquivosJson) {
    const caminho = path.join(MOCKS_DIR, arquivo);
    try {
      const raw = await fs.readFile(caminho, 'utf-8');
      const parsed = JSON.parse(raw);

      if (Array.isArray(parsed)) {
        const { chaves, exemplos } = coletarChavesEExemplos(parsed);
        relatorio.arquivos[arquivo] = {
          tipo: 'array',
          comprimento: parsed.length,
          chaves,
          exemplos
        };
      } else if (parsed && typeof parsed === 'object') {
        const chaves = Object.keys(parsed).sort();
        relatorio.arquivos[arquivo] = {
          tipo: 'objeto',
          chaves,
          exemplo: parsed
        };
      } else {
        relatorio.arquivos[arquivo] = {
          tipo: typeof parsed,
          valor: parsed
        };
      }
    } catch (err) {
      relatorio.arquivos[arquivo] = {
        erro: err.message
      };
    }
  }

  try {
    await fs.writeFile(REPORT_FILE, JSON.stringify(relatorio, null, 2), 'utf-8');
    console.log(`Inventário gerado em: ${REPORT_FILE}`);
  } catch (err) {
    console.error('Erro ao salvar relatório:', err);
    process.exit(1);
  }

  // Sumário no stdout
  console.log('Resumo:');
  for (const [arquivo, info] of Object.entries(relatorio.arquivos)) {
    if (info.erro) {
      console.log(` - ${arquivo}: ERRO (${info.erro})`);
      continue;
    }
    if (info.tipo === 'array') {
      console.log(` - ${arquivo}: array [${info.comprimento}] chaves=${info.chaves.length}`);
    } else if (info.tipo === 'objeto') {
      console.log(` - ${arquivo}: objeto chaves=${info.chaves.length}`);
    } else {
      console.log(` - ${arquivo}: tipo=${info.tipo}`);
    }
  }
}

inventariarMocks().catch(err => {
  console.error('Erro inesperado no inventariar-mocks:', err);
  process.exit(1);
});