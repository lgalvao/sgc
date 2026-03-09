# Alinhamento CDU-22 - Aceitar cadastros em bloco

## Cobertura atual do teste
O teste E2E cobre:

- **Setup de dados**: Criação de processo de mapeamento, adição de atividades por CHEFE, disponibilização de cadastro (Setup UI).
- **Passos 1-2**: Navegação de GESTOR ao painel, acesso ao processo, exibição de tela "Detalhes do processo" (Cenário 1).
- **Passo 3**: Verificação de presença do botão "Aceitar cadastro em bloco" (Cenário 1).
- **Passo 4**: Clique no botão (Cenário 1).
- **Passo 5**: Abertura de modal com:
  - Título "Aceite de cadastro em bloco"
  - Texto "Selecione as unidades cujos cadastros deverão ser aceitos"
  - Lista de unidades com checkboxes (pré-selecionadas)
  - Botões "Cancelar" e "Registrar aceite"
- **Passo 6**: Cancelamento da operação retorna à tela Detalhes do processo (Cenário 1).
- **Passos 7-9**: Confirmação do aceite (Cenário 2):
  - Clique em "Registrar aceite"
  - Mensagem de confirmação "Cadastros aceitos em bloco"
  - Redirecionamento ao painel

## Lacunas em relação ao requisito
O teste **NÃO cobre**:

- **Passo 5 - Detalhes do modal**:
  - Requisito diz "Lista das unidades operacionais ou interoperacionais subordinadas" com "checkbox (selecionado por padrão), a sigla e o nome da unidade"
  - Teste não valida:
    - Que apenas unidades subordinadas aparecem (não testa deselecionar/reselecionar unidades)
    - Que checkboxes estão pré-selecionados
    - Que sigla e nome aparecem corretamente para cada unidade

- **Passo 8 - Processamento para cada unidade selecionada**:
  - 8.1: Registro de análise com:
    - Data/hora: Data/hora atual
    - Unidade: [SIGLA_UNIDADE_ATUAL]
    - Resultado: "Aceite"
    - Observação: "De acordo com o cadastro de atividades da unidade"
    - Teste não valida se análise foi registrada
  
  - 8.2: Registro de movimentação com:
    - Data/hora, unidade origem, unidade destino, descrição específica
    - Teste não valida se movimentação foi registrada
  
  - 8.3: Registro de alerta com campos específicos
    - Teste não valida se alerta foi criado
  
  - 8.4: E-mail para unidade superior com template específico
    - Teste não valida envio de e-mail
    - Teste não valida conteúdo do e-mail

- **Pré-condições**:
  - Teste valida que GESTOR consegue aceitar, mas não valida acesso negado para outro perfil
  - Teste não valida que subprocessos devem estar em situação 'Cadastro disponibilizado' (pré-condição)

- **Seleção condicional de unidades**:
  - Requisito permite seleção individual de unidades
  - Teste não valida deselecção de uma unidade e aceite apenas das selecionadas
  - Teste assume que todas as unidades estão selecionadas

- **Múltiplas unidades**:
  - Setup cria aparentemente apenas SECAO_221
  - Requisito menciona "unidades subordinadas" (plural)
  - Teste não valida processamento de múltiplas unidades com passo 8 executado para cada uma

## Alterações necessárias no teste E2E
1. **Aprimorar validação da estrutura do modal (Passo 5)**:
   - Validar que checkboxes estão pré-selecionados
   - Validar que sigla e nome aparecem para cada unidade
   - Se houver múltiplas unidades subordinadas, validar todas aparecem

2. **Adicionar teste de seleção condicional (Passo 8)**:
   - Criar processo com múltiplas unidades subordinadas
   - Testar deselecção de uma unidade e confirmação de aceite
   - Validar que apenas unidades selecionadas recebem aceite

3. **Adicionar validação de pré-condição**:
   - Testar acesso negado para CHEFE ou ADMIN (apenas GESTOR deve ver botão)
   - Testar indisponibilidade do botão quando subprocessos não estão em 'Cadastro disponibilizado'

4. **Expandir setup com múltiplas unidades subordinadas**:
   - Garantir que processo tenha múltiplas unidades operacionais/interoperacionais subordinadas
   - Validar que todas aparecem no modal

5. **Adicionar validação de efeitos colaterais (se possível)**:
   - Se houver API para verificar análises/movimentações, validar registro após aceite
   - Validar mudança de situação ou dados de backend

## Notas e inconsistências do requisito
- **Falta de clareza em "unidades operacionais ou interoperacionais subordinadas"**: Não fica claro se interoperacionais é subset ou categoria separada. Afeta quais unidades devem aparecer.
- **Falta de especificação de ordem das unidades**: Requisito não define se lista é ordenada por sigla, nome, hierarquia, etc.
- **Observação fixa**: Passo 8.1 estabelece observação fixa "De acordo com o cadastro de atividades da unidade", mas não deixa claro se GESTOR pode personalizar.
- **Typo no e-mail**: Passo 8.4 diz "As análises já podem ser realizadas" mas em CDU-22 não há análises, apenas aceite (confusão com CDU-20).
- **Ambiguidade em seleção**: Requisito diz "checkbox (selecionado por padrão)" mas não especifica se há botão "Selecionar todos" ou "Desselecionar todos".
