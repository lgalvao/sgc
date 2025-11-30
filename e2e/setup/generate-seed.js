const fs = require('fs');
const path = require('path');

const unidadesPath = path.join(__dirname, '../../unidades.json');
const usuariosPath = path.join(__dirname, '../../usuarios.json');
const outputPath = path.join(__dirname, 'seed.sql');

const unidadesData = JSON.parse(fs.readFileSync(unidadesPath, 'utf8'));
const usuariosData = JSON.parse(fs.readFileSync(usuariosPath, 'utf8'));

let sql = '-- Seed data generated from JSON\n\n';

// Helper to escape strings
const escape = (str) => str ? `'${str.replace(/'/g, "''")}'` : 'NULL';

// 1. Insert Unidades (Hierarchical)
// We need to insert in order of hierarchy to satisfy foreign keys (unidade_superior_codigo)
// But wait, the schema has self-referencing FK. We might need to insert all first with NULL superior, then update?
// Or just insert top-down.
// The JSON structure is nested.

let unidadeIdCounter = 1;
const unidadeMap = new Map(); // sigla -> id

function processUnidade(unidade, parentId = null) {
    const id = unidadeIdCounter++;
    unidadeMap.set(unidade.sigla, id);

    const nome = escape(unidade.nome);
    const sigla = escape(unidade.sigla);
    const tipo = escape(unidade.tipo);
    const situacao = escape(unidade.situacao);
    const superior = parentId ? parentId : 'NULL';

    sql += `INSERT INTO sgc.unidade (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo) VALUES (${id}, ${nome}, ${sigla}, ${tipo}, ${situacao}, ${superior});\n`;

    if (unidade.subunidades) {
        for (const sub of unidade.subunidades) {
            processUnidade(sub, id);
        }
    }
}

for (const unidade of unidadesData.unidades) {
    processUnidade(unidade);
}

sql += '\n';

// 2. Insert Usuarios
// JSON has "unidade_lotacao" as sigla. We need to map to ID.
// JSON has "perfis_unidades" array.

for (const usuario of usuariosData) {
    const titulo = escape(usuario.titulo_eleitoral);
    const nome = escape(usuario.nome);
    const email = escape(`${usuario.nome.toLowerCase().replace(/\s+/g, '.')}@tre-pe.jus.br`); // Fake email
    const ramal = 'NULL';

    const unidadeLotacaoSigla = usuario.unidade_lotacao;
    const unidadeLotacaoId = unidadeMap.get(unidadeLotacaoSigla);

    if (!unidadeLotacaoId) {
        console.warn(`Unidade lotação not found for user ${usuario.nome}: ${unidadeLotacaoSigla}`);
        continue;
    }

    sql += `INSERT INTO sgc.usuario (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES (${titulo}, ${nome}, ${email}, ${ramal}, ${unidadeLotacaoId});\n`;

    // Insert Profiles
    if (usuario.perfis_unidades) {
        for (const pu of usuario.perfis_unidades) {
            const perfil = escape(pu.perfil);
            const unidadeSigla = pu.sigla;
            const unidadeId = unidadeMap.get(unidadeSigla);

            if (unidadeId) {
                sql += `INSERT INTO sgc.usuario_perfil (usuario_titulo_eleitoral, perfil, unidade_codigo) VALUES (${titulo}, ${perfil}, ${unidadeId});\n`;
            } else {
                console.warn(`Unidade profile not found for user ${usuario.nome}: ${unidadeSigla}`);
            }
        }
    }
}

fs.writeFileSync(outputPath, sql);
console.log(`Seed SQL generated at ${outputPath}`);
