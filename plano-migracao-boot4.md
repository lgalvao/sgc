# Plano de Migração para Spring Boot 4

**Data de Criação**: 2025-12-05
**Projeto**: SGC - Sistema de Gestão de Competências
**Versão Atual**: Spring Boot 3.5.8
**Versão Alvo**: Spring Boot 4.0.x

---

## 📋 Resumo Executivo

Este documento descreve os passos necessários para migrar o backend do projeto SGC de Spring Boot 3.5.8 para Spring Boot 4.0. Spring Boot 4 traz mudanças significativas baseadas no Spring Framework 7 e Jakarta EE 11.

### Estado Atual do Projeto

- **Spring Boot**: 3.5.8
- **Java**: 21 (configurado)
- **Gradle**: 9.2.1
- **Dependências Principais**:
    - Spring Data JPA
    - Spring Security
    - Spring Web
    - Hibernate (gerenciado pelo Spring Boot)
    - MapStruct 1.6.3
    - Lombok 1.18.42
    - JJWT 0.13.0
    - SpringDoc OpenAPI 2.8.13

### Análise de Código
- **Arquivos Java**: 309 arquivos
- **Usando javax**: 2 arquivos (`E2eController` e seu teste) - **NOTA**: Uso de `javax.sql.DataSource` é correto (Java SE), não requer migração.
- **Componentes Spring**: 64+ (@Entity, @Service, @Repository, @Controller)
- **Depreciações Identificadas**: Nenhuma encontrada via `-Xlint:deprecation` no build atual.

---

## 🎯 Principais Mudanças do Spring Boot 4

### 1. Requisitos de Plataforma

#### Java
- **Mínimo**: Java 17
- **Recomendado**: Java 21 ou Java 25
- **Atual no Projeto**: Java 21 (configurado) ✅
- **Ação**: Nenhuma, projeto já está no Java 21.

#### Jakarta EE
- **Versão Alvo**: Jakarta EE 11
- **Impacto**: O projeto já está alinhado com o namespace `jakarta.*` para persistência e web.
- **javax.sql**: O pacote `javax.sql` (DataSource) faz parte do Java SE (JDBC) e **não** mudou para Jakarta. Não requer alteração.

### 2. Spring Framework 7

Spring Boot 4 é construído sobre o Spring Framework 7, que traz:

- **Modularização completa**: JARs menores e mais focados
- **Null Safety com JSpecify**: Substituindo JSR-305
- **Suporte a API Versioning**: Versionamento nativo de REST APIs

### 3. Atualizações de Dependências Principais

| Biblioteca | Spring Boot 3.x | Spring Boot 4.0 | Impacto | Ação no SGC |
|---|---|---|---|---|
| Spring Data | 2024.x | 2025.x | Médio | Validar repositórios |
| Spring Security | 6.3.x | 6.4.x | Baixo | Validar `ConfigSeguranca` |
| Hibernate ORM | 6.x | 7.0 | **Alto** | Validar mapeamentos |
| Bean Validation | 3.0 | 3.1 | Baixo | - |
| Micrometer | 1.x | 2.x | Médio | - |
| Jackson | 2.x | 3.x | **Alto** | Migrar `ObjectMapper` |
| Testcontainers | 1.x | 2.0 | Médio | Validar testes E2E |

### 4. Remoções e Depreciações

#### Removido
- ✗ Anotações JSR-305 (`@Nullable`, `@Nonnull` do Spring) em favor de JSpecify.

#### Depreciado
- ⚠️ Jackson 2.x (migração para Jackson 3 recomendada)
- ⚠️ JUnit 4 (migração para JUnit 5 necessária) - **Projeto já usa JUnit 5** ✅

---

## 📝 Análise de Impacto no Projeto SGC

### Impacto Alto

1. **Hibernate 7.0**
    - **Risco**: Mudanças na API de critérias e consultas.
    - **Situação no SGC**: O projeto **não utiliza** `CriteriaBuilder`, `CriteriaQuery` ou `@Lob` nos arquivos fonte principais. Isso reduz significativamente o risco de quebras complexas.
    - **Ação**: Focar nos testes de integração básicos (CRUD) e associações.

2. **Jackson 3.0**
    - **Risco**: Mudanças na API do `ObjectMapper`.
    - **Situação no SGC**: Identificado uso direto de `ObjectMapper` em:
        - `sgc.comum.config.FiltroAutenticacaoSimulado`
        - `sgc.sgrh.UsuarioController`
    - **Ação**: Revisar estas classes para compatibilidade com o novo pacote/API do Jackson 3.

### Impacto Baixo

1. **Migrações javax → jakarta**
    - **Análise**: O único uso de `javax` remanescente é `javax.sql.DataSource`.
    - **Conclusão**: Correto conforme especificação Java SE. **Nenhuma migração necessária** para este item.

2. **Null Safety**
    - **Análise**: Encontrado uso de `@Nullable` e `@NonNull` (Spring) em:
        - `sgc.comum.erros.RestExceptionHandler`
        - `sgc.comum.config.FiltroAutenticacaoSimulado`
    - **Ação**: Substituir por anotações JSpecify.

---

## 🔧 Passos de Migração Detalhados

### Fase 1: Preparação (Antes da Migração)

#### 1.1. Verificar Dependências
- O projeto já utiliza JUnit 5, portanto a etapa de remoção do JUnit 4 pode ser pulada.
- O build com `-Xlint:deprecation` não retornou avisos, indicando código limpo de deprecações óbvias.

#### 1.2. Null Safety - Migração para JSpecify
Substituir anotações do pacote `org.springframework.lang` por `org.jspecify.annotations`.

**Arquivos Alvo**:
- `backend/src/main/java/sgc/comum/erros/RestExceptionHandler.java`
- `backend/src/main/java/sgc/comum/config/FiltroAutenticacaoSimulado.java`

**Exemplo**:
```java
// Antes
import org.springframework.lang.Nullable;
// Depois
import org.jspecify.annotations.Nullable;
```

### Fase 2: Atualização de Versões

#### 2.1. Atualizar Spring Boot para 4.0
Atualizar `backend/build.gradle.kts`:
```kotlin
plugins {
    id("org.springframework.boot") version "4.0.0"
}
```

#### 2.2. Atualizar Dependências no `backend/build.gradle.kts`
- MapStruct: Verificar versão compatível com Spring Boot 4.
- Lombok: Verificar versão compatível.

### Fase 3: Migração de Código

#### 3.1. Migração Jackson 2 → Jackson 3
O Spring Boot 4 migrará para Jackson 3.
**Arquivos para Refatoração**:
1. `backend/src/main/java/sgc/comum/config/FiltroAutenticacaoSimulado.java`:
   - Verificar importação de `com.fasterxml.jackson.databind.ObjectMapper`.
2. `backend/src/main/java/sgc/sgrh/UsuarioController.java`:
   - Instanciação manual de `ObjectMapper`.

#### 3.2. Configurações (application.yml)
Verificar propriedades que podem ter mudado. O arquivo `application.yml` atual é padrão e não usa configurações exóticas, mas vale verificar:
- `spring.jpa.hibernate.ddl-auto`: `create-drop` (padrão mantido?)
- `spring.jpa.defer-datasource-initialization`: `true` (ainda suportado?)

### Fase 4: Testes e Validação

#### 4.1. Executar Testes Unitários
```bash
cd /app && ./gradlew :backend:test
```
Como o projeto já está no JUnit 5 e não usa Criteria API, espera-se alta taxa de sucesso inicial.

#### 4.2. Executar Testes E2E
Validar fluxos críticos, especialmente autenticação (que usa o filtro simulado e Jackson).

---

## 📊 Estimativa Revisada

| Fase | Esforço Estimado | Risco |
|------|------------------|-------|
| Preparação (Null Safety) | 1 hora | Baixo |
| Atualização de Versões | 1 hora | Baixo |
| Migração Jackson | 2-4 horas | Médio |
| Validação Hibernate | 2-4 horas | Baixo (sem Criteria API) |
| Testes Gerais | 4 horas | Médio |
| **TOTAL** | **10-14 horas** | **Baixo-Médio** |

**Justificativa**: A análise revelou que o projeto está bem preparado (Java 21, JUnit 5, sem uso de APIs complexas do Hibernate). O maior esforço será na validação do Jackson 3 e testes de regressão.
