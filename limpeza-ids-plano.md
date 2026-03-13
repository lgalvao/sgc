# Plano de Limpeza de Identificadores (ID -> Código)

Este plano descreve a estratégia para migrar o uso de `id` para `codigo` em todo o ecossistema do SGC, alinhando a aplicação ao Modelo de Dados (DDL) já existente.

## 1. Premissas e Restrições
- **Banco de Dados Imutável:** As colunas do banco já são `codigo`. Não haverá alterações de DDL.
- **Interoperabilidade:** O sistema deve continuar funcionando durante a migração (estratégia de ponte).
- **Sem Quebra de Contrato:** O JSON da API deve manter suporte a `id` enquanto o Frontend não for atualizado.

## 2. Estratégia de Migração (Fases)

### Fase 1: Backend Core (Entidades e Repositories)
- Renomear campos `id` para `codigo` nas entidades JPA que ainda não utilizam (ex: AlertaUsuario).
- **Sem Ponte:** Não utilizar `@JsonProperty("id")`. O JSON deve refletir a limpeza imediatamente.
- **Jackson 3:** Utilizar pacotes `tools.jackson.annotation.*` caso anotações sejam necessárias.
- **Repositories:** Renomear métodos gerados pelo Spring Data (ex: `findById`) para nomes semânticos (ex: `buscarPorCodigo`) sempre que houver `@Query` customizada.

### Fase 2: Backend Services e Facades
- Renomear métodos de serviço de `Id` para `Codigo` (ex: `buscarPorCodigo` -> `buscarPorCodigo`).
- Renomear variáveis locais (ex: `unidadeIds` -> `unidadeCodigos`).
- Meta: Código Java limpo de referências a `id` de negócio.

### Fase 3: Frontend e Testes E2E
- Embora os DTOs no TS já usem `codigo`, validar se componentes `.vue` ou chamadas `axios` ainda tentam acessar `.id`.
- Atualizar Fixtures e Helpers de teste E2E que ainda utilizam o termo legado.

### Fase 4: Validação e Limpeza Final
- Rodar o script `find_id_legacy.py` para garantir que apenas `traceId` e termos técnicos externos permaneçam.


## 3. Critérios de Aceite
- [ ] Relatório `id-legacy-report.txt` não contém ocorrências de negócio/entidade.
- [ ] Todos os testes unitários e de integração (Backend) passando.
- [ ] Todos os testes E2E (Playwright) passando.
- [ ] Build de produção sem erros de tipo (TS).

## 4. Ordem de Migração Sugerida
1. Comum / Infra (EntidadeBase, ErroApi)
2. Unidade e Processo (Base de tudo)
3. Subprocesso e Mapa
4. Atividades, Conhecimentos e Competências
5. Alertas e Notificações
6. Analise e Movimentação
