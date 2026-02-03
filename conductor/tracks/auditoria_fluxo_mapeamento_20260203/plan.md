# Plano de Implementação: Auditoria e Refatoração do Fluxo de Mapeamento

## Fase 1: Auditoria e Alinhamento do Backend [checkpoint: 01fba03]
- [x] Task: Analisar classes de domínio e serviços relacionados às situações de mapeamento. [b905dd4]
- [x] Task: Mapear divergências entre o código atual e a máquina de estados do `_intro.md`. [9e1a6c3]
- [x] Task: Refatorar as transições de situação no backend. [e06a100]
    - [x] Escrever testes unitários (JUnit 6) cobrindo todas as transições válidas e inválidas.
    - [x] Implementar as correções na lógica de transição.
- [x] Task: Conductor - User Manual Verification 'Auditoria e Alinhamento do Backend' (Protocol in workflow.md)

## Fase 2: Sincronização e Validação do Frontend [checkpoint: f49f14f]
- [x] Task: Revisar os mappers e tipos TypeScript para refletir as situações corretas. [a30e352]
- [x] Task: Validar se as ações de interface (botões de envio, devolução, validação) respeitam o estado atual. [03fee28]
- [x] Task: Conductor - User Manual Verification 'Sincronização e Validação do Frontend' (Protocol in workflow.md)

## Fase 3: Homologação e Cobertura
- [x] Task: Executar o Quality Gate completo (`quality-check.sh`) para garantir 99% de cobertura. [53a2e69]
- [ ] Task: Verificar integração entre backend e frontend nos fluxos críticos.
- [ ] Task: Conductor - User Manual Verification 'Homologação e Cobertura' (Protocol in workflow.md)
