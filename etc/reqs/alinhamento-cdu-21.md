# Alinhamento CDU-21 - Finalizar processo de mapeamento ou de revisão

## Cobertura atual do teste
O teste E2E cobre:

- **Pré-condição e setup**: Criação de processo com múltiplos subprocessos em situação "Mapa homologado" (Preparações 1-7).
- **Passos 1-2**: Navegação ao painel, seleção do processo, exibição de tela "Detalhes do processo" (Cenário 1).
- **Passo 3**: Verificação de presença do botão "Finalizar processo" (Cenário 1).
- **Passo 6.1**: Cancelamento da finalização (Cenário 2) - clique em "Finalizar processo", abertura de diálogo com mensagem de confirmação, clique em "Cancelar", permanência na tela.
- **Passos 7-10**: Finalização bem-sucedida (Cenário 3) - clique em "Confirmar", redirecionamento ao painel, verificação de mensagem "Processo finalizado".
- **Estado pós-finalização** (Cenário 4): Validação de que processo finalizado não exibe botões de ação (Finalizar, Aceitar em bloco, Homologar em bloco) e subprocessos exibem card de visualização em vez de edição.

## Lacunas em relação ao requisito
O teste **NÃO cobre**:

- **Passo 4**: "O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na situação 'Mapa homologado'."
  - Teste não valida a verificação dessa condição
  - Teste não cobre cenário onde nem todos os subprocessos estão homologados (passo 5 - mensagem de erro)

- **Passo 5**: Mensagem de erro condicional - "Não é possível finalizar o processo enquanto houver unidades com mapa de competência ainda não homologado"
  - Teste não tenta finalizar processo com subprocesso em outro estado
  - Teste não valida mensagem de erro exata

- **Passo 6**: Diálogo de confirmação
  - Teste valida presença de diálogo mas não valida textos exatos:
    - Título: "Finalização de processo"
    - Mensagem: "Confirma a finalização do processo [DESCRICAO_PROCESSO]? Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades participantes do processo."
  - Mensagem inclui placeholders que teste não valida

- **Passo 8**: "O sistema define os mapas de competências dos subprocessos como os mapas de competências vigentes das respectivas unidades."
  - Teste não valida se mapas se tornaram "vigentes"
  - Validação requer acesso a dados de backend (não apenas UI)

- **Passo 9**: Mudança de situação do processo para "Finalizado"
  - Teste verifica que processo não aparece mais na lista ativa (Cenário 3, comentário)
  - Mas não valida explicitamente que situação mudou para "Finalizado"

- **Passo 9.1 e 9.2**: Notificações por e-mail
  - Teste não valida envio de e-mails
  - Teste não valida diferenciais de e-mails por tipo de unidade (operacional/interoperacional vs. intermediária)
  - Teste não valida conteúdo dos e-mails (templates específicos com placeholders)

- **Pré-condição "Perfil ADMIN"**:
  - Teste é executado com `autenticadoComoAdmin`, mas não há teste de acesso negado para outro perfil

- **Múltiplas unidades**:
  - Setup cria apenas uma unidade alvo (SECAO_221)
  - Requisito menciona notificação a "todas as unidades participantes do processo"
  - Teste não valida comportamento com múltiplas unidades operacionais, intermediárias e interoperacionais

## Alterações necessárias no teste E2E
1. **Adicionar validação de verificação de pré-requisito (Passo 4-5)**:
   - Criar novo teste que tenta finalizar processo com subprocesso em situação diferente de "Mapa homologado"
   - Validar mensagem de erro exata: "Não é possível finalizar o processo enquanto houver unidades com mapa de competência ainda não homologado"

2. **Aprimorar validação do diálogo de confirmação (Passo 6)**:
   - Validar título exato: "Finalização de processo"
   - Validar mensagem contendo: "Confirma a finalização do processo"
   - Validar presença de "[DESCRICAO_PROCESSO]" expandida corretamente na mensagem

3. **Adicionar validação de situação do processo (Passo 9)**:
   - Após finalização, navegar até processo e validar situação exibida como "Finalizado" ou "Concluído"

4. **Expandir setup com múltiplas unidades**:
   - Criar processo com unidades operacionais, intermediárias e interoperacionais
   - Validar que todos os tipos recebem notificação apropriada (mesmo sem poder verificar e-mail, validar logs ou alertas no sistema)

5. **Adicionar teste de acesso negado**:
   - Tentar finalizar processo com GESTOR e validar que botão não está visível ou não funciona

6. **Adicionar teste de estado definitivo (Passo 8)**:
   - Se possível acessar dados de backend, validar que mapas se tornaram "vigentes"
   - Ou validar que após finalização, atividades e mapas não podem ser mais editados

## Notas e inconsistências do requisito
- **Ambiguidade em "unidades operacionais e interoperacionais"**: Requisito não deixa claro se "interoperacionais" é subset de "operacionais" ou categoria separada. Afeta lógica de verificação no passo 4.
- **Comportamento em cadeia**: Requisito não especifica o que acontece se uma unidade não conseguir ser notificada (e-mail falha). Teste não pode validar isso.
- **Falta de especificidade em "mapas vigentes"**: Passo 8 não deixa claro se há histórico de mapas anteriores que são substituídos ou se é primeira vez que se torna "vigente".
- **Typo no e-mail**: Passo 9.1 e 9.2 contêm "no O sistema de Gestão de Competências" (redundância).
- **Inconsistência no nome**: Cenário 4 valida página com texto "Processo concluído" mas requisito usa "Finalizado". Possível divergência entre requisito e implementação.
