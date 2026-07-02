#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';
import {globby} from 'globby';

const raiz = process.cwd();
const dirRequisitos = path.join(raiz, 'specs');
const dirIntegracao = path.join(raiz, 'backend/src/test/java/sgc/integracao');
const dirE2e = path.join(raiz, 'e2e');
const dirTemplates = path.join(raiz, 'backend/src/main/resources/templates/email');
const arquivoRender = path.join(dirIntegracao, 'EmailModelosRenderIntegrationTest.java');

function lerSeExistir(caminho) {
    return fs.existsSync(caminho) ? fs.readFileSync(caminho, 'utf8') : '';
}

async function listarCdusComEmail() {
    const arquivos = await globby(path.join(dirRequisitos, 'cdu-*.md').replace(/\\/g, '/'), {absolute: true});
    const cdus = [];

    for (const caminho of arquivos) {
        const nome = path.basename(caminho);
        const texto = fs.readFileSync(caminho, 'utf8');
        const assuntos = [...texto.matchAll(/^\s*Assunto:\s*(.+)$/gm)].map(match => match[1].trim());
        const mencionaEmail = assuntos.length > 0
            || /enviar? notifica(?:ç|c)(?:ões|ao) por e-?mail/i.test(texto)
            || /envia(?:r)? e-?mails?/i.test(texto);

        cdus.push({
            numero: nome.match(/\d+/)?.[0] ?? '?',
            nome,
            caminho,
            texto,
            assuntos,
            mencionaEmail
        });
    }

    return cdus
        .filter(cdu => cdu.mencionaEmail)
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', {numeric: true}));
}

function detectarTemplates(renderTexto) {
    return new Set(
        [...renderTexto.matchAll(/templateEngine\.process\("([^"]+)"/g)].map(match => match[1])
    );
}

async function listarTemplatesEmail() {
    const arquivos = await globby(path.join(dirTemplates, '*.html').replace(/\\/g, '/'), {absolute: true});
    return arquivos
        .map(caminho => path.basename(caminho))
        .filter(nome => nome !== '_layout.html')
        .map(nome => nome.replace(/\.html$/, ''));
}


function resumirCobertura(cdu, renderTemplates, templatesEmail, analisarTemplates) {
    const numero = cdu.numero.padStart(2, '0');
    const arquivoIntegracao = path.join(dirIntegracao, `CDU${numero}IntegrationTest.java`);
    const arquivoE2e = path.join(dirE2e, `cdu-${numero}.spec.ts`);

    const textoIntegracao = lerSeExistir(arquivoIntegracao);
    const textoE2e = lerSeExistir(arquivoE2e);

    const temTesteIntegracao = textoIntegracao.length > 0;
    const temTesteE2e = textoE2e.length > 0;
    const verificaAssunto = /\.getAssunto\(\)|algumEmailComAssunto\(/.test(textoIntegracao);
    const verificaCorpo = /\.getCorpoHtml\(\)|algumEmailContem\(/.test(textoIntegracao);
    const verificaFila = /NotificacaoEmailRepo|TipoNotificacao/.test(textoIntegracao);
    const verificaDestinatario = /getDestinatario\(\)|unidadeDestinoSigla|usuarioDestinoTitulo|algumEmailPara\(/.test(textoIntegracao);
    const e2eMencionaAlerta = /alerta/i.test(textoE2e);
    const e2eMencionaEmail = /email|e-mail|notifica/i.test(textoE2e);

    const lacunas = [];

    if (!temTesteIntegracao) lacunas.push('sem teste de integração dedicado');
    if (temTesteIntegracao && cdu.assuntos.length > 0 && !verificaAssunto) lacunas.push('integração não valida assunto');
    if (temTesteIntegracao && cdu.assuntos.length > 0 && !verificaCorpo) lacunas.push('integração não valida corpo');
    if (temTesteIntegracao && cdu.assuntos.length > 0 && !verificaFila) lacunas.push('integração não valida notificação enfileirada');
    if (temTesteIntegracao && cdu.assuntos.length > 1 && !verificaDestinatario) lacunas.push('integração não diferencia destinatários/papéis');
    if (temTesteE2e && !e2eMencionaAlerta && !e2eMencionaEmail) lacunas.push('e2e não toca comunicação visível');
    if (!temTesteE2e) lacunas.push('sem e2e dedicado');
    if (analisarTemplates) {
        const candidatosTemplate = new Set();
        const textoTotal = `${textoIntegracao}\n${textoE2e}\n${cdu.texto}`.toLowerCase();
        for (const template of templatesEmail) {
            const partes = template.split('-').filter(Boolean);
            if (partes.every(parte => textoTotal.includes(parte.toLowerCase()))) {
                candidatosTemplate.add(template);
            }
        }
        const templatesSemRender = [...candidatosTemplate].filter(template => !renderTemplates.has(template));
        if (templatesSemRender.length > 0) lacunas.push(`templates sem render test: ${templatesSemRender.join(', ')}`);
    }

    return {
        numero,
        assuntos: cdu.assuntos,
        temTesteIntegracao,
        temTesteE2e,
        verificaAssunto,
        verificaCorpo,
        verificaFila,
        verificaDestinatario,
        e2eMencionaAlerta,
        e2eMencionaEmail,
        lacunas
    };
}

const renderTexto = lerSeExistir(arquivoRender);
const renderTemplates = detectarTemplates(renderTexto);
const templatesEmail = await listarTemplatesEmail();
const analisarTemplates = process.argv.includes('--templates');
const cdusComEmail = await listarCdusComEmail();
const resultados = cdusComEmail.map(cdu => resumirCobertura(cdu, renderTemplates, templatesEmail, analisarTemplates));

const somenteComLacuna = process.argv.includes('--lacunas');
const saida = somenteComLacuna ? resultados.filter(item => item.lacunas.length > 0) : resultados;

for (const item of saida) {
    console.log(`CDU-${item.numero}`);
    console.log(`  assuntos no requisito: ${item.assuntos.length}`);
    console.log(`  integracao: ${item.temTesteIntegracao ? 'sim' : 'nao'} | assunto: ${bool(item.verificaAssunto)} | corpo: ${bool(item.verificaCorpo)} | fila: ${bool(item.verificaFila)} | destinatarios: ${bool(item.verificaDestinatario)}`);
    console.log(`  e2e: ${item.temTesteE2e ? 'sim' : 'nao'} | alerta: ${bool(item.e2eMencionaAlerta)} | email: ${bool(item.e2eMencionaEmail)}`);
    if (item.lacunas.length === 0) {
        console.log('  lacunas: nenhuma heurística óbvia');
    } else {
        console.log(`  lacunas: ${item.lacunas.join('; ')}`);
    }
}

function bool(valor) {
    return valor ? 'sim' : 'nao';
}
