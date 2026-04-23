# Issue #1553 — Investigação e plano de correção

## Resumo da investigação

Durante a investigação, foi identificado que os fluxos de disponibilização de cadastro e disponibilização de revisão no frontend não transportam observações para a API.

Evidências levantadas:

- O tipo `DisponibilizarCadastroRequest` existe no frontend com campo `observacoes`, indicando intenção de contrato para esse dado.
- As funções `disponibilizarCadastro` e `disponibilizarRevisaoCadastro` no service chamam `POST` sem payload.
- Foram adicionados testes de caracterização para confirmar o comportamento atual (problema), validando que a chamada HTTP é feita apenas com a URL, sem corpo.

## Escopo provável do defeito

- Frontend (service/composable/view): falta de coleta e envio da observação de disponibilização.
- Backend (controller/service): endpoint de disponibilização de cadastro/revisão atualmente sem `@RequestBody`, sugerindo que a observação ainda não está sendo recebida e persistida nesse fluxo.

## Plano de correção

1. **Definir contrato explícito de request**
   - Padronizar request de disponibilização com `observacoes` (permitindo vazio).
   - Alinhar controller e frontend para o mesmo contrato.

2. **Ajustar backend para receber observações**
   - Atualizar endpoints de disponibilização para receber DTO de request.
   - Encaminhar observações para `SubprocessoTransicaoService` com normalização já existente.

3. **Ajustar frontend para coletar e enviar observações**
   - Incluir campo de observação no modal de confirmação de disponibilização.
   - Encaminhar observações no fluxo de `CadastroView` → composable → service.

4. **Expandir cobertura de testes**
   - Atualizar testes de service (trocar caracterização do problema por comportamento esperado corrigido).
   - Incluir testes de componente/view garantindo envio do payload e tratamento de campo vazio.
   - Incluir testes de controller/service no backend para confirmar recebimento e persistência da observação.

5. **Validação funcional**
   - Testar manualmente fluxo de disponibilização em cadastro normal e revisão.
   - Confirmar presença da observação no histórico/analise correspondente.

## Execução do plano (status atual)

- ✅ Contrato de request alinhado entre frontend e backend para `observacoes`.
- ✅ Backend atualizado para aceitar `observacoes` nos endpoints de disponibilização de cadastro e revisão.
- ✅ Frontend atualizado para coletar observações no modal e enviar no fluxo de confirmação.
- ✅ Cobertura de testes expandida (services/composables/componentes/views no frontend + controller/service no backend).
- ⏳ Validação funcional manual ponta a ponta pendente de homologação com ambiente integrado.

## Riscos e mitigação

- **Risco de quebra de compatibilidade** em chamadas existentes sem body.
  - Mitigação: aceitar payload opcional no backend na fase de transição.
- **Risco de divergência entre textos/labels de UI e comportamento**.
  - Mitigação: validar cenários de UI com testes de componente e smoke test manual.
