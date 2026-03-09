# Alinhamento CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos

## Cobertura atual do teste
O teste `cdu-10.spec.ts` em 6 testes:

**Teste 1: Setup**
- Criação de processo mapeamento finalizado (gera mapa vigente)
- Criação de processo de revisão
- CHEFE adiciona atividade + conhecimento e volta (situação muda para "Revisão em andamento")

**Teste 2: Validação - Atividade sem conhecimento**
- CHEFE adiciona atividade incompleta
- Clique em "Disponibilizar" dispara erro inline com "conhecimento"
- Adição de conhecimento permite prosseguir
- Cancelamento do diálogo de confirmação

**Teste 3: Caminho Feliz - Disponibilizar Revisão**
- CHEFE clica em "Disponibilizar"
- Valida diálogo com título exato "Disponibilização da revisão do cadastro" e mensagem sobre "finalização da revisão e disponibilização"
- Confirmação (botão específico `btn-confirmar-disponibilizacao`)
- Validação de mensagem "Disponibilizado com sucesso" no Painel
- GESTOR verifica alerta com texto específico "Revisão do cadastro da unidade [UNIDADE] disponibilizada para análise"
- GESTOR verifica situação "Revisão do cadastro disponibilizada" e movimentação "Disponibilização da revisão do cadastro de atividades"

**Teste 4: Devolução e Histórico**
- GESTOR devolve com observações
- CHEFE verifica situação "Revisão em andamento" (retornou de análise)
- CHEFE abre "Histórico de análise" e verifica resultado "Devolução" e observações
- CHEFE redisponibiliza

**Teste 5: Histórico Retém Análises Após Nova Disponibilização**
- GESTOR devolve 2 vezes (2ª e 3ª devoluções)
- CHEFE valida que histórico contém TODAS as devoluções em ordem decrescente (mais recente primeiro)

**Teste 6: Cancelar Disponibilização**
- CHEFE clica em "Disponibilizar"
- Cancelamento retorna para tela de atividades

## Lacunas em relação ao requisito
**Não coberto**:
- **Passo 5**: Validação de botão "Histórico de análise" quando subprocesso retornou de análise - teste cobre após devolução mas não valida contexto de "primeira disponibilização sem histórico anterior"
- **Passo 7.1**: Validação deve indicar "quais atividades estão precisando de adição de conhecimentos" - teste apenas verifica presença de erro, não identificação específica
- **Passo 10**: Mudança de situação para "Revisão do cadastro disponibilizada" - teste verifica após gestor aceita (passo 10 explícito), mas não valida imediatamente após confirmação do CHEFE
- **Passo 11**: Registro de movimentação com campos (Data/hora, Unidade origem, Unidade destino, Descrição exata) - teste valida descrição mas não os outros campos separadamente
- **Passo 12**: E-mail de notificação com modelo exato (Assunto "SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: [SIGLA]", corpo com "concluiu a revisão e disponibilizou seu cadastro") não é verificado
- **Passo 13**: Alerta interno com campos específicos (Descrição, Processo, Data/hora, Unidades origem/destino) não é completamente validado - teste valida apenas texto parcial do alerta
- **Passo 14**: Definição de data/hora de conclusão da Etapa 1
- **Passo 15**: Mensagem exata "Revisão do cadastro de atividades disponibilizada" não é validada (teste verifica "Disponibilizado com sucesso")

**Teste parcialmente coberto**:
- Teste usa o mesmo processo/unidade em vários testes em série, o que não reenacted cenários de múltiplos processos paralelos
- Validação de erro (7.1) é genérica
- Mensagem pós-redirecionamento não matches exatamente o requisito

## Alterações necessárias no teste E2E
- Validar que erro de validação especifica QUAIS atividades faltam conhecimento
- Adicionar validação de situação "Revisão do cadastro disponibilizada" imediatamente após CHEFE confirmar (antes de GESTOR agir)
- Validar e-mail com assunto e corpo exatos (ou via API/log)
- Validar alerta com todos os 5 campos especificados
- Adicionar teste que valida conclusão de Etapa 1 (data/hora atual)
- Validar mensagem exata "Revisão do cadastro de atividades disponibilizada" no Painel após redirecionamento
- Adicionar teste para validar movimentação com todos os campos (não apenas descrição)
- Adicionar teste que valida botão "Histórico de análise" ausente quando não há análises prévias vs presente quando há
- Testar cenário onde múltiplas devoluções ocorrem e histórico mantém todas em ordem (já feito no teste 5, bom)

## Notas e inconsistências do requisito
- **Ambiguidade em 7.1**: Formato de indicação de atividades incompletas (lista, badges, ícones)
- **Referência incompleta**: "[SIGLA_UNIDADE_SUPERIOR]" - cálculo de unidade superior não definido
- **Falta de detalhe em 12**: "[URL_SISTEMA]" não especifica se é completo ou parcial, e quebra de linha em "O" da terceira linha sugere erro de formatação
- **Indefinição em 14**: "Etapa 1" - estrutura de etapas não definida neste CDU
- **Imprecisão em 10 vs 11**: Requisito não clarifica se situação muda ANTES ou DEPOIS de registrar movimentação
