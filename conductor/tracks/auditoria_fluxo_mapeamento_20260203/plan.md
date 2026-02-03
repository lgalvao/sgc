# Plano de Implementação: Auditoria e Refatoração do Fluxo de Mapeamento

## Fase 1: Auditoria e Alinhamento do Backend
- [x] Task: Analisar classes de domínio e serviços relacionados às situações de mapeamento. [b905dd4]
- [ ] Task: Mapear divergências entre o código atual e a máquina de estados do `_intro.md`.
- [ ] Task: Refatorar as transições de situação no backend.
    - [ ] Escrever testes unitários (JUnit 6) cobrindo todas as transições válidas e inválidas.
    - [ ] Implementar as correções na lógica de transição.
- [ ] Task: Conductor - User Manual Verification 'Auditoria e Alinhamento do Backend' (Protocol in workflow.md)

## Fase 2: Sincronização e Validação do Frontend
- [ ] Task: Revisar os mappers e tipos TypeScript para refletir as situações corretas.
- [ ] Task: Validar se as ações de interface (botões de envio, devolução, validação) respeitam o estado atual.
- [ ] Task: Conductor - User Manual Verification 'Sincronização e Validação do Frontend' (Protocol in workflow.md)

## Fase 3: Homologação e Cobertura
- [ ] Task: Executar o Quality Gate completo (`quality-check.sh`) para garantir 99% de cobertura.
- [ ] Task: Verificar integração entre backend e frontend nos fluxos críticos.
- [ ] Task: Conductor - User Manual Verification 'Homologação e Cobertura' (Protocol in workflow.md)
