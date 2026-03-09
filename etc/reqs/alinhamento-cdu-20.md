# Alinhamento CDU-20 - Analisar validação de mapa de competências

## Cobertura atual do teste
O teste E2E cobre:

- **Pré-condição e setup**: Fixture cria um processo com mapa validado em estado apropriado (Cenário 1).
- **Acesso ao mapa**: Login com GESTOR_SECRETARIA_1, navegação para subprocesso ASSESSORIA_11 e acesso ao mapa (passo 1-3).
- **Tela de visualização**: Verifica presença dos botões "Histórico de análise" e "Devolver para ajustes" (passo 4).
- **Fluxo de devolução**:
  - Clique em "Devolver para ajustes" (passo 8.1)
  - Verificação de estado do botão de confirmação (desabilitado sem observação, habilitado com observação) - validação de regra de negócio
  - Preenchimento de observação (passo 8.4)
  - Cancelamento da devolução (passo 8.3)
- **Fluxo de aceite**:
  - Clique em "Registrar aceite" / "Homologar" (passos 9.1 / 10.1)
  - Confirmação no diálogo (passos 9.4 / 10.4)
  - Redirecionamento ao painel e validação de mensagem (passos 9.9 / 10.6)
- **Suporte para múltiplos perfis**: Cenário 1 testa GESTOR, Cenário 2 testa ADMIN (com botão "Homologar" em vez de "Registrar aceite").

## Lacunas em relação ao requisito
O teste **NÃO cobre**:

- **Passo 5 - Botão "Ver sugestões" (condicional)**:
  - Requisito: Se a situação do subprocesso for "Mapa com sugestões", deve-se exibir botão "Ver sugestões" antes de "Histórico de análise"
  - Teste não valida presença/ausência deste botão
  - Teste não valida abertura do modal com sugestões registradas

- **Passo 6 - Histórico de análise (aberto mas não testado completamente)**:
  - Requisito: Modal com tabela contendo data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite'), observações
  - Teste não clica em "Histórico de análise" para validar conteúdo
  - Teste não valida estrutura e dados da tabela

- **Passo 8 - Fluxo de devolução (parcialmente coberto)**:
  - 8.2: Modal com título "Devolução" e mensagem específica - teste não valida texto exato
  - 8.4: Campo de observação é "opcional" - teste testa habilitação do botão, mas não fluxo com observação omitida
  - 8.5: Registro de análise com campos específicos (data/hora, unidade, resultado 'Devolução', observação) - não testado se os dados são realmente persistidos
  - 8.6: "Identifica a unidade de devolução como sendo a unidade de origem da última movimentação" - lógica complexa não testada
  - 8.7: Registro de movimentação - não testado
  - 8.8: Alteração condicional de situação para "Mapa disponibilizado" se devolução for para a própria unidade - não testado
  - 8.8: Apagamento de data/hora de conclusão da etapa 2 - não testado
  - 8.9: Notificação por e-mail - não testado
  - 8.10: Criação de alerta interno - não testado
  - 8.11: Mensagem "Devolução realizada" - não testado

- **Passo 9 - Fluxo de aceite (GESTOR) (parcialmente coberto)**:
  - 9.2: Título do diálogo "Aceite" e mensagem específica - teste não valida textos exatos
  - 9.4: Campo de observação é opcional - teste não cobre caso sem observação
  - 9.5: Registro de análise - não testado se persistido corretamente
  - 9.6: Registro de movimentação - não testado
  - 9.7: Notificação por e-mail para unidade superior - não testado
  - 9.8: Criação de alerta interno - não testado

- **Passo 10 - Fluxo de homologação (ADMIN) (parcialmente coberto)**:
  - 10.2: Título "Homologação" e mensagem específica - teste não valida textos exatos
  - 10.5: Alteração de situação para "Mapa homologado" - teste não valida se foi alterada (apenas redireciona)
  - 10.6: Mensagem exata "Homologação efetivada" - teste valida presença, mas em contexto limitado

- **Cenários de validação condicional**:
  - Teste não valida presença/ausência do botão "Ver sugestões" quando situação é "Mapa com sugestões"
  - Teste não cobre devolução com unidade de destino ≠ unidade origem da última movimentação

- **Regra de acesso**:
  - Requisito especifica "GESTOR e ADMIN" como atores
  - Teste não valida acesso negado para CHEFE ou outro perfil

## Alterações necessárias no teste E2E
1. **Adicionar cobertura do botão "Ver sugestões"**:
   - Criar fixture com subprocesso em situação "Mapa com sugestões" (não apenas "Mapa validado")
   - Testar presença condicional do botão "Ver sugestões"
   - Testar clique e abertura de modal com sugestões registradas

2. **Expandir cobertura do "Histórico de análise"**:
   - Testar clique no botão "Histórico de análise"
   - Validar abertura do modal com tabela
   - Validar presença de colunas: data/hora, sigla unidade, resultado, observações
   - Validar dados exatos retornados

3. **Ampliar testes do fluxo de devolução**:
   - Testar devolução COM observação (validação de persistência)
   - Testar devolução SEM observação (campo optativo)
   - Validar mensagem exata "Devolução realizada"
   - Validar cenário onde unidade de devolução ≠ unidade atual (para testar passo 8.8)

4. **Ampliar testes do fluxo de aceite (GESTOR)**:
   - Testar aceite COM observação
   - Testar aceite SEM observação
   - Validar mensagem exata "Aceite registrado"

5. **Ampliar testes do fluxo de homologação (ADMIN)**:
   - Validar mudança real de situação para "Mapa homologado" (não apenas mensagem)
   - Validar que aceite (GESTOR) diferencia-se de homologação (ADMIN)

6. **Adicionar validação de diálogos**:
   - Validar textos exatos dos títulos e mensagens dos modais
   - Validar presença/ordem dos botões

## Notas e inconsistências do requisito
- **Ambiguidade no passo 8.6**: "O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do subprocesso." Não fica claro se isso é automático ou se há seleção pelo usuário. Teste não consegue validar isso sem acesso aos dados de backend.
- **Inconsistência de rótulo**: Passo 9.1 menciona "Registrar aceite" mas passo 10 menciona "Homologar". Não fica claro se ambos os perfis veem textos diferentes ou se há um botão único que muda de comportamento.
- **Falta de validação de campos**: O requisito não especifica se campo de observação tem limite de caracteres, formato ou validações.
- **Typo no e-mail**: Passo 8.9 e 9.7 contêm "no O sistema" (redundância).
- **Falta de clareza em passo 4**: O requisito diz "com os botões" mas não especifica a ordem ou disposição. Teste assume ordem específica que pode não ser garantida.
