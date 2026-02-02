# Skill: Auditoria de Qualidade Frontend

## Descrição
Avalia a qualidade do código frontend, incluindo cobertura de testes unitários, acessibilidade e validações consistentes com o backend.

## Comandos Relacionados
- `node frontend/etc/scripts/verificar-cobertura.cjs`: Relatório de cobertura Vitest.
- `node frontend/etc/scripts/verificar-acessibilidade.js`: Auditoria Lighthouse de acessibilidade.
- `node frontend/etc/scripts/audit-frontend-validations.cjs`: Compara validações frontend vs backend.
- `node frontend/etc/scripts/audit-view-validations.cjs`: Identifica validações redundantes em campos de Views.

## Fluxo de Uso
1. Gere a cobertura: `npm run coverage:unit` (no diretório `frontend`).
2. Execute a auditoria de acessibilidade (requer app rodando).
3. Verifique os relatórios gerados na raiz do projeto.
