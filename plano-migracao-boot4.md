# Plano de Migração para Spring Boot 4

**Data de Criação**: 2025-12-05  
**Projeto**: SGC - Sistema de Gestão de Competências  
**Versão Atual**: Spring Boot 3.5.8  
**Versão Alvo**: Spring Boot 4.0.x

---

## 📋 Resumo Executivo

Este documento descreve os passos necessários para migrar o backend do projeto SGC de Spring Boot 3.5.8 para Spring Boot 4.0. Spring Boot 4 foi lançado oficialmente em 20 de novembro de 2025 e traz mudanças significativas baseadas no Spring Framework 7 e Jakarta EE 11.

### Estado Atual do Projeto

- **Spring Boot**: 3.5.8
- **Java**: 21 (configurado), 17 (runtime disponível)
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

### Arquivos Java
- **Total**: 309 arquivos Java
- **Usando Jakarta**: 51 arquivos (16.5%)
- **Usando javax**: 2 arquivos (0.6%)
- **Componentes Spring**: 64+ (@Entity, @Service, @Repository, @Controller)

---

## 🎯 Principais Mudanças do Spring Boot 4

### 1. Requisitos de Plataforma

#### Java
- **Mínimo**: Java 17
- **Recomendado**: Java 21 ou Java 25
- **Atual no Projeto**: Java 21 (configurado) ✅
- **Ação**: Atualizar runtime para Java 21+ se ainda estiver usando Java 17

#### Jakarta EE
- **Versão Alvo**: Jakarta EE 11
- **Impacto**: Algumas APIs Jakarta foram atualizadas
- **Servlet**: Requer Servlet 6.1+

#### Servidores de Aplicação Suportados
- **Tomcat**: 11.x (atualizado de 10.x)
- **Jetty**: 12.1+ (atualizado de 12.0)
- **Undertow**: ⚠️ **REMOVIDO** (não suporta Servlet 6.1)

### 2. Spring Framework 7

Spring Boot 4 é construído sobre o Spring Framework 7, que traz:

- **Modularização completa**: JARs menores e mais focados
- **Null Safety com JSpecify**: Substituindo JSR-305
- **Suporte a API Versioning**: Versionamento nativo de REST APIs
- **Melhorias em Observability**: Novas anotações e módulos de tracing

### 3. Atualizações de Dependências Principais

| Biblioteca | Spring Boot 3.x | Spring Boot 4.0 | Impacto |
|-----------|-----------------|-----------------|---------|
| Spring Data | 2024.x | 2025.x | Médio |
| Spring Security | 6.3.x | 6.4.x | Baixo |
| Hibernate ORM | 6.x | 7.0 | **Alto** |
| Bean Validation | 3.0 | 3.1 | Baixo |
| Micrometer | 1.x | 2.x | Médio |
| Jackson | 2.x | 3.x | **Alto** |
| Testcontainers | 1.x | 2.0 | Médio |

### 4. Remoções e Depreciações

#### Removido
- ✗ Suporte a Undertow (não será usado pelo projeto)
- ✗ Spring JCL (substituído por SLF4J nativo)
- ✗ OkHttp3 como cliente HTTP padrão
- ✗ Anotações JSR-305 (`@Nullable`, `@Nonnull` do Spring)
- ✗ Script de lançamento executável incorporado em JARs

#### Depreciado (a ser removido em versões futuras)
- ⚠️ Jackson 2.x (migração para Jackson 3 recomendada)
- ⚠️ JUnit 4 (migração para JUnit 5 necessária)

---

## 📝 Análise de Impacto no Projeto SGC

### Impacto Alto

1. **Hibernate 7.0**
   - Mudanças na API de critérias e consultas
   - Possíveis alterações em mapeamentos JPA
   - **Arquivos Afetados**: ~51 entidades JPA

2. **Jackson 3.0**
   - Relocação de pacotes (`com.fasterxml.jackson` permanece, mas internos mudam)
   - Mudanças na API do ObjectMapper
   - **Arquivos Afetados**: Configurações JSON, DTOs

3. **Gradle 9.x**
   - Já está usando Gradle 9.2.1 ✅
   - Compatível com Spring Boot 4

### Impacto Médio

1. **Spring Data 2025**
   - Novos métodos de repositório
   - Mudanças em paginação/ordenação
   - **Arquivos Afetados**: ~15 repositórios

2. **Configurações YAML/Properties**
   - Algumas propriedades renomeadas ou removidas
   - Usar Spring Boot Properties Migrator para detectar

3. **Testcontainers 2.0**
   - Mudanças na API de containers
   - **Arquivos Afetados**: Testes E2E

### Impacto Baixo

1. **Spring Security 6.4**
   - Mudanças incrementais
   - Configuração atual já usa método moderno (SecurityFilterChain)

2. **Bean Validation 3.1**
   - Pequenas adições à API
   - Compatibilidade retroativa mantida

3. **Migrações javax → jakarta**
   - Apenas 2 arquivos ainda usam `javax.*`
   - **Arquivo Identificado**: `E2eController.java` (usa `javax.sql.DataSource`)

---

## 🔧 Passos de Migração Detalhados

### Fase 1: Preparação (Antes da Migração)

#### 1.1. Atualizar para Última Versão do Spring Boot 3.5.x

```kotlin
// build.gradle.kts
plugins {
    id("org.springframework.boot") version "3.5.10" // ou última disponível
}
```

**Razão**: Minimizar diferenças entre 3.x e 4.0

#### 1.2. Verificar Compatibilidade do Java

```bash
# Verificar versão atual
java -version

# Deve ser Java 17+ (recomendado 21+)
# Atualizar se necessário
```

#### 1.3. Remover Dependências Depreciadas

- Verificar se há uso de APIs depreciadas no Spring Boot 3.5.x
- Executar build com warnings habilitados

```bash
./gradlew clean build -Xlint:deprecation
```

#### 1.4. Migrar Imports javax → jakarta

**Arquivo a corrigir**: `backend/src/main/java/sgc/e2e/E2eController.java`

```java
// Antes
import javax.sql.DataSource;

// Depois
import jakarta.sql.DataSource;
```

**Script de verificação**:
```bash
# Encontrar todos os imports javax
grep -r "import javax\." backend/src --include="*.java"
```

#### 1.5. Garantir Uso de JUnit 5

- Verificar que todos os testes usam JUnit 5 (não JUnit 4)
- Remover dependências do JUnit 4 se existirem

```bash
# Verificar imports de testes
grep -r "org.junit.Test" backend/src/test --include="*.java"
# Não deve retornar nada (JUnit 4)

grep -r "org.junit.jupiter" backend/src/test --include="*.java"
# Deve retornar testes (JUnit 5) ✅
```

---

### Fase 2: Atualização de Versões

#### 2.1. Atualizar Spring Boot para 4.0

**Arquivo**: `build.gradle.kts` (raiz)

```kotlin
plugins {
    id("org.springframework.boot") version "4.0.0" // ou 4.0.x mais recente
    id("io.spring.dependency-management") version "1.1.7" // verificar versão compatível
}
```

**Arquivo**: `backend/build.gradle.kts`

```kotlin
plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    java
}
```

#### 2.2. Atualizar Dependências Principais

**Arquivo**: `backend/build.gradle.kts`

```kotlin
extra["jjwt.version"] = "0.13.0" // Verificar compatibilidade com Jackson 3
extra["mapstruct.version"] = "1.6.3" // Verificar atualização
extra["lombok.version"] = "1.18.42" // Atualizar para última versão

dependencies {
    // ... mantém as dependências existentes
    
    // Atualizar SpringDoc OpenAPI para versão compatível com Boot 4
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0") // verificar versão exata
    
    // Adicionar migrador de propriedades (temporário)
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
}
```

#### 2.3. Atualizar README

**Arquivo**: `README.md`

```markdown
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
```

---

### Fase 3: Migração de Código

#### 3.1. Migração Jackson 2 → Jackson 3

Spring Boot 4 deprecia Jackson 2 em favor do Jackson 3.

**Estratégia**:
1. **Curto prazo**: Usar módulo de compatibilidade
   ```kotlin
   dependencies {
       implementation("org.springframework.boot:spring-boot-jackson2-compat") // temporário
   }
   ```

2. **Longo prazo**: Migrar para Jackson 3
   - Verificar código que usa `ObjectMapper` diretamente
   - Testar serialização/desserialização de DTOs

**Arquivos a revisar**:
- Configurações customizadas de JSON
- DTOs com anotações Jackson
- Código que usa `ObjectMapper` diretamente

```bash
# Encontrar uso de ObjectMapper
grep -r "ObjectMapper" backend/src --include="*.java"
```

#### 3.2. Adaptação ao Hibernate 7

**Principais mudanças**:
- API de Criteria atualizada
- Mudanças em tipos de dados (ex: `@Lob`)
- Configurações de esquema

**Verificação**:
```bash
# Encontrar uso de Criteria API
grep -r "CriteriaBuilder\|CriteriaQuery" backend/src --include="*.java"

# Encontrar anotações @Lob (podem precisar revisão)
grep -r "@Lob" backend/src --include="*.java"
```

**Ações**:
- Executar testes após atualização
- Verificar logs de inicialização do Hibernate
- Revisar esquema gerado (se usar `ddl-auto`)

#### 3.3. Atualização de Configurações

**Arquivo**: `backend/src/main/resources/application.yml`

Adicionar migrador temporariamente:
```yaml
spring:
  boot:
    properties-migrator:
      enabled: true # Detecta propriedades obsoletas
```

**Após migração**, remover:
- Propriedades obsoletas identificadas pelo migrador
- Dependência do properties-migrator

#### 3.4. Null Safety - Migração de Anotações

Spring Boot 4 usa **JSpecify** em vez de JSR-305.

**Antes** (Spring Boot 3):
```java
import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;

public String processar(@Nullable String entrada) {
    // ...
}
```

**Depois** (Spring Boot 4):
```java
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public String processar(@Nullable String entrada) {
    // ...
}
```

**Ação**:
- Verificar uso de anotações Spring para null safety
- Atualizar para JSpecify se usado extensivamente
- **Impacto no SGC**: Baixo (verificar se essas anotações são usadas)

```bash
# Verificar uso
grep -r "@Nullable\|@NonNull" backend/src --include="*.java"
```

#### 3.5. Revisão de Configurações de Segurança

**Arquivo**: `backend/src/main/java/sgc/comum/config/ConfigSeguranca.java`

- Verificar que continua compatível com Spring Security 6.4
- Testar fluxo de autenticação após upgrade
- Configuração atual já usa padrão moderno (SecurityFilterChain) ✅

---

### Fase 4: Testes e Validação

#### 4.1. Executar Testes Unitários

```bash
./gradlew :backend:test
```

**Verificar**:
- Todos os testes passam
- Sem warnings de deprecação
- Logs de erro do Hibernate/JPA

#### 4.2. Executar Testes de Integração/E2E

```bash
# Testes E2E com Playwright
npm test # ou comando específico do projeto
```

**Verificar**:
- Fluxos de autenticação
- CRUD de entidades
- Serialização JSON (API REST)

#### 4.3. Validação de Observabilidade

Se o projeto usa métricas/tracing:

```yaml
# Atualizar configurações
management:
  metrics:
    export:
      # Configurações atualizadas
  tracing:
    # Nova estrutura de tracing
```

#### 4.4. Teste Manual da API

1. Iniciar aplicação:
   ```bash
   ./gradlew :backend:bootRun
   ```

2. Acessar Swagger:
   ```
   http://localhost:10000/swagger-ui.html
   ```

3. Testar endpoints principais:
   - Login/Autenticação
   - Listagem de processos
   - Criação de mapa
   - Operações CRUD

#### 4.5. Revisão de Logs

Verificar logs de startup para:
- Warnings de deprecação
- Erros de configuração
- Mudanças no schema JPA

---

### Fase 5: Limpeza e Otimização

#### 5.1. Remover Compatibilidade Temporária

```kotlin
dependencies {
    // REMOVER após migração completa:
    // runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
    // implementation("org.springframework.boot:spring-boot-jackson2-compat")
}
```

#### 5.2. Remover Propriedades Obsoletas

Baseado no output do properties-migrator.

#### 5.3. Atualizar Documentação

- `README.md`: Versões atualizadas
- `AGENTS.md`: Instruções de build atualizadas
- Documentação de setup/desenvolvimento

#### 5.4. Aproveitar Novos Recursos (Opcional)

##### API Versioning
Se o projeto precisar versionar APIs:

```java
@RestController
@RequestMapping("/api/v1/processos")
@ApiVersion("1.0") // Nova funcionalidade do Spring Boot 4
public class ProcessoController {
    // ...
}
```

##### Modularização
Avaliar se módulos mais granulares do Spring Boot 4 podem reduzir tamanho do JAR.

---

## 🚨 Riscos e Mitigações

### Risco 1: Breaking Changes no Hibernate 7

**Probabilidade**: Média  
**Impacto**: Alto  

**Mitigação**:
- Manter suite de testes abrangente
- Testar em ambiente de desenvolvimento primeiro
- Revisar changelogs do Hibernate 7.0
- Consultar: https://hibernate.org/orm/releases/7.0/

### Risco 2: Incompatibilidade de Dependências Externas

**Probabilidade**: Média  
**Impacto**: Médio  

**Mitigação**:
- Verificar compatibilidade de cada dependência externa:
  - JJWT 0.13.0 com Jackson 3
  - SpringDoc OpenAPI com Spring Boot 4
  - MapStruct com novas versões do JDK/Lombok
- Atualizar para versões compatíveis quando disponíveis

### Risco 3: Problemas com Jackson 3

**Probabilidade**: Média  
**Impacto**: Médio  

**Mitigação**:
- Usar módulo de compatibilidade inicialmente
- Testar todas as APIs REST
- Validar DTOs complexos
- Migrar gradualmente

### Risco 4: Mudanças em Propriedades de Configuração

**Probabilidade**: Baixa  
**Impacto**: Médio  

**Mitigação**:
- Usar `spring-boot-properties-migrator`
- Revisar documentação de migração
- Testar em diferentes perfis (dev, e2e, prod)

---

## 📚 Checklist de Migração

### Pré-Migração
- [ ] Atualizar para Spring Boot 3.5.x mais recente
- [ ] Verificar Java 17+ (recomendado 21+)
- [ ] Migrar todos `javax.*` para `jakarta.*`
- [ ] Garantir que testes usam JUnit 5
- [ ] Fazer backup/tag da versão atual
- [ ] Documentar estado atual (dependências, testes passando)

### Migração
- [ ] Atualizar Spring Boot para 4.0.x em `build.gradle.kts`
- [ ] Atualizar `io.spring.dependency-management` para versão compatível
- [ ] Adicionar `spring-boot-properties-migrator` (temporário)
- [ ] Atualizar SpringDoc OpenAPI para versão compatível
- [ ] Atualizar versões de Lombok, MapStruct conforme necessário
- [ ] Atualizar README.md com novas versões

### Código
- [ ] Executar build e corrigir erros de compilação
- [ ] Revisar warnings de deprecação
- [ ] Atualizar configurações YAML/properties conforme migrador
- [ ] Verificar código que usa ObjectMapper (Jackson)
- [ ] Revisar entidades JPA para compatibilidade Hibernate 7
- [ ] Atualizar anotações de null safety se necessário (JSpecify)

### Testes
- [ ] Executar testes unitários (`./gradlew :backend:test`)
- [ ] Corrigir testes quebrados
- [ ] Executar testes E2E
- [ ] Teste manual via Swagger UI
- [ ] Verificar logs de startup (sem erros/warnings críticos)

### Limpeza
- [ ] Remover `spring-boot-properties-migrator`
- [ ] Remover `spring-boot-jackson2-compat` (se usado)
- [ ] Remover propriedades obsoletas
- [ ] Atualizar documentação do projeto
- [ ] Criar PR com mudanças e descrição detalhada

### Pós-Migração
- [ ] Testar em ambiente de staging/QA
- [ ] Monitorar logs em produção (se aplicável)
- [ ] Documentar lições aprendidas
- [ ] Atualizar guias de desenvolvimento

---

## 🔗 Recursos e Referências

### Documentação Oficial

1. **Spring Boot 4.0 Migration Guide**  
   https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide

2. **Spring Boot 4.0 Release Notes**  
   https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes

3. **Spring Framework 7 Documentation**  
   https://docs.spring.io/spring-framework/docs/7.0.x/reference/html/

4. **Upgrading Spring Boot**  
   https://docs.spring.io/spring-boot/upgrading.html

### Dependências Principais

5. **Hibernate ORM 7.0**  
   https://hibernate.org/orm/releases/7.0/

6. **Jackson 3.0**  
   https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0

7. **Spring Data 2025**  
   https://spring.io/projects/spring-data

8. **Spring Security 6.4**  
   https://spring.io/projects/spring-security

### Artigos e Tutoriais

9. **Spring Boot 4 and Spring Framework 7: Key Features and Changes**  
   https://loiane.com/2025/08/spring-boot-4-spring-framework-7-key-features/

10. **Spring Framework 7 and Spring Boot 4 Deliver API Versioning**  
    https://www.infoq.com/news/2025/11/spring-7-spring-boot-4/

11. **Spring Framework 7 & Spring Boot 4: Baseline Updates**  
    https://www.rbaconsulting.com/blog/spring-framework-7-spring-boot-4/

### Ferramentas

12. **JSpecify Annotations**  
    https://jspecify.dev/

13. **Testcontainers 2.0**  
    https://testcontainers.com/

---

## 📊 Estimativa de Esforço

| Fase | Esforço Estimado | Risco |
|------|------------------|-------|
| Preparação | 2-4 horas | Baixo |
| Atualização de Versões | 1-2 horas | Baixo |
| Migração de Código | 4-8 horas | Médio |
| Testes e Validação | 4-8 horas | Médio |
| Limpeza e Otimização | 2-4 horas | Baixo |
| **TOTAL** | **13-26 horas** | **Médio** |

**Nota**: Estimativa baseada em projeto de porte médio (309 arquivos Java). Tempo pode variar conforme:
- Complexidade das customizações
- Número de testes
- Dependências de terceiros específicas
- Necessidade de migração Jackson 2 → 3 completa

---

## 🎯 Próximos Passos Recomendados

1. **Imediato**:
   - Criar branch de migração: `git checkout -b migrate/spring-boot-4`
   - Executar checklist de pré-migração
   - Atualizar para Spring Boot 3.5.x final

2. **Curto Prazo** (após testes):
   - Iniciar migração de versões
   - Corrigir erros de compilação/runtime
   - Executar suite de testes

3. **Médio Prazo**:
   - Migração completa de Jackson 3
   - Remoção de dependências de compatibilidade
   - Deploy em ambiente de teste

4. **Longo Prazo**:
   - Explorar novos recursos (API Versioning)
   - Otimizar modularização
   - Atualizar CI/CD para Spring Boot 4

---

## 📝 Notas Finais

- **Compatibilidade Retroativa**: Spring Boot 4 mantém compatibilidade razoável com 3.x, mas requer atenção aos breaking changes
- **Java 21**: Fortemente recomendado aproveitar recursos da linguagem moderna
- **Testes**: Fundamentais para garantir que migração não quebrou funcionalidades
- **Documentação**: Manter atualizada para facilitar manutenção futura

**Data da Análise**: 2025-12-05  
**Versão do Documento**: 1.0  
**Autor**: GitHub Copilot Agent
