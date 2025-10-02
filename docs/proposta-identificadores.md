# Proposta Técnica — Identificadores e Enums

Objetivo: apresentar recomendação e passos de migração para alinhar o protótipo com o modelo relacional descrito em [`modelo-dados.md`](modelo-dados.md:1).

Contexto
- O modelo relacional usa `usuario_titulo` em `VW_USUARIO` como identificador funcional (string) — ver [`modelo-dados.md`](modelo-dados.md:1).
- O protótipo/front-end atualmente usa `id` numérico em muitos lugares (ex.: [`src/types/tipos.ts`](src/types/tipos.ts:1), `src/mocks/servidores.json`).
- Há discrepâncias entre códigos de enum no BD (ex.: `MAPEAMENTO`) e rótulos legíveis exibidos no frontend (`Mapeamento`) — constantes em [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1).

Requisitos de decisão
- Minimizar alterações disruptivas no frontend.
- Manter compatibilidade com o modelo relacional e integrações (SGRH).
- Permitir mapeamento claro e testável entre DB ↔ frontend.

Recomendação (resumo)
- Adotar abordagem híbrida: manter `id: number` no frontend quando estiver presente nos dados, mas também preservar `titulo` (string) vindo do BD. Ou seja, o tipo frontend de usuário deve incluir ambos:
  - `id?: number`
  - `titulo?: string`
- Não migrar o BD. Em vez disso, implementar mappers/transformers que convertam `VW_USUARIO.titulo` ↔ `Servidor.titulo` e preencham `id` quando possível (por meio de lookup ou provisionamento).
- Para enums (processo/situação): manter constantes com os códigos DB em [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1) e expor helpers que retornem labels legíveis para a UI.

Motivos
- Evita mudanças disruptivas no backend e na equipe de dados.
- Permite que mocks e integrações atuais (que já usam `id` numérico) continuem funcionando.
- Proporciona um caminho de conversão reproduzível e testado (mappers + validação Zod).

Alterações recomendadas no código (passo a passo)
1) Ajustar tipos:
   - Atualizar [`src/types/tipos.ts`](src/types/tipos.ts:1) para que `Servidor` inclua `titulo?: string` sem remover `id: number`.
2) Melhorar mappers:
   - Atualizar [`src/mappers/servidores.ts`](src/mappers/servidores.ts:1) para preencher `titulo` a partir de `VW_USUARIO.titulo` e preencher `id` somente quando disponível.
3) Normalização de enums:
   - Criar utilitário `mapEnumCodeToLabel` (p.ex. em `src/utils/enums.ts`) e definir enums de código (ex.: 'MAPEAMENTO') como fonte da verdade.
   - Atualizar [`src/types/tipos.ts`](src/types/tipos.ts:1) para usar enums de código (valores constantes) e fornecer helpers de exibição.
4) Atualizar mappers para MAPA/ATIVIDADE/.. seguindo o mesmo padrão (snake_case → camelCase, preencher campos alternativos).
5) Reexecutar e endurecer validação de mocks (`scripts/validar-mocks.js` e `src/validators/mocks.ts`).
6) Refatorar stores para consumir mappers (já iniciado para servidores/unidades).
7) Atualizar documentação (`guias/guia-refatoracao.md`) com convenções: `id` vs `codigo`/`titulo`, formato de datas, lista de situações.

Plano de migração incremental (cronograma sugerido)
- Fase 1 (rápido): Ajustar tipos e mappers de `Servidor` e `Unidade`; executar testes unitários e validar mocks.
- Fase 2: Implementar mappers para `MAPA` e `ATIVIDADE` e refatorar stores correspondentes; rodar testes unitários.
- Fase 3: Implementar validação de contrato em CI (`scripts/validar-mocks.js`); aplicar transformador `--apply` opcionalmente.
- Fase 4: Refatorar lojas restantes e executar suíte E2E.

Testes e critérios de aceitação
- Todos os testes unitários devem passar (`npm run test:unit`).
- `scripts/validar-mocks.js` deve passar em CI sem erros.
- Nenhuma regressão visível na UI (E2E verdes ou falhas documentadas).

Notas operacionais
- Para integração com SGRH, evite assumir que `titulo` será numérico; trate `titulo` como string primária no BD.
- Se a equipe decidir unificar para `id` numérico no BD, será necessária migração de dados e atualização de integrações — isso é mais custoso.

Recomendações finais
- Implementar a abordagem híbrida (manter `id` numérico quando houver, guardar `titulo` sempre).
- Centralizar mapeamentos e helpers em `src/mappers/` e `src/utils/` para manter ponto único de transformação.
- Integrar `scripts/validar-mocks.js` ao pipeline de CI.

Referências
- Modelo relacional: [`modelo-dados.md`](modelo-dados.md:1)
- Tipos do frontend: [`src/types/tipos.ts`](src/types/tipos.ts:1)
- Constantes de situações: [`src/constants/situacoes.ts`](src/constants/situacoes.ts:1)