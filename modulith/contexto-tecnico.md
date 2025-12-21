# Contexto Técnico - Spring Modulith no SGC

**Baseado em:** `modulith-report.md` - Seção 8 (Configuração Técnica)

## Visão Geral

Este documento detalha a configuração técnica necessária para implementar o Spring Modulith no projeto SGC, incluindo dependências, configurações e exemplos de código.

---

## 1. Dependências Gradle

### Arquivo: `backend/build.gradle.kts`

Adicionar as seguintes dependências na seção `dependencies`:

```kotlin
dependencies {
    // ... dependências existentes do projeto ...
    
    // ===== Spring Modulith - Core =====
    // Funcionalidades principais do Spring Modulith
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    
    // API de eventos aprimorada
    implementation("org.springframework.modulith:spring-modulith-events-api")
    
    // ===== Spring Modulith - Event Publication Registry =====
    // Persistência de eventos com JPA
    implementation("org.springframework.modulith:spring-modulith-events-jpa")
    
    // ===== Spring Modulith - Observability (opcional mas recomendado) =====
    // Endpoints Actuator para monitoramento de módulos
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    
    // Observabilidade e métricas
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    
    // ===== Spring Modulith - Testes =====
    // Suporte para testes modulares
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    
    // ===== Spring Modulith - Documentação =====
    // Geração de diagramas C4 e PlantUML
    testImplementation("org.springframework.modulith:spring-modulith-docs")
}
```

### Versões

Spring Modulith usa o **BOM (Bill of Materials) do Spring Boot**, portanto:
- Não é necessário especificar versões manualmente
- A versão compatível com Spring Boot 4.0.1 será resolvida automaticamente
- Spring Modulith requer Spring Boot 3.2+ (✅ atendido - versão atual: 4.0.1)

### Verificar Resolução de Dependências

```bash
./gradlew :backend:dependencies | grep modulith
```

**Saída esperada:**
```
+--- org.springframework.modulith:spring-modulith-starter-core:1.x.x
+--- org.springframework.modulith:spring-modulith-events-api:1.x.x
+--- org.springframework.modulith:spring-modulith-events-jpa:1.x.x
...
```

---

## 2. Configuração da Aplicação

### Arquivo: `backend/src/main/resources/application.yml`

Adicionar/atualizar seção `spring.modulith`:

```yaml
spring:
  modulith:
    # ===== Verificação de Estrutura Modular =====
    verification:
      # Habilitar verificação na inicialização da aplicação
      # IMPORTANTE: Falha se houver violações de limites de módulos
      enabled: true  # Iniciar com 'false' durante implementação, depois 'true'
    
    # ===== Event Publication Registry =====
    events:
      # Habilitar persistência de eventos
      externalization:
        enabled: true
      
      # Modo de completude dos eventos
      # - on-completion: Marca como completo após processamento
      # - on-transaction-commit: Marca como completo após commit da transação
      completion-mode: on-completion
      
      # Limpeza automática de eventos completados
      # Formato: <número><unidade> (d=dias, h=horas, m=minutos)
      # null = nunca limpar (padrão)
      delete-completion-after: 7d  # Apagar após 7 dias
    
    # ===== Actuator (Observability) =====
    actuator:
      # Habilitar endpoints de monitoramento
      enabled: true

# ===== Configuração do Actuator =====
management:
  endpoints:
    web:
      exposure:
        # Expor endpoints relevantes
        # 'modulith' fornece informações sobre módulos e eventos
        include: health,info,modulith,metrics
  
  # Configurações adicionais de observabilidade (opcional)
  metrics:
    export:
      prometheus:
        enabled: true  # Se usando Prometheus

# ===== Configuração de Async (para @ApplicationModuleListener) =====
spring:
  task:
    execution:
      pool:
        # Pool de threads para processamento assíncrono de eventos
        core-size: 5
        max-size: 10
        queue-capacity: 100
      thread-name-prefix: sgc-async-
```

### Configuração por Ambiente

**Desenvolvimento (`application-dev.yml`):**
```yaml
spring:
  modulith:
    verification:
      enabled: false  # Permitir violações temporárias durante desenvolvimento
```

**Produção (`application-prod.yml`):**
```yaml
spring:
  modulith:
    verification:
      enabled: true   # Enforcement rígido em produção
```

---

## 3. Habilitar Processamento Assíncrono

### Arquivo: `backend/src/main/java/sgc/SgcApplication.java`

Adicionar anotação `@EnableAsync`:

```java
package sgc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // ← ADICIONAR: Habilita @Async em @ApplicationModuleListener
public class SgcApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SgcApplication.class, args);
    }
}
```

---

## 4. Estrutura de Pacotes

### Convenção Spring Modulith

Cada módulo deve seguir a estrutura:

```
sgc/{modulo}/
├── {Modulo}Service.java           # API pública (facade)
├── package-info.java              # Metadados e dependências permitidas
├── api/                           # API pública exportada
│   ├── {Modulo}Dto.java          # DTOs expostos
│   └── eventos/                   # Eventos publicados
│       └── Evento{Modulo}{Acao}.java
└── internal/                      # Implementação privada (NÃO acessível)
    ├── {Modulo}Controller.java    # REST endpoints
    ├── {Modulo}Mapper.java        # Mapeamento entidade ↔ DTO
    ├── listeners/                 # Event listeners
    │   └── {OutroModulo}Listener.java
    ├── model/                     # Entidades JPA e repositories
    │   ├── {Modulo}.java
    │   └── {Modulo}Repo.java
    └── erros/                     # Exceções customizadas
        └── Erro{Modulo}.java
```

### Regras de Visibilidade

- **Pacote raiz** (`sgc.{modulo}.*`): Classes públicas por padrão
- **Pacote `api/`** (`sgc.{modulo}.api.*`): Explicitamente público
- **Pacote `internal/`** (`sgc.{modulo}.internal.*`): Privado ao módulo

**⚠️ IMPORTANTE:** Outros módulos **NÃO** devem importar de `internal/`.

---

## 5. Metadados de Módulos

### Arquivo: `package-info.java` (em cada módulo)

Exemplo para módulo `processo`:

```java
/**
 * Módulo Orquestrador de Processos do SGC.
 * 
 * <p>Responsável por gerenciar o ciclo de vida completo dos processos,
 * coordenando subprocessos, mapas de competências e atividades.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.processo.ProcessoService} - Facade principal</li>
 *   <li>{@link sgc.processo.api.ProcessoDto} - DTO de processo</li>
 *   <li>{@link sgc.processo.api.eventos} - Eventos de domínio</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>subprocesso - Gerenciamento de subprocessos</li>
 *   <li>mapa - Mapas de competências</li>
 *   <li>atividade - Atividades e conhecimentos</li>
 *   <li>unidade - Estrutura organizacional</li>
 *   <li>comum - Componentes transversais</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>EventoProcessoIniciado - Quando processo é iniciado</li>
 *   <li>EventoProcessoFinalizado - Quando processo é finalizado</li>
 *   <li>EventoProcessoAtualizado - Quando processo é atualizado</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>EventoSubprocessoConcluido - Para atualizar status do processo</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Orquestrador de Processos",
    allowedDependencies = {"subprocesso", "mapa", "atividade", "unidade", "comum"}
)
package sgc.processo;
```

### Dependências Granulares

Para permitir dependência apenas em **subpacotes específicos**:

```java
@ApplicationModule(
    displayName = "Gestão de Subprocessos",
    allowedDependencies = {
        "processo::api.eventos",  // Apenas eventos, não todo o módulo
        "comum"
    }
)
package sgc.subprocesso;
```

---

## 6. Eventos de Domínio

### Estrutura de Eventos

**Localização:** `sgc/{modulo}/api/eventos/`

**Exemplo:** `sgc/processo/api/eventos/EventoProcessoIniciado.java`

```java
package sgc.processo.api.eventos;

import java.time.Instant;

/**
 * Evento publicado quando um processo é iniciado.
 * 
 * Este evento é consumido por módulos como:
 * - subprocesso (para criar subprocessos)
 * - alerta (para criar alertas)
 * - notificacao (para notificar usuários)
 */
public class EventoProcessoIniciado {
    
    private final Long codigoProcesso;
    private final String nomeProcesso;
    private final Long codigoUnidade;
    private final Instant dataHoraInicio;
    
    public EventoProcessoIniciado(Long codigoProcesso, String nomeProcesso, Long codigoUnidade) {
        this.codigoProcesso = codigoProcesso;
        this.nomeProcesso = nomeProcesso;
        this.codigoUnidade = codigoUnidade;
        this.dataHoraInicio = Instant.now();
    }
    
    // Getters
    public Long getCodigoProcesso() { return codigoProcesso; }
    public String getNomeProcesso() { return nomeProcesso; }
    public Long getCodigoUnidade() { return codigoUnidade; }
    public Instant getDataHoraInicio() { return dataHoraInicio; }
}
```

### Publicação de Eventos

**No service que publica:**

```java
@Service
public class ProcessoService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public ProcessoService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public ProcessoDto iniciar(Long codigo) {
        // Lógica de negócio
        Processo processo = processoRepo.findByCodigo(codigo)
            .orElseThrow(() -> new ErroProcesso("Processo não encontrado"));
        
        processo.iniciar();
        processoRepo.save(processo);
        
        // Publicar evento
        eventPublisher.publishEvent(
            new EventoProcessoIniciado(
                processo.getCodigo(),
                processo.getNome(),
                processo.getUnidadeCodigo()
            )
        );
        
        return mapper.toDto(processo);
    }
}
```

### Consumo de Eventos

**No módulo que consome:**

```java
package sgc.alerta.internal.listeners;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.api.eventos.EventoProcessoIniciado;
import sgc.alerta.AlertaService;

@Component
public class ProcessoListener {
    
    private final AlertaService alertaService;
    
    public ProcessoListener(AlertaService alertaService) {
        this.alertaService = alertaService;
    }
    
    /**
     * Cria alertas quando processo é iniciado.
     * 
     * Características:
     * - @ApplicationModuleListener: Suporte Spring Modulith (vs @EventListener)
     * - @Async: Processamento assíncrono (não bloqueia transação principal)
     * - @Transactional(REQUIRES_NEW): Transação independente
     */
    @ApplicationModuleListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        alertaService.criarAlertasParaProcesso(evento.getCodigoProcesso());
    }
}
```

---

## 7. Event Publication Registry

### Esquema de Banco de Dados

Spring Modulith cria automaticamente a tabela `EVENT_PUBLICATION`.

**Esquema (referência):**

```sql
CREATE TABLE EVENT_PUBLICATION (
    ID UUID NOT NULL PRIMARY KEY,
    EVENT_TYPE VARCHAR(512) NOT NULL,
    LISTENER_ID VARCHAR(512) NOT NULL,
    PUBLICATION_DATE TIMESTAMP NOT NULL,
    SERIALIZED_EVENT TEXT NOT NULL,
    COMPLETION_DATE TIMESTAMP,
    INDEX idx_completion_date (COMPLETION_DATE),
    INDEX idx_publication_date (PUBLICATION_DATE)
);
```

### Consultas Úteis

**Eventos pendentes (não processados):**
```sql
SELECT * FROM EVENT_PUBLICATION 
WHERE COMPLETION_DATE IS NULL 
ORDER BY PUBLICATION_DATE DESC;
```

**Eventos completados recentemente:**
```sql
SELECT * FROM EVENT_PUBLICATION 
WHERE COMPLETION_DATE IS NOT NULL 
ORDER BY COMPLETION_DATE DESC 
LIMIT 50;
```

**Contagem por tipo de evento:**
```sql
SELECT EVENT_TYPE, COUNT(*) as total
FROM EVENT_PUBLICATION
GROUP BY EVENT_TYPE
ORDER BY total DESC;
```

### Monitoramento

**Endpoint Actuator:**
```bash
curl http://localhost:8080/actuator/modulith
```

**Resposta (exemplo):**
```json
{
  "modules": [
    {
      "name": "processo",
      "displayName": "Orquestrador de Processos",
      "dependencies": ["subprocesso", "mapa", "atividade", "unidade"]
    }
  ],
  "events": {
    "pending": 5,
    "completed": 1234
  }
}
```

---

## 8. Testes de Estrutura Modular

### Arquivo: `backend/src/test/java/sgc/ModulithStructureTest.java`

```java
package sgc;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testes de verificação da estrutura modular usando Spring Modulith.
 */
class ModulithStructureTest {
    
    private final ApplicationModules modules = ApplicationModules.of(SgcApplication.class);
    
    @Test
    void deveDetectarModulosCorretamente() {
        System.out.println("=== Módulos Detectados ===");
        modules.forEach(module -> {
            System.out.println("Módulo: " + module.getName());
            System.out.println("  - Display Name: " + module.getDisplayName());
            System.out.println("  - Pacote Base: " + module.getBasePackage());
            System.out.println("  - Dependências: " + module.getDependencies());
        });
        
        assertThatCode(() -> modules.verify())
            .as("Estrutura de módulos deve ser válida")
            .doesNotThrowAnyException();
    }
    
    @Test
    void naoDevemExistirDependenciasCiclicas() {
        assertThatCode(() -> modules.verify())
            .as("Não devem existir dependências cíclicas entre módulos")
            .doesNotThrowAnyException();
    }
    
    @Test
    void gerarDocumentacaoDosModulos() {
        new Documenter(modules)
            .writeDocumentation()                  // Gera index.html
            .writeIndividualModulesAsPlantUml()    // Diagrama de cada módulo
            .writeModulesAsPlantUml();             // Diagrama geral
        
        System.out.println("Documentação gerada em: backend/build/spring-modulith-docs/");
    }
}
```

### Executar Testes

```bash
# Verificar estrutura
./gradlew :backend:test --tests ModulithStructureTest.deveDetectarModulosCorretamente

# Verificar ausência de ciclos
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas

# Gerar documentação
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

---

## 9. Testes de Integração Modulares

### Anotação @ApplicationModuleTest

Testa módulo isoladamente, carregando apenas suas dependências diretas.

**Exemplo:** `backend/src/test/java/sgc/alerta/AlertaModuleTest.java`

```java
package sgc.alerta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import sgc.alerta.api.AlertaDto;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração modular do módulo 'alerta'.
 * 
 * Carrega apenas o módulo 'alerta' e suas dependências diretas,
 * resultando em contexto Spring menor e testes mais rápidos.
 */
@ApplicationModuleTest  // Substitui @SpringBootTest para testes modulares
class AlertaModuleTest {
    
    @Autowired
    private AlertaService alertaService;
    
    @Test
    void deveCarregarContextoDoModulo() {
        assertThat(alertaService).isNotNull();
    }
    
    @Test
    void deveCriarAlerta() {
        AlertaDto dto = AlertaDto.builder()
            .titulo("Teste")
            .descricao("Descrição de teste")
            .build();
        
        AlertaDto criado = alertaService.criar(dto);
        
        assertThat(criado.getCodigo()).isNotNull();
    }
}
```

**Benefícios:**
- Contexto Spring ~60% menor
- Testes ~30% mais rápidos
- Validação de isolamento de módulos

---

## 10. Resolução de Problemas

### Problema: Violação de Limites de Módulos

**Erro:**
```
Module 'alerta' depends on non-exposed type sgc.processo.internal.ProcessoRepository from module 'processo'
```

**Solução:**
- Remover import direto de `internal/`
- Usar apenas API pública (`ProcessoService`, DTOs em `api/`)

### Problema: Dependência Cíclica

**Erro:**
```
Cycle detected: processo -> subprocesso -> processo
```

**Solução:**
- Mover eventos para `api/eventos/`
- Remover dependências diretas
- Usar eventos de domínio para comunicação

### Problema: Eventos Não São Persistidos

**Diagnóstico:**
```sql
SELECT COUNT(*) FROM EVENT_PUBLICATION;
-- Retorna 0 ou tabela não existe
```

**Solução:**
- Verificar dependência `spring-modulith-events-jpa`
- Verificar configuração `spring.modulith.events.externalization.enabled: true`
- Verificar que listeners usam `@ApplicationModuleListener`

### Problema: Aplicação Não Inicia com verification.enabled: true

**Erro:**
```
Application failed to start due to module violations
```

**Solução:**
- Revisar violações no log
- Corrigir imports
- Ou temporariamente configurar `verification.enabled: false`

---

## 11. Referências

- [Spring Modulith - Documentação Oficial](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith - GitHub](https://github.com/spring-projects/spring-modulith)
- [Spring Events - Documentação](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [PlantUML](https://plantuml.com/)

---

**Documento preparado para:** Implementação técnica do Spring Modulith  
**Versão:** 1.0  
**Última atualização:** 2025-12-21
