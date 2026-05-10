import {test} from '../fixtures/a11y.js';
import {login, USUARIOS} from '../helpers/helpers-auth.js';
import fs from 'fs';
import path from 'path';

const ROUTES = [
    { name: 'Login', path: '/login' },
    { name: 'Painel', path: '/painel' },
    { name: 'Histórico', path: '/historico' },
    { name: 'Relatórios', path: '/relatorios' },
    { name: 'Relatório de Andamento', path: '/relatorios/andamento' },
    { name: 'Relatório de Mapas', path: '/relatorios/mapas-vigentes' },
    { name: 'Administradores', path: '/administradores' },
    { name: 'Notificações Admin', path: '/administracao/notificacoes' },
    { name: 'Feedbacks Admin', path: '/administracao/feedbacks' },
    { name: 'Parâmetros', path: '/configuracoes' }
];

test.describe('Accessibility Crawler (Axe-core)', () => {
    let scanResults = [];

    test.afterAll(async () => {
        const resultsPath = path.join(process.cwd(), 'a11y-scan-results.json');
        fs.writeFileSync(resultsPath, JSON.stringify(scanResults, null, 2));
        console.log(`Scan results saved to ${resultsPath}`);
    });

    for (const route of ROUTES) {
        test(`Audit route: ${route.name} (${route.path})`, async ({page, makeAxeBuilder}) => {
            if (route.path !== '/login' && route.path !== '/erro') {
                await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
                await page.waitForURL('/painel');
            }
            
            console.log(`Navigating to ${route.path}...`);
            await page.goto(route.path);
            
            // Wait for some content to be visible to ensure vue has rendered
            await page.waitForTimeout(1000); 

            const accessibilityScanResults = await makeAxeBuilder().analyze();
            
            scanResults.push({
                route: route.path,
                name: route.name,
                violations: accessibilityScanResults.violations
            });

            // We don't fail the test here, we collect results for the report
            console.log(`Route ${route.name}: Found ${accessibilityScanResults.violations.length} violations.`);
        });
    }
});
