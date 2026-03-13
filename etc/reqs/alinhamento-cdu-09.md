# Alinhamento CDU-09 - Disponibilizar cadastro de atividades e conhecimentos

## Cobertura atual do teste
O teste `cdu-09.spec.ts` cobre:

**Cenário 1: Validação (Atividade sem conhecimento)**
- Adição de atividade sem conhecimento
- Clique em "Disponibilizar" dispara erro inline com menção a "conhecimento" (passos 1-7.1)
- Adição de conhecimento corrige validação
- Cancelamento do diálogo de confirmação

**Cenário 2: Caminho feliz**
- Adição de atividade com conhecimento
- Clique em "Disponibilizar"
- Apresentação de diálogo de confirmação (título "Disponibilização do cadastro", mensagem sobre finalização e análise) (passo 8)
- Confirmação (passo 9) leva a Painel

**Cenário 3: Devolução e Histórico de Análise**
- GESTOR acessa subprocesso e clica em "Devolver"
- Preenchimento de observações (motivo)
- Confirmação de devolução leva a Painel
- CHEFE volta ao subprocesso, verifica situação "Cadastro em andamento" e abre "Histórico de análise"
- Modal mostra resultado "Devolução" e observações inseridas

## Lacunas em relação ao requisito
**Não coberto**:
- **Passo 5**: Validação de botão "Histórico de análise" quando subprocesso retornou de análise pelas unidades superiores
  - Teste cobre devolução posterior mas não testa botão na primeira disponibilização sem histórico anterior
  - Teste não valida que botão desaparece quando não há análises prévias
- **Passo 7.1**: Validação deve indicar "quais atividades estão precisando de adição de conhecimentos" - teste apenas verifica presença de erro genérico, não a identificação específica
- **Passo 10**: Mudança de situação para "Cadastro disponibilizado" não é explicitamente verificada (teste verifica "Cadastro em andamento" após devolução, não "Cadastro disponibilizado" após sucesso)
- **Passo 11**: Registro de movimentação com campos específicos (Data/hora, Unidade origem, Unidade destino, Descrição "Disponibilização do cadastro de atividades")
- **Passo 12**: Notificação por e-mail com modelo exato (Assunto, Prezado, corpo com URL do sistema) não é verificada
- **Passo 13**: Criação de alerta interno (Descrição, Processo, Data/hora, Unidade origem, Unidade destino) não é validada
- **Passo 14**: Definição de data/hora de conclusão da etapa 1
- **Passo 15**: Mensagem de sucesso "Cadastro de atividades disponibilizado" não é validada após confirmação

**Teste parcialmente coberto**:
- Erro de validação (7.1) é genérico; requisito exige indicação específica de quais atividades faltam conhecimento
- Fluxo de devolução é testado mas não está no requisito (é fluxo externo, provavelmente de outro CDU)
- Teste não valida situação exata "Cadastro disponibilizado" (apenas "Cadastro em andamento" em contexto de devolução)

## Alterações necessárias no teste E2E
- Adicionar teste que valida mensagem de erro especificando QUAIS atividades faltam conhecimento (não apenas presença de erro)
- Validar situação exata do subprocesso após sucesso: "Cadastro disponibilizado"
- Adicionar teste que verifica movimentação com todos os campos corretos na tabela
- Validar presença de alerta na tela de alertas para unidade superior (ou via API)
- Adicionar verificação de e-mail enviado (ou log de e-mail) com modelo correto
- Validar mensagem de sucesso "Cadastro de atividades disponibilizado" na tela após confirmação
- Adicionar teste para botão "Histórico de análise" ausente quando não há análises prévias
- Dividir teste em: "Primeiro envio" (sem histórico) e "Reenviio após devolução" (com histórico)
- Validar conclusão de etapa 1 (data/hora atual) se acesso a dados de etapa existir

## Notas e inconsistências do requisito
- **Ambiguidade em Passo 7.1**: "indica quais atividades" não especifica se é lista numerada, badges, destaque visual ou ícone
- **Referência incompleta**: Requisito cita "[SIGLA_UNIDADE_SUPERIOR]" mas não define como sistema calcula hierarquia
- **Falta de detalhe em Passo 12**: "[URL_SISTEMA]" não especifica se é URL completa ou apenas domínio
- **Indefinição em 14**: "Etapa 1" é conceito não definido neste CDU - refere-se a estrutura de etapas da CDU-01?
- **Ambiguidade em 15**: "redireciona para o Painel" - se houve erro em 7.1, mensagem de erro fica visível na mesma tela ou há redirecionamento com erro?
