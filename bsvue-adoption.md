# Análise da Migração para BootstrapVueNext

Este documento detalha um plano de ação para a adoção da biblioteca de componentes `BootstrapVueNext` no frontend da aplicação SGC.

## Contexto Atual

A `BootstrapVueNext` é uma biblioteca que implementa os componentes do Bootstrap 5 como componentes Vue 3, seguindo as melhores práticas do ecossistema Vue, incluindo `TypeScript` e a `Composition API`.

O projeto já utiliza o `Bootstrap 5` para estilização. Os componentes Vue existentes são construídos com elementos HTML padrão e classes do Bootstrap, o que significa que toda a lógica de interação (como controle de modais, abas e `tooltips`) é gerenciada manualmente.


## Plano de Migração

Propomos uma adoção gradual e controlada para minimizar riscos e permitir que a equipe se familiarize com a nova biblioteca.

### Migração Gradual
1.  **Priorizar Componentes:**
    - Crie uma lista de todos os componentes a serem migrados e priorize-os com base na complexidade e no impacto. Sugestão de ordem:
        1.  Modais (`*Modal.vue`)
        2.  Tabelas (`Tabela*.vue`)
        3.  Formulários e componentes de layout.
2.  **Trabalho em Paralelo:**
    - A migração pode ser dividida em `tasks` menores e distribuída entre os membros da equipe.
    - Novos componentes devem, obrigatoriamente, ser criados com a `BootstrapVueNext`.
3.  **Revisão de Código (PRs):**
    - Realize revisões de código cuidadosas para garantir que a nova biblioteca está sendo usada corretamente e que não há regressões.

### Limpeza
1.  **Remover Código Legado:**
    - Após a migração de todos os componentes, remova qualquer código de suporte ou `helpers` que se tornaram obsoletos.
2.  **Atualizar Documentação:**
    - Atualize o `README.md` do frontend e outros guias internos para refletir o novo `stack` de tecnologia.

## Progresso da Migração

- [x] **Modais (`*Modal.vue`):** Todos os componentes de modal foram migrados para usar o componente `<b-modal>` da `BootstrapVueNext`. O componente `BaseModal.vue` foi removido.
- [x] **Tabelas (`Tabela*.vue`):** Migração concluída.
    - [x] `TabelaAlertas.vue`: Migrado para `b-table`.
    - [x] `TabelaProcessos.vue`: Migrado para `b-table`.
    - [x] `TabelaMovimentacoes.vue`: Migrado para `b-table`.
- [x] **Formulários e componentes de layout:** Migração em andamento.
  - [x] `CriarCompetenciaModal.vue`: Formulário migrado para `BFormTextarea` e `BFormCheckbox`.
  - [x] `LoginView.vue`: Formulário migrado para `BFormInput` e `BFormSelect`.
  - [x] `CadAtividades.vue`: Formulários migrados para `BFormInput`.
  - [x] `CadAtribuicao.vue`: Formulário migrado para `BFormSelect`, `BFormInput` e `BFormTextarea`.
  - [x] `CadProcesso.vue`: Formulário migrado para `BFormInput` e `BFormSelect`.
  - [x] `ConfiguracoesView.vue`: Formulários migrados para `BFormInput`.
  - [x] `OcupacoesCriticas.vue`: Formulário migrado para `BFormInput`, `BFormSelect`, `BFormTextarea` e `BFormCheckbox`.
  - [x] `CadMapa.vue`: Formulário migrado para `BFormTextarea`, `BFormCheckbox` e `BFormInput`.
  - [x] `DiagnosticoEquipe.vue`: Formulário migrado para `BFormSelect` e `BFormTextarea`.
  - [x] `RelatoriosView.vue`: Formulário migrado para `BFormSelect` e `BFormInput`.
  - [x] `VisAtividades.vue`: Formulário migrado para `BFormTextarea`.
  - [x] `VisMapa.vue`: Formulário migrado para `BFormTextarea`.
  - [x] `AceitarMapaModal.vue`: Formulário migrado para `BFormTextarea`.
  - [x] `AcoesEmBlocoModal.vue`: Formulário migrado para `BFormCheckbox`.
  - [x] `ArvoreUnidades.vue`: Formulário migrado para `BFormCheckbox`.
  - [x] `DisponibilizarMapaModal.vue`: Formulário migrado para `BFormInput`.
  - [x] `EditarConhecimentoModal.vue`: Formulário migrado para `BFormTextarea`.
  - [x] `ImportarAtividadesModal.vue`: Formulário migrado para `BFormSelect` e `BFormCheckbox`.
  - [x] `ModalAcaoBloco.vue`: Formulário migrado para `BFormCheckbox`.
