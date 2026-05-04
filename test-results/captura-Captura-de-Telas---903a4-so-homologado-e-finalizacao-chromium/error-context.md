# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: captura.spec.ts >> Captura de Telas - Sistema SGC >> 03 - Fluxo de Processo >> Captura processo homologado e finalizacao
- Location: e2e\captura.spec.ts:693:9

# Error details

```
Test timeout of 20000ms exceeded.
```

```
Error: locator.click: Target page, context or browser has been closed
Call log:
  - waiting for getByTestId('btn-processo-finalizar')

```

# Test source

```ts
  614 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  615 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  616 |             await navegarParaCadastro(page);
  617 |             await adicionarAtividade(page, atividade);
  618 |             await adicionarConhecimento(page, atividade, conhecimento);
  619 |             await capturarTela(page, 'processo', 'cadastro-chefe', {
  620 |                 extra: {perfil: 'CHEFE', unidade: unidadeAlvo, acao: 'cadastro-atividades'}
  621 |             });
  622 |             await disponibilizarCadastro(page);
  623 | 
  624 |             await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, USUARIOS.GESTOR_SECRETARIA_1.perfil);
  625 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  626 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  627 |             await navegarParaCadastro(page);
  628 |             await capturarTela(page, 'processo', 'analise-gestor', {
  629 |                 extra: {perfil: 'GESTOR', unidade: unidadeAlvo, acao: 'analise-cadastro'}
  630 |             });
  631 |             await (await abrirAcaoCadastroPrincipal(page)).click();
  632 |             await expect(page.getByRole('dialog')).toBeVisible();
  633 |             await capturarTela(page, 'processo', 'modal-aceite-gestor', {
  634 |                 tags: ['modal', 'aceite']
  635 |             });
  636 |             await page.getByTestId('inp-aceite-cadastro-obs').fill('Aceite para captura de tela');
  637 |             await page.getByTestId('btn-aceite-cadastro-confirmar').click();
  638 |             await verificarPaginaPainel(page);
  639 | 
  640 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  641 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  642 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  643 |             await navegarParaCadastro(page);
  644 |             await (await abrirAcaoCadastroPrincipal(page)).click();
  645 |             await expect(page.getByRole('dialog')).toBeVisible();
  646 |             await capturarTela(page, 'processo', 'modal-homologacao-admin', {
  647 |                 tags: ['modal', 'homologacao'],
  648 |                 extra: {perfil: 'ADMIN'}
  649 |             });
  650 |             await page.getByTestId('inp-aceite-cadastro-obs').fill('Homologação para captura');
  651 |             await page.getByTestId('btn-aceite-cadastro-confirmar').click();
  652 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  653 |             await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  654 | 
  655 |             await navegarParaMapa(page);
  656 |             await abrirModalCriarCompetencia(page);
  657 |             await page.getByTestId('inp-criar-competencia-descricao').fill(competencia);
  658 |             await page.locator('label').filter({hasText: atividade}).click();
  659 |             await page.getByTestId('btn-criar-competencia-salvar').click();
  660 |             await expect(page.locator('.competencia-card', {has: page.getByText(competencia)})).toBeVisible();
  661 |             await capturarTela(page, 'processo', 'mapa-criado', {
  662 |                 extra: {perfil: 'ADMIN', acao: 'edicao-mapa'}
  663 |             });
  664 |             await disponibilizarMapa(page);
  665 |         });
  666 | 
  667 |         test('Captura validacao do mapa', async ({page, request}) => {
  668 |             const timestamp = Date.now();
  669 |             const unidadeAlvo = 'ASSESSORIA_12';
  670 |             const descricao = `Processo validacao mapa ${timestamp}`;
  671 | 
  672 |             const processoCodigo = await criarProcessoMapeamentoComMapaDisponibilizadoPorFixture(
  673 |                 request,
  674 |                 cleanup,
  675 |                 descricao,
  676 |                 unidadeAlvo
  677 |             );
  678 | 
  679 |             await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
  680 |             await page.goto(`/processo/${processoCodigo}/${unidadeAlvo}`);
  681 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}/${unidadeAlvo}(?:\?.*)?$`));
  682 |             await navegarParaMapa(page);
  683 |             await MapaHelpers.abrirValidacaoMapa(page);
  684 |             await expect(page.getByRole('dialog')).toBeVisible();
  685 |             await capturarTela(page, 'processo', 'modal-validar-mapa', {
  686 |                 tags: ['modal', 'validacao-mapa'],
  687 |                 extra: {perfil: 'CHEFE'}
  688 |             });
  689 |             await page.getByTestId('btn-validar-mapa-confirmar').click();
  690 |             await verificarPaginaPainel(page);
  691 |         });
  692 | 
  693 |         test('Captura processo homologado e finalizacao', async ({page, request}) => {
  694 |             const timestamp = Date.now();
  695 |             const unidadeAlvo = 'SECAO_121';
  696 |             const descricao = `Processo finalizacao ${timestamp}`;
  697 | 
  698 |             const processoCodigo = await criarProcessoMapeamentoComMapaHomologadoPorFixture(
  699 |                 request,
  700 |                 cleanup,
  701 |                 descricao,
  702 |                 unidadeAlvo
  703 |             );
  704 | 
  705 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  706 |             await page.goto(`/processo/${processoCodigo}`);
  707 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoCodigo}(?:\?.*)?$`));
  708 |             await capturarTela(page, 'processo', 'detalhes-processo-finalizavel', {
  709 |                 fullPage: true,
  710 |                 extra: {perfil: 'ADMIN', acao: 'finalizacao-processo'}
  711 |             });
  712 | 
  713 |             // Modal de finalizar processo
> 714 |             await page.getByTestId('btn-processo-finalizar').click();
      |                                                              ^ Error: locator.click: Target page, context or browser has been closed
  715 |             await expect(page.getByRole('dialog')).toBeVisible();
  716 |             await capturarTela(page, 'processo', 'modal-finalizar-processo', {
  717 |                 tags: ['modal', 'finalizar']
  718 |             });
  719 |             await page.getByRole('button', {name: 'Cancelar'}).click();
  720 |             await capturarTela(page, 'processo', 'detalhes-processo-apos-cancelar-finalizacao', {
  721 |                 fullPage: true
  722 |             });
  723 |         });
  724 |     });
  725 | 
  726 |     test.describe('04 - Subprocesso e Atividades', () => {
  727 |         test('Captura fluxo completo de atividades (incluindo validações de form)', async ({page}) => {
  728 |             const descricao = `Proc atividades ${Date.now()}`;
  729 |             const UNIDADE_ALVO = 'SECAO_211';
  730 | 
  731 |             await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
  732 |             await page.getByTestId('btn-painel-criar-processo').click();
  733 |             await expect(page).toHaveURL(/\/processo\/cadastro/);
  734 | 
  735 |             // Estado inicial vazio
  736 |             await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeDisabled();
  737 |             await capturarTela(page, 'processo', 'botoes-desativados-form-vazio', {
  738 |                 tags: ['validacao', 'form'],
  739 |                 extra: {motivo: 'falta-campos-obrigatorios'}
  740 |             });
  741 | 
  742 |             await page.getByTestId('inp-processo-descricao').fill(descricao);
  743 |             await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeDisabled();
  744 |             await capturarTela(page, 'processo', 'botoes-desativados-falta-data-unidade', {
  745 |                 extra: {preenchido: 'descricao'}
  746 |             });
  747 | 
  748 |             await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
  749 | 
  750 |             const dataLimite = new Date();
  751 |             dataLimite.setDate(dataLimite.getDate() + 30);
  752 |             await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);
  753 |             await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeDisabled();
  754 |             await capturarTela(page, 'processo', 'botoes-desativados-falta-unidade', {
  755 |                 extra: {preenchido: ['descricao', 'tipo', 'data']}
  756 |             });
  757 | 
  758 |             // Selecionar unidade e validar ativação
  759 |             await expect(page.getByText('Carregando unidades...')).toBeHidden();
  760 |             await page.getByTestId('btn-arvore-expand-SECRETARIA_2').click();
  761 |             await expect(page.getByTestId('btn-arvore-expand-COORD_21')).toBeVisible();
  762 |             await page.getByTestId('btn-arvore-expand-COORD_21').click();
  763 |             await expect(page.getByTestId('chk-arvore-unidade-SECAO_211')).toBeVisible();
  764 |             await page.getByTestId('chk-arvore-unidade-SECAO_211').click();
  765 |             await expect(page.getByTestId('btn-processo-salvar-rodape')).toBeEnabled();
  766 |             await capturarTela(page, 'processo', 'botoes-ativados-form-completo', {
  767 |                 tags: ['validacao', 'sucesso']
  768 |             });
  769 | 
  770 |             await page.getByTestId('btn-processo-iniciar-rodape').click();
  771 |             await confirmarInicioProcessoPeloDialogo(page, {
  772 |                 descricao,
  773 |                 tipo: 'MAPEAMENTO'
  774 |             });
  775 | 
  776 |             // Registrar para cleanup
  777 |             const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descricao)});
  778 |             await linhaProcesso.click();
  779 |             await page.waitForURL(/\/processo\/\d+/);
  780 |             const codProcesso = await extrairProcessoCodigo(page);
  781 |             registrarProcessoParaCleanup(cleanup, codProcesso);
  782 | 
  783 |             await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
  784 |             await page.getByTestId('tbl-processos').getByText(descricao).first().click();
  785 |             await navegarParaSubprocesso(page, UNIDADE_ALVO);
  786 |             await capturarTela(page, 'subprocesso', 'dashboard-subprocesso', {
  787 |                 fullPage: true,
  788 |                 extra: {perfil: 'CHEFE', unidade: UNIDADE_ALVO}
  789 |             });
  790 | 
  791 |             // Entrar em atividades
  792 |             await navegarParaCadastro(page);
  793 |             await capturarTela(page, 'subprocesso', 'cadastro-atividades-vazio', {
  794 |                 fullPage: true,
  795 |                 tags: ['atividades', 'vazio']
  796 |             });
  797 | 
  798 |             const atividadeDesc = `Atividade teste ${Date.now()}`;
  799 |             await page.getByTestId('inp-nova-atividade').fill(atividadeDesc);
  800 |             await capturarTela(page, 'subprocesso', 'cadastro-atividades-preenchendo', {
  801 |                 tags: ['interacao']
  802 |             });
  803 |             await page.getByTestId('btn-adicionar-atividade').click();
  804 |             await expect(page.getByText(atividadeDesc, {exact: true})).toBeVisible();
  805 |             await capturarTela(page, 'subprocesso', 'cadastro-atividades-com-uma', {
  806 |                 fullPage: true,
  807 |                 extra: {atividade: atividadeDesc}
  808 |             });
  809 | 
  810 |             const card = page.locator('.atividade-card', {has: page.getByText(atividadeDesc)});
  811 |             await card.getByTestId('inp-novo-conhecimento').fill('Java');
  812 |             await capturarTela(page, 'subprocesso', 'cadastro-conhecimento-preenchendo', {
  813 |                 tags: ['conhecimento', 'edicao']
  814 |             });
```