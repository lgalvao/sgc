# Guia de Refatoração

Este guia documenta as convenções e passos recomendados para refatorações no projeto SGC.

Objetivo
- Padronizar mapeamentos entre o modelo relacional (snake_case) e a representação do frontend (camelCase).
- Garantir contratos de dados via validação de mocks.
- Minimizar mudanças disruptivas no backend usando mappers/adapters.

Resumo das decisões de mapeamento
- Converter chaves snake_case → camelCase no frontend.
- Campos de identificação:
  - Abordagem híbrida: preservar `id: number` quando presente e também manter `titulo`/`usuarioTitulo` quando disponível.
  - Frontend deve preferir `id` numérico para operações internas; manter `titulo` como referência funcional.
- Datas: normalizar para ISO e converter para Date objetos no frontend usando `parseDate`.
- Situações/enums: usar códigos do BD como fonte de verdade e helpers para labels (ver [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1)).

Localização dos mappers
- [`src/mappers/servidores.ts`](src/mappers/servidores.ts:1) — mapeia `VW_USUARIO` → `Servidor`.
- [`src/mappers/unidades.ts`](src/mappers/unidades.ts:1) — mapeia `UNIDADE`, `UNIDADE_PROCESSO` → `Unidade` / `UnidadeSnapshot`.
- [`src/mappers/entidades.ts`](src/mappers/entidades.ts:1) — mapas, atividades, competências, conhecimentos, movimentações, análises, alertas.

Exemplos rápidos
- snake_case JSON: `{ "codigo": 1, "nome": "X", "data_hora": "01/01/2020" }`
- Depois do mapper: `{ id: 1, nome: "X", dataHora: "2020-01-01T00:00:00.000Z" }` (convertido para Date no frontend)

Validação e transformação de mocks
- Validar: execute `node scripts/validar-mocks.js` (usa schemas Zod em [`src/validators/mocks.ts`](src/validators/mocks.ts:1)).
- Transformar (dry-run): `node scripts/transformar-mocks-para-formato-frontend.js --dry`
- Aplicar transformações: `node scripts/transformar-mocks-para-formato-frontend.js --apply`
- Os backups são gerados em `src/mocks/backups/<timestamp>/` antes de sobrescrever.

Checklist de qualidade antes de merge
1. Rodar lint: `npm run lint`
2. Rodar typecheck: `npm run typecheck`
3. Rodar testes unitários: `npm run test:unit`
4. Rodar testes E2E: `npx playwright test` (quando aplicável)
5. Validar mocks: `node scripts/validar-mocks.js`

Como adicionar um novo mapper
- Criar função em `src/mappers/*` seguindo padrões existentes.
- Tratar chaves alternativas (codigo/id; data_hora/dataHora).
- Usar `parseDate` de `src/utils` para normalizar datas.
- Adicionar testes unitários em `src/mappers/__tests__/`.
- Atualizar stores para consumir o mapper em vez de manipular raw mocks.

Política de identificadores (detalhes)
- Preferir trabalhar com `id: number` internamente.
- Se o backend expõe `usuario_titulo` apenas como string chave, preserve-o no objeto mapeado (campo `usuarioTitulo`).
- Evitar remover campos originais nos mappers — preserve informações úteis para diagnóstico.

Notas sobre enums e situações
- Centralizar códigos em [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1).
- Mapear strings vindas do backend para enums TypeScript quando necessário (usar mappers para isso).

Passos operacionais para este repositório
- Quando alterar mappers: atualizar e executar os testes unitários que cobrem os converters.
- Se aplicar transformações nos mocks, revisar diffs do backup vs novo mock.

Histórico e contato
- Mudanças principais: mappers implementados, validação Zod adicionada, transformador de mocks criado.
- Para dúvidas, contate o autor do PR ou mantenedor do repositório.