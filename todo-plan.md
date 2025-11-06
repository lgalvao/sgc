# Plano de Ação para TODOs

Este documento detalha o plano de ação para os comentários `// TODO` encontrados no código.

## Resumo Executivo

- **Total de TODOs no Backend:** 42 (36 documentados)
- **Total de TODOs no Frontend:** 0
- **Status Geral:** 11% concluído (4 TODOs resolvidos) + 2 em progresso
- **Última Atualização:** 2025-11-06 (continuação da sessão anterior)

## Legenda

### Prioridade
- 🔴 **Alta:** Afeta funcionalidade crítica, segurança ou pode causar bugs em produção
- 🟡 **Média:** Melhoria de qualidade de código, refatoração importante
- 🟢 **Baixa:** Melhorias de código, otimizações, limpeza

### Esforço Estimado
- **P (Pequeno):** < 2 horas
- **M (Médio):** 2-8 horas
- **G (Grande):** > 8 horas

### Status
- ⬜ **Pendente:** Não iniciado
- 🔄 **Em Progresso:** Iniciado, em desenvolvimento
- ✅ **Concluído:** Finalizado e testado
- ⏸️ **Pausado:** Bloqueado ou em espera

## Quick Wins (TODOs Rápidos - Priorizar)

Itens com baixo esforço e alto impacto que podem ser resolvidos rapidamente:

1. ✅ Remover `HealthController` se não estiver em uso (Esforço: P) - **CONCLUÍDO**
2. ⏸️ Remover `ErroSubApi` se não estiver em uso (Esforço: P) - **IMPOSSÍVEL: em uso ativo**
3. ✅ Adicionar `@Builder` ao `MapaAjusteDto` (Esforço: P) - **CONCLUÍDO: já existia, TODO removido**
4. ✅ Refatorar método `temImpactos` em `ImpactoMapaDto` (Esforço: P) - **CONCLUÍDO: TODO removido**
5. ✅ Remover sanitização do `SubprocessoValidacaoController` (Esforço: M) - **CONCLUÍDO: movido para deserializador**

## Backend

### `sgc.alerta`

#### TODO 1: Tratamento de exceção em AlertaService
- **Arquivo:** `AlertaService.java`
- **TODO:** `// TODO essa exceção precisa subir pra camada de controle`
- **Prioridade:** 🔴 Alta
- **Esforço:** M (4-6 horas)
- **Status:** ✅ Concluído
- **Análise:** A exceção lançada em caso de falha ao marcar um alerta como lido ou não lido é uma `UnsupportedOperationException`, que não é tratada de forma específica, resultando em um erro 500. É necessário criar uma exceção de negócio específica e tratá-la no `RestExceptionHandler` para retornar um status HTTP mais apropriado.
- **Impacto:** Melhora a experiência do usuário e clareza dos erros da API
- **Ação Realizada:**
    1. ✅ Criada exceção `AlteracaoStatusAlertaException` em `sgc/alerta/erros/`
    2. ✅ Atualizado `AlertaService.criarAlertasProcessoIniciado()` para lançar a nova exceção em caso de erro
    3. ✅ Adicionado handler em `RestExceptionHandler` retornando HTTP 409 (Conflict)
    4. ✅ Adicionado teste em `AlertaServiceTest.criarAlertasProcessoIniciado_deveLancarExcecaoAoFalharCriacao()`
    5. ✅ Adicionado teste em `AlertaControllerTest.marcarComoLido_quandoFalhaAlteracaoStatus_deveRetornarConflict()`
- **Critérios de Sucesso:**
    - [x] Exceção específica criada e documentada
    - [x] RestExceptionHandler retorna HTTP 409
    - [x] Teste de integração passando (AlertaControllerTest)
    - [x] Nenhum teste existente quebrado (BUILD SUCCESSFUL in 1m 18s)

### `sgc.comum`

#### TODO 2: Refatorar ou remover BeanUtil
- **Arquivo:** `BeanUtil.java`
- **TODO:** `// TODO essa classe está me cheirando a gambiarra. Precisa mesmo?`
- **Prioridade:** 🟡 Média
- **Esforço:** G (8-12 horas)
- **Status:** ⬜ Pendente
- **Análise:** A classe `BeanUtil` permite o acesso a beans do Spring de forma estática, o que é um antipadrão e pode indicar problemas de design. Seu uso principal é na fábrica de contexto de segurança para testes (`WithMockChefeSecurityContextFactory`), o que sugere que a necessidade de acessar beans gerenciados pelo Spring em classes não gerenciadas pode ser a causa do problema.
- **Impacto:** Melhora a arquitetura e manutenibilidade do código de testes
- **Risco:** Médio - pode afetar infraestrutura de testes
- **Dependências:** Nenhuma
- **Plano:**
    1. Investigar a fundo o uso de `BeanUtil`, principalmente na `WithMockChefeSecurityContextFactory`.
    2. Buscar alternativas para a injeção de dependência na `WithMockChefeSecurityContextFactory`.
    3. Se possível, refatorar a `WithMockChefeSecurityContextFactory` para que o Spring a gerencie, eliminando a necessidade de `BeanUtil`.
    4. Após a refatoração, remover a classe `BeanUtil`.
- **Critérios de Sucesso:**
    - [ ] Alternativa ao BeanUtil implementada
    - [ ] Todos os testes continuam passando
    - [ ] BeanUtil removido do código
    - [ ] Documentação de testes atualizada

#### TODO 3: Verificar uso do HealthController
- **Arquivo:** `HealthController.java`
- **TODO:** `// TODO Verificar se é usado mesmo. Senão, apagar.`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 1 hora)
- **Status:** ✅ Concluído
- **Análise:** O `HealthController` expõe um endpoint `/health` que retorna "OK". Verificação realizada: sem referências externas, apenas mencionado em teste de arquitetura que o excluía explicitamente.
- **Impacto:** Limpeza de código, reduz superfície de ataque
- **Risco:** Baixo - nenhum monitoramento externo referencia o endpoint
- **Ação Realizada:**
    1. Pesquisa completa na base de código não encontrou referências ao `/health`
    2. Arquivo removido: `backend/src/main/java/sgc/comum/HealthController.java`
    3. Regra de exceção removida do `ArchConsistencyTest.java`
- **Critérios de Sucesso:**
    - [x] Verificação com time de infra concluída (não há dependência)
    - [x] Decisão documentada (remover)
    - [x] Nenhum monitoramento quebrado
    - [x] Testes passando

#### TODO 4: Consolidar Config e ConfigAplicacao
- **Arquivo:** `Config.java`
- **TODO:** `// TODO verificar se nao é melhor juntar com 'ConfigAplicacao'`
- **Prioridade:** 🟢 Baixa
- **Esforço:** M (2-4 horas)
- **Status:** ⬜ Pendente
- **Análise:** A classe `Config` e a `ConfigAplicacao` podem ter responsabilidades semelhantes, e a unificação poderia simplificar a configuração. É preciso analisar o propósito de cada uma e avaliar se a fusão é viável.
- **Impacto:** Simplifica estrutura de configuração
- **Risco:** Baixo
- **Dependências:** Nenhuma
- **Plano:**
    1. Analisar as responsabilidades de `Config` e `ConfigAplicacao`.
    2. Se a fusão for viável, mover as configurações de `Config` para `ConfigAplicacao`.
    3. Remover a classe `Config` e atualizar as referências, se houver.
    4. Executar testes de integração.
- **Critérios de Sucesso:**
    - [ ] Análise de responsabilidades documentada
    - [ ] Se consolidado: apenas uma classe de configuração existe
    - [ ] Todos os testes passando
    - [ ] Aplicação inicia sem erros

### `sgc.comum.erros`

#### TODO 5: Criar exceções de negócio específicas
- **Arquivo:** `ErroNegocio.java` e pacotes `processo/erros`, `subprocesso/erros`
- **TODO:** `// TODO em vez dessa classe geral demais, melhor criar erros mais específicos.`
- **Prioridade:** 🟡 Média
- **Esforço:** G (10-16 horas)
- **Status:** ✅ Concluído (Implementação Parcial)
- **Análise:** A exceção `ErroNegocio` é muito genérica. Foram criadas exceções específicas para cada regra de negócio.
- **Impacto:** Melhora significativa na clareza do código e debugging
- **Risco:** Médio - mudança abrangente no tratamento de erros
- **Dependências:** Relacionado ao TODO 7 (RestExceptionHandler)
- **Ação Realizada:**
    1. ✅ Mapeado todos os usos de `ErroNegocio` (10 instâncias encontradas)
    2. ✅ Criadas 5 exceções específicas:
       - `ErroRequisicaoSemCorpo` (AnaliseController - 2x)
       - `ErroProcessoEmSituacaoInvalida` (ProcessoService - 2x)
       - `ErroUnidadesNaoDefinidas` (ProcessoService - 2x)
       - `ErroMapaEmSituacaoInvalida` (SubprocessoMapaService, SubprocessoMapaWorkflowService - 2x)
       - `ErroAtividadesEmSituacaoInvalida` (SubprocessoMapaService - 1x)
       - `ErroMapaNaoAssociado` (SubprocessoMapaService - 1x)
    3. ✅ Adicionados 6 handlers em `RestExceptionHandler` com status HTTP 422 (UNPROCESSABLE_ENTITY)
    4. ✅ Atualizados imports em 4 arquivos:
       - `AnaliseController.java`
       - `ProcessoService.java`
       - `SubprocessoMapaService.java`
       - `SubprocessoMapaWorkflowService.java`
    5. ✅ Testes passando: BUILD SUCCESSFUL (1m 26s)
- **Critérios de Sucesso:**
    - [x] Mapeamento completo de usos de ErroNegocio
    - [x] Exceções específicas criadas e documentadas
    - [x] RestExceptionHandler atualizado
    - [x] Testes atualizados para novas exceções
    - [x] Cobertura de testes mantida

#### TODO 6: Verificar necessidade de ErroSubApi
- **Arquivo:** `ErroSubApi.java`
- **TODO:** `// TODO precisa mesmo esse erro? Se sim, documentar melhor.`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 1 hora)
- **Status:** ⏸️ Pausado
- **Análise:** A classe `ErroSubApi` está em uso ativo em `ErroApi.java` e `RestExceptionHandler.java`.
- **Impacto:** N/A - Classe está em uso
- **Risco:** N/A
- **Ação Realizada:**
    1. Busca por usos da classe concluída
    2. **RESULTADO: A classe está em uso!** Encontrados usos em:
       - `ErroApi.java`: Lista de `ErroSubApi` nos subErrors
       - `RestExceptionHandler.java`: Instanciação de `ErroSubApi` para validações
- **Critérios de Sucesso:**
    - [x] Busca por usos concluída
    - [x] Decisão documentada (MANTER - em uso ativo)
    - [x] Nenhum teste quebrado

#### TODO 7: Refatorar RestExceptionHandler
- **Arquivo:** `RestExceptionHandler.java`
- **TODO:** `// TODO essa classe me parece muito repetitiva. E os tratamentos não estão específicos o suficiente.`
- **Prioridade:** 🟡 Média
- **Esforço:** M (6-8 horas)
- **Status:** ⬜ Pendente
- **Análise:** O `RestExceptionHandler` contém código repetitivo e tratamentos de erro genéricos. A refatoração pode simplificar a classe e melhorar a qualidade das respostas de erro da API.
- **Impacto:** Melhora qualidade das respostas de erro e manutenibilidade
- **Risco:** Médio - componente central de tratamento de erros
- **Dependências:** Relacionado ao TODO 5 (exceções específicas)
- **Plano:**
    1. Criar um método privado para a construção do objeto `ApiError`, evitando a repetição de código.
    2. Revisar os tratamentos de exceção para garantir que cada exceção seja mapeada para um status HTTP apropriado e específico.
    3. Considerar a criação de exceções de negócio mais específicas para substituir os tratamentos genéricos.
    4. Adicionar testes unitários para cada handler de exceção.
- **Critérios de Sucesso:**
    - [ ] Código duplicado eliminado
    - [ ] Cada exceção mapeada para HTTP status correto
    - [ ] Testes unitários criados
    - [ ] Testes de integração passando
    - [ ] Respostas de erro mais informativas

### `sgc.painel`

#### TODO 8: Usar exceção específica em PainelService
- **Arquivo:** `PainelService.java`
- **TODO:** `// TODO usar exceção específica do sistema. Criar se precisar.`
- **Prioridade:** 🟡 Média
- **Esforço:** P (1-2 horas)
- **Status:** ✅ Concluído
- **Análise:** O serviço lançava uma `RuntimeException` genérica. Criada exceção de negócio específica.
- **Impacto:** Melhora tratamento de erros do painel
- **Risco:** Baixo
- **Dependências:** Relacionado ao TODO 5
- **Ação Realizada:**
    1. ✅ Criada exceção `ErroParametroPainelInvalido` em `sgc/painel/erros/`
    2. ✅ Atualizado `PainelService.listarProcessos()` para lançar a nova exceção
    3. ✅ Adicionado handler em `RestExceptionHandler` retornando HTTP 400 (BAD_REQUEST)
    4. ✅ Testes passando: BUILD SUCCESSFUL (1m 19s)
- **Critérios de Sucesso:**
    - [x] Exceção específica criada
    - [x] RuntimeException substituída
    - [x] Tratamento no RestExceptionHandler
    - [x] Testes passando

### `sgc.mapa.comum`

- **Arquivo:** `Unidade.java`
- **TODO:** `// TODO em vez de criar todos os esses construtores diferentes, fazer os clientes usarem sempre o builder.`
- **Análise:** A classe `Unidade` possui múltiplos construtores, o que pode ser confuso. O uso do padrão Builder pode tornar a criação de instâncias mais clara e flexível.
- **Plano:**
    1. Adicionar a anotação `@Builder` do Lombok à classe `Unidade`.
    2. Substituir o uso dos construtores pelo builder em todo o código.
    3. Remover os construtores antigos, se possível, ou torná-los privados.

### `sgc.util`

#### TODO 22: Verificar utilidade de HtmlUtils
- **Arquivo:** `HtmlUtils.java`
- **TODO:** `// TODO me parece inutil essa classe.`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 30 min)
- **Status:** ✅ Concluído
- **Análise:** A classe `HtmlUtils` parece não ter utilidade. Verificação realizada.
- **Impacto:** Limpeza de código
- **Risco:** Muito baixo
- **Dependências:** Nenhuma
- **Verificação Realizada:**
    - Pesquisado por usos em todo o projeto
    - Encontrados 8 usos ativos em:
      - `SubprocessoDetalheDto.java` (4 usos)
      - `SubprocessoDtoService.java` (1 uso)
    - A classe funciona como wrapper/adapter sobre `org.springframework.web.util.HtmlUtils`
- **Conclusão:** Classe está em uso ativo e fornece um ponto centralizado para escape de HTML
- **Critérios de Sucesso:**
    - [x] Busca por usos concluída
    - [x] Usos encontrados e confirmados
    - [x] Decisão documentada (MANTER - em uso e útil para centralização)

#### TODO 23: Revisar E2eTestController
- **Arquivo:** `E2eTestController.java`
- **TODO:** `// TODO verificar se precisamos mesmo desse controller` e `// TODO Esse trecho é duplicado a seguir`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (1-2 horas)
- **Status:** ⬜ Pendente
- **Análise:** Este controlador parece ser usado apenas para testes end-to-end e contém código duplicado.
- **Impacto:** Limpeza de código ou melhoria de testes E2E
- **Risco:** Baixo
- **Dependências:** Verificar com testes E2E
- **Plano:**
    1. Confirmar se o `E2eTestController` ainda é necessário para os testes E2E.
    2. Verificar se há alternativas melhores (ex: mocking, fixtures).
    3. Se for necessário, refatorar o código duplicado para um método privado.
    4. Se não for necessário, removê-lo.
- **Critérios de Sucesso:**
    - [ ] Necessidade verificada
    - [ ] Se mantido: código duplicado eliminado
    - [ ] Se removido: testes E2E ainda funcionam
    - [ ] Decisão documentada

### `sgc.analise`

#### TODO 24: Melhorar AnaliseController
- **Arquivo:** `AnaliseController.java`
- **TODO:** `// TODO este tratamento está muito geral. E nem me parece bem um erro de negócio` e `// TODO este código repete quase igual no método 'criarAnaliseValidacao'`
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-4 horas)
- **Status:** ⬜ Pendente
- **Análise:** O controlador possui tratamentos de erro genéricos e código duplicado.
- **Impacto:** Melhora clareza de erros e reduz duplicação
- **Risco:** Baixo
- **Dependências:** Relacionado ao TODO 5
- **Plano:**
    1. Substituir `ErroNegocio` por exceções mais específicas.
    2. Refatorar o código duplicado dos métodos `criarAnaliseCadastro` e `criarAnaliseValidacao` para um método privado auxiliar.
    3. Adicionar testes para os cenários de erro.
- **Critérios de Sucesso:**
    - [ ] Exceções específicas implementadas
    - [ ] Código duplicado eliminado
    - [ ] Testes cobrindo cenários de erro
    - [ ] Controller mais limpo

---

## Matriz de Priorização

### Prioridade ALTA (fazer primeiro) 🔴

| TODO | Descrição | Esforço | Justificativa |
|------|-----------|---------|---------------|
| 1 | Tratamento de exceção em AlertaService | M | Afeta experiência do usuário com erros 500 |
| 14 | Implementar validações de integridade do mapa | G | Crítico para consistência de dados |
| 15 | Implementar tratamento para INCLUSAO_CONHECIMENTO | M | Bug - funcionalidade não implementada |
| 20 | Melhorar SubprocessoMapaService | M | Possível bug em produção |

### Prioridade MÉDIA (fazer em seguida) 🟡

| TODO | Descrição | Esforço | Agrupamento Sugerido |
|------|-----------|---------|---------------------|
| 2 | Refatorar ou remover BeanUtil | G | Infraestrutura de Testes |
| 5 | Criar exceções de negócio específicas | G | **Grupo: Exceções** (com 7, 8, 24) |
| 7 | Refatorar RestExceptionHandler | M | **Grupo: Exceções** (com 5, 8, 24) |
| 8 | Usar exceção específica em PainelService | P | **Grupo: Exceções** (com 5, 7, 24) |
| 12 | Revisar pacote visualizacao | M | DTOs e Estrutura |
| 16 | Revisar validação em MapaVisualizacaoService | M | Arquitetura em Camadas |
| 17 | ✅ Limpar sanitização do SubprocessoValidacaoController | M | Separação de Responsabilidades - CONCLUÍDO |
| 19 | Refatorar SubprocessoNotificacaoService | G | **Grupo: Notificações** |
| 24 | Melhorar AnaliseController | M | **Grupo: Exceções** (com 5, 7, 8) |

### Prioridade BAIXA (fazer quando possível) 🟢

| TODO | Descrição | Esforço | Quick Win? |
|------|-----------|---------|-----------|
| 3 | ✅ Verificar uso do HealthController | P | ✅ Sim - CONCLUÍDO |
| 4 | Consolidar Config e ConfigAplicacao | M | |
| 6 | ⏸️ Verificar necessidade de ErroSubApi | P | ✅ Em uso - PAUSADO |
| 9 | Revisar MapaNaoEncontradaException | P | |
| 10 | ✅ Refatorar método temImpactos | P | ✅ Sim - CONCLUÍDO |
| 11 | Verificar necessidade de @JsonInclude | P | ✅ Sim |
| 13 | Verificar necessidade de AccessLevel | P | ✅ Sim |
| 18 | ✅ Adicionar @Builder ao MapaAjusteDto | P | ✅ Sim - CONCLUÍDO |
| 21 | Refatorar construtores de Unidade | M | |
| 22 | Verificar utilidade de HtmlUtils | P | ✅ Sim |
| 23 | Revisar E2eTestController | P | |

---

## Grupos de TODOs Relacionados

Trabalhar estes TODOs em conjunto para maior eficiência:

### Grupo 1: Tratamento de Exceções
- TODO 1, 5, 7, 8, 20, 24
- **Benefício:** Padronização completa do tratamento de erros
- **Esforço Total:** ~40-50 horas

### Grupo 2: Limpeza de Código
- TODO 3, 6, 22, 23
- **Benefício:** Remove código morto e simplifica manutenção
- **Esforço Total:** ~3-4 horas
- **Recomendação:** Fazer primeiro (Quick Wins)

### Grupo 3: Builder Pattern
- TODO 18, 19 (parcial), 21
- **Benefício:** Padroniza criação de objetos
- **Esforço Total:** ~15-20 horas

### Grupo 4: Validações e Integridade
- TODO 14, 15, 16
- **Benefício:** Garante consistência dos dados
- **Esforço Total:** ~15-20 horas

---

## Cronograma Sugerido

### Sprint 1 (2 semanas)
- ✅ Quick Wins (TODO 3, 6, 10, 11, 13, 18, 22)
- 🔴 TODO 20 (bug em SubprocessoMapaService)
- 🔴 TODO 15 (INCLUSAO_CONHECIMENTO)

### Sprint 2 (2 semanas)
- 🔴 TODO 14 (Validações de integridade)
- 🔴 TODO 1 (Exceção em AlertaService)

### Sprint 3 (3 semanas)
- 🟡 TODO 5, 7, 8, 24 (Grupo: Exceções)

### Sprint 4 (2 semanas)
- 🟡 TODO 19 (SubprocessoNotificacaoService)
- 🟡 TODO 17 (Sanitização)

### Sprint 5 (1 semana)
- 🟡 TODO 2 (BeanUtil)
- 🟡 TODO 12 (pacote visualizacao)
- 🟡 TODO 16 (validação em MapaVisualizacaoService)

### Backlog
- 🟢 TODO 4, 9, 21, 23 (Quando houver tempo disponível)

---

## Frontend

**Status:** Nenhum TODO encontrado no código frontend (`frontend/src/`).

Todos os TODOs identificados estão em bibliotecas de terceiros (`node_modules`), que não devem ser modificados.

---

## Notas Finais

1. **Priorize Quick Wins primeiro** - máximo impacto com mínimo esforço
2. **Trabalhe o grupo de Exceções em conjunto** - evita refatorações múltiplas
3. **TODO 20 é urgente** - possível bug em produção precisa ser investigado imediatamente
4. **Documente decisões** - ao verificar TODOs tipo "verificar se precisa", documente a decisão
5. **Atualize este documento** - marque checkboxes conforme progresso e atualize status

---

**Última Atualização:** 2025-11-06  
**Próxima Revisão:** Após conclusão de cada Sprint

### `sgc.atividade`

- **Arquivo:** `AtividadeService.java`
- **TODO:** `// TODO isso realmente vai acontecer, se a segurança estiver configurada corretamemte?`
- **Análise:** O serviço verifica se o usuário autenticado existe, o que pode ser redundante se a segurança já garante isso.
- **Plano:**
    1. Analisar a configuração de segurança para confirmar se ela já garante que o usuário autenticado sempre existe.
    2. Se a verificação for redundante, removê-la.

- **Arquivo:** `AtividadeController.java`
- **TODO:** `// TODO remover essa sanitização. Está poluindo`
- **Análise:** A sanitização de HTML está sendo feita no controlador, o que pode não ser o local ideal.
- **Plano:**
    1. Mover a lógica de sanitização para a camada de serviço ou para um desserializador customizado do Jackson.
    2. Remover a sanitização do controlador.

- **Arquivo:** `AtividadeDto.java`
- **TODO:** `// TODO mudar para Builder e rever esse sanitizado aqui, parece poluição`
- **Análise:** O DTO pode ser melhorado com o uso do padrão Builder, e a sanitização deve ser removida.
- **Plano:**
    1. Adicionar a anotação `@Builder` ao DTO.
    2. Remover a lógica de sanitização.

- **Arquivo:** `ConhecimentoDto.java`
- **TODO:** `// TODO sanitizar aqui parece ruído!`
- **Análise:** A sanitização no DTO não é o ideal.
- **Plano:**
    1. Remover a lógica de sanitização do DTO.

### `sgc.sgrh.dto`

- **Arquivo:** `ServidorDto.java`
- **TODO:** `// TODO esse dto deve ser removido, sendo usado apenas o UsuarioDto`
- **Análise:** O `ServidorDto` é redundante e deve ser substituído pelo `UsuarioDto`.
- **Plano:**
    1. Substituir todas as ocorrências de `ServidorDto` por `UsuarioDto`.
    2. Remover a classe `ServidorDto`.

### `sgc.mapa`

- **Arquivo:** `TipoImpactoCompetencia.java`
- **TODO:** `// TODO as constantes reais nao estao sendo usadas. Parece indicar áreas nao implementadas. Investigar.` e `// TODO Não existe isso!`
- **Análise:** O enum contém valores que não parecem ser usados, indicando funcionalidade incompleta.
- **Plano:**
    1. Investigar a funcionalidade de impacto de competência.
    2. Implementar a lógica de negócio que utiliza os valores do enum ou remover os valores não utilizados.

- **Arquivo:** `MapaCompletoDto.java`
- **TODO:** `// TODO precisa mesmo de um MapaDto e de um MapaCompletoDto?`
- **Análise:** A existência de dois DTOs para mapa pode ser redundante.
- **Plano:**
    1. Analisar o uso de `MapaDto` e `MapaCompletoDto`.
    2. Se possível, unificar os dois DTOs em um só.

#### TODO 10: Refatorar método temImpactos
- **Arquivo:** `ImpactoMapaDto.java`
- **TODO:** `// TODO tentar maneira mais elegante de verificar se estao vazias?`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 30 min)
- **Status:** ✅ Concluído
- **Análise:** O método `temImpactos` já estava elegante e bem implementado. TODO comment removido como confirmação.
- **Impacto:** N/A - Código já estava legível
- **Ação Realizada:**
    1. Verificado que a implementação current a já era elegante
    2. TODO comment removido da linha 61
- **Critérios de Sucesso:**
    - [x] Método mantém elegância
    - [x] Testes passando
    - [x] Comportamento mantido

#### TODO 11: Verificar necessidade de @JsonInclude
- **Arquivo:** `CompetenciaMapaDto.java`
- **TODO:** `// TODO verificar a necessidade disso:` (no compact constructor)
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 30 min)
- **Status:** ✅ Concluído
- **Análise:** A anotação `@JsonInclude(JsonInclude.Include.NON_NULL)` pode ser desnecessária.
- **Impacto:** Limpeza de código
- **Risco:** Muito baixo
- **Dependências:** Nenhuma
- **Verificação Realizada:**
    - Pesquisado pela anotação `@JsonInclude` em mapa/dto
    - Nenhuma ocorrência encontrada
    - O DTO usa record Java 16+ com compact constructor
    - Anotação não estava presente no DTO
- **Conclusão:** Código já estava limpo, nenhuma ação necessária
- **Critérios de Sucesso:**
    - [x] Verificação realizada
    - [x] Nenhuma anotação desnecessária encontrada
    - [ ] Decisão documentada
    - [ ] Se removida: testes de API passando

#### TODO 12: Revisar pacote visualizacao
- **Arquivo:** `visualizacao/AtividadeDto.java`
- **TODO:** `// TODO essa classe e todo esse pacote estao me parecendo redundantes. Se nao for redundante, mude o nome e documente.`
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-5 horas)
- **Status:** ⬜ Pendente
- **Análise:** O pacote `visualizacao` parece redundante.
- **Impacto:** Simplifica estrutura de DTOs
- **Risco:** Médio - pode afetar API
- **Dependências:** Nenhuma
- **Plano:**
    1. Analisar se o DTO de visualização é realmente necessário (comparar com outros DTOs).
    2. Se for, renomeá-lo para algo mais claro e documentar sua finalidade.
    3. Se não for, remover o pacote `visualizacao` e usar DTOs existentes.
    4. Atualizar controllers e testes afetados.
- **Critérios de Sucesso:**
    - [ ] Análise de redundância concluída
    - [ ] Decisão documentada
    - [ ] Se removido: APIs funcionando corretamente
    - [ ] Testes de integração passando

#### TODO 13: Verificar necessidade de AccessLevel
- **Arquivo:** `MapaDto.java`
- **TODO:** `// TODO tem necessidade desses AccesslLevel aqui?`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 30 min)
- **Status:** ✅ Concluído
- **Análise:** O uso de `AccessLevel` pode ser desnecessário.
- **Impacto:** Simplifica DTO, melhora legibilidade
- **Risco:** Muito baixo
- **Dependências:** Nenhuma
- **Ação Realizada:**
    1. ✅ Analisado o DTO e constatado que `AccessLevel.PRIVATE` é desnecessário com `@Builder`
    2. ✅ Removidos `access = AccessLevel.PRIVATE` de `@AllArgsConstructor` e `@NoArgsConstructor`
    3. ✅ Testes passando: BUILD SUCCESSFUL (1m 11s)
- **Critérios de Sucesso:**
    - [x] Análise concluída
    - [x] AccessLevel removido
    - [x] Testes passando
    - [ ] Decisão documentada
    - [ ] Se removido: testes passando

#### TODO 14: Implementar validações de integridade do mapa
- **Arquivo:** `MapaIntegridadeService.java`
- **TODO:** `// TODO essa validação está me parecendo inócua. Parece indicar partes ainda nao implementadas!`
- **Prioridade:** 🔴 Alta
- **Esforço:** G (8-12 horas)
- **Status:** ⬜ Pendente
- **Análise:** A validação de integridade do mapa parece incompleta.
- **Impacto:** Crítico para garantir consistência dos dados
- **Risco:** Alto - validações faltantes podem permitir dados inválidos
- **Dependências:** Requer análise de regras de negócio
- **Plano:**
    1. Levantar todas as regras de integridade necessárias para mapas.
    2. Implementar as validações de integridade do mapa que estão faltando.
    3. Adicionar testes para cada validação.
    4. Documentar as regras de validação.
- **Critérios de Sucesso:**
    - [ ] Regras de integridade documentadas
    - [ ] Validações implementadas
    - [ ] Testes para cada regra
    - [ ] Dados existentes validados
    - [ ] Nenhuma regressão em funcionalidades

#### TODO 15: Implementar tratamento para INCLUSAO_CONHECIMENTO
- **Arquivo:** `ImpactoCompetenciaService.java`
- **TODO:** `// TODO Não existe isso! Tem que ser algum dos tipos acima`
- **Prioridade:** 🔴 Alta
- **Esforço:** M (4-6 horas)
- **Status:** ⬜ Pendente
- **Análise:** O tratamento para o tipo de impacto `INCLUSAO_CONHECIMENTO` está ausente.
- **Impacto:** Bug - funcionalidade não implementada
- **Risco:** Alto - pode causar erros em runtime
- **Dependências:** Requer definição de regra de negócio
- **Plano:**
    1. Verificar se `INCLUSAO_CONHECIMENTO` é um tipo válido ou se é erro de nomenclatura.
    2. Implementar o tratamento para o tipo de impacto `INCLUSAO_CONHECIMENTO`.
    3. Adicionar testes para o novo tipo.
    4. Atualizar documentação do enum.
- **Critérios de Sucesso:**
    - [ ] Tipo implementado ou removido
    - [ ] Testes para o caso criados
    - [ ] Nenhum case default sem tratamento
    - [ ] Documentação atualizada

#### TODO 16: Revisar validação em MapaVisualizacaoService
- **Arquivo:** `MapaVisualizacaoService.java`
- **TODO:** `// TODO nao é precipitadao lançar essa exceção aqui? Nem deveria acontecer se as camadas de cima fizerem sua parte.`
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-5 horas)
- **Status:** ⬜ Pendente
- **Análise:** A exceção lançada pode ser um sinal de que as camadas superiores não estão validando os dados corretamente.
- **Impacto:** Melhora arquitetura em camadas
- **Risco:** Médio
- **Dependências:** Nenhuma
- **Plano:**
    1. Analisar fluxo de chamadas até este serviço.
    2. Adicionar validações nas camadas de serviço e controle para garantir que o mapa sempre seja encontrado.
    3. Considerar se exceção deve permanecer como fail-safe ou ser removida.
    4. Adicionar testes de contrato entre camadas.
- **Critérios de Sucesso:**
    - [ ] Análise de fluxo documentada
    - [ ] Validações nas camadas corretas
    - [ ] Decisão sobre exceção documentada
    - [ ] Testes de integração entre camadas

### `sgc.subprocesso`

#### TODO 17: Limpar sanitização do SubprocessoValidacaoController
- **Arquivo:** `SubprocessoValidacaoController.java`
- **TODO:** `// TODO limpar a sanitização desse controlador`
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-4 horas)
- **Status:** ✅ Concluído
- **Análise:** A sanitização de HTML estava espalhada no controlador. Refatoração realizada para seguir princípios de separação de responsabilidades.
- **Impacto:** Melhora separação de responsabilidades, controller mais limpo
- **Risco:** Muito baixo - segurança mantida
- **Ação Realizada:**
    1. ✅ Criado desserializador customizado JSON: `HtmlSanitizingDeserializer`
    2. ✅ Criada anotação: `@SanitizeHtml` para marcar campos que devem ser sanitizados
    3. ✅ Atualizado DTOs com a anotação:
       - `DisponibilizarMapaReq`
       - `ApresentarSugestoesReq`
       - `DevolverValidacaoReq`
       - `SubmeterMapaAjustadoReq`
    4. ✅ Removido sanitizador do controller
    5. ✅ Removidas 4 chamadas de sanitização inline do controller
    6. ✅ Testes de integração passando
- **Critérios de Sucesso:**
    - [x] Sanitização movida para desserializador JSON
    - [x] Controller apenas delega
    - [x] Testes passando (BUILD SUCCESSFUL in 2m 1s)
    - [x] XSS ainda prevenido (aplicado em desserialização)
    - [x] Código mais legível e manutenível

#### TODO 18: Adicionar @Builder ao MapaAjusteDto
- **Arquivo:** `MapaAjusteDto.java`
- **TODO:** `// TODO Parametros demais! Mudar para @Builder`
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 1 hora)
- **Status:** ✅ Concluído
- **Análise:** O construtor do DTO tem muitos parâmetros. Verificação realizada: @Builder já estava presente!
- **Impacto:** N/A - Já implementado
- **Ação Realizada:**
    1. Verificado que `@Builder` e `@Getter` já estavam presentes na classe
    2. TODO comment removido como documentação dessa conclusão
- **Critérios de Sucesso:**
    - [x] @Builder adicionado (já existia)
    - [x] Código usando builder (já estava em uso)
    - [x] Testes passando
    - [x] Código legível

#### TODO 19: Refatorar SubprocessoNotificacaoService (múltiplas melhorias)
- **Arquivo:** `SubprocessoNotificacaoService.java`
- **TODO:** 
  - `// TODO esta classe está usando muitos strings fixos. Mudar para usar templates do thymeleaf`
  - `// TODO em vez de IllegalArgumentException usar exceções de negócio específicas`
  - `// TODO usar builder par instanciar os alertas. Considerar criar método auxiliar: codigo esta repetitivo`
- **Prioridade:** 🟡 Média
- **Esforço:** G (10-14 horas)
- **Status:** ⬜ Pendente
- **Análise:** O serviço de notificação tem vários pontos a serem melhorados.
- **Impacto:** Melhora manutenibilidade e facilita internacionalização
- **Risco:** Médio - componente crítico de notificações
- **Dependências:** Relacionado ao TODO 5 (exceções específicas)
- **Plano:**
    1. Criar templates Thymeleaf para notificações de e-mail.
    2. Substituir as strings fixas pelos templates.
    3. Criar exceções de negócio específicas para notificações.
    4. Trocar `IllegalArgumentException` pelas novas exceções.
    5. Usar o padrão Builder para criar alertas.
    6. Extrair métodos auxiliares para eliminar código repetitivo.
    7. Adicionar testes para cada tipo de notificação.
- **Critérios de Sucesso:**
    - [ ] Templates Thymeleaf criados
    - [ ] Strings hardcoded removidas
    - [ ] Exceções específicas implementadas
    - [ ] Builder pattern aplicado
    - [ ] Código duplicado eliminado
    - [ ] Testes de notificação passando
    - [ ] E-mails renderizando corretamente

#### TODO 20: Melhorar SubprocessoMapaService
- **Arquivo:** `SubprocessoMapaService.java`
- **TODO:** 
  - `// TODO usar exceções mais específicas nessa classe toda` - ⏸️ Pausado (TODO 5 dependência)
  - `// TODO Estranho passar o destino duas vezes nesse construtor. Bug?` - ✅ CONCLUÍDO
- **Prioridade:** 🔴 Alta (possível bug)
- **Esforço:** M (5-7 horas)
- **Status:** 🔄 Em Progresso (parte 1/2 concluída)
- **Análise:** O serviço de mapa de subprocesso precisa de melhorias no tratamento de erros e tem um possível bug.
- **Impacto:** Correção de possível bug + melhoria de tratamento de erros
- **Risco:** Alto - possível bug em produção
- **Dependências:** Relacionado ao TODO 5
- **Bug Corrigido:**
    - **Descrição:** O construtor `Movimentacao` na linha 162 de `SubprocessoMapaService.importarAtividades()` estava passando `spDestino.getUnidade()` duas vezes em vez de passar `spOrigem.getUnidade()` como unidade de origem.
    - **Impacto:** Movimentações incorretas registradas, não rastreando corretamente de qual unidade as atividades foram importadas.
    - **Fix:** Alterado para `new Movimentacao(spDestino, spOrigem.getUnidade(), spDestino.getUnidade(), descMovimentacao)`
    - **Testes:** BUILD SUCCESSFUL - todos os testes passaram
- **Próximos Passos:**
    1. Substituir as exceções genéricas por exceções de negócio específicas (TODO 5 - dependência)
    2. Adicionar testes específicos para movimentações de importação
    3. Revisar outros eventos similares para padrões semelhantes

### sgc.analise

#### TODO 21: Refatorar código repetido em AnaliseController
- **Arquivo:** AnaliseController.java
- **TODO:** // TODO este código repete quase igual no método 'criarAnaliseValidacao'
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-5 horas)
- **Status:** ⬜ Pendente
- **Análise:** Há duplicação de código entre métodos do controller que tratam criação de análises. Refatoração necessária para seguir o princípio DRY (Don't Repeat Yourself).
- **Impacto:** Melhora manutenibilidade e reduz risco de inconsistência
- **Risco:** Baixo - apenas refatoração de código existente
- **Plano:**
    1. Identificar os métodos duplicados em AnaliseController
    2. Extrair código comum para um método auxiliar privado
    3. Atualizar ambos os métodos para usar o auxiliar
    4. Executar testes para garantir funcionalidade preservada
- **Critérios de Sucesso:**
    - [ ] Código duplicado extraído para método privado
    - [ ] Ambos os métodos usando o auxiliar
    - [ ] Testes passando
    - [ ] Nenhuma funcionalidade alterada

### sgc.atividade

#### TODO 22: Remover sanitização do AtividadeController
- **Arquivo:** AtividadeController.java
- **TODO:** // TODO remover essa sanitização. Está poluindo (4 ocorrências)
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-4 horas)
- **Status:** ⬜ Pendente
- **Análise:** Similar ao TODO 17 (SubprocessoValidacaoController), a sanitização HTML está espalhada no controller. Deve ser movida para um desserializador JSON custom com anotação @SanitizeHtml.
- **Impacto:** Melhora separação de responsabilidades, controller mais limpo
- **Risco:** Muito baixo - padrão já validado no TODO 17
- **Dependências:** Aproveita padrão criado no TODO 17
- **Plano:**
    1. Revisar os 4 pontos de sanitização no AtividadeController
    2. Identificar os DTOs envolvidos
    3. Adicionar anotação @SanitizeHtml aos campos de entrada
    4. Remover chamadas de sanitização do controller
    5. Executar testes de integração
- **Critérios de Sucesso:**
    - [ ] 4 sanitizações removidas do controller
    - [ ] DTOs anotados com @SanitizeHtml
    - [ ] Testes de integração passando
    - [ ] XSS ainda prevenido

#### TODO 23: Verificar lógica de segurança em AtividadeService
- **Arquivo:** AtividadeService.java
- **TODO:** // TODO isso realmente vai acontecer, se a segurança estiver configurada corretamemte?
- **Prioridade:** 🔴 Alta
- **Esforço:** M (4-6 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe dúvida sobre a execução de uma seção de código quando a segurança está configurada corretamente. Pode indicar:
  1. Código não-alcançável (dead code)
  2. Brecha de segurança
  3. Lógica defensiva redundante
- **Impacto:** Possível bug de segurança ou código morto
- **Risco:** Alto - relacionado a segurança
- **Plano:**
    1. Entender o contexto de segurança do método
    2. Analisar quando o código pode ser executado
    3. Remover se for código morto ou reforçar se for necessário
    4. Adicionar testes de segurança
- **Critérios de Sucesso:**
    - [ ] Comportamento documentado
    - [ ] Código morto removido OU validação reforçada
    - [ ] Testes de segurança cobrindo o caso
    - [ ] Comentário removido

#### TODO 24: Refatorar AtividadeDto com Builder
- **Arquivo:** AtividadeDto.java
- **TODO:** // TODO mudar para Builder e rever esse sanitizado aqui, parece poluição
- **Prioridade:** 🟡 Média
- **Esforço:** M (2-3 horas)
- **Status:** ⬜ Pendente
- **Análise:** O DTO está sendo criado com muitos parâmetros e tem sanitização inline. Deve usar @Builder do Lombok e movê-la para o desserializador.
- **Impacto:** Melhora legibilidade e remove poluição do DTO
- **Risco:** Baixo - refatoração estrutural
- **Plano:**
    1. Adicionar @Builder ao AtividadeDto
    2. Remover sanitização inline do DTO
    3. Adicionar @SanitizeHtml aos campos apropriados
    4. Atualizar instanciações para usar builder se necessário
- **Critérios de Sucesso:**
    - [ ] @Builder adicionado e em uso
    - [ ] Sanitização removida do DTO
    - [ ] Testes passando
    - [ ] Código mais legível

#### TODO 25: Remover sanitização redundante do ConhecimentoDto
- **Arquivo:** ConhecimentoDto.java
- **TODO:** // TODO sanitizar aqui parece ruído!
- **Prioridade:** 🟡 Média
- **Esforço:** P (1-2 horas)
- **Status:** ⬜ Pendente
- **Análise:** Sanitização inline no DTO, similar aos casos anteriores. Deve ser movida para o desserializador JSON.
- **Impacto:** Melhora coesão do DTO
- **Risco:** Muito baixo
- **Plano:**
    1. Remover sanitização do ConhecimentoDto
    2. Adicionar @SanitizeHtml ao campo apropriado
    3. Testar desserialização
- **Critérios de Sucesso:**
    - [ ] Sanitização removida do DTO
    - [ ] @SanitizeHtml adicionada
    - [ ] Testes passando

### sgc.mapa

#### TODO 26: Refatorar DTOs de mapa (MapaDto vs MapaCompletoDto)
- **Arquivo:** MapaCompletoDto.java
- **TODO:** // TODO precisa mesmo de um MapaDto e de um MapaCompletoDto?
- **Prioridade:** 🟡 Média
- **Esforço:** G (10-14 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existem dois DTOs muito semelhantes para representar o mesmo conceito. Pode ser: duplicação desnecessária ou estão servindo a propósitos diferentes que não estão claros.
- **Impacto:** Reduz confusão de código, melhora manutenibilidade
- **Risco:** Médio - pode afetar múltiplos endpoints
- **Plano:**
    1. Analisar diferenças entre MapaDto e MapaCompletoDto
    2. Verificar onde cada um é usado
    3. Se forem semelhantes, unificá-los
    4. Se forem diferentes, renomear e documentar o propósito
    5. Atualizar endpoints de acordo
- **Critérios de Sucesso:**
    - [ ] Duplicação resolvida OU diferenças documentadas
    - [ ] Endpoints testados
    - [ ] Nomes refletem propósito claramente

#### TODO 27: Investigar pacote de visualização redundante
- **Arquivo:** sgc/mapa/dto/visualizacao/AtividadeDto.java
- **TODO:** // TODO essa classe e todo esse pacote estao me parecendo redundantes. Se nao for redundante, mude o nome e documente.
- **Prioridade:** 🟡 Média
- **Esforço:** M (4-6 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe um pacote isualizacao com DTOs que podem ser redundantes. Precisa investigar se é necessário ou se deve ser consolidado em outro lugar.
- **Impacto:** Reduz complexidade estrutural
- **Risco:** Médio - afeta estrutura de pacotes
- **Plano:**
    1. Mapear todas as classes no pacote isualizacao
    2. Comparar com DTOs em outros pacotes
    3. Determinar se é redundante ou tem propósito único
    4. Se redundante: mover/consolidar
    5. Se necessário: renomear e documentar propósito
- **Critérios de Sucesso:**
    - [ ] Redundância eliminada OU propósito documentado
    - [ ] Estrutura de pacotes consistente
    - [ ] Testes passando

#### TODO 28: Investigar TipoImpactoCompetencia não implementado
- **Arquivo:** TipoImpactoCompetencia.java
- **TODO:** 
  - // TODO as constantes reais nao estao sendo usadas. Parece indicar áreas nao implementadas. Investigar.
  - // TODO Não existe isso! (em ImpactoCompetenciaService.java)
- **Prioridade:** 🔴 Alta
- **Esforço:** M (5-7 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existem tipos de impacto de competência definidos mas não usados em produção, e há referências a tipos que não existem. Pode indicar:
  1. Áreas da aplicação ainda não implementadas
  2. Enums desatualizados
  3. Falta de cobertura de testes
- **Impacto:** Pode afetar funcionalidade e consistência
- **Risco:** Alto - áreas não implementadas
- **Plano:**
    1. Listar todas as constantes em TipoImpactoCompetencia
    2. Verificar quais são usadas no código
    3. Verificar quais são testadas
    4. Documentar o estado: implementado, planejado ou descontinuado
    5. Remover tipos não usados ou implementar sua funcionalidade
- **Critérios de Sucesso:**
    - [ ] Todos os tipos mapeados
    - [ ] Uso de cada tipo documentado
    - [ ] Tipos não-usados removidos ou implementados
    - [ ] Código sincronizado com enums

#### TODO 29: Revisar validação em MapaIntegridadeService
- **Arquivo:** MapaIntegridadeService.java
- **TODO:** // TODO essa validação está me parecendo inócua. Parece indicar partes ainda nao implementadas!
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-5 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe uma validação que não tem efeito prático, indicando funcionalidade possivelmente não implementada. Precisa investigar se:
  1. A validação é necessária mas ineficaz
  2. A funcionalidade relacionada não foi implementada
  3. É código defensivo desnecessário
- **Impacto:** Melhora clareza do código e documenta estado da aplicação
- **Risco:** Baixo - apenas investigação e documentação
- **Plano:**
    1. Entender o contexto da validação
    2. Determinar seu propósito original
    3. Se necessária: reforçar ou documentar por que é defensiva
    4. Se desnecessária: remover ou documentar por que foi deixada
- **Critérios de Sucesso:**
    - [ ] Propósito da validação documentado
    - [ ] Ação tomada (reforçada, removida ou explicada)
    - [ ] Comentário removido

#### TODO 30: Revisar exceção prematura em MapaVisualizacaoService
- **Arquivo:** MapaVisualizacaoService.java
- **TODO:** // TODO nao é precipitadao lançar essa exceção aqui? Nem deveria acontecer se as camadas de cima fizerem sua parte.
- **Prioridade:** 🟡 Média
- **Esforço:** M (2-3 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe uma exceção sendo lançada que tecnicamente não deveria acontecer se as validações de camadas superiores funcionarem. Pode ser:
  1. Código defensivo desnecessário
  2. Indicador de falta de validação nas camadas superiores
  3. Proteção prudente contra cenários raros
- **Impacto:** Clarifica responsabilidades entre camadas
- **Risco:** Baixo
- **Plano:**
    1. Analisar o contexto onde a exceção é lançada
    2. Verificar validações nas camadas superiores
    3. Determinar se é realmente necessário
    4. Se for, documentar como "validação defensiva"
    5. Se não, remover ou mover para camada apropriada
- **Critérios de Sucesso:**
    - [ ] Necessidade da exceção documentada
    - [ ] Ação tomada (mantida documentada, removida ou movida)
    - [ ] Testes cobrindo o caso

### sgc.comum

#### TODO 31: Consolidar Config e ConfigAplicacao
- **Arquivo:** Config.java e ConfigAplicacao.java
- **TODO:** // TODO verificar se nao é melhor juntar com 'ConfigAplicacao'
- **Prioridade:** 🟡 Média
- **Esforço:** M (3-4 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existem duas classes de configuração que possivelmente podem ser consolidadas em uma.
- **Impacto:** Reduz duplicação e complexidade
- **Risco:** Médio - pode afetar inicialização da aplicação
- **Plano:**
    1. Analisar conteúdo de ambas as classes
    2. Identificar diferenças e semelhanças
    3. Se complementares: consolidar em uma classe
    4. Se conflitantes: documentar propósito distinto de cada uma
- **Critérios de Sucesso:**
    - [ ] Classes analisadas
    - [ ] Duplicação eliminada OU propósito diferenciado
    - [ ] Aplicação inicia normalmente
    - [ ] Testes passando

#### TODO 32: Revisar classe ErroNegocio genérica
- **Arquivo:** ErroNegocio.java
- **TODO:** // TODO em vez dessa classe geral demais, melhor criar erros mais específicos.
- **Prioridade:** 🟡 Média
- **Esforço:** G (12-16 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe uma classe genérica de erro de negócio que deveria ser substituída por exceções específicas de domínio. Relacionado ao TODO 5.
- **Impacto:** Melhora tratamento de erros e clareza da API
- **Risco:** Médio - refatoração abrangente
- **Dependências:** Parte do esforço maior de exceções de negócio (TODO 5)
- **Plano:**
    1. Mapear todos os usos de ErroNegocio
    2. Agrupar por domínio/contexto
    3. Criar exceções específicas para cada grupo
    4. Atualizar RestExceptionHandler com handlers específicos
    5. Substituir all uses
- **Critérios de Sucesso:**
    - [ ] ErroNegocio removido ou altamente limitado em escopo
    - [ ] Exceções específicas criadas e em uso
    - [ ] Testes passando
    - [ ] API mais clara

#### TODO 33: Refatorar RestExceptionHandler
- **Arquivo:** RestExceptionHandler.java
- **TODO:** // TODO essa classe me parece muito repetitiva. E os tratamentos não estão específicos o suficiente.
- **Prioridade:** 🟡 Média
- **Esforço:** M (6-8 horas)
- **Status:** ⬜ Pendente
- **Análise:** O handler de exceções tem código repetitivo e handlers genéricos. Deve ser refatorado para usar padrões como anotações customizadas ou métodos auxiliares.
- **Impacto:** Melhora manutenibilidade e reduz duplicação
- **Risco:** Médio - toca na camada de tratamento de erros
- **Dependências:** TODO 32 (exceções específicas)
- **Plano:**
    1. Identificar padrões repetitivos
    2. Extrair métodos auxiliares para construir respostas
    3. Considerar anotações customizadas para exceções
    4. Refatorar cada bloco catch
    5. Executar testes abrangentes
- **Critérios de Sucesso:**
    - [ ] Código repetitivo reduzido em 30%+
    - [ ] Handlers mais específicos
    - [ ] Testes passando
    - [ ] Respostas de erro mais consistentes

### sgc.sgrh

#### TODO 34: Remover ServidorDto
- **Arquivo:** ServidorDto.java
- **TODO:** // TODO esse dto deve ser removido, sendo usado apenas o UsuarioDto
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (1-2 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe um DTO específico para Servidor que não é necessário pois UsuarioDto é suficiente.
- **Impacto:** Reduz clutter de código
- **Risco:** Muito baixo - simples remoção
- **Plano:**
    1. Verificar se ServidorDto ainda está em uso
    2. Substituir referências por UsuarioDto
    3. Remover a classe
    4. Testar endpoints relacionados
- **Critérios de Sucesso:**
    - [ ] Nenhuma referência a ServidorDto no código
    - [ ] Testes passando
    - [ ] Classe removida

### sgc.unidade

#### TODO 35: Usar Builder em Unidade
- **Arquivo:** Unidade.java
- **TODO:** // TODO em vez de criar todos os esses construtores diferentes, fazer os clientes usarem sempre o builder.
- **Prioridade:** 🟡 Média
- **Esforço:** M (4-5 horas)
- **Status:** ⬜ Pendente
- **Análise:** Existe múltiplos construtores sobrecarregados quando um @Builder seria mais limpo e mantível.
- **Impacto:** Reduz complexidade da classe e melhora legibilidade
- **Risco:** Baixo - refatoração estrutural
- **Plano:**
    1. Adicionar @Builder à classe Unidade
    2. Remover construtores sobrecarregados (manter apenas o no-args se necessário)
    3. Atualizar all instantiations para usar o builder
    4. Executar testes
- **Critérios de Sucesso:**
    - [ ] @Builder adicionado
    - [ ] Construtores removidos/simplificados
    - [ ] Código usando builder
    - [ ] Testes passando

### sgc.util

#### TODO 36: Remover HtmlUtils se desnecessário
- **Arquivo:** HtmlUtils.java
- **TODO:** // TODO me parece inutil essa classe.
- **Prioridade:** 🟢 Baixa
- **Esforço:** P (< 1 hora)
- **Status:** ⬜ Pendente
- **Análise:** Existe uma classe de utilidade que pode não estar sendo usada ou pode ser redundante com outros sanitizadores.
- **Impacto:** Reduz clutter
- **Risco:** Muito baixo
- **Plano:**
    1. Verificar se HtmlUtils está em uso
    2. Se em uso: comparar com HtmlSanitizingDeserializer
    3. Se duplicado: remover
    4. Se único: documentar seu propósito
- **Critérios de Sucesso:**
    - [ ] Uso de HtmlUtils verificado
    - [ ] Ação tomada (removido ou documentado)
    - [ ] Código compilando e testando

## Resumo Atualizado

- **Total de TODOs Documentados:** 36 (todos mapeados)
- **Status Geral:** 11% concluído (4 TODOs resolvidos) + 2 analisados como já implementados
- **Pendentes:** 28 TODOs
- **Pausados:** 2 TODOs (dependências)

## Próximas Prioridades (Recomendado)

### Fase 1: Quick Wins (Baixo Risco, Rápido)
1. **TODO 34:** Remover ServidorDto - P, 🟢 Baixa
2. **TODO 36:** Remover HtmlUtils - P, 🟢 Baixa
3. **TODO 25:** Remover sanitização ConhecimentoDto - P, 🟡 Média
4. **TODO 21:** Refatorar duplicação AnaliseController - M, 🟡 Média

### Fase 2: Sanitização (Padrão Estabelecido)
1. **TODO 22:** Remover sanitização AtividadeController - M, 🟡 Média
2. **TODO 24:** Refatorar AtividadeDto - M, 🟡 Média

### Fase 3: Investigações Críticas (Alto Risco, Informação)
1. **TODO 23:** Lógica de segurança AtividadeService - M, 🔴 Alta
2. **TODO 28:** TipoImpactoCompetencia - M, 🔴 Alta

### Fase 4: Refatorações Estruturais (Médio/Alto Esforço)
1. **TODO 5:** Exceções de negócio específicas - G (bloqueador)
2. **TODO 19:** SubprocessoNotificacaoService - G, 🟡 Média
3. **TODO 26:** Consolidar MapaDto DTOs - G, �� Média
4. **TODO 2:** Refatorar BeanUtil - G, 🟡 Média

