# Relatório de Dívida Técnica - SGC

Este relatório documenta a análise de dívida técnica realizada no projeto SGC (Sistema de Gestão de Competências), cobrindo tanto o backend quanto o frontend.

## 1. Backend

### Estado Atual
A análise do código fonte do backend e da documentação existente revelou que as dívidas técnicas previamente identificadas foram resolvidas.

- **TD-001 (DTOs Request com Setters):** Resolvido. O uso de `@Setter` e `@NoArgsConstructor` foi removido dos DTOs de Request, adotando-se imutabilidade com Records ou Builders.
- **TD-002 (Separar DTOs Bidirecionais):** Resolvido. DTOs como `AtividadeDto` e `ConhecimentoDto` foram divididos em pares Request/Response específicos (e.g., `CriarAtividadeRequest`, `AtividadeResponse`).
- **Remoção de `@Data`:** O plano de refatoração para remover a anotação `@Data` do Lombok foi concluído. Uma varredura no código fonte não encontrou ocorrências residuais de `@Data`.

### Arquivos de Referência
- `backend/tech-debt.md`: Documenta a resolução de TD-001 e TD-002.
- `backend/plano-remover-data.md`: Documenta o plano executado para remoção do `@Data`.

## 2. Frontend

### Dívida Identificada: Definição de Tipo `MapaCompleto` Incompleta

**Descrição:**
A interface `MapaCompleto` no frontend não estava refletindo corretamente a estrutura de dados retornada pelo backend/mapper, especificamente em relação à estrutura aninhada de atividades dentro de competências. A interface `Competencia` utilizada por `MapaCompleto` não possuía o campo `atividades` (apenas `atividadesAssociadas`), obrigando o uso de `as any` em testes e potencialmente escondendo erros de tipo.

**Ação Tomada:**
- Criada a interface `CompetenciaCompleta` em `frontend/src/types/tipos.ts`, estendendo `Competencia` e adicionando o campo `atividades: Atividade[]`.
- Atualizada a interface `MapaCompleto` para utilizar `CompetenciaCompleta[]`.
- Atualizado o mapper `mapMapaCompletoDtoToModel` em `frontend/src/mappers/mapas.ts` para garantir a tipagem correta.
- Corrigidos os testes em `frontend/src/mappers/__tests__/mappers.spec.ts` para remover conversões de tipo inseguras (`as any`) e comentários `TODO`.
- Corrigidos os testes em `frontend/src/stores/__tests__/mapas.spec.ts` para refletir a nova estrutura obrigatória de `CompetenciaCompleta` nos mocks.

### Verificação
- `npm run test:unit`: Todos os testes passaram.
- `npm run typecheck`: Verificação estática de tipos concluída sem erros.

---
*Relatório gerado em: 2025-01-20*
