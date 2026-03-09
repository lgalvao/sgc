# Alinhamento CDU-23 - Homologar cadastros em bloco

## Cobertura atual do teste
O teste E2E cobre:

- **Setup de dados**: Criação de processo, adição de atividades por CHEFE, disponibilização de cadastro, aceite de cadastro por GESTORs (Setup UI).
- **Passos 1-2**: Navegação de ADMIN ao painel, acesso ao processo, exibição de tela "Detalhes do processo" (Cenário 1).
- **Passo 3**: Verificação de presença do botão "Homologar cadastro em bloco" na seção "Unidades participantes" (Cenário 1).
- **Passo 4**: Clique no botão (Cenário 1).
- **Passo 5**: Abertura de modal com:
  - Título "Homologação de cadastro em bloco"
  - Texto "Selecione abaixo as unidades cujos cadastros deverão ser homologados"
  - Lista de unidades com checkboxes (pré-selecionados)
  - Botões "Cancelar" e "Homologar"
- **Passo 6**: Cancelamento retorna à tela Detalhes do processo (Cenário 1).
- **Passos 7-9**: Confirmação da homologação (Cenário 2):
  - Clique em "Homologar"
  - Permanência na tela Detalhes do processo
  - Validação de que botão "Homologar cadastro em bloco" fica desabilitado
  - Validação de que subprocesso SECAO_221 exibe situação "Cadastro homologado"

## Lacunas em relação ao requisito
O teste **NÃO cobre**:

- **Passo 5 - Detalhes do modal**:
  - Requisito diz "Lista das unidades operacionais ou interoperacionais subordinadas cujos cadastros poderão ser homologados"
  - Teste não valida:
    - Que apenas unidades elegíveis aparecem
    - Que checkboxes estão pré-selecionados (assume, mas não valida)
    - Que sigla e nome aparecem corretamente

- **Passo 8 - Processamento para cada unidade selecionada**:
  - 8.1: Registro de movimentação com:
    - Data/hora: Data/hora atual
    - Unidade origem: "ADMIN"
    - Unidade destino: "ADMIN"
    - Descrição: "Cadastro de atividades e conhecimentos homologado"
    - Teste não valida se movimentação foi registrada
  
  - 8.2: Alteração de situação para "Cadastro homologado"
    - Teste valida que SECAO_221 exibe "Cadastro homologado" (Cenário 2)
    - Mas não valida para outras unidades se existirem múltiplas
  
  - 8.3: Criação de alerta com campos específicos
    - Teste não valida se alerta foi criado
  
  - 8.4: E-mail para unidade do subprocesso com template específico
    - Teste não valida envio de e-mail
    - Teste não valida conteúdo do e-mail

- **Pré-condições**:
  - Teste valida que ADMIN consegue homologar
  - Mas não valida acesso negado para outro perfil (GESTOR, CHEFE)
  - Teste não valida pré-condição "situação 'Cadastro disponibilizado' ou 'Cadastro aceito'" (setup coloca em "Cadastro aceito", mas teste não valida cenário com "Cadastro disponibilizado")

- **Seleção condicional de unidades**:
  - Requisito permite seleção individual de unidades
  - Teste não valida deselecção de uma unidade e homologação apenas das selecionadas
  - Teste assume que todas estão selecionadas

- **Estado após homologação (Passo 9)**:
  - Teste valida que botão fica desabilitado (Cenário 2)
  - Mas não valida se isso é porque não há mais unidades elegíveis (correto) ou outro motivo

## Alterações necessárias no teste E2E
1. **Aprimorar validação da estrutura do modal (Passo 5)**:
   - Validar que checkboxes estão pré-selecionados (não apenas assumir)
   - Validar que sigla e nome aparecem para cada unidade
   - Validar que apenas unidades operacionais/interoperacionais aparecem (não intermediárias)

2. **Adicionar teste de seleção condicional (Passo 8)**:
   - Criar processo com múltiplas unidades subordinadas
   - Testar deselecção de uma unidade e confirmação de homologação
   - Validar que apenas unidades selecionadas são homologadas

3. **Adicionar teste com múltiplas unidades**:
   - Validar que situação muda para "Cadastro homologado" em TODAS as unidades selecionadas
   - Validar comportamento quando há múltiplos subprocessos

4. **Adicionar teste de pré-condição com "Cadastro disponibilizado"**:
   - Criar outro processo onde cadastro está em "Cadastro disponibilizado" (antes de GESTOR aceitar)
   - Testar que botão de homologação aparece e funciona nesse estado também

5. **Adicionar validação de acesso negado**:
   - Testar que GESTOR ou CHEFE não veem botão ou não conseguem homologar

6. **Melhorar validação de desabilitação do botão (Passo 9)**:
   - Após homologação bem-sucedida, validar explicitamente que não há mais unidades elegíveis
   - Ou criar novo subprocesso com "Cadastro disponibilizado" e validar que seu aceite habilitaria o botão novamente

## Notas e inconsistências do requisito
- **Ambiguidade em "operacionais ou interoperacionais subordinadas"**: Mesmo problema do CDU-22. Não fica claro a hierarquia exata.
- **Unidade origem/destino = "ADMIN"**: Passo 8.1 diz que origem e destino são "ADMIN", o que parece estranho para uma movimentação. Possível erro no requisito ou significado especial não documentado.
- **Falta de clareza em quando botão aparece**: Requisito diz que botão aparece quando há "unidades subordinadas cujos cadastros poderão ser homologados", mas não define explicitamente os estados elegíveis. Pré-condição menciona "Cadastro disponibilizado" ou "Revisão do cadastro disponibilizada", mas não "Cadastro aceito" (embora setup do teste mostre funcionamento com "Cadastro aceito").
- **Ordem de apresentação**: Requisito não especifica se unidades aparecem em ordem hierárquica, alfabética, etc.
- **Ausência de mensagem de confirmação**: Passo 9 diz "O sistema mostra mensagem de confirmação: 'Cadastros homologados em bloco'" mas teste não valida presença dessa mensagem. Teste valida que permanece na tela, mas mensagem pode não ser exibida.
