# Sprint 3: Módulos Core Domain

**Baseado em:** `modulith-report.md` - Seção 6.2 (Sprint 3)

## Contexto

Esta é a **sprint mais complexa e crítica**, pois envolve:
- Módulos de **domínio central** do sistema
- **Dependências cíclicas** que precisam ser quebradas
- **Eventos de domínio** que precisam ser reorganizados
- **Maior risco de regressão** funcional

### Módulos Alvo
1. **`mapa`** - Mapas de competências
2. **`atividade`** - Atividades e conhecimentos
3. **`processo`** - Orquestrador central
4. **`subprocesso`** - Máquina de estados e workflow

### Dependências Cíclicas Identificadas
```
processo ↔ subprocesso (ciclo principal)
mapa ↔ atividade (ciclo secundário)
```

### Status do Projeto
Após Sprint 2:
- ✅ 4 módulos refatorados: `alerta`, `analise`, `unidade`, `sgrh`
- ✅ Padrão Spring Modulith estabelecido
- ✅ Nenhuma dependência cíclica nos módulos refatorados

---

## Objetivo da Sprint

Refatorar módulos de **domínio central** e **quebrar dependências cíclicas** usando eventos de domínio e inversão de dependência.

### Entregáveis
1. ✅ Análise detalhada das dependências cíclicas
2. ✅ Quebra do ciclo `mapa ↔ atividade`
3. ✅ Quebra do ciclo `processo ↔ subprocesso`
4. ✅ Migração de eventos para estrutura Spring Modulith
5. ✅ Event Publication Registry configurado
6. ✅ Todos os 4 módulos refatorados
7. ✅ Teste `naoDevemExistirDependenciasCiclicas()` **passando**
8. ✅ Build e testes funcionais passando

---

## Tarefas Detalhadas

### Tarefa 1: Analisar Dependências Cíclicas

#### Executar Teste de Estrutura

```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```

**Resultado esperado:** Falha com detalhes dos ciclos.

#### Analisar Manualmente

**Buscar dependências entre processo e subprocesso:**
```bash
# processo depende de subprocesso?
grep -r "import sgc.subprocesso" backend/src/main/java/sgc/processo/ --include="*.java"

# subprocesso depende de processo?
grep -r "import sgc.processo" backend/src/main/java/sgc/subprocesso/ --include="*.java"
```

**Buscar dependências entre mapa e atividade:**
```bash
# mapa depende de atividade?
grep -r "import sgc.atividade" backend/src/main/java/sgc/mapa/ --include="*.java"

# atividade depende de mapa?
grep -r "import sgc.mapa" backend/src/main/java/sgc/atividade/ --include="*.java"
```

#### Documentar Ciclos

Criar arquivo temporário `/tmp/ciclos-detectados.md`:

```markdown
# Dependências Cíclicas Detectadas

## Ciclo 1: processo ↔ subprocesso

### processo → subprocesso
- Arquivo: ProcessoService.java
- Importa: sgc.subprocesso.dto.SubprocessoDto
- Importa: sgc.subprocesso.SubprocessoRepo (PROBLEMA!)
- Motivo: Acesso direto ao repositório para consultas

### subprocesso → processo
- Arquivo: SubprocessoListener.java
- Importa: sgc.processo.eventos.EventoProcessoIniciado
- Motivo: Escuta eventos de processo

### Estratégia de Quebra
1. Mover eventos para pacote `comum.eventos` ou `processo.api.eventos`
2. Remover acesso direto a `SubprocessoRepo` de `processo`
3. Criar interface `SubprocessoApi` em `subprocesso.api`

## Ciclo 2: mapa ↔ atividade

### mapa → atividade
- Arquivo: MapaService.java
- Importa: sgc.atividade.dto.AtividadeDto
- Motivo: Mapas contêm atividades

### atividade → mapa
- Arquivo: AtividadeService.java
- Importa: sgc.mapa.dto.MapaDto
- Motivo: Atividades podem estar em múltiplos mapas

### Estratégia de Quebra
1. Mover DTOs para pacotes `api/`
2. Considerar módulo compartilhado se necessário
3. Usar eventos para notificações bidirecionais
```

**Critério de Aceite:**
- ✅ Ciclos documentados com arquivos e imports específicos
- ✅ Estratégia de quebra definida para cada ciclo

---

### Tarefa 2: Quebrar Ciclo `mapa ↔ atividade`

**Nota:** Começar pelo ciclo mais simples.

#### Passo 2.1: Refatorar Módulo `atividade`

**Estrutura Alvo:**
```
sgc/atividade/
├── AtividadeService.java           # API pública
├── package-info.java
├── api/
│   ├── AtividadeDto.java
│   └── AtividadeEvento.java        # Eventos publicados
└── internal/
    ├── AtividadeController.java
    ├── AtividadeMapper.java
    ├── listeners/                  # Event listeners
    │   └── MapaListener.java
    ├── model/
    │   ├── Atividade.java
    │   ├── AtividadeRepo.java
    │   └── Conhecimento.java
    └── erros/
        └── ErroAtividade.java
```

**package-info.java:**
```java
/**
 * Módulo de Gestão de Atividades e Conhecimentos.
 * 
 * <p>Responsável por gerenciar atividades, conhecimentos associados
 * e suas relações com mapas de competências.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.atividade.AtividadeService} - Facade de atividades</li>
 *   <li>{@link sgc.atividade.api.AtividadeDto} - DTO de atividade</li>
 *   <li>{@link sgc.atividade.api.AtividadeEvento} - Eventos publicados</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>AtividadeCriada - Quando atividade é criada</li>
 *   <li>AtividadeAtualizada - Quando atividade é atualizada</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>MapaCriado - Para associar atividades a mapas (se necessário)</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Gestão de Atividades",
    allowedDependencies = {"comum"}
)
package sgc.atividade;
```

**Executar refatoração:**
```bash
# Criar estrutura
mkdir -p backend/src/main/java/sgc/atividade/api
mkdir -p backend/src/main/java/sgc/atividade/internal/model
mkdir -p backend/src/main/java/sgc/atividade/internal/erros
mkdir -p backend/src/main/java/sgc/atividade/internal/listeners

# Mover DTOs
git mv backend/src/main/java/sgc/atividade/dto/AtividadeDto.java backend/src/main/java/sgc/atividade/api/

# Mover implementações
git mv backend/src/main/java/sgc/atividade/AtividadeController.java backend/src/main/java/sgc/atividade/internal/
git mv backend/src/main/java/sgc/atividade/dto/AtividadeMapper.java backend/src/main/java/sgc/atividade/internal/
git mv backend/src/main/java/sgc/atividade/model/* backend/src/main/java/sgc/atividade/internal/model/
git mv backend/src/main/java/sgc/atividade/erros/* backend/src/main/java/sgc/atividade/internal/erros/

# Remover diretórios vazios
rmdir backend/src/main/java/sgc/atividade/dto
rmdir backend/src/main/java/sgc/atividade/model
rmdir backend/src/main/java/sgc/atividade/erros

# Atualizar imports
find backend/src -name "*.java" -exec sed -i 's/sgc\.atividade\.dto\.AtividadeDto/sgc.atividade.api.AtividadeDto/g' {} +
find backend/src -name "*.java" -exec sed -i 's/sgc\.atividade\.model\./sgc.atividade.internal.model./g' {} +
find backend/src -name "*.java" -exec sed -i 's/sgc\.atividade\.erros\./sgc.atividade.internal.erros./g' {} +

# Atualizar declarações de package
sed -i 's/package sgc.atividade;/package sgc.atividade.internal;/g' backend/src/main/java/sgc/atividade/internal/AtividadeController.java
sed -i 's/package sgc.atividade.dto;/package sgc.atividade.internal;/g' backend/src/main/java/sgc/atividade/internal/AtividadeMapper.java
sed -i 's/package sgc.atividade.dto;/package sgc.atividade.api;/g' backend/src/main/java/sgc/atividade/api/AtividadeDto.java
find backend/src/main/java/sgc/atividade/internal/model -name "*.java" -exec sed -i 's/package sgc.atividade.model;/package sgc.atividade.internal.model;/g' {} +
find backend/src/main/java/sgc/atividade/internal/erros -name "*.java" -exec sed -i 's/package sgc.atividade.erros;/package sgc.atividade.internal.erros./g' {} +

# Compilar
./gradlew :backend:compileJava
```

#### Passo 2.2: Refatorar Módulo `mapa`

**Estrutura Alvo:**
```
sgc/mapa/
├── MapaService.java                # API pública
├── package-info.java
├── api/
│   ├── MapaDto.java
│   └── MapaEvento.java
└── internal/
    ├── MapaController.java
    ├── MapaMapper.java
    ├── listeners/
    │   └── AtividadeListener.java
    ├── model/
    │   ├── Mapa.java
    │   └── MapaRepo.java
    └── erros/
        └── ErroMapa.java
```

**package-info.java:**
```java
/**
 * Módulo de Gestão de Mapas de Competências.
 * 
 * <p>Responsável por gerenciar mapas de competências,
 * associando atividades e conhecimentos necessários para cada processo.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.mapa.MapaService} - Facade de mapas</li>
 *   <li>{@link sgc.mapa.api.MapaDto} - DTO de mapa</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>atividade - Para referenciar atividades (via API)</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>MapaCriado - Quando mapa é criado</li>
 *   <li>MapaAtualizado - Quando mapa é atualizado</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>AtividadeCriada - Para atualizar mapas (se necessário)</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Gestão de Mapas de Competências",
    allowedDependencies = {"atividade", "comum"}
)
package sgc.mapa;
```

**Executar refatoração** (mesmos passos de `atividade`).

**Importante:** Garantir que `mapa` importe apenas de `sgc.atividade.api.*`, nunca de `sgc.atividade.internal.*`.

**Critério de Aceite:**
- ✅ Ambos os módulos compilam
- ✅ Imports apenas de APIs públicas
- ✅ Testes passam

---

### Tarefa 3: Quebrar Ciclo `processo ↔ subprocesso`

**Nota:** Este é o ciclo **mais complexo** devido ao forte acoplamento.

#### Análise do Problema

**processo → subprocesso:**
- `ProcessoService` acessa `SubprocessoRepo` diretamente
- `ProcessoService` cria `SubprocessoDto`

**subprocesso → processo:**
- `SubprocessoListener` escuta `EventoProcessoIniciado`
- Eventos estão no pacote `sgc.processo.eventos`

#### Estratégia de Quebra

**Opção 1:** Mover eventos para pacote compartilhado
- Criar `sgc.comum.eventos` ou `sgc.processo.api.eventos`
- Mover eventos de processo para lá

**Opção 2:** Remover dependência direta de processo → subprocesso
- Criar `SubprocessoApi` (interface ou facade) em `sgc.subprocesso`
- `ProcessoService` usa apenas a API pública

**Vamos aplicar ambas as opções.**

#### Passo 3.1: Criar Pacote de Eventos em `processo`

**Estrutura:**
```
sgc/processo/
├── ProcessoService.java
├── package-info.java
├── api/
│   ├── ProcessoDto.java
│   └── eventos/                    # NOVO
│       ├── EventoProcessoIniciado.java
│       ├── EventoProcessoFinalizado.java
│       └── ...
└── internal/
    ├── ProcessoController.java
    ├── ProcessoMapper.java
    ├── listeners/
    ├── model/
    └── erros/
```

**Mover eventos:**
```bash
# Criar diretório
mkdir -p backend/src/main/java/sgc/processo/api/eventos

# Mover eventos (exemplo - ajustar conforme estrutura real)
git mv backend/src/main/java/sgc/processo/eventos/* backend/src/main/java/sgc/processo/api/eventos/

# Ou se estão em outro pacote
find backend/src/main/java/sgc/processo -name "Evento*.java" -exec git mv {} backend/src/main/java/sgc/processo/api/eventos/ \;

# Atualizar package em eventos
find backend/src/main/java/sgc/processo/api/eventos -name "*.java" -exec sed -i 's/package sgc.processo.eventos;/package sgc.processo.api.eventos;/g' {} +

# Atualizar imports em todos os módulos
find backend/src -name "*.java" -exec sed -i 's/sgc\.processo\.eventos\./sgc.processo.api.eventos./g' {} +
```

#### Passo 3.2: Refatorar Módulo `subprocesso`

**Estrutura Alvo:**
```
sgc/subprocesso/
├── SubprocessoService.java         # API pública
├── package-info.java
├── api/
│   ├── SubprocessoDto.java
│   ├── SubprocessoApi.java         # Interface pública (se necessário)
│   └── eventos/
│       └── EventoSubprocessoCriado.java
└── internal/
    ├── SubprocessoController.java
    ├── SubprocessoMapper.java
    ├── listeners/
    │   └── ProcessoListener.java    # Escuta eventos de processo
    ├── model/
    │   ├── Subprocesso.java
    │   ├── SubprocessoRepo.java
    │   └── EstadoSubprocesso.java
    └── erros/
        └── ErroSubprocesso.java
```

**package-info.java:**
```java
/**
 * Módulo de Máquina de Estados e Workflow de Subprocessos.
 * 
 * <p>Responsável por gerenciar o ciclo de vida e estados dos subprocessos,
 * implementando a máquina de estados do workflow.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.SubprocessoService} - Facade de subprocessos</li>
 *   <li>{@link sgc.subprocesso.api.SubprocessoDto} - DTO de subprocesso</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>processo - Para eventos de processo (apenas api.eventos)</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>EventoSubprocessoCriado</li>
 *   <li>EventoSubprocessoTransicao</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>EventoProcessoIniciado - Para criar subprocessos</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Máquina de Estados de Subprocessos",
    allowedDependencies = {"processo::api.eventos", "comum"}
)
package sgc.subprocesso;
```

**Nota:** A sintaxe `"processo::api.eventos"` permite dependência **apenas** ao subpacote `api.eventos` de `processo`.

**Executar refatoração:**
```bash
# Criar estrutura
mkdir -p backend/src/main/java/sgc/subprocesso/api
mkdir -p backend/src/main/java/sgc/subprocesso/api/eventos
mkdir -p backend/src/main/java/sgc/subprocesso/internal/model
mkdir -p backend/src/main/java/sgc/subprocesso/internal/erros
mkdir -p backend/src/main/java/sgc/subprocesso/internal/listeners

# Mover arquivos
git mv backend/src/main/java/sgc/subprocesso/dto/SubprocessoDto.java backend/src/main/java/sgc/subprocesso/api/
git mv backend/src/main/java/sgc/subprocesso/SubprocessoController.java backend/src/main/java/sgc/subprocesso/internal/
# ... (seguir padrão das sprints anteriores)

# Atualizar imports
find backend/src -name "*.java" -exec sed -i 's/sgc\.subprocesso\.dto\./sgc.subprocesso.api./g' {} +
find backend/src -name "*.java" -exec sed -i 's/sgc\.subprocesso\.model\./sgc.subprocesso.internal.model./g' {} +

# Compilar
./gradlew :backend:compileJava
```

#### Passo 3.3: Remover Acesso Direto a SubprocessoRepo de Processo

**Problema:** `ProcessoService` acessa `SubprocessoRepo` diretamente.

**Solução:** Use apenas `SubprocessoService` (API pública).

**Exemplo - Antes:**
```java
@Service
public class ProcessoService {
    private final SubprocessoRepo subprocessoRepo; // ❌ ERRADO
    
    public void iniciarProcesso(Long codigo) {
        // ...
        List<Subprocesso> subs = subprocessoRepo.findByProcessoCodigo(codigo);
    }
}
```

**Exemplo - Depois:**
```java
@Service
public class ProcessoService {
    private final SubprocessoService subprocessoService; // ✅ CORRETO
    
    public void iniciarProcesso(Long codigo) {
        // ...
        List<SubprocessoDto> subs = subprocessoService.buscarPorProcesso(codigo);
    }
}
```

**Ação:**
1. Identificar onde `ProcessoService` usa `SubprocessoRepo`
2. Substituir por chamadas a `SubprocessoService`
3. Se necessário, adicionar métodos em `SubprocessoService`

**Comandos:**
```bash
# Buscar uso de SubprocessoRepo em processo
grep -r "SubprocessoRepo" backend/src/main/java/sgc/processo/ --include="*.java"

# Após correção, verificar que não há mais referências
grep -r "sgc.subprocesso.internal" backend/src/main/java/sgc/processo/ --include="*.java"
# Deve retornar vazio
```

**Critério de Aceite:**
- ✅ `ProcessoService` não importa `SubprocessoRepo`
- ✅ `ProcessoService` usa apenas `SubprocessoService` e DTOs públicos

#### Passo 3.4: Refatorar Módulo `processo`

**Estrutura Alvo:**
```
sgc/processo/
├── ProcessoService.java
├── package-info.java
├── api/
│   ├── ProcessoDto.java
│   └── eventos/
│       ├── EventoProcessoIniciado.java
│       ├── EventoProcessoFinalizado.java
│       └── EventoProcessoAtualizado.java
└── internal/
    ├── ProcessoController.java
    ├── ProcessoMapper.java
    ├── listeners/
    │   └── SubprocessoListener.java
    ├── model/
    │   ├── Processo.java
    │   └── ProcessoRepo.java
    └── erros/
        └── ErroProcesso.java
```

**package-info.java:**
```java
/**
 * Módulo Orquestrador Central de Processos.
 * 
 * <p>Responsável por orquestrar o ciclo de vida completo dos processos,
 * coordenando subprocessos, mapas, atividades e notificações.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.processo.ProcessoService} - Facade principal</li>
 *   <li>{@link sgc.processo.api.ProcessoDto} - DTO de processo</li>
 *   <li>{@link sgc.processo.api.eventos} - Eventos de processo</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>subprocesso - Para gerenciar subprocessos (apenas API)</li>
 *   <li>mapa - Para mapas de competências (apenas API)</li>
 *   <li>atividade - Para atividades (apenas API)</li>
 *   <li>unidade - Para estrutura organizacional</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>EventoProcessoIniciado</li>
 *   <li>EventoProcessoFinalizado</li>
 *   <li>EventoProcessoAtualizado</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Orquestrador de Processos",
    allowedDependencies = {"subprocesso", "mapa", "atividade", "unidade", "comum"}
)
package sgc.processo;
```

**Executar refatoração** (mesmos passos anteriores).

**Critério de Aceite:**
- ✅ Eventos em `api/eventos/`
- ✅ Nenhum import de `internal` de outros módulos
- ✅ Build compila

---

### Tarefa 4: Configurar Event Publication Registry

#### Adicionar Dependência JPA para Eventos

**Arquivo:** `backend/build.gradle.kts`

**Adicionar:**
```kotlin
dependencies {
    // ... dependências existentes ...
    
    // Spring Modulith - Event Publication Registry com JPA
    implementation("org.springframework.modulith:spring-modulith-events-jpa")
}
```

#### Configurar application.yml

**Arquivo:** `backend/src/main/resources/application.yml`

**Atualizar seção Spring Modulith:**
```yaml
spring:
  modulith:
    verification:
      enabled: true
    events:
      # Habilitar persistência de eventos
      externalization:
        enabled: true
      # Modo de completude
      completion-mode: on-completion
      # Cleanup de eventos completados (após 7 dias)
      delete-completion-after: 7d
```

#### Criar Entidade de Event Publication (Automático)

Spring Modulith criará automaticamente a tabela `EVENT_PUBLICATION` no banco H2/PostgreSQL.

**Verificar criação:**
```bash
# Executar aplicação
./gradlew :backend:bootRun

# Verificar logs para:
# Creating table EVENT_PUBLICATION
```

**Ou criar migration manual (Flyway/Liquibase):**

**Arquivo:** `backend/src/main/resources/db/migration/V999__create_event_publication.sql`

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

**Critério de Aceite:**
- ✅ Dependência adicionada
- ✅ Configuração em `application.yml`
- ✅ Aplicação inicia e cria tabela `EVENT_PUBLICATION`

---

### Tarefa 5: Migrar Event Listeners para @ApplicationModuleListener

#### Identificar Listeners Existentes

**Buscar listeners:**
```bash
grep -r "@EventListener" backend/src/main/java/sgc/ --include="*.java"
```

#### Padrão de Migração

**Antes:**
```java
@Component
public class AlertaProcessoListener {
    
    @EventListener
    @Transactional
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        // Cria alertas
    }
}
```

**Depois:**
```java
@Component
public class AlertaProcessoListener {
    
    @ApplicationModuleListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        // Cria alertas
        // Agora é assíncrono e com transação separada
    }
}
```

**Mudanças:**
- `@EventListener` → `@ApplicationModuleListener`
- Adicionar `@Async` para processamento assíncrono
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` para transação independente

#### Habilitar Async

**Arquivo:** `backend/src/main/java/sgc/SgcApplication.java`

**Adicionar:**
```java
@SpringBootApplication
@EnableAsync  // ← ADICIONAR
public class SgcApplication {
    public static void main(String[] args) {
        SpringApplication.run(SgcApplication.class, args);
    }
}
```

#### Migrar Listeners por Módulo

**Executar para cada módulo:**
```bash
# Buscar listeners no módulo
grep -r "@EventListener" backend/src/main/java/sgc/{modulo}/ --include="*.java"

# Substituir anotação
sed -i 's/@EventListener/@ApplicationModuleListener/g' backend/src/main/java/sgc/{modulo}/internal/listeners/*.java

# Adicionar @Async manualmente (ou via script)
```

**Módulos com listeners:**
- `alerta` - Escuta eventos de processo
- `notificacao` - Escuta múltiplos eventos
- `subprocesso` - Escuta eventos de processo
- Outros

**Critério de Aceite:**
- ✅ `@EnableAsync` adicionado
- ✅ Listeners migrados para `@ApplicationModuleListener`
- ✅ Testes passam

---

### Tarefa 6: Validação Final

#### Executar Teste de Dependências Cíclicas

```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```

**Resultado Esperado:** ✅ **PASSAR**

Se falhar:
1. Revisar imports em módulos refatorados
2. Verificar `package-info.java` (allowedDependencies)
3. Documentar ciclos remanescentes

#### Executar Suite Completa

```bash
# Clean build
./gradlew clean :backend:build

# Todos os testes
./gradlew :backend:test

# Testes de integração
./gradlew :backend:test --tests sgc.integracao.*
```

#### Validar Eventos Persistidos

**Executar aplicação e triggerar evento:**
```bash
./gradlew :backend:bootRun
```

**Em outro terminal, criar processo via API:**
```bash
curl -X POST http://localhost:8080/api/processos \
  -H "Content-Type: application/json" \
  -d '{"nome": "Teste", "unidadeCodigo": 1}'
```

**Verificar tabela EVENT_PUBLICATION:**
```sql
SELECT * FROM EVENT_PUBLICATION;
```

**Deve conter registro de `EventoProcessoIniciado`.**

**Critério de Aceite:**
- ✅ Teste de ciclos passa
- ✅ Build completo sem erros
- ✅ Testes passam
- ✅ Eventos são persistidos

---

## Comandos de Verificação

### Verificar ausência de ciclos
```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```

### Buscar acessos a internal/ de outros módulos
```bash
for modulo in processo subprocesso mapa atividade; do
    echo "=== Verificando $modulo ==="
    grep -r "import sgc.$modulo.internal" backend/src/main/java/ --exclude-dir=$modulo
done
```

### Listar listeners migrados
```bash
grep -r "@ApplicationModuleListener" backend/src/main/java/sgc/ --include="*.java"
```

### Verificar Event Publication Registry
```bash
# Após executar aplicação
echo "SELECT COUNT(*) FROM EVENT_PUBLICATION;" | ./gradlew :backend:bootRun
```

---

## Critérios de Aceite da Sprint

### Obrigatórios
- ✅ Ciclo `mapa ↔ atividade` quebrado
- ✅ Ciclo `processo ↔ subprocesso` quebrado
- ✅ 4 módulos refatorados: `processo`, `subprocesso`, `mapa`, `atividade`
- ✅ Eventos movidos para `api/eventos/`
- ✅ Event Publication Registry configurado e funcionando
- ✅ Listeners migrados para `@ApplicationModuleListener`
- ✅ Teste `naoDevemExistirDependenciasCiclicas()` **PASSA**
- ✅ Build completo sem erros
- ✅ Todos os testes passam

### Opcionais
- ⚙️ Configurar retry de eventos falhados
- ⚙️ Dashboard de eventos pendentes/completados
- ⚙️ Testes de integração usando `@ApplicationModuleTest`

---

## Próximos Passos

Após concluir esta sprint:
1. ✅ Validar que sistema funciona sem regressões
2. ✅ Revisar performance de eventos assíncronos
3. ✅ Documentar lições aprendidas da quebra de ciclos
4. ➡️ Prosseguir para **Sprint 4: Módulos Supporting e Finalização**

---

## Diretrizes para Agentes de IA

### Regras de Ouro
1. **Atenção máxima** - Esta sprint é crítica e complexa
2. **Testar constantemente** - Após cada mudança significativa
3. **Documentar ciclos** - Antes de quebrar, entender o motivo
4. **Commits granulares** - Um commit por módulo ou quebra de ciclo
5. **Validar funcionalidade** - Executar aplicação e testar manualmente

### Checklist Geral
- [ ] Analisar e documentar dependências cíclicas
- [ ] Definir estratégia de quebra para cada ciclo
- [ ] Refatorar módulos para estrutura Spring Modulith
- [ ] Mover eventos para `api/eventos/`
- [ ] Remover acessos diretos a repositórios entre módulos
- [ ] Configurar Event Publication Registry
- [ ] Migrar listeners para `@ApplicationModuleListener`
- [ ] Validar que teste de ciclos passa
- [ ] Validar que aplicação funciona corretamente

### Comandos Críticos
```bash
# Após cada módulo refatorado
./gradlew :backend:compileJava
./gradlew :backend:test

# Após quebrar um ciclo
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas

# Ao final
./gradlew clean :backend:build
./gradlew :backend:test
./gradlew :backend:bootRun  # Validar manualmente
```

### Troubleshooting

**Problema:** Teste de ciclos ainda falha
- **Causa:** Imports diretos remanescentes
- **Solução:** Usar grep para encontrar todos os imports problemáticos

**Problema:** Eventos não são persistidos
- **Causa:** Configuração incorreta
- **Solução:** Verificar `application.yml` e dependência `spring-modulith-events-jpa`

**Problema:** Listeners não são executados
- **Causa:** `@EnableAsync` não configurado
- **Solução:** Adicionar em `SgcApplication`

**Problema:** Testes de integração falhando
- **Causa:** Eventos assíncronos não completam antes de assertions
- **Solução:** Usar `@Async(org.springframework.modulith.test.EnableScenarios)` ou `Awaitility`

---

**Status Sprint 3**: 🟡 Pronto para Execução  
**Duração Estimada**: 2 semanas  
**Complexidade**: **Alta** 🔴  
**Dependências**: Sprints 1 e 2 concluídas  
**Riscos**: Alto - Requer atenção máxima
