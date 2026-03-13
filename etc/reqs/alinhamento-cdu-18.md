# Alinhamento CDU-18 - Visualizar mapa de competências

## Cobertura atual do teste
O teste E2E cobre:
- **Cenário 1 (ADMIN via processo)** (linhas 18-62):
  - Login como ADMIN (implícito via fixture).
  - Clica em processo 99 (finalizado) na tabela.
  - Seleciona unidade ASSESSORIA_* participante.
  - Acessa mapa de competências.
  - Valida presença de:
    - Título "Mapa de competências técnicas" ✓
    - Identificação da unidade (sigla + nome) ✓
    - Competência "Competência técnica seed 99" ✓
    - Atividades "Atividade seed 1" e "Atividade seed 2" ✓
    - Conhecimentos "Conhecimento seed 1.1" e "Conhecimento seed 2.1" ✓

- **Cenário 2 (CHEFE da unidade)** (linhas 64-86):
  - Login como CHEFE_ASSESSORIA_12.
  - Clica em processo 99 na tabela.
  - Sistema redireciona diretamente para detalhes do subprocesso da sua unidade.
  - Acessa mapa de competências.
  - Valida presença de:
    - Título "Mapa de competências técnicas" ✓
    - Identificação da unidade (sigla + nome) ✓
    - Competência "Competência técnica seed 99" ✓

## Lacunas em relação ao requisito
**Pré-condições não completamente validadas:**
- **Pré-condição 1**: Requisito diz "Usuário logado com qualquer perfil" mas teste apenas cobre ADMIN e CHEFE. Faltam cenários com GESTOR e SERVIDOR.
- **Pré-condição 2**: Requisito diz "Processo de mapeamento ou de revisão iniciado ou finalizado" - Teste usa processo finalizado. Falta cenário com processo em andamento (iniciado mas não finalizado).
- **Pré-condição 3**: Requisito diz "Subprocesso da unidade com mapa de competência já disponibilizado" - Teste valida isso implicitamente (seed contém mapa disponibilizado), mas não há assertion explícita de situação do subprocesso.

**Fluxo principal (passos 1-4) parcialmente coberto:**
- **Passo 1**: Requisito diz "No `Painel`, o usuário clica no processo de mapeamento ou revisão na situação 'Em andamento' ou 'Finalizado'."
  - Teste clica em processo finalizado ✓ (cenário 1).
  - Faltam cenários: processo em andamento.

- **Passo 2** (se perfil ADMIN ou GESTOR): "O sistema exibe a tela `Detalhes do processo`."
  - Teste valida que URL muda para padrão `/processo/\d+$` ✓.
  - Não há assertion de título/heading "Detalhes do processo" ou conteúdo visual da tela.

- **Passo 2.2** (se ADMIN/GESTOR): "Usuário clica em uma unidade subordinada que seja operacional ou interoperacional."
  - Teste clica em linha com `ASSESSORIA_*` ✓.
  - Não há validation de que unidade selecionada é "operacional ou interoperacional" (poderia ser unidade raiz).

- **Passo 2.3** (se ADMIN/GESTOR): "O sistema exibe a tela `Detalhes do subprocesso`..."
  - Teste valida URL `/processo/\d+/ASSESSORIA_\d+$` ✓.
  - Não há assertion de título/heading "Detalhes do subprocesso" ou conteúdo visual.

- **Passo 3** (se CHEFE/SERVIDOR): "O sistema exibe a tela `Detalhes do subprocesso`..."
  - Teste valida redirecionamento direto para `/processo/\d+/ASSESSORIA_12$` ✓.
  - Não há assertion de conteúdo visual da tela.

- **Passo 4**: "Na tela de `Detalhes do subprocesso`, usuário clica no card `Mapa de Competências`."
  - Teste chama helper `navegarParaMapa(page)` que provavelmente clica no card, mas não há assertion explícita de clique no card ou presença do card.

**Tela visualização de Mapa (passo 5) parcialmente validada:**
- **5.1** - Título "Mapa de competências técnicas": ✓ Validado.
- **5.2** - Identificação da unidade (sigla e nome): ✓ Validado com regex `/ASSESSORIA_\d+\s*-\s*Assessoria/i`.
- **5.3** - Conjunto de competências com cada competência em bloco individual:
  - Teste valida que competência "Competência técnica seed 99" é visível ✓.
  - Não valida:
    - Se há múltiplas competências (teste tem apenas 1).
    - Se cada competência está em "bloco individual" (visual/layout não validado).
    - Se há elementos visuais como ícones ou cores distintas para cada bloco.

- **5.3.1** - Descrição da competência como título:
  - Teste valida que texto "Competência técnica seed 99" é visível ✓.
  - Não valida que é estruturalmente um "título" (tag h3/h4 ou estilo de título).

- **5.3.2** - Conjunto das atividades associadas àquela competência:
  - Teste valida que "Atividade seed 1" e "Atividade seed 2" são visíveis ✓.
  - Não valida:
    - Se apenas as atividades ASSOCIADAS à competência aparecem (pode haver outras atividades não mostradas).
    - Se atividades são estruturalmente agrupadas sob a competência (layout/hierarquia não validado).

- **5.3.3** - Para cada atividade, conjunto de conhecimentos da atividade:
  - Teste valida que "Conhecimento seed 1.1" e "Conhecimento seed 2.1" são visíveis ✓.
  - Não valida:
    - Se conhecimentos estão hierarquicamente subordinados à atividade (layout não validado).
    - Se apenas conhecimentos ASSOCIADOS à atividade aparecem.
    - Estrutura visual (blocos internos, ícones, etc.).

**Cenários não cobertos:**
- **Múltiplas competências**: Seed tem apenas 1 competência. Teste não valida visualização de múltiplas competências, ordem de exibição, ou comportamento visual.
- **Múltiplas atividades por competência**: Competência tem 2 atividades. Falta testar cenário com 3+ atividades.
- **Múltiplos conhecimentos por atividade**: Teste valida 1.1 conhecimento por atividade. Falta testar atividade com 2+ conhecimentos.
- **Processo em andamento**: Teste usa processo finalizado. Requisito diz "iniciado ou finalizado" - falta cenário com processo em andamento.
- **Perfil GESTOR**: Requisito diz qualquer perfil. Teste cobre ADMIN e CHEFE. Falta GESTOR (que deveria seguir passo 2).
- **Perfil SERVIDOR**: Requisito cobre. Teste não implementa (não há fixture de SERVIDOR).

**Validações de acesso/permissão:**
- Teste não valida que CHEFE vê apenas mapa da sua unidade (não consegue acessar outras unidades).
- Teste não valida que SERVIDOR consegue acessar o mapa (assumindo que seed fornece acesso).

**Comportamento interativo não testado:**
- Requisito implica que visualização é apenas leitura (não há botões de edição mencionados).
- Teste não valida que não há botões de edição (como em CDU-15 para ADMIN).
- Teste não valida que elementos são clicáveis ou não (ex: pode clicar em competência para expandir?).

## Alterações necessárias no teste E2E
1. **Adicionar cenário com GESTOR**:
   - GESTOR clica em processo em Painel.
   - Sistema exibe "Detalhes do processo".
   - GESTOR clica em unidade subordinada.
   - Sistema exibe "Detalhes do subprocesso".
   - GESTOR clica no card "Mapa de Competências".
   - Valida visualização do mapa.

2. **Adicionar cenário com SERVIDOR** (se houver seed preparado):
   - SERVIDOR clica em processo no Painel.
   - Sistema redireciona direto para "Detalhes do subprocesso" da unidade do SERVIDOR.
   - SERVIDOR clica no card "Mapa de Competências".
   - Valida visualização do mapa (apenas leitura).

3. **Adicionar cenário com processo em andamento**:
   - Usar seed ou criar processo em situação 'Em andamento'.
   - ADMIN acessa processo em andamento.
   - Valida que consegue visualizar mapa mesmo com processo não finalizado.

4. **Validar presença de elementos visuais específicos**:
   - Assertar que título é estruturalmente um heading (tag h2 ou similar).
   - Validar que identificação da unidade está visível de forma clara (sigla + nome).
   - Validar que cada competência está em um "bloco" visualmente distinto (ex: border, background).

5. **Validar hierarquia e estrutura visual**:
   - Validar que atividades aparecem **dentro** do bloco da competência (não apenas presentes na página).
   - Validar que conhecimentos aparecem **dentro** do bloco da atividade.
   - Usar `locator.locator()` para validar relações pai-filho.

6. **Adicionar cenário com múltiplas competências** (se criar novo seed ou usar preparação):
   - Mapa com 2+ competências.
   - Validar que todas aparecem.
   - Validar ordem de exibição.
   - Validar que cada uma é um bloco distinto.

7. **Adicionar cenário com múltiplos conhecimentos por atividade**:
   - Atividade com 2+ conhecimentos.
   - Validar que todos aparecem em ordem.

8. **Validar que visualização é somente leitura**:
   - Validar que botões de edição (criar, editar, excluir competências) **não aparecem**.
   - Validar que campos não são editáveis.

9. **Adicionar assertion para pré-condições**:
   - Ao navegaração, validar que subprocesso está em situação de mapa disponibilizado/validado/homologado.
   - Validar que mapa não está vazio (tem competências).

10. **Validar redirecionamento automático para CHEFE/SERVIDOR**:
    - Confirmar que CHEFE/SERVIDOR não veem tela "Detalhes do processo" (vão direto para subprocesso).
    - Confirmar que só conseguem acessar mapa da sua unidade.

## Notas e inconsistências do requisito
- **Passo 2 vs 3**: Requisito separa comportamento por perfil mas falta clareza sobre:
  - Se CHEFE consegue acessar "Detalhes do processo" ou vai direto para subprocesso.
  - Se CHEFE consegue ver múltiplas unidades em "Detalhes do processo" ou apenas a sua.
  - Se SERVIDOR consegue ver "Detalhes do processo" ou vai direto.

- **Passo 5**: Estrutura descrita é genérica e não detalha:
  - Se há paginação, scroll, ou limite de competências por página.
  - Se há ordenação (alfabética, por data, customizada).
  - Se há busca/filtro.
  - Se há collapsed/expanded state para competências.

- **Pré-condição**: Diz "mapa de competência já disponibilizado" mas requisitos anteriores (CDU-15, CDU-17) mencionam situações como 'Mapa homologado' ou 'Mapa validado'. Não fica claro qual é a situação exata esperada para que mapa seja visualizável.

- **Falta de referência**: Requisito não menciona se visualização de mapa para CHEFE/SERVIDOR inclui também "Mapa de Impactos" ou outros dados contextuais (como validação realizada, histórico de alterações).
