# Especificação: Auditoria e Refatoração do Fluxo de Situações do Mapeamento

## Objetivo
Garantir que todas as transições de 'Situação' (Status) dos subprocessos de mapeamento no SGC sigam rigorosamente as regras definidas na documentação oficial (`etc/reqs/_intro.md`).

## Escopo
- **Backend:** Revisar os enums de situação e os serviços que realizam as transições (ex: `MapeamentoService`).
- **Frontend:** Verificar se os componentes de UI exibem as situações corretamente e se as ações disponíveis respeitam o estado atual.
- **Testes:** Implementar testes de cobertura total (99% de linhas) para as máquinas de estado.

## Requisitos de Negócio (Baseados no _intro.md)
- Transições válidas de 'Não iniciado' até 'Mapa homologado'.
- Validação hierárquica (UDP -> Unidade Intermediária -> SEDOC).
- Tratamento de devoluções e sugestões no mapa de competências.
