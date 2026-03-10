# Alinhamento CDU-24 - Disponibilizar mapas de competências em bloco

## Cobertura atual do teste
O teste E2E cobre:

- **Setup completo**: Criação de processo, adição de atividades, disponibilização de cadastro, aceites de cadastro por GESTORs, homologação de cadastro por ADMIN (Fluxo completo até "Cadastro homologado").
- **Criação de mapa**: ADMIN cria competência associada à atividade (passo 10 do fluxo).
- **Passos 4-5**: Navegação ao processo, identificação de unidades com mapas criados, clique em "Disponibilizar mapas em bloco", abertura de modal (Fluxo completo).
- **Passo 5 - Validação de modal**:
  - Título "Disponibilização de mapa em bloco"
  - Texto "Selecione abaixo as unidades cujos mapas deverão ser disponibilizados"
  - Campo de data "Data Limite" visível (Fluxo completo)
  - Checkboxes para unidades (assumido)
  - Botões "Cancelar" e "Disponibilizar"
- **Passo 7**: Preenchimento de data limite (Fluxo completo).
- **Passos 6 e 7**: Cancelamento não testado, mas clique em "Disponibilizar" é testado (Fluxo completo).
- **Passos 9-11**: Validação de sucesso:
  - Mensagem "Mapas de competências disponibilizados em bloco" (Fluxo completo)
  - Redirecionamento ao painel (Fluxo completo)

## Lacunas em relação ao requisito
O teste **NÃO cobre**:

- **Passo 6**: Cancelamento da operação (teste não testa este passo)
  - Requisito: Modal deve fechar, tela Detalhes do processo deve permanecer

- **Passo 8**: Verificação de consistência entre mapas de competências e cadastros de atividades
  - Requisito: "O sistema verifica se todas as competências dos mapas de competências dos subprocessos das unidades selecionadas estão associadas a pelo menos uma atividade dos cadastros das unidades, e, em sentido oposto, se todas as atividades foram associadas a pelo menos uma competência do mapa da unidade."
  - Teste não valida essa verificação
  - Teste cria apenas uma competência e uma atividade e as associa corretamente, então nunca testa falha

- **Passo 9**: Mensagem de erro condicional
  - Requisito: Se verificação falhar, exibir mensagem "Não é possível realizar a disponibilização em bloco dos mapas de competências das unidades [LISTA_UNIDADES_SELECIONADAS]. Realize a disponibilização individual do mapa de cada unidade para obter maiores detalhes."
  - Teste não valida essa mensagem (não testa cenário de falha)

- **Passo 10 - Processamento para cada unidade selecionada**:
  - 10.1: Registro de observação "Mapa disponibilizado em bloco" e data limite na etapa 2
    - Teste não valida se observação e data limite foram registradas
  
  - 10.2: Alteração de situação para "Mapa disponibilizado"
    - Teste não valida explicitamente se situação mudou (apenas redireciona)
  
  - 10.3: Registro de movimentação com campos específicos
    - Teste não valida se movimentação foi registrada
  
  - 10.4: E-mail para unidade do subprocesso com template específico
    - Teste não valida envio de e-mail
    - Teste não valida conteúdo do e-mail
    - Teste não valida que placeholder [DATA_LIMITE] é substituído
  
  - 10.5: Criação de alerta
    - Teste não valida se alerta foi criado
  
  - 10.6: Exclusão de sugestões apresentadas do mapa
    - Teste não valida se sugestões foram apagadas
  
  - 10.7: Notificação de unidades superiores com lista de subordinadas selecionadas
    - Teste não valida envio de e-mail para unidades superiores
    - Teste não valida que lista de subordinadas é mostrada corretamente
    - Teste não valida que todos os níveis da hierarquia são notificados

- **Pré-condições**:
  - Teste valida que ADMIN consegue disponibilizar
  - Mas não valida acesso negado para outro perfil
  - Teste não valida pré-condição exata: processo deve ser "mapeamento" com subprocessos em "Mapa criado" OU "revisão" com subprocessos em "Mapa ajustado"
  - Setup cria "MAPEAMENTO" e mapa em estado "Mapa criado" (implícito), então pré-condição é atendida, mas teste não valida cenário de "revisão" e "Mapa ajustado"

- **Seleção condicional de unidades**:
  - Requisito permite seleção individual
  - Teste não valida deselecção de uma unidade e disponibilização apenas das selecionadas
  - Teste assume todas selecionadas

- **Múltiplas unidades**:
  - Setup cria aparentemente apenas SECAO_221
  - Requisito menciona "unidades" (plural)
  - Teste não valida processamento de múltiplas unidades com passo 10 executado para cada uma
  - Teste não valida cenários onde há unidades em hierarquias diferentes (para testar passo 10.7)

- **Validação de campo Data Limite**:
  - Requisito diz campo é "de preenchimento obrigatório"
  - Teste não valida se campo é obrigatório (tenta submeter sem data? Esperado falhar)
  - Teste preenche data sem validar formato ou constraints

- **Estado pós-disponibilização**:
  - Teste não volta e valida que situação mudou para "Mapa disponibilizado"
  - Teste não valida que mapas agora podem ser validados por CHEFEs

## Alterações necessárias no teste E2E
1. **Adicionar teste de cancelamento (Passo 6)**:
   - Clicar em "Cancelar" no modal
   - Validar que modal fecha e tela Detalhes do processo permanece

2. **Adicionar testes de validação de consistência (Passos 8-9)**:
   - Criar processo com mapa que tem competência NÃO associada a atividade
   - Testar que disponibilização falha com mensagem de erro exata
   - Criar processo com atividade NÃO associada a competência
   - Testar que disponibilização falha

3. **Adicionar teste de seleção condicional (Passo 10)**:
   - Criar processo com múltiplas unidades
   - Deselecionar uma unidade
   - Testar que apenas unidades selecionadas são disponibilizadas

4. **Adicionar validação de campo Data Limite**:
   - Testar que campo é obrigatório (tentar submeter sem data)
   - Testar que campo rejeita datas inválidas se houver validação
   - Testar que data é salva e aparece em notificações

5. **Expandir setup com múltiplas unidades em hierarquias**:
   - Criar processo com unidades em diferentes níveis hierárquicos
   - Validar que unidades superiores recebem notificação com lista de subordinadas

6. **Adicionar validação de pré-condição**:
   - Testar com processo de "REVISÃO" e subprocessos em "Mapa ajustado"
   - Testar que botão não aparece quando subprocessos não estão em estados elegíveis

7. **Adicionar validação pós-disponibilização**:
   - Após sucesso, navegar de volta ao processo
   - Validar que situação mudou para "Mapa disponibilizado"
   - Validar que mapas estão prontos para validação

8. **Adicionar teste de acesso negado**:
   - Testar que GESTOR ou CHEFE não conseguem acessar/usar botão

9. **Adicionar validação de exclusão de sugestões (Passo 10.6)**:
   - Se sugestões foram apresentadas em ciclo anterior, validar que são apagadas após disponibilização em bloco

## Notas e inconsistências do requisito
- **Passo 3**: "O sistema identifica que existem unidades com subprocessos com mapas criados ou ajustados mas ainda não disponibilizados." - Requisito não especifica com clareza quando botão aparece. Pré-condição diz que processo deve ter "ao menos uma unidade com subprocesso na situação 'Mapa criado'" mas não está claro se botão aparece só quando há unidades nesse estado.

- **Passo 8**: Verificação de consistência é complexa ("todas as competências... associadas a pelo menos uma atividade" AND "todas as atividades... associadas a pelo menos uma competência"). Teste não consegue validar essa lógica sem criar cenários de falha.

- **Passo 10.1**: "Registra a informação 'Mapa disponibilizado em bloco' na observação de disponibilização" - Significa um campo de observação separado? Concatenação? Não fica claro como essa observação é armazenada/exibida.

- **Passo 10.7**: "Agrupa as unidades selecionadas com suas unidades superiores em todos os níveis da hierarquia" - Linguagem ambígua. Significa que notifica TODAS as unidades superiores de CADA unidade selecionada? Ou que agrupa por ramo hierárquico antes de notificar?

- **Pré-condição ambígua**: "Processo de mapeamento com ao menos uma unidade com subprocesso na situação 'Mapa criado' ou processo de revisão com ao menos uma unidade com subprocesso na situação 'Mapa ajustado'." - Não fica claro se esses são os ÚNICOS estados elegíveis ou se há outros. Teste funciona quando há também unidades com outros estados.

- **Falta de clareza em "data limite"**: Requisito diz campo é "obrigatório" mas não especifica:
  - Formato de entrada (dd/mm/yyyy, yyyy-mm-dd, etc.)
  - Constraints (data no futuro? No mínimo X dias?)
  - Mensagens de erro se inválida

- **Typo no e-mail**: Passo 10.4 e 10.7 contêm "no O sistema" (redundância em alguns cenários).
