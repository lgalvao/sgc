# Skill: Auditoria de Qualidade Backend

## Descrição
Esta skill permite realizar uma auditoria completa da qualidade do código backend, focando em cobertura de testes, complexidade ciclomática e verificações de segurança/nulos.

## Comandos Relacionados
- `node backend/etc/scripts/verificar-cobertura.cjs`: Relatório rápido de cobertura JaCoCo.
- `node backend/etc/scripts/analisar-complexidade.cjs`: Ranking de classes por complexidade.
- `node backend/etc/scripts/auditar-verificacoes-null.js`: Identifica null-checks redundantes ou ausentes.
- `node backend/etc/scripts/super-cobertura.cjs`: Análise profunda de lacunas de cobertura.

## Fluxo de Uso
1. Certifique-se de que os testes rodaram: `./gradlew :backend:test :backend:jacocoTestReport`.
2. Execute o script de interesse.
3. Analise o arquivo gerado (ex: `complexity-ranking.md`, `cobertura_lacunas.json`).
