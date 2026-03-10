# Alinhamento CDU-08 - Manter cadastro de atividades e conhecimentos

SITUACAO: Cobertura melhorada -- PENDENTE validação.

## Cobertura atual do teste
O teste `cdu-08.spec.ts` em dois cenários:

**Cenário 1 (Processo de Mapeamento)**:
- Criação de processo finalizado para importação
- Navegação para tela de atividades
- **Importação**: Seleção de processo finalizado, unidade origem, atividades (fluxo 13.1 a 13.7)
- **Detecção de duplicidade**: Tentativa de reimportar atividades com erro esperado (13.7.1-13.7.2)
- **Adição manual**: Atividade + conhecimento (passos 6-9)
- **Persistência**: Reload de página para verificar auto-save (passo 15.1)
- **Edição**: Atividade e conhecimento (passos 11, 12)
- **Remoção**: Atividade e conhecimento (passos 11.2, 12.2)
- **Ausência de botão "Impactos no mapa"** para processo de mapeamento
- **Disponibilização**: Transição para Painel (passo 15)

**Cenário 2 (Processo de Revisão)**:
- **Presença do botão "Impacto no mapa"** (passo 5)
- Abertura de modal de impactos (tela adicional além do CDU-08)

## Lacunas em relação ao requisito
**Não coberto**:
- **Passo 4**: "Se o mapa de competências da unidade do subprocesso já tiver atividades cadastradas, a tela virá previamente preenchida" - teste não valida pré-preenchimento
- **Passo 10.1**: Flexibilidade de trabalho - "Pode-se incluir primeiro várias atividades e depois os conhecimentos" vs "trabalhar em uma atividade por vez" - teste não valida ambos os fluxos
- **Passo 11.1.1**: Confirmação visual após salvar edição de atividade (volta a exibir botões Editar/Remover)
- **Passo 11.1.2**: Confirmação visual após cancelar edição (volta ao texto anterior)
- **Passo 11.2**: Confirmação de exclusão (diálogo) e remoção em cascata de conhecimentos associados
- **Passo 12.1-12.2**: Confirmação visual análoga após editar/cancelar edição de conhecimento (não há teste de cancelamento)
- **Passo 12.2**: Confirmação de exclusão de conhecimento (diálogo)
- **Passo 13.1**: Validação de que "TODOS os processos finalizados, de TODAS as unidades, devem ser mostrados para importação" - teste usa apenas uma unidade origem
- **Passo 13.3**: Verificação de expansão do modal com unidades operacionais E interoperacionais
- **Passo 14**: Mudança de situação de "Não iniciado" para "Cadastro em andamento" (ou "Revisão do cadastro em andamento") com cada ação
- **Passo 15**: Validação de que não é necessária ação adicional para persistência
- **Seção 5 (Revisão)**: Botão "Histórico de análise" não é testado no CDU-08

**Teste parcialmente coberto**:
- Importação testa apenas 1 unidade; requisito exige múltiplas unidades
- Seleção múltipla de atividades (13.5) é apenas parcialmente validada
- Auto-save é testado via reload, mas não há validação de timing automático

## Alterações necessárias no teste E2E
- Adicionar teste de pré-preenchimento quando mapa já existe
- Criar teste para validar flexibilidade de fluxo (múltiplas atividades antes de conhecimentos)
- Adicionar teste de cancelamento de edição (atividade e conhecimento) com rollback visual
- Adicionar diálogos de confirmação para remoção (atividade e conhecimento)
- Testar importação de múltiplos processos finalizados com unidades variadas (operacionais e interoperacionais)
- Validar mudança de situação após cada CRUD (create, update, delete)
- Adicionar verificação de que seleção múltipla funciona corretamente
- Adicionar teste do botão "Histórico de análise" para processo de revisão
- Expandir testes de importação para validar cenário de todas as atividades já existentes

## Notas e inconsistências do requisito
- **Ambiguidade em 13.1**: "IMPORTANTE: Todos os processos finalizados, de todas as unidades, devem ser mostrados" - não clarifica se filtra por tipo (Mapeamento/Revisão) ou mostra todos
- **Indefinição em 13.1**: Como o sistema diferencia processos "finalizados" de outras situações (em andamento, cancelado)?
- **Falta de detalhe em 13.3**: "unidades operacionais e interoperacionais" - diferença entre os dois tipos não é definida aqui
- **Imprecisão em 14**: Quando exatamente a situação muda (na primeira ação ou após todas)?
- **Ambiguidade em 15.1**: "salvas automaticamente e vinculadas" - não fica claro se vinculação é parte da persistência ou ação separada
