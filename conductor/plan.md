# Plano de Ação: Padronização de Botões de Confirmação (Issue #1383)

## Objetivo
Atender aos requisitos da Issue #1383 padronizando os botões de confirmação dentro dos modais de todo o sistema. A padronização tem duas frentes:
1. **Nome do Botão:** O nome do botão de confirmação dentro do modal deve usar o verbo exato da ação que o usuário acionou (ex: "Disponibilizar", "Iniciar", "Homologar").
2. **Cores do Botão:** As cores dos botões de confirmação também seguirão um padrão de estilo (`variant` / `ok-variant`):
   - **Verde (🟢 / `success`):** Botões de cadastro, confirmação, envio, aceite, etc.
   - **Azul (🔵 / `primary` ou `info`):** Botões de edição ou salvamento em rascunho (salvar alterações).
   - **Vermelho (🔴 / `danger`):** Botões de exclusão, devolução, remoção, cancelamento e encerramento/finalização.

## Escopo & Arquivos a Serem Alterados

1. **`frontend/src/views/ProcessoCadastroView.vue`**
   - Modal de Iniciar Processo: Alterar título para `"Iniciar"` e garantir variante `success` (Verde).
   - Modal de Remoção de Processo: Alterar título para `"Excluir"` e variante para `danger` (Vermelho).

2. **`frontend/src/views/CadastroVisualizacaoView.vue`**
   - Modal de Validação/Aceite/Homologação: Alterar título dinamicamente para `"Validar"`, `"Aceitar"` ou `"Homologar"` e variante para `success`.
   - Modal de Devolução: Alterar título para `"Devolver"` e variante para `danger`.

3. **`frontend/src/views/SubprocessoView.vue`**
   - Modal de Reabrir: Título de confirmação para `"Reabrir"` e usar `success`.
   - Modal de Lembrete: Ajustar variante para `success` (texto já é "Enviar").

4. **`frontend/src/views/AdministradoresView.vue`**
   - Modal Criar Administrador: Variante para `success`.
   - Modal Remover Administrador: Variante para `danger`.

5. **`frontend/src/views/MapaVisualizacaoView.vue`**
   - Modal Apresentar Sugestões: Alterar título de "Confirmar" para `"Apresentar"` e usar variante `success`.
   - Modal de Validação e Devolução: Ajustar variantes (`success` para Validar, `danger` para Devolver).

6. **`frontend/src/views/ProcessoDetalheView.vue`**
   - Modal de Finalização: Modificar o `ok-title` para `"Finalizar"` e ajustar variante para `danger` (ação terminal).

7. **`frontend/src/components/mapa/ConfirmacaoDisponibilizacaoModal.vue`**
   - Alterar título de confirmação para `"Disponibilizar"` e variante `success`.

8. **`frontend/src/components/mapa/AceitarMapaModal.vue`**
   - O título já é dinâmico ("Aceitar" / "Homologar"). Garantir a variante `success`.

9. **`frontend/src/components/processo/ModalAcaoBloco.vue`**
   - Ajustar para garantir que o botão da modal repita o verbo da ação de bloco e seja `success` (Aceitar / Homologar / etc).

10. **`frontend/src/views/CadastroView.vue`**
    - Modal de exclusão/remoção: Ajustar `ok-title` para `"Excluir"` ou `"Remover"` e variante para `danger`.

## Implementação
- Substituir o prop genérico `:ok-title="TEXTOS.comum.BOTAO_CONFIRMAR"` em `ModalConfirmacao` e `<BModal>` pelo verbo correspondente (adicionando constantes em `textos.ts` ou literais de template onde fizer mais sentido).
- Passar explicitamente `variant="success"`, `variant="danger"` ou `variant="primary"` aos componentes `<ModalConfirmacao>`.
- Garantir que a padronização reflete os exemplos fornecidos na issue.

## Verificação e Testes (Garantia de Qualidade)
Para garantir que as mudanças de estilo e texto não quebrem testes e2e e unitários, adotaremos os seguintes passos:

1. **Testes Unitários (Frontend)**:
   - Rodar a suíte de testes unitários (`npm run test:unit`) imediatamente após as alterações nos componentes Vue.
   - Ajustar quaisquer testes (em `frontend/src/views/__tests__/` e `frontend/src/components/__tests__/`) que façam queries buscando especificamente pelo botão com texto "Confirmar".
   - Ajustar snapshots ou testes que verifiquem propriedades específicas como `ok-title` ou `variant`.

2. **Testes E2E (Playwright)**:
   - Em arquivos do diretório `e2e/`, realizar uma busca global por `getByRole('button', { name: /Confirmar/i })` ou seletores similares que possam ser afetados pelas mudanças no modal.
   - Atualizar os testes E2E correspondentes nos casos em que a ação não será mais "Confirmar" e sim "Iniciar", "Homologar", "Disponibilizar", etc.
   - Rodar a suíte E2E completa na raiz (ou pasta e2e) para assegurar que os fluxos de ponta a ponta que englobam interações com os modais de confirmação permaneçam operacionais.
