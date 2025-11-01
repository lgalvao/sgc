# Análise de Cobertura de Testes E2E

Este documento detalha a análise de cobertura dos testes end-to-end (E2E) em relação aos casos de uso (CDU) definidos nas especificações.

## CDU-01: Realizar login e exibir estrutura das telas

- **Status:** Cobertura Parcial
- **Observações:**
  - O teste cobre o fluxo de login para usuários com perfil único e múltiplos perfis.
  - **GAPs:**
    - Não há teste para o caso de falha na autenticação (senha inválida).
    - Não há verificações detalhadas da estrutura da tela (barra de navegação, rodapé, ícones específicos de perfil).

---

## CDU-02: Visualizar Painel

- **Status:** Cobertura Parcial
- **Observações:**
  - O teste verifica a visibilidade de componentes-chave (como o botão "Criar processo") com base no perfil do usuário e confirma que os processos são filtrados corretamente pela unidade do usuário.
  - **GAPs:**
    - Não verifica todas as colunas da tabela de Processos.
    - Não testa a funcionalidade de ordenação de nenhuma das tabelas (Processos e Alertas).
    - Não testa a navegação para detalhes do processo para perfis `GESTOR`, `CHEFE` e `SERVIDOR`.
    - Não verifica a funcionalidade de "marcar como lido" dos alertas.

---

## CDU-03: Manter processo

- **Status:** Cobertura Parcial
- **Observações:**
  - O teste cobre os fluxos básicos de criação, edição e remoção de um processo do tipo 'Mapeamento'.
  - Cobre as validações de campos obrigatórios (descrição e unidade).
  - **GAPs:**
    - Não testa a lógica de seleção em árvore das unidades participantes.
    - Não testa a validação específica para processos de 'Revisão' ou 'Diagnóstico'.
    - Não testa o fluxo alternativo "Iniciar processo".
    - Não verifica a funcionalidade do botão "Cancelar".

---

## CDU-04: Iniciar processo de mapeamento

- **Status:** Cobertura Baixa
- **Observações:**
  - O teste verifica apenas a interação com a UI (exibição do modal de confirmação) e a desabilitação dos botões de edição após o início do processo.
  - **GAPs:**
    - Falha crítica em verificar os resultados mais importantes da ação.
    - Não há verificação da mudança de status do processo para "Em andamento".
    - Não há verificação da criação dos subprocessos.
    - Não há verificação da criação do mapa de competências vazio.
    - Não há verificação do registro de `Movimentacao`.
    - Não há verificação da criação de alertas ou do envio de e-mails.

---

## CDU-05: Iniciar processo de revisão

- **Status:** Cobertura Parcial
- **Observações:**
  - O teste é mais robusto que o do CDU-04. Ele verifica a mudança de status, o bloqueio da edição, a criação de subprocessos (via API) e a criação de um alerta (via UI).
  - **GAPs:**
    - A falha mais crítica é não verificar se uma **cópia** do mapa de competências existente foi criada.
    - Não verifica o registro de `Movimentacao`.
    - Não verifica o envio de notificações por e-mail.

---

## CDU-06: Detalhar processo

- **Status:** Cobertura Baixa
- **Observações:**
  - O teste apenas verifica se a tela de detalhes é aberta e se é possível navegar para um subprocesso.
  - **GAPs:**
    - Não verifica a visibilidade condicional de botões com base no perfil (`Finalizar processo` para ADMIN, botões de `Aceitar/Homologar` para GESTOR).
    - Não testa as funcionalidades específicas do ADMIN para alterar dados do subprocesso a partir desta tela.
    - Não diferencia a visão e as permissões entre `ADMIN` e `GESTOR`.

---

## CDU-07: Detalhar subprocesso

- **Status:** Cobertura Média
- **Observações:**
  - O teste tem uma boa cobertura do fluxo de estados e da visibilidade condicional dos "cards de ação" para os perfis `CHEFE` e `GESTOR` em um processo de 'Mapeamento'.
  - **GAPs:**
    - Não realiza uma verificação detalhada das seções "Dados da Unidade" e "Movimentações".
    - Não inclui testes para o perfil `SERVIDOR`.
    - Não cobre os cards específicos que deveriam ser exibidos em um processo do tipo 'Diagnóstico'.

---

## CDU-08: Manter cadastro de atividades e conhecimentos

- **Status:** Cobertura Média
- **Observações:**
  - O teste cobre bem as funcionalidades de CRUD para atividades e conhecimentos.
  - **GAPs:**
    - Não testa a funcionalidade de "Importar atividades".
    - Não verifica a mudança de status do subprocesso para 'Cadastro em andamento' após a primeira alteração.
    - Não verifica a exibição do botão "Impacto no mapa" em processos de 'Revisão'.

---

## CDU-09: Disponibilizar cadastro de atividades e conhecimentos

- **Status:** Cobertura Média
- **Observações:**
  - O teste valida corretamente a regra de negócio que impede a disponibilização de atividades sem conhecimentos e testa a exibição do histórico de análise.
  - **GAPs:**
    - Não verifica o modal de confirmação.
    - Não verifica os efeitos da ação no backend (mudança de status, criação de `Movimentacao`, alerta, e-mail).
    - Não verifica se o timestamp de conclusão da etapa é gravado.
    - Não verifica se o histórico de análise é limpo após a submissão.

---

## CDU-10: Disponibilizar revisão do cadastro de atividades e conhecimentos

- **Status:** Cobertura Média
- **Observações:**
  - Assim como o CDU-09, o teste valida a regra de negócio principal e a exibição do histórico.
  - **GAPs:**
    - Não verifica o modal de confirmação.
    - Não verifica os efeitos da ação no backend (mudança de status para 'Revisão do cadastro disponibilizada', criação de `Movimentacao`, alerta, e-mail).
    - Não verifica se o timestamp da etapa é gravado e se o histórico de análise é limpo.

---

## CDU-11: Visualizar cadastro de atividades e conhecimentos

- **Status:** Cobertura Média
- **Observações:**
  - O teste cobre bem a visualização em modo somente leitura para os perfis `ADMIN` e `GESTOR`.
  - **GAPs:**
    - Faltam testes para os perfis `CHEFE` e `SERVIDOR`.

---

## CDU-12: Verificar impactos no mapa de competências

- **Status:** Cobertura Baixa
- **Observações:**
  - O teste verifica o cenário de "nenhum impacto" e confirma que o modal de impactos aparece quando há alterações.
  - **GAPs:**
    - Falha crítica em não verificar o **conteúdo** do modal de impactos.
    - Não testa os cenários de alteração ou remoção de atividades.
    - Não testa a funcionalidade para os perfis `GESTOR` e `ADMIN`.

---

## CDU-13: Analisar cadastro de atividades e conhecimentos

- **Status:** Cobertura Média
- **Observações:**
  - O teste cobre bem os fluxos de UI para as ações de `GESTOR` (aceitar, devolver) e `ADMIN` (homologar, devolver).
  - **GAPs:**
    - Falha crítica em não verificar nenhum dos efeitos da ação no backend (criação de `Analise`, `Movimentacao`, mudança de status, alerta, e-mail).

---

## CDU-14: Analisar revisão de cadastro de atividades e conhecimentos

- **Status:** Cobertura Média
- **Observações:**
  - O teste cobre os fluxos de UI para `GESTOR` e `ADMIN` de forma similar ao CDU-13.
  - **GAPs:**
    - Falha crítica em não verificar nenhum dos efeitos da ação no backend.
    - Não testa a lógica condicional da homologação pelo `ADMIN` (com vs. sem impactos no mapa).

---

## CDU-15: Manter mapa de competências

- **Status:** Cobertura Alta
- **Observações:**
  - Teste excelente e robusto, cobrindo todo o fluxo de CRUD de competências.
  - **GAPs:**
    - Não verifica a mudança de status do subprocesso para 'Mapa criado' após a criação da primeira competência.
    - Não verifica os detalhes da UI (badge de conhecimentos e tooltip).

---

## CDU-16: Ajustar mapa de competências

- **Status:** Cobertura Alta
- **Observações:**
  - O teste cobre bem o fluxo de CRUD de competências no contexto de um ajuste de mapa e a submissão final.
  - **GAPs:**
    - Não verifica a funcionalidade "Impactos no mapa", que é um guia essencial para o usuário nesta tela.

---

## CDU-17: Disponibilizar mapa de competências

- **Status:** Cobertura Média
- **Observações:**
  - O teste valida bem o fluxo de UI do modal de disponibilização (validação do formulário, cancelar, confirmar).
  - **GAPs:**
    - Falha crítica em não testar as regras de validação do backend (toda atividade/competência deve ter pelo menos uma associação).
    - Não verifica nenhum dos efeitos da ação no backend (mudança de status, `Movimentacao`, `Alerta`, e-mail).

---

## CDU-18: Visualizar mapa de competências

- **Status:** Cobertura Alta
- **Observações:**
  - O teste tem uma excelente cobertura, com uma preparação de dados complexa e verificações para múltiplos perfis (`ADMIN`, `CHEFE`, `SERVIDOR`).
  - **GAPs:**
    - Apenas uma pequena lacuna: falta um teste específico para o perfil `GESTOR`.

---

## CDU-19: Validar mapa de competências

- **Status:** Cobertura Baixa
- **Observações:**
  - O teste cobre o "caminho feliz" para as duas ações principais (`Apresentar sugestões` e `Validar`).
  - **GAPs:**
    - Não verifica o modal de confirmação da ação de validar.
    - Falha crítica em não verificar nenhum dos efeitos da ação no backend (mudança de status, `Movimentacao`, `Alerta`, e-mail) para nenhum dos dois fluxos.
    - Não testa a funcionalidade do botão "Histórico de análise".

---

## CDU-20: Analisar validação de mapa de competências

- **Status:** Cobertura Média
- **Observações:**
  - O teste cobre os fluxos de UI para as ações de `GESTOR` (devolver, aceitar) e `ADMIN` (homologar).
  - **GAPs:**
    - Falha crítica em não verificar nenhum dos efeitos da ação no backend (criação de `Analise`, `Movimentacao`, mudança de status, alerta, e-mail).
    - Não testa a funcionalidade dos botões "Ver sugestões" e "Histórico de análise".

---

## CDU-21: Finalizar processo de mapeamento ou de revisão

- **Status:** Cobertura Baixa
- **Observações:**
  - O teste valida bem as permissões (`ADMIN` vs. `GESTOR`) e o fluxo de UI do modal de finalização.
  - **GAPs:**
    - Falha crítica em não testar a regra de validação do backend (todos os subprocessos devem estar com 'Mapa homologado').
    - Não verifica os principais efeitos da ação (tornar os mapas vigentes e enviar e-mails).

---
