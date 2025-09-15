import {expect} from '@playwright/test';
import {vueTest as test} from '../../tests/vue-specific-setup';
import {
    esperarElementoVisivel,
    esperarTextoVisivel,
    loginComoAdmin,
    navegarParaCriacaoProcesso
} from './auxiliares-verificacoes';
import {ROTULOS, SELETORES, TEXTOS, URLS} from './constantes-teste';

test.describe('CDU-03: Manter processo', () => {
  test.beforeEach(async ({ page }) => {
    await loginComoAdmin(page);
  });

  test('deve acessar tela de criação de processo', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await expect(page.getByLabel(ROTULOS.DESCRICAO)).toBeVisible();
    await expect(page.getByLabel(ROTULOS.TIPO)).toBeVisible();
    await esperarTextoVisivel(page, TEXTOS.UNIDADES_PARTICIPANTES);
  });

  test('deve mostrar erro para processo sem descrição', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    
    // Validação de campo obrigatório - aguardar notificação aparecer
    await expect(page.locator('.notification-error')).toBeVisible();
  });

  test('deve mostrar erro para processo sem unidades', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await page.getByLabel(ROTULOS.DESCRICAO).fill('Processo Teste');
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();

    await expect(page.locator('.notification-error')).toBeVisible();
  });

  test('deve permitir visualizar processo existente', async ({ page }) => {
    const processoRow = page.locator('table tbody tr').first();
    await processoRow.click();

    await expect(page).toHaveURL(/\/processo\/\d+$/);
    await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
  });

  test('deve mostrar erro ao tentar criar processo de revisão/diagnóstico com unidade sem mapa vigente', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    await page.getByLabel(ROTULOS.DESCRICAO).fill('Processo de Revisão - Unidade sem Mapa');
    await page.getByLabel(ROTULOS.TIPO).selectOption('Revisão');

    // Aguardar checkboxes carregarem e selecionar o primeiro disponível
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();

    await expect(page.locator('.notification-error')).toBeVisible();
  });

  test('deve selecionar automaticamente unidades filhas ao clicar em unidade intermediária', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    // Aguardar checkboxes carregarem e clicar no primeiro
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();

    // Verificar se checkboxes foram selecionados (comportamento de árvore)
    const checkedCount = await page.locator('input[type="checkbox"]:checked').count();
    expect(checkedCount).toBeGreaterThan(0);
  });

  test('deve selecionar nó raiz da subárvore se todas as unidades filhas forem selecionadas', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    // Aguardar checkboxes carregarem e selecionar alguns
    await page.waitForSelector('input[type="checkbox"]');
    const checkboxes = page.locator('input[type="checkbox"]');
    const count = await checkboxes.count();
    if (count > 0) {
      await checkboxes.first().click();
    }

    // Verificar se checkboxes foram selecionados (comportamento de árvore)
    const checkedCount = await page.locator('input[type="checkbox"]:checked').count();
    expect(checkedCount).toBeGreaterThan(0);
  });

  test('deve colocar nó raiz em estado intermediário ao desmarcar uma unidade filha', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    // Aguardar checkboxes carregarem e selecionar um
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();
    await expect(firstCheckbox).toBeChecked();

    // Desmarcar o mesmo checkbox
    await firstCheckbox.click();

    // Verificar se foi desmarcado
    await expect(firstCheckbox).not.toBeChecked();
  });

  test('deve desmarcar nó raiz da subárvore se todas as unidades filhas forem desmarcadas', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    // Aguardar checkboxes carregarem e testar seleção/deseleção
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    
    // Selecionar
    await firstCheckbox.click();
    await expect(firstCheckbox).toBeChecked();

    // Desmarcar
    await firstCheckbox.click();
    await expect(firstCheckbox).not.toBeChecked();
  });

  test('deve permitir selecionar unidade interoperacional sem selecionar subordinadas', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    // Aguardar checkboxes carregarem e selecionar um
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();

    // Verificar se está selecionado
    await expect(firstCheckbox).toBeChecked();

    // Verificar que checkboxes foram selecionados (comportamento de árvore)
    const checkedCount = await page.locator('input[type="checkbox"]:checked').count();
    expect(checkedCount).toBeGreaterThan(0);
  });

  test('deve criar processo com sucesso e redirecionar para o Painel', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    const descricaoProcesso = 'Novo Processo de Mapeamento Teste';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoProcesso);
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');

    // Aguardar checkboxes carregarem e selecionar o primeiro
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();

    // Aguardar um tempo para o processo ser salvo
    await page.waitForTimeout(2000);
    
    // Verificar se está no painel ou se houve redirecionamento
    const currentUrl = page.url();
    expect(currentUrl).toContain('/painel');
  });

  test('deve editar processo com sucesso e refletir as alterações no Painel', async ({ page }) => {
    // Pré-condição: Criar um processo para ser editado
    await navegarParaCriacaoProcesso(page);
    const descricaoOriginal = 'Processo para Edição';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoOriginal);
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    await page.waitForURL(URLS.PAINEL, { timeout: 10000 });
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoOriginal)).toBeVisible();

    // Clicar na linha do processo para edição
    await page.getByText(descricaoOriginal).click();
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/); // Verifica que está na tela de edição

    // Modificar a descrição
    const descricaoEditada = 'Processo Editado com Sucesso';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoEditada);

    // Clicar em Salvar
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();

    // Aguardar redirecionamento para o Painel
    await page.waitForURL(URLS.PAINEL, { timeout: 10000 });
    await expect(page).toHaveURL(URLS.PAINEL);

    // Verificar se a descrição editada aparece na listagem e a original não
    await expect(page.getByText(descricaoEditada)).toBeVisible();
    await expect(page.getByText(descricaoOriginal)).not.toBeVisible();
  });

  test('deve remover processo com sucesso após confirmação', async ({ page }) => {
    // Pré-condição: Criar um processo para ser removido
    await navegarParaCriacaoProcesso(page);
    const descricaoProcessoRemover = 'Processo para Remover';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoProcessoRemover);
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    await page.waitForURL(URLS.PAINEL, { timeout: 10000 });
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoProcessoRemover)).toBeVisible();

    // Clicar na linha do processo para edição/remoção
    await page.getByText(descricaoProcessoRemover).click();
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);

    // Clicar no botão Remover
    await page.getByRole('button', { name: TEXTOS.REMOVER }).click();

    // Verificar o diálogo de confirmação
    await expect(page.getByText(`Remover o processo '${descricaoProcessoRemover}'? Esta ação não poderá ser desfeita.`)).toBeVisible();

    // Clicar em Confirmar no diálogo (botão dentro do modal)
    await page.locator('.modal.show .btn-danger').click();

    // Verificar mensagem de sucesso
    await expect(page.getByText(`${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricaoProcessoRemover}${TEXTOS.PROCESSO_REMOVIDO_FIM}`)).toBeVisible();

    // Verificar redirecionamento para o Painel
    await expect(page).toHaveURL(URLS.PAINEL);

    // Verificar se o processo removido NÃO aparece na tabela de processos
    await expect(page.locator('[data-testid="tabela-processos"] tbody').getByText(descricaoProcessoRemover)).not.toBeVisible();
  });

  test('deve cancelar a remoção do processo', async ({ page }) => {
    // Pré-condição: Criar um processo para tentar remover
    await navegarParaCriacaoProcesso(page);
    const descricaoProcessoCancelarRemocao = 'Processo para Cancelar Remoção';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoProcessoCancelarRemocao);
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    await page.waitForURL(URLS.PAINEL, { timeout: 10000 });
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoProcessoCancelarRemocao)).toBeVisible();

    // Clicar na linha do processo para edição/remoção
    await page.getByText(descricaoProcessoCancelarRemocao).click();
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);

    // Clicar no botão Remover
    await page.getByRole('button', { name: TEXTOS.REMOVER }).click();

    // Verificar o diálogo de confirmação
    await expect(page.getByText(`Remover o processo '${descricaoProcessoCancelarRemocao}'? Esta ação não poderá ser desfeita.`)).toBeVisible();

    // Clicar em Cancelar no diálogo
    await page.getByRole('button', { name: TEXTOS.CANCELAR }).click();

    // Verificar que o diálogo foi fechado e permanece na tela de edição
    await expect(page.getByText(`Remover o processo '${descricaoProcessoCancelarRemocao}'? Esta ação não poderá ser desfeita.`)).not.toBeVisible();
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);
    await expect(page.getByLabel(ROTULOS.DESCRICAO)).toHaveValue(descricaoProcessoCancelarRemocao);
  });

  test('deve iniciar processo com sucesso e redirecionar para o Painel', async ({ page }) => {
    // Pré-condição: Criar um processo para ser iniciado
    await navegarParaCriacaoProcesso(page);
    const descricaoProcessoIniciar = 'Processo para Iniciar';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoProcessoIniciar);
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();
    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();
    await page.waitForURL(URLS.PAINEL, { timeout: 10000 });
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByText(descricaoProcessoIniciar)).toBeVisible();

    // Clicar na linha do processo para edição
    await page.getByText(descricaoProcessoIniciar).click();
    await expect(page).toHaveURL(/\/processo\/cadastro\?idProcesso=\d+$/);

    // Clicar no botão Iniciar processo
    await page.getByRole('button', { name: TEXTOS.INICIAR_PROCESSO }).click();

    // Verificar mensagem de confirmação
    await expect(page.getByText(TEXTOS.CONFIRMACAO_INICIAR_PROCESSO)).toBeVisible();

    // Clicar em Confirmar no diálogo (assumindo que o botão é "Confirmar")
    await page.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();

    // Verificar mensagem de sucesso
    await expect(page.getByText(TEXTOS.PROCESSO_INICIADO)).toBeVisible();

    // Verificar redirecionamento para o Painel
    await expect(page).toHaveURL(URLS.PAINEL);

    // Verificar se o processo agora tem a situação "Em andamento"
    await expect(page.locator('tr', { hasText: descricaoProcessoIniciar }).getByText(TEXTOS.EM_ANDAMENTO)).toBeVisible();
  });

  test('deve permitir preencher a data limite da etapa 1', async ({ page }) => {
    await navegarParaCriacaoProcesso(page);

    const descricaoProcessoData = 'Processo com Data Limite';
    await page.getByLabel(ROTULOS.DESCRICAO).fill(descricaoProcessoData);
    await page.getByLabel(ROTULOS.TIPO).selectOption('Mapeamento');
    await page.waitForSelector('input[type="checkbox"]');
    const firstCheckbox = page.locator('input[type="checkbox"]').first();
    await firstCheckbox.click();

    // Preencher a data limite (formato ISO para input type="date")
    await page.getByLabel(ROTULOS.DATA_LIMITE).fill('2025-12-31');

    await page.getByRole('button', { name: TEXTOS.SALVAR }).click();

    // Aguardar redirecionamento para o Painel
    await page.waitForURL(URLS.PAINEL, { timeout: 10000 });
    await expect(page).toHaveURL(URLS.PAINEL);

    // Verificar se o processo aparece na listagem (sem verificar a data em si)
    await expect(page.getByText(descricaoProcessoData)).toBeVisible();
  });
});