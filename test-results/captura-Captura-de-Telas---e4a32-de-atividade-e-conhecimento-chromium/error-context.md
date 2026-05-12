# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 14 - Modais de Remoção de Atividade e Conhecimento >> Captura modais de remoção de atividade e conhecimento
- Location: e2e/captura.spec.ts:1785:9

# Error details

```
Error: locator.click: Error: strict mode violation: getByTestId('btn-modal-confirmacao-cancelar') resolved to 5 elements:
    1) <button type="button" data-v-4f1e08f8="" data-testid="btn-modal-confirmacao-cancelar" class="btn btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link">Cancelar</button> aka getByLabel('Reabrir cadastro').getByTestId('btn-modal-confirmacao-cancelar')
    2) <button type="button" data-v-4f1e08f8="" data-testid="btn-modal-confirmacao-cancelar" class="btn btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link">Cancelar</button> aka getByLabel('Enviar lembrete').getByTestId('btn-modal-confirmacao-cancelar')
    3) <button type="button" data-v-4f1e08f8="" data-testid="btn-modal-confirmacao-cancelar" class="btn btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link">Cancelar</button> aka getByRole('button', { name: 'Cancelar' })
    4) <button type="button" data-v-4f1e08f8="" data-testid="btn-modal-confirmacao-cancelar" class="btn btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link">Cancelar</button> aka getByLabel('', { exact: true }).getByTestId('btn-modal-confirmacao-cancelar')
    5) <button type="button" data-v-4f1e08f8="" data-testid="btn-modal-confirmacao-cancelar" class="btn btn-link text-decoration-none text-secondary fw-medium btn-cancelar-link">Cancelar</button> aka getByLabel('Devolução do cadastro').getByTestId('btn-modal-confirmacao-cancelar')

Call log:
  - waiting for getByTestId('btn-modal-confirmacao-cancelar')

```

# Test source

```ts
  1718 |                 USUARIOS.GESTOR_SECRETARIA_1.perfil!
  1719 |             );
  1720 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1721 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1722 |             await navegarParaMapa(page);
  1723 |             await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeHidden();
  1724 |             await capturarTela(page, 'mapa-modais', 'mapa-homologado-somente-leitura-gestor', {
  1725 |                 fullPage: true,
  1726 |                 tags: ['mapa', 'homologado', 'somente-leitura', 'gestor'],
  1727 |                 extra: {estado: 'MAPA_HOMOLOGADO', perfil: 'GESTOR'}
  1728 |             });
  1729 | 
  1730 |             // Histórico do mapa (abre modal, não navega para outra página)
  1731 |             await expect(page.getByTestId('btn-mapa-historico')).toBeVisible();
  1732 |             await page.getByTestId('btn-mapa-historico').click();
  1733 |             const modalHistoricoMapa = page.getByRole('dialog');
  1734 |             await expect(modalHistoricoMapa).toBeVisible();
  1735 |             await capturarTela(page, 'mapa-modais', 'historico-mapa', {
  1736 |                 fullPage: false,
  1737 |                 tags: ['historico', 'mapa'],
  1738 |                 extra: {perfil: 'GESTOR'}
  1739 |             });
  1740 |             await page.keyboard.press('Escape');
  1741 |         });
  1742 |     });
  1743 | 
  1744 |     // SEÇÃO 13 - PERFIL SERVIDOR
  1745 |     test.describe('13 - Perfil Servidor (Somente Leitura)', () => {
  1746 |         test('Captura painel e acesso restrito do perfil Servidor', async ({page, request}) => {
  1747 |             const unidadeAlvo = 'SECAO_221';
  1748 |             const descricao = `Proc servidor ${Date.now()}`;
  1749 | 
  1750 |             const processoCodigo = await criarProcessoMapeamentoIniciadoPorFixture(
  1751 |                 request, cleanup, descricao, unidadeAlvo
  1752 |             );
  1753 | 
  1754 |             // SERVIDOR faz login e vê o painel
  1755 |             await login(page, USUARIOS.SERVIDOR_SECAO_221.titulo, USUARIOS.SERVIDOR_SECAO_221.senha);
  1756 |             await expect(page).toHaveURL(/\/painel/);
  1757 |             await capturarTela(page, 'perfil-servidor', 'painel-servidor', {
  1758 |                 fullPage: true,
  1759 |                 tags: ['servidor', 'painel'],
  1760 |                 extra: {perfil: 'SERVIDOR'}
  1761 |             });
  1762 | 
  1763 |             // SERVIDOR não deve ver o botão de criar processo
  1764 |             await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
  1765 |             await capturarTela(page, 'perfil-servidor', 'painel-servidor-sem-criar', {
  1766 |                 tags: ['servidor', 'acesso-restrito']
  1767 |             });
  1768 | 
  1769 |             // SERVIDOR não deve ter acesso ao menu de Unidades, Relatórios, etc.
  1770 |             await expect(page.getByRole('link', {name: /Unidades/i})).toBeHidden();
  1771 | 
  1772 |             // SERVIDOR acessa subprocesso da sua unidade (somente leitura)
  1773 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1774 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1775 |             await capturarTela(page, 'perfil-servidor', 'subprocesso-servidor', {
  1776 |                 fullPage: true,
  1777 |                 tags: ['servidor', 'subprocesso', 'somente-leitura'],
  1778 |                 extra: {perfil: 'SERVIDOR'}
  1779 |             });
  1780 |         });
  1781 |     });
  1782 | 
  1783 |     // SEÇÃO 14 - MODAIS DE REMOÇÃO DE ATIVIDADE E CONHECIMENTO
  1784 |     test.describe('14 - Modais de Remoção de Atividade e Conhecimento', () => {
  1785 |         test('Captura modais de remoção de atividade e conhecimento', async ({page, request}) => {
  1786 |             const unidadeAlvo = 'SECAO_111';
  1787 |             const descricao = `Proc remocao ${Date.now()}`;
  1788 |             const atividadeDesc = `Atividade para remover ${Date.now()}`;
  1789 |             const conhecimentoDesc = 'Conhecimento para remover';
  1790 |             const atividadeComConhecimento = `Atividade com conhecimento ${Date.now()}`;
  1791 | 
  1792 |             const processoCodigo = await criarProcessoMapeamentoIniciadoPorFixture(
  1793 |                 request, cleanup, descricao, unidadeAlvo
  1794 |             );
  1795 | 
  1796 |             await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);
  1797 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1798 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1799 |             await navegarParaCadastro(page);
  1800 | 
  1801 |             // Adicionar atividade e conhecimento para remoção
  1802 |             await adicionarAtividade(page, atividadeDesc);
  1803 |             await adicionarAtividade(page, atividadeComConhecimento);
  1804 |             await adicionarConhecimento(page, atividadeComConhecimento, conhecimentoDesc);
  1805 | 
  1806 |             // Modal de remoção de conhecimento - abrir
  1807 |             const cardComConhecimento = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeComConhecimento)});
  1808 |             const linhaConhecimento = cardComConhecimento.getByTestId('cad-atividades__item-conhecimento').filter({hasText: conhecimentoDesc});
  1809 |             await linhaConhecimento.hover();
  1810 |             await linhaConhecimento.getByTestId('btn-remover-conhecimento').click();
  1811 |             const modalRemoverConhecimento = page.getByRole('dialog');
  1812 |             await expect(modalRemoverConhecimento).toBeVisible();
  1813 |             await capturarTela(page, 'remocao', 'modal-remover-conhecimento', {
  1814 |                 tags: ['modal', 'remocao', 'conhecimento'],
  1815 |                 extra: {conhecimento: conhecimentoDesc}
  1816 |             });
  1817 |             // Cancelar remoção
> 1818 |             await page.getByTestId('btn-modal-confirmacao-cancelar').click();
       |                                                                      ^ Error: locator.click: Error: strict mode violation: getByTestId('btn-modal-confirmacao-cancelar') resolved to 5 elements:
  1819 |             await expect(page.getByRole('dialog')).toBeHidden();
  1820 |             await expect(cardComConhecimento.getByText(conhecimentoDesc)).toBeVisible();
  1821 |             await capturarTela(page, 'remocao', 'conhecimento-mantido-apos-cancelar', {
  1822 |                 tags: ['cancelamento', 'conhecimento']
  1823 |             });
  1824 | 
  1825 |             // Modal de remoção de atividade - hover e abrir
  1826 |             const cardAtividadeRemover = page.getByTestId('cad-atividades__card-atividade').filter({has: page.getByText(atividadeDesc, {exact: true})});
  1827 |             const hoverRow = cardAtividadeRemover.getByTestId('cad-atividades__hover-row');
  1828 |             await hoverRow.hover();
  1829 |             await aguardarPinturaEstavel(page);
  1830 |             await capturarTela(page, 'remocao', 'atividade-hover-com-remover', {
  1831 |                 tags: ['hover', 'atividade']
  1832 |             });
  1833 |             await cardAtividadeRemover.getByTestId('btn-remover-atividade').click();
  1834 |             const modalRemoverAtividade = page.getByRole('dialog');
  1835 |             await expect(modalRemoverAtividade).toBeVisible();
  1836 |             await capturarTela(page, 'remocao', 'modal-remover-atividade', {
  1837 |                 tags: ['modal', 'remocao', 'atividade'],
  1838 |                 extra: {atividade: atividadeDesc}
  1839 |             });
  1840 |             // Confirmar remoção
  1841 |             await page.getByTestId('btn-modal-confirmacao-confirmar').click();
  1842 |             await expect(page.getByText(atividadeDesc, {exact: true})).toBeHidden();
  1843 |             await capturarTela(page, 'remocao', 'atividade-removida', {
  1844 |                 tags: ['remocao', 'confirmado']
  1845 |             });
  1846 |         });
  1847 | 
  1848 |         test('Captura modal de impacto no mapa (revisão)', async ({page, request}) => {
  1849 |             const unidadeAlvo = 'ASSESSORIA_11';
  1850 |             const descricao = `Proc impacto ${Date.now()}`;
  1851 | 
  1852 |             const processoCodigo = await criarProcessoMapeamentoComCadastroDisponibilizadoPorFixture(
  1853 |                 request, cleanup, descricao, unidadeAlvo
  1854 |             );
  1855 | 
  1856 |             // Homologar o processo via admin para ter um mapa base
  1857 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  1858 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  1859 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1860 |             await navegarParaCadastro(page);
  1861 |             await (await abrirAcaoCadastroPrincipal(page)).click();
  1862 |             await expect(page.getByRole('dialog')).toBeVisible();
  1863 |             await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologado para teste de impacto');
  1864 |             await page.getByTestId('btn-aceite-cadastro-confirmar').click();
  1865 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  1866 | 
  1867 |             // Criar mapa e disponibilizar
  1868 |             await navegarParaMapa(page);
  1869 |             const competencia = `Competência impacto ${Date.now()}`;
  1870 |             await MapaHelpers.criarCompetencia(page, competencia, []);
  1871 |             await capturarTela(page, 'impacto-mapa', 'mapa-antes-disponibilizar', {
  1872 |                 fullPage: true,
  1873 |                 tags: ['mapa', 'impacto']
  1874 |             });
  1875 | 
  1876 |             // Botão de impacto no mapa
  1877 |             const btnImpactoMapa = page.getByTestId('cad-mapa__btn-impactos-mapa');
  1878 |             if (await btnImpactoMapa.isVisible()) {
  1879 |                 await btnImpactoMapa.click();
  1880 |                 await expect(page.getByRole('dialog')).toBeVisible();
  1881 |                 await capturarTela(page, 'impacto-mapa', 'modal-impacto-mapa', {
  1882 |                     tags: ['modal', 'impacto', 'mapa']
  1883 |                 });
  1884 |                 await page.keyboard.press('Escape');
  1885 |             }
  1886 |         });
  1887 |     });
  1888 | });
  1889 | 
```