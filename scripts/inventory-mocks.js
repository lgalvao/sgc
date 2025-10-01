import fs from 'fs/promises';
import path from 'path';

/**
 * Inventory de mocks em src/mocks/
 * - Lista arquivos JSON
 * - Para cada arquivo que contenha um array de objetos, coleta as chaves observadas e alguns exemplos de valores
 * - Gera relatório em scripts/reports/mocks-inventory.json
 *
 * Uso:
 *   node scripts/inventory-mocks.js
 */

const ROOT = process.cwd();
const MOCKS_DIR = path.join(ROOT, 'src', 'mocks');
const REPORTS_DIR = path.join(ROOT, 'scripts', 'reports');
const REPORT_FILE = path.join(REPORTS_DIR, 'mocks-inventory.json');

async function ensureReportsDir() {
  try {
    await fs.mkdir(REPORTS_DIR, { recursive: true });
  } catch (err) {
    console.error('Erro ao garantir diretório de reports:', err);
    throw err;
  }
}

function collectKeysAndSamples(arr, sampleLimit = 3) {
  const keys = new Set();
  const samples = [];
  for (const item of arr) {
    if (item && typeof item === 'object' && !Array.isArray(item)) {
      Object.keys(item).forEach(k => keys.add(k));
      if (samples.length < sampleLimit) samples.push(item);
    }
  }
  return {
    keys: Array.from(keys).sort(),
    samples
  };
}

async function inventoryMocks() {
  await ensureReportsDir();

  let files;
  try {
    files = await fs.readdir(MOCKS_DIR);
  } catch (err) {
    console.error(`Não foi possível listar ${MOCKS_DIR}:`, err);
    process.exit(1);
  }

  const jsonFiles = files.filter(f => f.endsWith('.json'));
  const report = {
    generatedAt: new Date().toISOString(),
    root: MOCKS_DIR,
    files: {}
  };

  for (const file of jsonFiles) {
    const fullPath = path.join(MOCKS_DIR, file);
    try {
      const raw = await fs.readFile(fullPath, 'utf-8');
      const parsed = JSON.parse(raw);

      if (Array.isArray(parsed)) {
        const { keys, samples } = collectKeysAndSamples(parsed);
        report.files[file] = {
          type: 'array',
          length: parsed.length,
          keys,
          samples
        };
      } else if (parsed && typeof parsed === 'object') {
        const keys = Object.keys(parsed).sort();
        report.files[file] = {
          type: 'object',
          keys,
          sample: parsed
        };
      } else {
        report.files[file] = {
          type: typeof parsed,
          value: parsed
        };
      }
    } catch (err) {
      report.files[file] = {
        error: err.message
      };
    }
  }

  try {
    await fs.writeFile(REPORT_FILE, JSON.stringify(report, null, 2), 'utf-8');
    console.log(`Inventory gerado em: ${REPORT_FILE}`);
  } catch (err) {
    console.error('Erro ao salvar relatório:', err);
    process.exit(1);
  }

  // Breve sumário no stdout
  console.log('Resumo:');
  for (const [file, info] of Object.entries(report.files)) {
    if (info.error) {
      console.log(` - ${file}: ERRO (${info.error})`);
      continue;
    }
    if (info.type === 'array') {
      console.log(` - ${file}: array [${info.length}] chaves=${info.keys.length}`);
    } else if (info.type === 'object') {
      console.log(` - ${file}: object chaves=${info.keys.length}`);
    } else {
      console.log(` - ${file}: tipo=${info.type}`);
    }
  }
}

inventoryMocks().catch(err => {
  console.error('Erro inesperado no inventory-mocks:', err);
  process.exit(1);
});