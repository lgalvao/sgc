import {expect, test} from '../fixtures/a11y.js';
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
    const scanResults: Array<{
        route: string;
        name: string;
        violations: unknown[];
    }> = [];

    test.afterAll(async () => {
        if (process.env.SGC_RELATORIO_A11Y === 'sim') {
            const resultsPath = path.join(process.cwd(), 'a11y-scan-results.json');
            fs.writeFileSync(resultsPath, JSON.stringify(scanResults, null, 2));
        }
    });

    for (const route of ROUTES) {
        test(`Audit route: ${route.name} (${route.path})`, async ({page, makeAxeBuilder}) => {
            if (route.path !== '/login' && route.path !== '/erro') {
                await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
                await page.waitForURL('/painel');
            }
            
            await page.goto(route.path);
            
            // Aguarda a renderização do Vue e o término das requisições de rede
            await page.waitForLoadState('networkidle');

            const accessibilityScanResults = await makeAxeBuilder().analyze();
            expect(accessibilityScanResults.violations).toEqual([]);
            
            scanResults.push({
                route: route.path,
                name: route.name,
                violations: accessibilityScanResults.violations
            });
        });
    }
});
