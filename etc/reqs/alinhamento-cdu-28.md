# Alinhamento CDU-28 - Manter atribuição temporária

## Cobertura atual do teste
O teste cobre:
- **Cenário 1**: Navegação do ADMIN até página de detalhes da unidade e verificação de visibilidade do botão "Criar atribuição".
- **Cenário 2**: Validação de campos obrigatórios (usuário, data de início, data de término, justificativa). Testa que ao deixar data de início em branco, validação do HTML5 é acionada.
- **Cenário 3**: Preenchimento correto de todos os campos (usuário selecionado, datas futuras, justificativa) e validação de mensagem de sucesso "Atribuição criada".

## Lacunas em relação ao requisito
**Lacunas críticas:**

1. **Falta validação de modal vs. página**: O requisito (passo 6) especifica que o sistema "apresenta um **modal**" com campos, mas o teste acessa uma página (`/unidade/\d+\/atribuicao$`), não um modal. Há inconsistência entre requisito e implementação.

2. **Falta validação de dropdown pesquisável**: O requisito especifica "Dropdown pesquisável `Servidores` com os nomes dos servidores da unidade". O teste usa `selectOption()` que não valida se é pesquisável ou se filtra corretamente.

3. **Falta validação de conteúdo do dropdown**: O teste não valida se o dropdown contém apenas servidores da unidade específica (SECRETARIA_2).

4. **Falta validação de notificação por e-mail**: O requisito (passo 9) especifica envio de e-mail ao servidor com formato específico (assunto, corpo com nome, período, justificativa). O teste não valida.

5. **Falta validação de alerta interno**: O requisito (passo 10) especifica criação de alerta com campos específicos (`Descrição`, `Processo` vazio, `Data/hora`, `Unidade de origem: ADMIN`, `Usuário destino`). O teste não valida.

6. **Falta validação de direitos conferidos**: O requisito (passo 11) especifica que o usuário passa a ter "os mesmos direitos do perfil CHEFE" e que "A atribuição temporária terá prioridade sobre os dados de titularidade lidos do SGRH". O teste não valida se o usuário efetivamente pode exercer direitos de CHEFE.

7. **Falta validação de datas**: O teste preenche datas hardcoded (2030-01-01 e 2030-12-31), mas não valida:
   - Se data de término posterior a data de início é validada
   - Se datas no passado são permitidas/rejeitadas
   - Se o sistema permite atribuições com datas que já passaram

8. **Falta cenário de cancelamento**: Não há teste que valida o botão "Cancelar" do modal/formulário.

9. **Falta validação de justificativa obrigatória**: O teste preenche justificativa, mas não valida explicitamente que é obrigatória tentando enviar sem ela.

10. **Falta validação de atualizações ou edições**: O requisito intitula-se "Manter atribuição temporária" que sugere CRUD (Create, Read, Update, Delete), mas o teste apenas cobre criação. Não há testes para consultar, alterar ou remover atribuições.

## Alterações necessárias no teste E2E
1. **Clarificar se é modal ou página**: 
   - Se deve ser modal, o formulário deve estar em um modal, não uma página separada
   - Se é página, o requisito precisa ser revisado para remover "modal"

2. **Adicionar validação de dropdown pesquisável**: 
   - Testar digitação no dropdown (se suporta filtro)
   - Validar que apenas servidores da unidade aparecem

3. **Adicionar validação de notificação por e-mail**: 
   - Simular ou interceptar envio de e-mail
   - Validar assunto, corpo com todos os dados (nome, período, justificativa)

4. **Adicionar validação de alerta interno**: 
   - Consultar API ou base de dados
   - Validar campos esperados, incluindo `Processo` vazio

5. **Adicionar validação de direitos conferidos**: 
   - Após criar atribuição, fazer login com o servidor
   - Validar que pode exercer ações de CHEFE (ex: aceitar mapas)

6. **Adicionar validação de ordem de datas**: 
   - Tentar preencher data de término anterior a de início
   - Validar mensagem de erro

7. **Adicionar cenário de cancelamento**: 
   - Abrir formulário, preencher parcialmente, clicar "Cancelar"
   - Validar que nenhuma atribuição foi criada

8. **Adicionar cenários de validação de datas**: 
   - Tentar datas no passado
   - Tentar datas iguais (período zero)
   - Validar comportamento esperado

9. **Adicionar testes de leitura, atualização e remoção de atribuições** (se "Manter" significa CRUD):
   - Consultar atribuição criada
   - Alterar período ou justificativa
   - Remover atribuição
   - Validar que direitos de CHEFE são revertidos após expiração

## Notas e inconsistências do requisito
- **Inconsistência modal vs. página**: O requisito diz "modal" mas a implementação usa página. Precisa clarificar qual é a intenção.

- **Falta de clareza em "prioridade sobre SGRH"**: O requisito menciona que atribuição temporária tem prioridade sobre dados do SGRH (sistema de recursos humanos externo), mas não especifica como essa prioridade é implementada ou validada.

- **Ambiguidade em "Manter"**: O título sugere operações CRUD, mas o fluxo principal descreve apenas criação. Não fica claro se há funcionalidades de leitura, atualização ou remoção.

- **Falta de clareza em "Usuário destino" do alerta**: O requisito especifica `Usuário destino: [USUARIO_SERVIDOR]`, mas não clarifica o formato (ID, username, e-mail, nome completo).
