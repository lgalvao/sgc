#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');

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
            comandoSgc: 'java instalar-certificados',
            scriptDireto: 'java-instalar-certificados.cjs',
            descricao: 'Importa cert-tre.cer e cert-for.cer em cacerts usando keytool.',
            exemplos: [
                'node backend/etc/scripts/sgc.cjs java instalar-certificados'
            ]
        });
        process.exit(0);
    }

    const deployDir = path.join(__dirname, '../deploy');
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
