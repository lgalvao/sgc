import {expect, test} from '../fixtures/a11y.js';
import {login, USUARIOS} from '../helpers/helpers-auth.js';
import fs from 'fs';
import path from 'path';

const ROUTES = [
    {name: 'Login', path: '/login'},
    {name: 'Painel', path: '/painel'},
    {name: 'Histórico', path: '/historico'},
    {name: 'Relatórios', path: '/relatorios'},
    {name: 'Relatório de Andamento', path: '/relatorios/andamento'},
    {name: 'Relatório de Mapas', path: '/relatorios/mapas-vigentes'},
    {name: 'Administradores', path: '/administradores'},
    {name: 'Notificações Admin', path: '/administracao/notificacoes'},
    {name: 'Feedbacks Admin', path: '/administracao/feedbacks'},
    {name: 'Parâmetros', path: '/configuracoes'},

    // Rotas estáticas adicionais
    {name: 'Erro', path: '/erro'},
    {name: 'Relatório de Gaps de Diagnóstico', path: '/relatorios/diagnostico/gaps'},
    {name: 'Relatório de Situações de Capacitação', path: '/relatorios/diagnostico/situacao-capacitacao'},
    {name: 'Relatório de Unidades sem Mapas Vigentes', path: '/relatorios/unidades-sem-mapas-vigentes'},
    {name: 'Limpeza de Processos Admin', path: '/administracao/limpeza-processos'},
    {name: 'Novo Processo', path: '/processo/cadastro'},
    {name: 'Unidades', path: '/unidades'},

    // Rotas dinâmicas adicionais baseadas no seed.sql (Processo 99, Subprocesso 99, Unidade 4)
    {name: 'Detalhes do Processo', path: '/processo/99'},
    {name: 'Processos da Unidade', path: '/processo/99/ASSESSORIA_12'},
    {name: 'Mapa do Subprocesso', path: '/processo/99/ASSESSORIA_12/mapa'},
    {name: 'Cadastro do Subprocesso', path: '/processo/99/ASSESSORIA_12/cadastro'},
    {name: 'Detalhes da Unidade', path: '/unidade/4'},
    {name: 'Mapa da Unidade', path: '/unidade/4/mapa?codProcesso=99'},
    {name: 'Atribuição Temporária', path: '/unidade/4/atribuicao'},
    // As rotas de diagnóstico ficam fora do crawler por enquanto.
    // O seed E2E atual não possui um subprocesso de diagnóstico válido para esse cenário,
    // e usar o subprocesso 99 gera chamadas de backend sabidamente inválidas.
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

            // Aguarda a renderização básica do Vue
            await page.waitForLoadState('load');

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
