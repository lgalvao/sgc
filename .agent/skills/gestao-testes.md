# Skill: Gestão de Testes

## Descrição
Auxilia na identificação de lacunas de testes unitários e na priorização de quais classes devem ser testadas primeiro com base em risco e categoria.

## Comandos Relacionados
- `python3 backend/etc/scripts/analyze_tests.py`: Mapeia quais classes possuem arquivos de teste correspondentes.
- `python3 backend/etc/scripts/prioritize_tests.py`: Gera um plano de ação prioritário (P1, P2, P3).
- `node frontend/etc/scripts/listar-test-ids.cjs`: Lista todos os test-ids usados no frontend.
- `node frontend/etc/scripts/listar-test-ids-duplicados.cjs`: Identifica IDs de teste duplicados que podem quebrar testes E2E.

## Fluxo de Uso
1. Execute o `analyze_tests.py` para gerar o relatório base.
2. Use o `prioritize_tests.py` para criar o plano de trabalho.
3. Siga o arquivo `prioritized-tests.md` para implementar novos testes.
