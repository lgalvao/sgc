# Plano de Refatoração de DTOs

**Data:** 2025-12-14
**Objetivo:** Padronizar e melhorar a consistência dos DTOs, eliminando construtores longos, padronizando a instanciação e identificando onde mappers devem ser adicionados ou simplificados.

---

## 1. Análise Geral

### 1.1. Números Gerais
- **Total de DTOs analisados:** 42 arquivos
- **Total de Mappers existentes:** 11 (Adicionados: MapaCompletoMapper, SgrhMapper, SubprocessoDetalheMapper, MapaAjusteMapper)
- **Módulos com DTOs:** alerta, analise, atividade, diagnostico, mapa, notificacao, processo, sgrh, subprocesso, unidade

### 1.2. Padrões Identificados

#### Padrões de Instanciação Encontrados:
1. **Lombok @Builder** (padrão mais usado) - 35+ ocorrências
2. **Records Java** (para DTOs imutáveis simples) - 10 ocorrências
3. **Mappers MapStruct** - 4 novos mappers criados para substituir lógica manual e factory methods complexos.

#### Anotações Lombok Utilizadas:
- **@Builder**: Padrão recomendado para DTOs mutáveis
- **@Data**: Usado em alguns DTOs (gera getters, setters, equals, hashCode, toString)
- **@Value**: Usado em alguns DTOs imutáveis (com @Builder)
- **@Getter + @Setter**: Usado em alguns DTOs
- **@AllArgsConstructor**: Presente em 91 casos (muitas vezes desnecessário quando há @Builder)
- **@NoArgsConstructor**: Comum para DTOs que precisam de serialização/desserialização

---

## 2. Progresso da Refatoração

### Fase 1: Correções Críticas (Sprint 1) - ✅ CONCLUÍDO
1. ✅ Refatorar `UnidadeService` para usar builders (Concluído)
2. ✅ Refatorar `MapaService` para usar builders (Concluído)
3. ✅ Simplificar `ProcessoDetalheMapperCustom` (Concluído - substituído por `ProcessoDetalheBuilder`)

### Fase 2: Padronização (Sprint 2) - ✅ CONCLUÍDO
4. ✅ Criar mappers faltando
   - `MapaCompletoMapper` criado e integrado em `MapaService`.
   - `SgrhMapper` criado e integrado em `UnidadeService`.
5. ✅ Converter factory methods complexos para mappers
   - `SubprocessoDetalheDto.of()` substituído por `SubprocessoDetalheMapper`.
   - `MapaAjusteDto.of()` substituído por `MapaAjusteMapper`.
6. ✅ Padronizar annotations Lombok em todos os DTOs
   - Verificado o uso consistente de `@Builder` nos DTOs chave (`UnidadeDto`, `CompetenciaMapaDto`, `MapaCompletoDto`, `SubprocessoDetalheDto`, `MapaAjusteDto`, etc.).

### Fase 3: Refinamento (Sprint 3)
7. Revisar e otimizar queries em mappers
8. Atualizar documentação (AGENTS.md, README.md)
9. Adicionar testes para novos mappers

---

## 3. Detalhes da Implementação

### 3.1. Novos Mappers Criados
- **MapaCompletoMapper**: Gerencia a conversão complexa de Mapa + Competências + Atividades para `MapaCompletoDto` e `CompetenciaMapaDto`.
- **SgrhMapper**: Centraliza a conversão de `Usuario` -> `ServidorDto` e `Unidade` -> `UnidadeDto`, removendo lógica manual de `UnidadeService`.
- **SubprocessoDetalheMapper**: Substitui o método estático complexo `SubprocessoDetalheDto.of()`, separando a lógica de mapeamento da estrutura de dados.
- **MapaAjusteMapper**: Substitui o método estático `MapaAjusteDto.of()`, facilitando a manutenção da lógica de ajuste de mapa.

### 3.2. Refatoração de Services
- **MapaService**: Agora usa `MapaCompletoMapper` para operações de leitura e escrita de mapa completo.
- **UnidadeService**: Utiliza `SgrhMapper` para construir DTOs de unidade e servidor, simplificando métodos de busca e hierarquia.
- **SubprocessoDtoService**: Utiliza `SubprocessoDetalheMapper` e `MapaAjusteMapper` para construir os DTOs de resposta, removendo a dependência de métodos estáticos nos DTOs.

### 3.3. Correção de Estrutura de Pacotes
- Os mappers `MovimentacaoMapper` e `SubprocessoMapper` foram movidos do pacote `sgc.subprocesso.dto` para `sgc.subprocesso.mapper` para seguir a convenção de arquitetura.
- Todas as referências e imports foram atualizados em classes de produção e testes.

---

## 4. Próximos Passos (Fase 3)

1. **Testes Unitários para Mappers**: Adicionar testes específicos para `MapaCompletoMapper`, `SgrhMapper`, `SubprocessoDetalheMapper` e `MapaAjusteMapper` para garantir cobertura de borda (ex: listas nulas, campos opcionais).
2. **Revisão de Desempenho**: Verificar se os novos mappers introduziram algum overhead, especialmente em listagens grandes (embora MapStruct seja performático).
3. **Documentação**: Atualizar `AGENTS.md` com as novas diretrizes sobre criação de Mappers e proibição de lógica complexa em factory methods de DTOs.

---

**Status Atual:** Fase 2 Concluída com sucesso. O código está mais limpo, padronizado e testável.
