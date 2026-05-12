# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: fluxo-completo-semantico.spec.ts >> Jornada geral semântica - mapeamento e revisão ponta a ponta >> Fase 2 - CHEFE cadastra atividades e a hierarquia analisa o cadastro
- Location: e2e/fluxo-completo-semantico.spec.ts:456:5

# Error details

```
Error: expect(locator).toContainText(expected) failed

Locator: getByTestId('mdl-historico-analise')
Expected substring: "Cadastro devolvido: favor verificar as atividades antes de disponibilizar novamente."
Received string:    "Histórico de análiseData/HoraUnidadeAçãoUsuárioObservaçãoVisualizar12/05/2026 23:30COORD_11DevoluçãoGESTOR_COORD_11Cadastro devolvido: favor verificar as atividades antes de disponibilizar nov... Fechar "
Timeout: 5000ms

Call log:
  - Expect "toContainText" with timeout 5000ms
  - waiting for getByTestId('mdl-historico-analise')
    9 × locator resolved to <div fade="false" role="dialog" tabindex="-1" data-testid="mdl-historico-analise" id="BootstrapVueNext__ID__v-56__modal___" aria-labelledby="BootstrapVueNext__ID__v-56__modal___-label" aria-describedby="BootstrapVueNext__ID__v-56__modal___-body" class="modal fade show stack-position-0 stack-inverse-position-0">…</div>
      - unexpected value "Histórico de análiseData/HoraUnidadeAçãoUsuárioObservaçãoVisualizar12/05/2026 23:30COORD_11DevoluçãoGESTOR_COORD_11Cadastro devolvido: favor verificar as atividades antes de disponibilizar nov... Fechar "

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - generic [ref=e6]:
        - navigation [ref=e7]:
          - generic [ref=e8]:
            - link "SGC" [ref=e9] [cursor=pointer]:
              - /url: /painel
            - generic [ref=e10]:
              - list [ref=e11]:
                - listitem [ref=e12]:
                  - link "Painel" [ref=e13] [cursor=pointer]:
                    - /url: /painel
                    - generic [ref=e14]: 
                    - text: Painel
                - listitem [ref=e15]:
                  - link "Minha unidade" [ref=e16] [cursor=pointer]:
                    - /url: /unidade/6
                    - generic [ref=e17]: 
                    - text: Minha unidade
                - listitem [ref=e18]:
                  - link "Histórico" [ref=e19] [cursor=pointer]:
                    - /url: /historico
                    - generic [ref=e20]: 
                    - text: Histórico
              - list [ref=e21]:
                - listitem [ref=e22]:
                  - link "CHEFE - SECAO_111" [ref=e23] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e24]:
                      - generic [ref=e25]: 
                      - generic [ref=e26]: CHEFE - SECAO_111
                - listitem "Ativar modo escuro" [ref=e27]:
                  - link "Ativar modo escuro" [ref=e28] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e29]: Ativar modo escuro
                    - generic [ref=e30]: 
                - listitem "Sair" [ref=e31]:
                  - link "Sair" [ref=e32] [cursor=pointer]:
                    - /url: "#"
                    - generic [ref=e33]: Sair
                    - generic [ref=e34]: 
        - generic [ref=e37]:
          - button "Voltar" [ref=e38] [cursor=pointer]:
            - generic [ref=e39]: 
          - navigation "breadcrumb" [ref=e40]:
            - list [ref=e41]:
              - listitem [ref=e42]:
                - link "Início" [ref=e43] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e44]: 
                  - generic [ref=e45]: Início
              - listitem [ref=e46]:
                - text: ›
                - link "SECAO_111" [ref=e47] [cursor=pointer]:
                  - /url: /processo/400/SECAO_111
                  - generic [ref=e48]: SECAO_111
              - listitem [ref=e49]:
                - text: ›
                - generic [ref=e50]: Atividades e conhecimentos
      - main [ref=e51]:
        - generic [ref=e52]:
          - generic [ref=e53]:
            - generic [ref=e54]:
              - heading "Atividades e conhecimentos" [level=2] [ref=e55]
              - paragraph [ref=e56]: SECAO_111
            - generic [ref=e57]:
              - generic [ref=e58]:
                - button "Histórico de análise" [ref=e59] [cursor=pointer]:
                  - generic [ref=e60]: 
                  - text: Histórico de análise
                - button "Disponibilizar" [ref=e61] [cursor=pointer]:
                  - generic [ref=e62]: 
                  - text: Disponibilizar
              - button "Importar" [ref=e64] [cursor=pointer]:
                - generic [ref=e65]: 
                - text: Importar
          - generic [ref=e66]:
            - textbox "Nova atividade" [ref=e68]
            - button "Adicionar atividade" [ref=e70] [cursor=pointer]:
              - generic [ref=e71]: 
          - generic [ref=e74]:
            - 'heading "Editar Remover atividade: Atividade 1 Atividade 1" [level=4] [ref=e75]':
              - generic [ref=e76]:
                - generic [ref=e77]:
                  - button "Editar" [ref=e78] [cursor=pointer]:
                    - generic [ref=e79]: 
                  - 'button "Remover atividade: Atividade 1" [ref=e80] [cursor=pointer]':
                    - generic [ref=e81]: 
                - strong [ref=e83]: Atividade 1
            - generic [ref=e84]:
              - generic [ref=e86]:
                - textbox "Novo conhecimento" [ref=e88]
                - button "Adicionar conhecimento" [ref=e90] [cursor=pointer]:
                  - generic [ref=e91]: 
              - generic [ref=e93]:
                - generic [ref=e94]:
                  - button "Editar" [ref=e95] [cursor=pointer]:
                    - generic [ref=e96]: 
                  - 'button "Remover conhecimento: Conhecimento 1.1" [ref=e97] [cursor=pointer]':
                    - generic [ref=e98]: 
                - generic [ref=e99]: Conhecimento 1.1
      - contentinfo [ref=e100]:
        - generic [ref=e101]:
          - generic [ref=e102]: Versão 1.0.4
          - generic [ref=e103]: © SESEL/COSIS/TRE-PE
  - button "Enviar feedback" [ref=e104] [cursor=pointer]:
    - generic [ref=e105]: 
  - text:         
  - dialog "Histórico de análise" [active] [ref=e106]:
    - generic [ref=e107]:
      - generic [ref=e108]:
        - heading "Histórico de análise" [level=5] [ref=e109]
        - button "Close" [ref=e110] [cursor=pointer]
      - table [ref=e115]:
        - rowgroup [ref=e116]:
          - row "Data/Hora Unidade Ação Usuário Observação Visualizar" [ref=e117]:
            - columnheader "Data/Hora" [ref=e118]
            - columnheader "Unidade" [ref=e119]
            - columnheader "Ação" [ref=e120]
            - columnheader "Usuário" [ref=e121]
            - columnheader "Observação" [ref=e122]
            - columnheader "Visualizar" [ref=e123]:
              - generic [ref=e124]: Visualizar
        - rowgroup [ref=e125]:
          - 'row "12/05/2026 23:30 COORD_11 Devolução GESTOR_COORD_11 Cadastro devolvido: favor verificar as atividades antes de disponibilizar nov..." [ref=e126]':
            - cell "12/05/2026 23:30" [ref=e127]
            - cell "COORD_11" [ref=e128]
            - cell "Devolução" [ref=e129]
            - cell "GESTOR_COORD_11" [ref=e130]:
              - generic "GESTOR_COORD_11" [ref=e131]
            - 'cell "Cadastro devolvido: favor verificar as atividades antes de disponibilizar nov..." [ref=e132]':
              - 'generic "Cadastro devolvido: favor verificar as atividades antes de disponibilizar novamente." [ref=e133]': "Cadastro devolvido: favor verificar as atividades antes de disponibilizar nov..."
            - cell [ref=e134]:
              - button [ref=e136] [cursor=pointer]:
                - generic [ref=e137]: 
      - button "Fechar" [ref=e140] [cursor=pointer]
  - text:         
```

# Test source

```ts
  416 |         // O ADMIN cria um novo processo de mapeamento com uma unidade.
  417 |         await criarProcessoSimples(page, {
  418 |             descricao: descProcesso, tipo: 'MAPEAMENTO', unidades: [SIGLA_SECAO]
  419 |         });
  420 | 
  421 |         // Após criar, o sistema retorna ao Painel.
  422 |         await expect(page).toHaveURL(/\/painel/);
  423 | 
  424 |         // No Painel, o processo deve aparecer ainda na situação inicial "Criado".
  425 |         await verificarProcessoTabela(page, {
  426 |             descricao: descProcesso, tipo: 'Mapeamento', situacao: SIT_PROCESSO.CRIADO
  427 |         });
  428 | 
  429 |         // O ADMIN abre os detalhes do processo recém-criado.
  430 |         await acessarDetalhesProcesso(page, descProcesso);
  431 | 
  432 |         // O ADMIN inicia o processo.
  433 |         await iniciarProcesso(page, descProcesso);
  434 | 
  435 |         // Depois da iniciação, o sistema retorna ao Painel.
  436 |         await expect(page).toHaveURL(/\/painel/);
  437 | 
  438 |         // No Painel, o processo deve aparecer como "Em andamento".
  439 |         await verificarProcessoTabela(page, {
  440 |             descricao: descProcesso, tipo: 'Mapeamento', situacao: SIT_PROCESSO.EM_ANDAMENTO
  441 |         });
  442 | 
  443 |         // O ADMIN reabre os detalhes do processo iniciado.
  444 |         await acessarDetalhesProcesso(page, descProcesso);
  445 | 
  446 |         // O ADMIN abre os detalhes do subprocesso criado para a seção participante.
  447 |         await navegarParaSubprocesso(page, SIGLA_SECAO);
  448 | 
  449 |         // O subprocesso da unidade deve existir, estar "Não iniciado" e localizado na própria unidade.
  450 |         await expect(page.getByTestId('header-subprocesso')).toBeVisible();
  451 |         await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(SIGLA_SECAO);
  452 |         await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(SIT_SUBPROCESSO.NAO_INICIADO);
  453 |         await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toContainText(SIGLA_SECAO);
  454 |     });
  455 | 
  456 |     test('Fase 2 - CHEFE cadastra atividades e a hierarquia analisa o cadastro', async ({page}) => {
  457 |         // O CHEFE da seção faz login.
  458 |         await login(page, CHEFE_SECAO.titulo, CHEFE_SECAO.senha);
  459 | 
  460 |         // O CHEFE abre o subprocesso da sua unidade.
  461 |         await acessarSubprocessoChefeDireto(page, descProcesso, SIGLA_SECAO);
  462 | 
  463 |         // O subprocesso deve começar "Não iniciado" e permanecer localizado na própria seção.
  464 |         await verificarDetalhesSubprocesso(page, {
  465 |             sigla: SIGLA_SECAO,
  466 |             situacao: 'Não iniciado',
  467 |             localizacao: SIGLA_SECAO
  468 |         });
  469 | 
  470 |         // O CHEFE abre a tela de atividades e conhecimentos.
  471 |         await navegarParaCadastro(page);
  472 | 
  473 |         // O CHEFE registra uma atividade e um conhecimento da unidade.
  474 |         await adicionarAtividade(page, DESC_ATIVIDADE);
  475 |         await adicionarConhecimento(page, DESC_ATIVIDADE, DESC_CONHECIMENTO);
  476 | 
  477 |         // O CHEFE disponibiliza o cadastro para análise das unidades superiores.
  478 |         await disponibilizarCadastro(page);
  479 | 
  480 |         // O GESTOR da coordenadoria faz login.
  481 |         await login(page, GESTOR_COORDENADORIA.titulo, GESTOR_COORDENADORIA.senha);
  482 | 
  483 |         // O GESTOR da coordenadoria abre o subprocesso da seção.
  484 |         await acessarSubprocessoGestor(page, descProcesso, SIGLA_SECAO);
  485 | 
  486 | 
  487 |         // O cadastro deve chegar à coordenadoria já disponibilizado para análise.
  488 |         await verificarDetalhesSubprocesso(page, {
  489 |             sigla: SIGLA_SECAO,
  490 |             situacao: SIT_CADASTRO_DISPONIBILIZADO,
  491 |             localizacao: SIGLA_COORDENADORIA
  492 |         });
  493 | 
  494 |         // O GESTOR da coordenadoria abre a tela de atividades, consulta o histórico e devolve o cadastro.
  495 |         await navegarParaCadastro(page);
  496 |         const modalHistoricoCoord = await abrirHistoricoAnalise(page);
  497 |         await expect(modalHistoricoCoord).toBeVisible();
  498 |         await fecharHistoricoAnalise(page);
  499 |         await devolverCadastroMapeamento(page, 'Cadastro devolvido: favor verificar as atividades antes de disponibilizar novamente.');
  500 | 
  501 |         // Após a devolução, o CHEFE recebe o cadastro de volta na sua unidade.
  502 |         await login(page, CHEFE_SECAO.titulo, CHEFE_SECAO.senha);
  503 |         await acessarSubprocessoChefeDireto(page, descProcesso, SIGLA_SECAO);
  504 | 
  505 |         // O cadastro deve retornar ao estado "Cadastro em andamento" na seção.
  506 |         await verificarDetalhesSubprocesso(page, {
  507 |             sigla: SIGLA_SECAO,
  508 |             situacao: 'Cadastro em andamento',
  509 |             localizacao: SIGLA_SECAO
  510 |         });
  511 | 
  512 |         // O CHEFE abre o cadastro e verifica o histórico de devolução.
  513 |         await navegarParaCadastro(page);
  514 |         const modalHistoricoChefe = await abrirHistoricoAnalise(page);
  515 |         await expect(modalHistoricoChefe.getByTestId('cell-resultado-0')).toHaveText(/Devolu/i);
> 516 |         await expect(modalHistoricoChefe).toContainText('Cadastro devolvido: favor verificar as atividades antes de disponibilizar novamente.');
      |                                           ^ Error: expect(locator).toContainText(expected) failed
  517 |         await fecharHistoricoAnalise(page);
  518 | 
  519 |         // O CHEFE re-disponibiliza o cadastro (sem precisar alterar dados).
  520 |         await disponibilizarCadastro(page);
  521 | 
  522 |         // O GESTOR da coordenadoria faz login novamente e aceita o cadastro após correção.
  523 |         await login(page, GESTOR_COORDENADORIA.titulo, GESTOR_COORDENADORIA.senha);
  524 | 
  525 |         // O GESTOR da coordenadoria abre o subprocesso da seção.
  526 |         await acessarSubprocessoGestor(page, descProcesso, SIGLA_SECAO);
  527 | 
  528 |         // O cadastro deve chegar à coordenadoria disponibilizado para análise novamente.
  529 |         await verificarDetalhesSubprocesso(page, {
  530 |             sigla: SIGLA_SECAO,
  531 |             situacao: SIT_CADASTRO_DISPONIBILIZADO,
  532 |             localizacao: SIGLA_COORDENADORIA
  533 |         });
  534 | 
  535 |         // O GESTOR da coordenadoria aceita o cadastro após a correção do CHEFE.
  536 |         await navegarParaCadastro(page);
  537 |         await aceitarCadastroMapeamento(page, 'Aceite da COORD_11 após correção.');
  538 | 
  539 |         // O GESTOR da secretaria faz login.
  540 |         await loginComPerfil(page, GESTOR_SECRETARIA.titulo, GESTOR_SECRETARIA.senha, GESTOR_SECRETARIA.perfil!);
  541 | 
  542 |         // O GESTOR da secretaria abre o subprocesso da mesma seção.
  543 |         await acessarSubprocessoGestor(page, descProcesso, SIGLA_SECAO);
  544 | 
  545 |         // O cadastro deve subir para a secretaria ainda como item pendente de análise.
  546 |         await verificarDetalhesSubprocesso(page, {
  547 |             sigla: SIGLA_SECAO,
  548 |             situacao: SIT_CADASTRO_DISPONIBILIZADO,
  549 |             localizacao: SIGLA_SECRETARIA
  550 |         });
  551 | 
  552 |         // O GESTOR da secretaria registra aceite direto (sem devolução), cobrindo o caminho positivo.
  553 |         await navegarParaCadastro(page);
  554 |         await aceitarCadastroMapeamento(page, 'Obs. cadastro aceito');
  555 | 
  556 |         // O ADMIN faz login.
  557 |         await login(page, ADMIN.titulo, ADMIN.senha);
  558 | 
  559 |         // O ADMIN abre o subprocesso da seção.
  560 |         await acessarSubprocessoAdmin(page, descProcesso, SIGLA_SECAO);
  561 | 
  562 |         // O cadastro deve chegar ao ADMIN ainda disponibilizado para homologação final.
  563 |         await verificarDetalhesSubprocesso(page, {
  564 |             sigla: SIGLA_SECAO,
  565 |             situacao: SIT_CADASTRO_DISPONIBILIZADO,
  566 |             localizacao: SIGLA_ADMIN
  567 |         });
  568 | 
  569 |         // O ADMIN abre a tela de atividades e homologa o cadastro.
  570 |         await navegarParaCadastro(page);
  571 |         await homologarCadastroMapeamento(page, "Cadastro homologado OK");
  572 | 
  573 |         // Após homologar, o subprocesso deve indicar cadastro homologado.
  574 |         await verificarDetalhesSubprocesso(page, {
  575 |             sigla: SIGLA_SECAO,
  576 |             situacao: SIT_CADASTRO_HOMOLOGADO,
  577 |             localizacao: SIGLA_ADMIN
  578 |         });
  579 | 
  580 |         // Após a homologação, o card de mapa deve ficar disponível para a próxima etapa.
  581 |         await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
  582 |     });
  583 | 
  584 |     test('Fase 3 - ADMIN cria o mapa do mapeamento e a hierarquia valida até a homologação', async ({page}) => {
  585 |         await test.step('ADMIN estrutura e disponibiliza o primeiro mapa da seção', async () => {
  586 |             await adminCriaEDisponibilizaMapaInicial(page);
  587 |         });
  588 | 
  589 |         await test.step('CHEFE valida o mapa disponibilizado sem perder o contexto carregado da SPA', async () => {
  590 |             await chefeConsultaEValidaMapaDisponibilizado(page);
  591 |         });
  592 | 
  593 |         await test.step('COORDENADORIA e SECRETARIA registram os dois aceites hierárquicos', async () => {
  594 |             await coordenadoriaAceitaValidacaoDoMapa(page);
  595 |             await secretariaAceitaValidacaoDoMapa(page);
  596 |         });
  597 | 
  598 |         await test.step('ADMIN homologa o mapa e encerra o processo de mapeamento', async () => {
  599 |             await adminHomologaMapaEFinalizaMapeamento(page);
  600 |         });
  601 | 
  602 |         await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  603 |     });
  604 | 
  605 |     test('Fase 4 - ADMIN cria e inicia o processo de revisão da mesma seção', async ({page}) => {
  606 |         await test.step('ADMIN abre a revisão já apoiada no mapa vigente da seção', async () => {
  607 |             await adminCriaEIniciaProcessoDeRevisao(page);
  608 |         });
  609 | 
  610 |         await test.step('CHEFE confirma que a revisão nasce com o cadastro vigente e botão de impactos disponível', async () => {
  611 |             await chefeConfereBaseVigenteEDisponibilizaRevisao(page);
  612 |         });
  613 | 
  614 |         await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
  615 |     });
  616 | 
```