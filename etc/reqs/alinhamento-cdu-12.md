# Alinhamento CDU-12 - Verificar impactos no mapa de competências

## Cobertura atual do teste
O teste `cdu-12.spec.ts` em 3 testes:

**Teste Setup**:
- Cria processo mapeamento finalizado (gera Mapa Vigente)
- Cria processo de revisão
- Ambos na mesma unidade

**Teste 1: Passo 3.1 - Verificação pelo CHEFE na Tela de Cadastro**:
- CHEFE acessa subprocesso de revisão
- Navega para tela de Cadastro de atividades
- **Detecta INCLUSÃO**: Adiciona nova atividade + conhecimento
- **Detecta ALTERAÇÃO**: Edita descrição da atividade
- Valida presença de botão "Impactos no mapa" (dropdown button test)
- Abre modal de impactos
- Valida seção "Atividades inseridas" com atividade alterada
- Fecha modal
- Disponibiliza cadastro (passo pré-requisito para próximos testes)

**Teste 2: Passo 3.2 - Verificação pelo GESTOR na Tela de Visualização**:
- GESTOR acessa subprocesso (após disponibilização)
- Navega para tela de Atividades e conhecimentos (visualização)
- Valida presença de botão "Impactos no mapa" (direto, não dropdown)
- Abre modal de impactos
- Valida seção "Atividades inseridas"
- Fecha modal

## Lacunas em relação ao requisito
**Não coberto**:
- **Pré-condição**: Teste cobre apenas CHEFE com "Revisão do cadastro em andamento"; não testa:
  - GESTOR com "Revisão do cadastro disponibilizada" e "localização atual na unidade do usuário" (teste apenas verifica presença de botão)
  - ADMIN com "Revisão do cadastro disponibilizada", "Revisão do cadastro homologada", "Mapa ajustado" (não testado)
  - Validação que GESTOR só vê impactos se localização está na sua unidade (não validado)
  - Acesso via "Edição de mapa" para ADMIN (passo 3.3 não testado)

- **Passo 5.1**: Detecção de INCLUSÃO - teste adiciona atividade mas não valida que foi realmente detectada como "inclusão" vs simplesmente adicionada
- **Passo 5.2-5.2.2**: Detecção de REMOÇÃO - não testado (teste apenas testa alteração + inclusão)
- **Passo 5.2**: "identifica as competências associadas a essas atividades no mapa vigente" - teste não valida se competências foram identificadas, apenas se seção visual foi mostrada
- **Passo 6**: "Se nenhuma divergência for detectada, o sistema mostra a mensagem 'Nenhum impacto no mapa da unidade.'" - não testado
- **Passo 7.1**: Seção "Atividades inseridas" - teste apenas valida presença de atividade editada, não valida:
  - Ícone de adição mencionado no requisito
  - Estrutura com atividade como cabeçalho e conhecimentos abaixo
  - Formato esperado
  
- **Passo 7.2**: Seção "Competências impactadas" - não testado (teste só testa "Atividades inseridas")
  - Blocos com descrição de competências
  - Formato listado com ícone de remoção/alteração
  - Descrição de alteração (Ex: 'Descrição alterada para X', 'Conhecimento Y removido', etc.)

- **Passo 8-9**: Análise e fechamento - teste cobre close, mas não valida que "permanece com seu estado inalterado"

**Teste parcialmente coberto**:
- Remoção de atividade não é testada como causa de impacto (apenas inclusão + alteração)
- Remoção de conhecimento dentro de atividade mantida não é testada
- Detecção de impactos é validada visualmente mas não logicamente (não valida se sistema realmente comparou com mapa vigente)
- Perfil ADMIN com diferentes situações (disponibilizada, homologada, mapa ajustado) não testado
- Acesso via tela "Edição de mapa" (passo 3.3) não testado

## Alterações necessárias no teste E2E
- Adicionar teste para GESTOR com validação de que botão "Impactos no mapa" está visível E localização está na unidade do GESTOR (validação da pré-condição 3.2)
- Adicionar teste para ADMIN acessando via tela "Edição de mapa" (passo 3.3)
- Adicionar teste de REMOÇÃO de atividade como fonte de impacto
- Adicionar teste de REMOÇÃO de conhecimento dentro de atividade mantida
- Adicionar teste para cenário onde nenhum impacto é detectado (validar mensagem "Nenhum impacto no mapa da unidade.")
- Validar estrutura visual de "Atividades inseridas" com ícone de adição
- Adicionar teste de "Competências impactadas" seção com:
  - Blocos com nomes de competências
  - Ícones de remoção/alteração
  - Descrição de alterações no formato esperado
- Validar que atividade removida mostra impacto especificando qual conhecimento foi removido
- Adicionar teste para validar estado inalterado da tela de origem após fechar modal
- Separar testes para cada tipo de impacto (inclusão, alteração, remoção) para clareza

## Notas e inconsistências do requisito
- **Ambiguidade em 5.2.2**: "Descrição da alteração (Ex.: 'Descrição alterada para X', 'Conhecimento Y removido', 'Conhecimento Z adicionado' etc)" - "etc" sugere formato aberto; não está claro se há lista predefinida ou formato estruturado
- **Indefinição em 5.2**: "identifica as competências associadas" - não clarifica como sistema sabe qual competência está associada a qual atividade (relação não definida)
- **Falta de detalhe em 7.1**: "Abaixo de cada atividade, deverão ser listados também os conhecimentos associados a elas" - indentação ou outro nível visual?
- **Ambiguidade em 3.3**: "Edição de mapa" - não fica claro qual é a tela exata (referência a outro CDU não fornecido)
- **Imprecisão em 5**: "realiza uma comparação entre as atividades e conhecimentos do mapa vigente da unidade e a mesma informação do mapa do subprocesso" - "mapa vigente" vs "mapa do subprocesso" - qual é armazenado onde?
- **Confusão de fluxo**: Teste valida que CHEFE clica em "Impactos no mapa" após editar, sugerindo que está em processo de edição, não visualização. Requisito passo 3.1 diz "acessível a partir do card Atividades e conhecimentos, quando o perfil logado for CHEFE" - não fica claro se é edição ou visualização.
