#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import {fileURLToPath} from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * Script para limpar o diretório de screenshots antes dos testes visuais
 * Garante que não haja arquivos antigos de outras iterações
 */
const screenshotsDir = path.join(__dirname, '..', 'screenshots');

try {
    if (fs.existsSync(screenshotsDir)) {
        // Remove todos os arquivos do diretório
        const files = fs.readdirSync(screenshotsDir);
        if (files.length > 0) {
            files.forEach(file => {
                const filePath = path.join(screenshotsDir, file);
                fs.unlinkSync(filePath);
            });
        } else {
        }
    } else {
        // Cria o diretório se não existir
        fs.mkdirSync(screenshotsDir, {recursive: true});
    }
} catch (error) {
    console.error('❌ Erro ao limpar screenshots:', error.message);
    process.exit(1);
}