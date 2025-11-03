// @ts-nocheck - File not currently in use (no globalSetup in playwright.config.ts)
import axios from 'axios';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function globalSetup() {
    try {
        console.log('Executando o seed do banco de dados para testes E2E...');
        const response = await axios.post('http://localhost:10000/api/test/seed');
        const testData = response.data;
        console.log('Dados recebidos do seed:', testData);

        const dataDir = path.join(__dirname, '..');
        if (!fs.existsSync(dataDir)) {
            fs.mkdirSync(dataDir, { recursive: true });
        }

        fs.writeFileSync(path.join(dataDir, '.test-data.json'), JSON.stringify(testData, null, 2));
        console.log('Dados de teste gerados e salvos em .test-data.json');

    } catch (error) {
        console.error('Falha ao executar o seed do banco de dados:', error);
        process.exit(1); // Aborta a execução dos testes se o seed falhar
    }
}

export default globalSetup;