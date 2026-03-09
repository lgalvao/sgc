# Alinhamento CDU-15 - Manter mapa de competências

## Cobertura atual do teste
O teste E2E cobre:
- **Preparação**: Cria processo, CHEFE adiciona 2 atividades e 2 conhecimentos, gestores aceitam, ADMIN homologa cadastro (linhas 41-91).
- **Navegação para Edição de Mapa**: Acessa mapa e valida presença de título, botões (`Criar competência`, `Disponibilizar`), e que mapa está vazio (linhas 93-106).
- **Criar competência**: Cria competência com atividades associadas; valida mudança de situação para "Mapa criado" (linhas 102-106).
- **Editar competência**: Clica botão de edição, modifica descrição e atividades associadas, confirma e valida atualização (linhas 108-111).
- **Exclusão com cancelamento**: Clica exclusão, valida cancelamento permanece na tela com competência visível (linhas 113-115).
- **Exclusão com confirmação**: Clica exclusão, confirma, valida remoção; botão Disponibilizar desabilita (linhas 117-119).
- **Disponibilização**: Cria nova competência, clica Disponibilizar, valida redirecionamento para Painel (linhas 121-132).

## Lacunas em relação ao requisito
**Fluxo principal (passos 1-4) não coberto:**
- **Passo 1-2**: Não há validação de que no Painel o ADMIN escolhe um processo ou navega para "Detalhes do processo".
- **Passo 3-4**: Não valida que ADMIN clica em unidade com subprocesso nas situações 'Cadastro homologado' ou 'Mapa criado', nem que a tela "Detalhes do subprocesso" é exibida.

**Elemento visual da tela Edição de Mapa (passo 4):**
- Teste valida presença de botões mas não valida estrutura visual completa:
  - **4a**: Bloco para cada competência com título (descrição) ✓
  - **4b**: Botões de ação (ícones) para editar e excluir ✓ (implícito no teste)
  - **4c**: Dentro de cada bloco, descrições de atividades em blocos internos - **Não validado**.
  - **4d**: Badge com número de conhecimentos; tooltip ao passar mouse - **Não validado**.
  - **4e**: Botões `Criar competência` e `Disponibilizar` alinhados canto superior direito - **Parcialmente validado** (presença, não alinhamento).

**Fluxo de criação de competências (passos 5-10):**
- **Passo 5-6**: Não valida que o modal "Edição de competência" abre com campos específicos (descrição, lista de atividades para seleção, botões Cancelar/Salvar).
- **Passo 7**: Não valida que ADMIN pode selecionar "uma ou mais atividades".
- **Passo 8-9**: Não valida que competência é armazenada internamente ou que vínculo é criado.
- **Passo 10**: Valida mudança de situação de 'Cadastro homologado' → 'Mapa criado' mas não explicita que isso ocorre **apenas na primeira competência criada** (teste cria 2).

**Fluxo de edição (passos 12):**
- **12.1-12.2**: Teste executa mas não valida que modal abre "preenchida com a descrição da competência" ou que atividades aparecem "selecionadas".
- **12.4-12.5**: Não valida que "novo description e vínculos são armazenados" ou que bloco é "atualizado" na tela.

**Fluxo de exclusão (passos 13):**
- **13.2-13.3**: Modal validado parcialmente (cancelamento ✓, confirmação ✓) mas não valida título exato "Exclusão de competência" ou pergunta com placeholder `[DESCRICAO_COMPETENCIA]`.
- **13.4**: Não valida que "todos os vínculos com atividades da unidade são removidos".

**Passo 11 não coberto:**
- Requisito diz ADMIN "repete o fluxo de criação de competências até que o mapa esteja completo" - teste cria 3 competências (na prep + na validação), mas não valida um cenário de "múltiplas repetições" com estado intermediário claro.

**Passo 14 não coberto:**
- Navegação para "Disponibilizar mapa de competências" - teste executa mas não detalha validações do fluxo específico de CDU-17.

**Validações de estado da UI:**
- Teste não valida que botão `Disponibilizar` fica **desabilitado** até que haja competências (passo 4e implica isso).
- Teste não valida validação de "todas as atividades devem estar associadas a uma competência" em momento de edição (essa validação ocorre em CDU-17, passo 9).

## Alterações necessárias no teste E2E
1. **Validar estrutura visual completa da tela "Edição de Mapa"**:
   - Blocos de competência contendo blocos internos de atividades.
   - Badge com número de conhecimentos em cada atividade.
   - Tooltip ao passar mouse sobre badge mostrando lista de conhecimentos.

2. **Validar modal "Edição de competência"**:
   - Campo de descrição.
   - Lista de todas as atividades cadastradas pela unidade (não apenas as associadas).
   - Checkboxes/seleção múltipla funcionando corretamente.
   - Botões `Cancelar` e `Salvar` presentes.

3. **Adicionar cenário que seleciona múltiplas atividades**:
   - Criar competência com 2+ atividades.
   - Validar que todas aparecem no bloco de competência.

4. **Validar comportamento do botão "Disponibilizar"**:
   - Desabilitado quando mapa está vazio.
   - Habilitado após primeira competência criada.

5. **Adicionar validação de comportamento de edição**:
   - Modal abre com descrição preenchida.
   - Atividades aparecem selecionadas (checkbox marcado).
   - Alterações são salvas e refletidas na UI.

6. **Validar título e mensagem de confirmação de exclusão**:
   - Título exato: "Exclusão de competência".
   - Mensagem com descrição dinâmica da competência.

7. **Testar estado após exclusão**:
   - Se era última competência, botão `Disponibilizar` fica desabilitado.
   - Competência não aparece mais no mapa.

8. **Adicionar validação de mensagem de navegação**:
   - Quando clica "Disponibilizar", redireciona para CDU-17 (não necessário validar CDU-17 completo, mas confirmar navegação).

## Notas e inconsistências do requisito
- **Passo 10**: Diz "Se a situação do subprocesso da unidade ainda for 'Cadastro homologado', o sistema altera a situação para 'Mapa criado'" - Implicação é que depois de criar a primeira competência, o estado muda. Teste não valida essa transição explicitamente na primeira iteração (valida no setup, mas não no teste principal).
- **Passo 4e**: "Botões alinhados no canto superior direito da lista de blocos" - Não especifica se os botões ficam flutuantes ou inline. UI pode estar ambígua.
- **Falta de cenário de erro**: Requisito não especifica o que acontece se ADMIN tentar salvar competência sem selecionar atividades (obrigatório? opcional?).
- **Requisito não cobre**: Pode ADMIN criar competência sem atividades? Depois adiciona? Ou é obrigatório na criação?
