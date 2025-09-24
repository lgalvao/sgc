import * as fs from 'fs';
import * as path from 'path';

async function globalSetup() {
    // Limpar diretório de screenshots para garantir que não há arquivos antigos
    const screenshotsDir = path.join(process.cwd(), 'screenshots');
    try {
        if (fs.existsSync(screenshotsDir)) {
            const files = fs.readdirSync(screenshotsDir);
            if (files.length > 0) {
                files.forEach(file => {
                    const filePath = path.join(screenshotsDir, file);
                    fs.unlinkSync(filePath);
                });
            } else {
            }
        } else {
            // Criar diretório se não existir
            fs.mkdirSync(screenshotsDir, {recursive: true});
        }
    } catch (error) {
        console.warn('⚠️  Erro ao limpar screenshots:', error);
    }
}

export default globalSetup;