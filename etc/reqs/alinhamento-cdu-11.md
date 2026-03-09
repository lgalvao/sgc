# Alinhamento CDU-11 - Visualizar cadastro de atividades e conhecimentos

## Cobertura atual do teste
O teste `cdu-11.spec.ts` em dois grupos (Em Andamento, Finalizado):

**Grupo 1: Em Processo em Andamento**
- Setup: Cria processo com cadastro disponibilizado via fixture

**Fluxo ADMIN/GESTOR (Passo 2)**:
- Clica em processo em andamento na tabela
- Navega para tela Detalhes do processo
- Clica em unidade subordinada
- Sistema mostra Detalhes do subprocesso
- Navega para Atividades e conhecimentos
- Valida presença de sigla/nome da unidade e atividades/conhecimentos

**Fluxo CHEFE/SERVIDOR (Passo 3)**:
- Clica em processo em andamento
- Sistema direto em Detalhes do subprocesso (sem passagem por Detalhes do processo)
- URL valida `/processo/{id}/{unidade}` (direto, não passando por `/processo/{id}`)
- Navega para Atividades e conhecimentos
- Valida mesma cobertura (sigla/nome/atividades/conhecimentos)

**Grupo 2: Em Processo Finalizado**
- Setup: Cria processo mapeamento finalizado via fixture

**Fluxo ADMIN**:
- Clica em processo finalizado
- Navega para detalhes do subprocesso
- Navega para Atividades e conhecimentos
- Valida sigla/nome/atividades/conhecimentos

## Lacunas em relação ao requisito
**Não coberto**:
- **Passo 6**: Estrutura de apresentação - "cada atividade é apresentada como uma tabela, com cabeçalho a descrição da atividade, e as linhas preenchidas com os conhecimentos cadastrados" - teste apenas valida presença de atividades/conhecimentos, não estrutura de tabela com cabeçalho
- **Passo 6 (Detalhe visual)**: Validação de que "sigla e nome da unidade" aparecem na tela (teste valida via `.unidade-sigla` e `.unidade-nome`, mas não valida apresentação conjunta)
- **Pré-condição**: "Subprocesso da unidade com cadastro de atividades e conhecimentos já disponibilizados" - teste presume que fixture criar cadastro disponibilizado, mas não valida situação exata do subprocesso (deve estar em "Cadastro disponibilizado" ou "Cadastro homologado" ou "Mapa Ajustado" conforme fluxo)
- **Atores**: Teste não cobre **SERVIDOR** (apenas ADMIN, GESTOR, CHEFE)
- **Pré-condição "processo iniciado ou finalizado"**: Teste cobre ambos em fixtures diferentes, mas não em mesmo teste
- **Cobertura por perfil**: Requisito cita "Todos os perfis" como atores, mas teste cobre apenas 3 perfis explícitos

**Teste parcialmente coberto**:
- Estrutura de tabela não é validada (apenas texto dentro dela)
- Fixture não é clara sobre situação exata do subprocesso

## Alterações necessárias no teste E2E
- Adicionar validação de estrutura de tabela (cabeçalho com descrição de atividade, linhas com conhecimentos)
- Adicionar teste para perfil SERVIDOR (fluxo 3)
- Adicionar teste explícito para processo "Finalizado" com fluxo CHEFE/SERVIDOR (atualmente só testa ADMIN)
- Validar que apresentação de sigla e nome da unidade é clara/conjunta
- Adicionar teste de múltiplas atividades com múltiplos conhecimentos para validar estrutura de indentação/agrupamento
- Adicionar teste para verificar que tela é somente leitura (sem edição, botões de remoção, etc.)
- Validar que situação do subprocesso é uma das estados "Cadastro disponibilizado" (ou posterior) antes de entrar em visulização

## Notas e inconsistências do requisito
- **Ambiguidade em Passo 6**: "tabela com cabeçalho" - não especifica se é tabela HTML literal, cards, ou outra estrutura visual
- **Falta de detalhe**: Não especifica se há paginação, ordenação, ou outras interações na visualização
- **Indefinição de "somente leitura"**: Requisito não explicita que é modo visualização (sem edição), apenas implícito pelo título
- **Estrutura incompleta**: Não menciona se há botão "Voltar" ou navegação adicional
