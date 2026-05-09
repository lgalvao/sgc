#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import {execFileSync} from "node:child_process";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

function resolveKeytool() {
    if (process.env.JAVA_HOME) {
        const keytoolJavaHome = path.join(process.env.JAVA_HOME, 'bin', process.platform === 'win32' ? 'keytool.exe' : 'keytool');
        if (fs.existsSync(keytoolJavaHome)) {
            return keytoolJavaHome;
        }
    }
    return 'keytool';
}

function importarCertificado(keytoolBin, alias, certPath) {
    execFileSync(keytoolBin, [
        '-cacerts',
        '-storepass',
        'changeit',
        '-noprompt',
        '-trustcacerts',
        '-importcert',
        '-alias',
        alias,
        '-file',
        certPath
    ], {
        stdio: 'inherit'
    });
}

function main() {
    if (process.argv.includes('--help') || process.argv.includes('-h')) {
        exibirAjudaComando({
            comandoSgc: "backend java instalar-certificados",
            scriptDireto: "backend/java-instalar-certificados.js",
            descricao: 'Importa cert-tre.cer e cert-for.cer em cacerts usando keytool.',
            exemplos: [
                'node etc/scripts/sgc.js backend java instalar-certificados'
            ]
        });
        process.exit(0);
    }

    const deployDir = path.join(import.meta.dirname, '../../../backend/etc/deploy');
    const certificados = [
        {alias: 'cert-tre', caminho: path.join(deployDir, 'cert-tre.cer')},
        {alias: 'cert-for', caminho: path.join(deployDir, 'cert-for.cer')}
    ];

    certificados.forEach(certificado => {
        if (!fs.existsSync(certificado.caminho)) {
            throw new Error(`Certificado nao encontrado: ${certificado.caminho}`);
        }
    });

    const keytoolBin = resolveKeytool();
    certificados.forEach(certificado => importarCertificado(keytoolBin, certificado.alias, certificado.caminho));
    console.log('Certificados importados com sucesso.');
}

try {
    main();
} catch (error) {
    console.error(`Erro ao instalar certificados: ${error.message}`);
    process.exit(1);
}
