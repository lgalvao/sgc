# Plano de Correção de Testes E2E - Refatoração de Acesso

## 1. Diagnóstico de Falhas
Após a refatoração do controle de acesso para o modelo "Dono vs. Localização", 13 testes E2E falharam. A análise aprofundada revelou a causa real:

*   **Rigor no Fluxo de Trabalho:** O novo sistema de segurança (`SubprocessoSecurity.java`) é rigoroso quanto à **Localização Atual** do subprocesso. Ações de execução (homologar, aceitar, devolver) só podem ser realizadas se o subprocesso estiver na unidade ativa do usuário.
*   **Testes Incompletos:** Muitos testes E2E (como `cdu-05`) estavam pulando passos intermediários de hierarquia. Por exemplo, o CHEFE disponibilizava o cadastro para a unidade superior (GESTOR), mas o teste tentava fazer o ADMIN homologar imediatamente, sem o aceite prévio do GESTOR.
*   **Bypass Inexistente:** Diferente do sistema antigo, o perfil ADMIN **não possui bypass** de localização para ações de fluxo de trabalho, conforme exigido no `acesso.md`.

## 2. Estratégia de Correção: Alinhamento de Fluxo
A correção consiste em atualizar os testes E2E para que sigam o fluxo completo de "mesa em mesa", garantindo que a Localização Atual do subprocesso coincida com a unidade do usuário que executa a ação.

### Ações Realizadas:
- [x] **Backend:** Implementado `PermissoesSubprocessoDto` e integrado ao `SubprocessoDetalheResponse`.
- [x] **Backend:** Ajustado `SubprocessoSecurity` para ser a Única Fonte da Verdade.
- [x] **Frontend:** Simplificado `useAcesso.ts` para consumir flags do backend.
- [x] **Frontend:** Refatorados componentes de visualização para usar as novas flags.
- [x] **Testes:** Corrigido `e2e/cdu-05.spec.ts` adicionando passos de aceite do GESTOR intermediário. **Resultado: PASSOU.**
- [x] **Testes:** Validado `e2e/cdu-13.spec.ts` (que já seguia o fluxo completo). **Resultado: PASSOU.**

## 3. Próximos Passos (Pendentes)

### Ajustes Finos de Regras (Backend):
- [ ] Corrigir `SubprocessoSecurity.java` para remover o perfil `ADMIN` das ações `ACEITAR_CADASTRO`, `ACEITAR_REVISAO_CADASTRO` e `ACEITAR_MAPA`. Estas ações são exclusivas de `GESTOR` (transição para nível superior). ADMIN apenas homologa (topo da cadeia).
- [ ] Verificar se as ações em bloco no `SubprocessoSecurity` também precisam de refinamento.

### Verificação de Views (Frontend):
- [ ] Revisar `SubprocessoDetalheView.vue` para garantir que o card de ações (Alterar Data Limite, Reabrir) use as novas flags.
- [ ] Revisar `SubprocessoCards.vue` para garantir visibilidade correta dos ícones de edição.

### Validação:
- [ ] Executar testes unitários do backend: `./gradlew :backend:test`.
- [ ] Executar testes E2E críticos de forma serial: `npx playwright test e2e/cdu-05.spec.ts e2e/cdu-13.spec.ts`.
- [ ] Verificar se o erro do botão "Registrar aceite" aparecendo para o ADMIN foi resolvido.

## 4. Regras de Ouro Mantidas
*   **Visão:** Hierarquia de Unidade Responsável.
*   **Execução:** Localização Atual do Subprocesso (última movimentação).
*   **Independência:** O ADMIN só executa ações se o processo estiver fisicamente na unidade `ADMIN`.
