/**
 * Script para transformar os arquivos em src/mocks/ para o formato esperado pelo frontend.
 *
 * Regras aplicadas (inicia com um conjunto seguro e extensível):
 * - Renomear chaves comuns:
 *   - "codigo" -> "id"
 *   - "processo_codigo" -> "idProcesso"
 *   - "atividade_codigo" -> "idAtividade"
 *   - "competencia_codigo" -> "idCompetencia"
 *   - "unidade_codigo" -> "unidadeCodigo" (mantém o código; opcionalmente converte para sigla se possível)
 *   - "usuario_titulo" -> "usuarioTitulo"
 *   - "data_hora" / "dataHora" / "data_hora_leitura" -> padroniza para ISO em "dataHora" (string ISO)
 *   - campos em snake_case -> camelCase (aplica transformação simples)
 *
 * - Conversão de datas:
 *   - Se detectar formato DD/MM/YYYY tenta converter para ISO (YYYY-MM-DDTHH:mm:ss.sssZ) assumindo hora 00:00
 *   - Se já for ISO mantém
 *
 * Modos de execução:
 * - Dry-run (default): mostra relatório das mudanças propostas sem sobrescrever
 * - Aplicar: usar flag --apply para aplicar transformações
 *
 * Uso:
 *   node scripts/transformar-mocks-para-formato-frontend.js --dry
 *   node scripts/transformar-mocks-para-formato-frontend.js --apply
 *
 * Observação:
 * - Antes de aplicar, o script faz backup de src/mocks/ em src/mocks/backups/<timestamp>/
 * - Todos os logs e resultados são exibidos no console e também gravados em scripts/relatorios/transformacao-mocks.json
 *
 * Este script deve ser utilizado apenas como auxílio; revise o resultado antes de considerar as mudanças definitivas.
 */

import fs from 'fs/promises';
import path from 'path';

const ROOT = process.cwd();
const MOCKS_DIR = path.join(ROOT, 'src', 'mocks');
const BACKUP_DIR_ROOT = path.join(MOCKS_DIR, 'backups');
const REPORTS_DIR = path.join(ROOT, 'scripts', 'relatorios');
const REPORT_FILE = path.join(REPORTS_DIR, 'transformacao-mocks.json');

const args = process.argv.slice(2);
const APPLY = args.includes('--apply');

function toCamelCase(s) {
    return s.replace(/[_-][a-z]/g, (m) => m[1].toUpperCase());
}

function renameKeys(obj, keyMap) {
    if (!obj || typeof obj !== 'object' || Array.isArray(obj)) return obj;
    const res = {};
    for (const [k, v] of Object.entries(obj)) {
        const novoK = keyMap[k] || toCamelCase(k);
        res[novoK] = v;
    }
    return res;
}

function isDateString(value) {
    if (typeof value !== 'string') return false;
    // ISO-like detection
    const isoRe = /^\d{4}-\d{2}-\d{2}T/;
    if (isoRe.test(value)) return true;
    // DD/MM/YYYY
    const ddmmRe = /^\d{2}\/\d{2}\/\d{4}$/;
    return ddmmRe.test(value);
}

function convertDateToISO(value) {
    if (!value || typeof value !== 'string') return value;
    const isoRe = /^\d{4}-\d{2}-\d{2}T/;
    if (isoRe.test(value)) return value;
    const ddmmRe = /^(\d{2})\/(\d{2})\/(\d{4})$/;
    const m = ddmmRe.exec(value);
    if (m) {
        const day = Number(m[1]);
        const month = Number(m[2]);
        const year = Number(m[3]);
        const date = new Date(Date.UTC(year, month - 1, day));
        if (!isNaN(date.getTime())) return date.toISOString();
        return value;
    }
    return value;
}

async function garantirRelatorios() {
    try {
        await fs.mkdir(REPORTS_DIR, {recursive: true});
    } catch (err) {
        console.error('Erro criando diretório de relatórios:', err);
    }
}

async function criarBackup(srcFiles) {
    const ts = new Date().toISOString().replace(/[:.]/g, '-');
    const backupDir = path.join(BACKUP_DIR_ROOT, ts);
    await fs.mkdir(backupDir, {recursive: true});
    for (const f of srcFiles) {
        const dest = path.join(backupDir, path.basename(f));
        await fs.copyFile(f, dest);
    }
    return backupDir;
}

function mapObject(obj, keyMap, unidadesLookup) {
    if (Array.isArray(obj)) {
        return obj.map(item => mapObject(item, keyMap, unidadesLookup));
    }
    if (!obj || typeof obj !== 'object') return obj;

    let renamed = renameKeys(obj, keyMap);

    // Conversão recursiva
    for (const k of Object.keys(renamed)) {
        const v = renamed[k];
        if (v && typeof v === 'object') {
            renamed[k] = mapObject(v, keyMap, unidadesLookup);
        } else {
            // Datas
            if (isDateString(v)) {
                renamed[k] = convertDateToISO(v);
            }
            // Transformar unidadeCodigo para sigla se possível
            if ((k === 'unidadeCodigo' || k === 'unidade_codigo' || k === 'unidade') && unidadesLookup) {
                const str = String(renamed[k]);
                renamed[k] = unidadesLookup[str] || unidadesLookup[Number(str)] || renamed[k];
            }
        }
    }

    return renamed;
}

async function carregarUnidadesLookup() {
    try {
        const raw = await fs.readFile(path.join(MOCKS_DIR, 'unidades.json'), 'utf-8');
        const parsed = JSON.parse(raw);
        const lookup = {};
        if (Array.isArray(parsed)) {
            for (const u of parsed) {
                // Possíveis chaves: codigo, id, sigla
                const codigo = u.codigo ?? u.id ?? u.codigo_unidade ?? null;
                const sigla = u.sigla ?? u.sigla_unidade ?? null;
                if (codigo != null && sigla) lookup[codigo] = sigla;
                if (u.sigla) {
                    // também indexar por sigla direta
                    lookup[u.sigla] = u.sigla;
                }
                if (u.id) lookup[u.id] = u.sigla ?? u.sigla_unidade ?? u.sigla;
            }
        }
        return lookup;
    } catch (err) {
        // Não é crítico se não existir; retornamos lookup vazio
        return {};
    }
}

async function transformar() {
    await garantirRelatorios();

    let arquivos;
    try {
        arquivos = await fs.readdir(MOCKS_DIR);
    } catch (err) {
        console.error('Erro listando mocks:', err);
        process.exit(1);
    }

    const jsonFiles = arquivos.filter(f => f.endsWith('.json') && f !== 'backups' && !f.startsWith('backup'));
    const fullPaths = jsonFiles.map(f => path.join(MOCKS_DIR, f));
    const unidadesLookup = await carregarUnidadesLookup();

    const keyMap = {
        'codigo': 'id',
        'processo_codigo': 'idProcesso',
        'atividade_codigo': 'idAtividade',
        'competencia_codigo': 'idCompetencia',
        'usuario_titulo': 'usuarioTitulo',
        'data_hora': 'dataHora',
        'data_hora_leitura': 'dataHoraLeitura',
        'data_inicio': 'dataInicio',
        'data_termino': 'dataTermino',
        'unidade_origem_codigo': 'unidadeOrigem',
        'unidade_destino_codigo': 'unidadeDestino'
    };

    const report = {
        geradoEm: new Date().toISOString(),
        aplicou: APPLY,
        arquivos: {}
    };

    if (APPLY) {
        // backup
        try {
            const backupDir = await criarBackup(fullPaths);
            report.backupDir = path.relative(ROOT, backupDir);
            console.log('Backup criado em:', backupDir);
        } catch (err) {
            console.error('Erro criando backup:', err);
            process.exit(1);
        }
    }

    for (const file of jsonFiles) {
        const full = path.join(MOCKS_DIR, file);
        try {
            const raw = await fs.readFile(full, 'utf-8');
            const parsed = JSON.parse(raw);

            const transformed = mapObject(parsed, keyMap, unidadesLookup);

            // comparar superficially para relatar mudanças
            const changed = JSON.stringify(parsed, null, 2) !== JSON.stringify(transformed, null, 2);

            report.arquivos[file] = {
                mudou: changed,
                tipo: Array.isArray(parsed) ? 'array' : typeof parsed,
                exemplosAntes: Array.isArray(parsed) ? parsed.slice(0, 2) : parsed,
                exemplosDepois: Array.isArray(transformed) ? transformed.slice(0, 2) : transformed
            };

            if (APPLY && changed) {
                await fs.writeFile(full, JSON.stringify(transformed, null, 2), 'utf-8');
                console.log(`Arquivo transformado: ${file}`);
            } else if (!APPLY && changed) {
                console.log(`(dry-run) Mudanças detectadas em: ${file}`);
            } else {
                console.log(`Sem mudanças necessárias em: ${file}`);
            }

        } catch (err) {
            console.error(`Erro processando ${file}:`, err.message);
            report.arquivos[file] = {erro: err.message};
        }
    }

    try {
        await fs.writeFile(REPORT_FILE, JSON.stringify(report, null, 2), 'utf-8');
        console.log('Relatório gerado em:', REPORT_FILE);
    } catch (err) {
        console.error('Erro salvando relatório:', err);
    }
}

transformar().catch(err => {
    console.error('Erro inesperado na transformação:', err);
    process.exit(1);
});