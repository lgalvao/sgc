# Sprint 1: Setup Inicial e Prova de Conceito

**Baseado em:** `modulith-report.md` - Seção 6.2 (Sprint 1)

## Contexto do Projeto SGC

### Arquitetura Atual
O SGC é um **Modular Monolith** desenvolvido com:
- **Backend:** Java 21, Spring Boot 4.0.1, Hibernate/JPA
- **Build:** Gradle 9.2.1 (Gradle Wrapper)
- **Testes:** JUnit 5, Mockito, AssertJ, Spring Boot Test, ArchUnit
- **Base de Dados:** PostgreSQL (produção), H2 (testes)

### Estrutura de Módulos
O backend possui 10 módulos em `backend/src/main/java/sgc/`:
- **Core Domain:** `processo`, `subprocesso`, `mapa`, `atividade`
- **Supporting:** `alerta`, `notificacao`, `analise`, `painel`
- **Integration:** `sgrh` (integração com sistema de RH)
- **Foundation:** `unidade` (estrutura organizacional)
- **Common:** `comum` (componentes transversais)

### Problemas Arquiteturais Identificados
- ❌ Dependências cíclicas entre módulos
- ❌ Ausência de enforcement de limites em tempo de compilação
- ❌ Eventos síncronos por padrão (risco transacional)
- ❌ Acoplamento implícito (qualquer classe `public` é acessível)

### Referências
- Arquitetura: `backend/README.md`
- Convenções: `AGENTS.md` e `/regras/backend-padroes.md`
- Análise completa: `modulith/modulith-report.md`

---

## Objetivo da Sprint

Implementar a **infraestrutura básica** do Spring Modulith e validar os benefícios com uma **Prova de Conceito (PoC)** em 2 módulos de baixa complexidade.

### Entregáveis
1. ✅ Dependências Spring Modulith configuradas
2. ✅ Teste de verificação de estrutura modular funcionando
3. ✅ 2 módulos refatorados para estrutura Spring Modulith (`alerta` e `analise`)
4. ✅ Build e testes passando sem regressões
5. ✅ Documentação inicial gerada automaticamente

---

## Tarefas Detalhadas

### Tarefa 1: Adicionar Dependências Spring Modulith

**Arquivo:** `backend/build.gradle.kts`

**Ação:** Adicionar dependências na seção `dependencies`:

```kotlin
dependencies {
    // ... dependências existentes ...
    
    // Spring Modulith - Core
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    
    // Spring Modulith - Observability (opcional, mas recomendado)
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    
    // Spring Modulith - Testes
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    
    // Spring Modulith - Documentação
    testImplementation("org.springframework.modulith:spring-modulith-docs")
}
```

**Versão:** O Spring Modulith usa BOM do Spring Boot, portanto não precisa especificar versão.

**Comando de Verificação:**
```bash
./gradlew :backend:dependencies | grep modulith
```

**Critério de Aceite:**
- ✅ Gradle resolve as dependências sem erros
- ✅ Build completo funciona: `./gradlew :backend:build`

---

### Tarefa 2: Configurar Spring Modulith no application.yml

**Arquivo:** `backend/src/main/resources/application.yml`

**Ação:** Adicionar configurações do Spring Modulith:

```yaml
spring:
  modulith:
    # Verificação de estrutura modular na inicialização
    verification:
      enabled: true
    # Event Publication Registry - persiste eventos para garantir entrega
    events:
      externalization:
        enabled: true
      completion-mode: on-completion
    # Observability - endpoints de monitoramento
    actuator:
      enabled: true

# Habilitar endpoint /actuator/modulith (opcional)
management:
  endpoints:
    web:
      exposure:
        include: health,info,modulith
```

**Notas:**
- `verification.enabled: true` → Falha na inicialização se houver violações de módulos
- `events.externalization.enabled: true` → Habilita Event Publication Registry
- Durante a PoC, você pode querer `verification.enabled: false` para não bloquear a inicialização

**Critério de Aceite:**
- ✅ Aplicação inicia sem erros
- ✅ Log mostra verificação de módulos (se habilitada)

---

### Tarefa 3: Criar Teste de Verificação de Módulos

**Arquivo:** `backend/src/test/java/sgc/ModulithStructureTest.java`

**Ação:** Criar teste que verifica a estrutura modular:

```java
package sgc;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Teste de estrutura modular do SGC usando Spring Modulith.
 * 
 * Valida:
 * - Detecção correta de módulos
 * - Ausência de dependências cíclicas
 * - Respeito aos limites de módulos (api/ e internal/)
 */
class ModulithStructureTest {
    
    private final ApplicationModules modules = ApplicationModules.of(SgcApplication.class);
    
    @Test
    void deveDetectarModulosCorretamente() {
        // Lista todos os módulos detectados
        System.out.println("=== Módulos Detectados ===");
        modules.forEach(module -> {
            System.out.println("Módulo: " + module.getName());
            System.out.println("  - Pacote base: " + module.getBasePackage());
            System.out.println("  - Dependências: " + module.getDependencies());
        });
        
        // Verifica que ao menos os módulos principais foram detectados
        assertThatCode(() -> modules.verify())
            .as("Estrutura de módulos deve ser válida")
            .doesNotThrowAnyException();
    }
    
    @Test
    void naoDevemExistirDependenciasCiclicas() {
        // Este teste vai FALHAR inicialmente, pois existem ciclos
        // Durante a Sprint 1, podemos aceitar falha aqui
        // Durante Sprints 2-4, vamos quebrar os ciclos
        
        assertThatCode(() -> modules.verify())
            .as("Não devem existir dependências cíclicas entre módulos")
            .doesNotThrowAnyException();
    }
    
    @Test
    void gerarDocumentacaoDosModulos() {
        // Gera documentação em target/spring-modulith-docs
        new Documenter(modules)
            .writeDocumentation()           // Cria index HTML
            .writeIndividualModulesAsPlantUml()  // Diagrama de cada módulo
            .writeModulesAsPlantUml();      // Diagrama geral
        
        System.out.println("Documentação gerada em: backend/build/spring-modulith-docs/");
    }
}
```

**Comando de Execução:**
```bash
./gradlew :backend:test --tests ModulithStructureTest
```

**Critério de Aceite:**
- ✅ Teste `deveDetectarModulosCorretamente()` passa
- ✅ Teste `gerarDocumentacaoDosModulos()` gera arquivos em `backend/build/spring-modulith-docs/`
- ⚠️ Teste `naoDevemExistirDependenciasCiclicas()` pode falhar (esperado nesta sprint)

---

### Tarefa 4: Refatorar Módulo `alerta` para Estrutura Spring Modulith

**Estrutura Atual:**
```
sgc/alerta/
├── AlertaController.java
├── AlertaService.java
├── README.md
├── dto/
│   ├── AlertaDto.java
│   └── AlertaMapper.java
├── erros/
│   └── ErroAlerta.java
└── model/
    ├── Alerta.java
    ├── AlertaRepo.java
    ├── AlertaUsuario.java
    ├── AlertaUsuarioRepo.java
    └── TipoAlerta.java
```

**Estrutura Alvo (Spring Modulith):**
```
sgc/alerta/
├── AlertaFacade.java               # API pública (se necessário)
├── package-info.java               # Metadados do módulo
├── api/                            # API pública exportada
│   ├── AlertaDto.java              # DTO exposto a outros módulos
│   └── AlertaEvento.java           # Eventos publicados (se houver)
└── internal/                       # Implementação interna (não acessível)
    ├── AlertaController.java       # REST controller
    ├── AlertaService.java          # Lógica de negócio
    ├── AlertaMapper.java           # Mapeamento interno
    ├── model/                      # Modelo de dados
    │   ├── Alerta.java
    │   ├── AlertaRepo.java
    │   ├── AlertaUsuario.java
    │   ├── AlertaUsuarioRepo.java
    │   └── TipoAlerta.java
    └── erros/
        └── ErroAlerta.java
```

#### Passo 4.1: Criar `package-info.java`

**Arquivo:** `backend/src/main/java/sgc/alerta/package-info.java`

```java
/**
 * Módulo de Gestão de Alertas do SGC.
 * 
 * <p>Responsável por criar, gerenciar e notificar alertas para usuários
 * relacionados a processos, subprocessos e outras entidades do sistema.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.alerta.api.AlertaDto} - DTO para transferência de dados de alertas</li>
 *   <li>{@link sgc.alerta.AlertaService} - Facade para operações de alertas</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>sgrh - Para obter informações de usuários</li>
 *   <li>comum - Para componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <p>Nenhum evento é publicado por este módulo no momento.</p>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>EventoProcessoIniciado - Cria alertas ao iniciar processo</li>
 *   <li>EventoSubprocessoCriado - Cria alertas para novos subprocessos</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Gestão de Alertas",
    allowedDependencies = {"sgrh", "comum"}
)
package sgc.alerta;
```

#### Passo 4.2: Criar Pacote `api/` e Mover DTOs

**Criar diretório:**
```bash
mkdir -p backend/src/main/java/sgc/alerta/api
```

**Mover arquivo:**
```bash
git mv backend/src/main/java/sgc/alerta/dto/AlertaDto.java backend/src/main/java/sgc/alerta/api/
```

**Atualizar imports** em todos os arquivos que referenciam `AlertaDto`:
```bash
# Buscar referências
grep -r "sgc.alerta.dto.AlertaDto" backend/src/main/java/
grep -r "sgc.alerta.dto.AlertaDto" backend/src/test/java/

# Substituir manualmente ou usar sed:
find backend/src/main/java -name "*.java" -exec sed -i 's/sgc\.alerta\.dto\.AlertaDto/sgc.alerta.api.AlertaDto/g' {} +
find backend/src/test/java -name "*.java" -exec sed -i 's/sgc\.alerta\.dto\.AlertaDto/sgc.alerta.api.AlertaDto/g' {} +
```

#### Passo 4.3: Criar Pacote `internal/` e Mover Implementações

**Criar diretórios:**
```bash
mkdir -p backend/src/main/java/sgc/alerta/internal
mkdir -p backend/src/main/java/sgc/alerta/internal/model
mkdir -p backend/src/main/java/sgc/alerta/internal/erros
```

**Mover arquivos:**
```bash
# Controller
git mv backend/src/main/java/sgc/alerta/AlertaController.java backend/src/main/java/sgc/alerta/internal/

# Mapper (se não foi movido)
git mv backend/src/main/java/sgc/alerta/dto/AlertaMapper.java backend/src/main/java/sgc/alerta/internal/

# Model
git mv backend/src/main/java/sgc/alerta/model/* backend/src/main/java/sgc/alerta/internal/model/

# Erros
git mv backend/src/main/java/sgc/alerta/erros/* backend/src/main/java/sgc/alerta/internal/erros/

# Remover diretórios vazios
rmdir backend/src/main/java/sgc/alerta/dto
rmdir backend/src/main/java/sgc/alerta/model
rmdir backend/src/main/java/sgc/alerta/erros
```

**Atualizar imports:**
```bash
# AlertaController
sed -i 's/package sgc.alerta;/package sgc.alerta.internal;/g' backend/src/main/java/sgc/alerta/internal/AlertaController.java

# AlertaMapper
sed -i 's/package sgc.alerta.dto;/package sgc.alerta.internal;/g' backend/src/main/java/sgc/alerta/internal/AlertaMapper.java

# Model
find backend/src/main/java/sgc/alerta/internal/model -name "*.java" -exec sed -i 's/package sgc.alerta.model;/package sgc.alerta.internal.model;/g' {} +

# Erros
find backend/src/main/java/sgc/alerta/internal/erros -name "*.java" -exec sed -i 's/package sgc.alerta.erros;/package sgc.alerta.internal.erros;/g' {} +

# Atualizar referências em toda a base
find backend/src/main/java -name "*.java" -exec sed -i 's/sgc\.alerta\.AlertaController/sgc.alerta.internal.AlertaController/g' {} +
find backend/src/main/java -name "*.java" -exec sed -i 's/sgc\.alerta\.model\./sgc.alerta.internal.model./g' {} +
find backend/src/main/java -name "*.java" -exec sed -i 's/sgc\.alerta\.erros\./sgc.alerta.internal.erros./g' {} +
find backend/src/test/java -name "*.java" -exec sed -i 's/sgc\.alerta\.model\./sgc.alerta.internal.model./g' {} +
find backend/src/test/java -name "*.java" -exec sed -i 's/sgc\.alerta\.erros\./sgc.alerta.internal.erros./g' {} +
```

#### Passo 4.4: Decidir sobre AlertaService

**AlertaService** deve permanecer no **pacote raiz** (`sgc.alerta`) pois é a **API pública** do módulo.

Por convenção Spring Modulith:
- Classes no pacote raiz (`sgc.alerta.*`) são **públicas** por padrão
- Classes em `sgc.alerta.api.*` são explicitamente públicas
- Classes em `sgc.alerta.internal.*` são **privadas** ao módulo

**Não mover** `AlertaService.java`. Ele deve ficar em `sgc.alerta.AlertaService`.

**Critério de Aceite:**
- ✅ Estrutura de diretórios criada corretamente
- ✅ Build compila sem erros: `./gradlew :backend:compileJava`
- ✅ Testes passam: `./gradlew :backend:test`

---

### Tarefa 5: Refatorar Módulo `analise` para Estrutura Spring Modulith

**Aplicar os mesmos passos da Tarefa 4** para o módulo `analise`:

1. Criar `package-info.java`
2. Criar pacote `api/` e mover DTOs públicos
3. Criar pacote `internal/` e mover implementações
4. Manter Service no pacote raiz
5. Atualizar todos os imports

**Estrutura Alvo:**
```
sgc/analise/
├── AnaliseService.java             # API pública
├── package-info.java
├── api/
│   └── AnaliseDto.java
└── internal/
    ├── AnaliseController.java
    ├── AnaliseMapper.java
    ├── model/
    │   ├── Analise.java
    │   └── AnaliseRepo.java
    └── erros/
        └── ErroAnalise.java
```

**package-info.java:**
```java
/**
 * Módulo de Auditoria e Análise de Processos do SGC.
 * 
 * <p>Responsável por realizar auditorias, revisões e análises
 * de processos e subprocessos do sistema.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Auditoria e Análise",
    allowedDependencies = {"processo", "subprocesso", "comum"}
)
package sgc.analise;
```

**Critério de Aceite:**
- ✅ Estrutura criada seguindo padrão do módulo `alerta`
- ✅ Build e testes passam

---

### Tarefa 6: Validar Build e Testes Completos

**Comandos:**

```bash
# Clean build completo
./gradlew clean :backend:build

# Executar todos os testes
./gradlew :backend:test

# Executar teste de estrutura modular
./gradlew :backend:test --tests ModulithStructureTest

# Verificar se existem erros de compilação
./gradlew :backend:compileJava :backend:compileTestJava
```

**Critério de Aceite:**
- ✅ Build completo sem erros
- ✅ Todos os testes passam (ou apenas testes não relacionados falham)
- ✅ Teste `ModulithStructureTest.deveDetectarModulosCorretamente()` passa
- ✅ Documentação gerada em `backend/build/spring-modulith-docs/`

---

### Tarefa 7: Atualizar Documentação

#### Atualizar README.md do Backend

**Arquivo:** `backend/README.md`

**Adicionar seção:**

```markdown
## Spring Modulith

O projeto SGC adota **Spring Modulith** para garantir limites arquiteturais entre módulos.

### Estrutura de Módulos

Cada módulo segue a convenção:
- **Pacote raiz** (`sgc.{modulo}.*`): API pública do módulo
- **Pacote `api/`** (`sgc.{modulo}.api.*`): DTOs e contratos exportados
- **Pacote `internal/`** (`sgc.{modulo}.internal.*`): Implementação privada

### Verificação de Módulos

Execute o teste de estrutura:
```bash
./gradlew :backend:test --tests ModulithStructureTest
```

### Documentação Automatizada

A documentação da arquitetura é gerada automaticamente:
```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

Veja os diagramas em: `backend/build/spring-modulith-docs/`
```

#### Atualizar README.md do Módulo `alerta`

**Arquivo:** `backend/src/main/java/sgc/alerta/README.md`

**Adicionar seção:**

```markdown
## Estrutura Spring Modulith

Este módulo segue a convenção Spring Modulith:

- **API Pública:**
  - `AlertaService` (pacote raiz)
  - `AlertaDto` (em `api/`)

- **Implementação Interna:**
  - `AlertaController`
  - `AlertaMapper`
  - Model e Repositories
  - Erros customizados

Outros módulos **não devem** acessar classes em `internal/`.
```

**Fazer o mesmo para o módulo `analise`.**

---

## Comandos de Verificação

### Listar módulos detectados
```bash
./gradlew :backend:test --tests ModulithStructureTest.deveDetectarModulosCorretamente
```

### Verificar dependências circulares
```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```
**Nota:** Este teste pode falhar nesta sprint. Isso é esperado.

### Gerar documentação
```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

### Verificar se não há referências a pacotes internos de outros módulos
```bash
# Exemplo: nenhum módulo deve importar sgc.alerta.internal.*
grep -r "import sgc.alerta.internal" backend/src/main/java/ --exclude-dir=alerta
```

---

## Critérios de Aceite da Sprint

### Obrigatórios
- ✅ Dependências Spring Modulith adicionadas e resolvidas
- ✅ Configuração básica em `application.yml`
- ✅ Teste `ModulithStructureTest` criado e detecta módulos
- ✅ Módulos `alerta` e `analise` refatorados para estrutura Spring Modulith
- ✅ Build completo sem erros: `./gradlew clean :backend:build`
- ✅ Todos os testes passam: `./gradlew :backend:test`
- ✅ Documentação gerada: `backend/build/spring-modulith-docs/`

### Opcionais (Nice to Have)
- ⚙️ Teste de dependências cíclicas passa (pode falhar, será resolvido nas próximas sprints)
- ⚙️ Endpoint `/actuator/modulith` acessível

---

## Problemas Esperados e Soluções

### Problema 1: Testes Falhando por Imports

**Sintoma:** Testes não compilam após mover classes para `internal/`

**Solução:**
- Atualizar imports em arquivos de teste
- Considerar mover testes para `sgc.{modulo}.internal` se testam classes internas
- Ou criar testes de integração via API pública

### Problema 2: Dependências Cíclicas Detectadas

**Sintoma:** `ModulithStructureTest.naoDevemExistirDependenciasCiclicas()` falha

**Solução:**
- **Esperado nesta sprint**
- Documentar os ciclos detectados
- Serão resolvidos nas Sprints 2-4

### Problema 3: Aplicação Não Inicia com `verification.enabled: true`

**Sintoma:** Erro na inicialização da aplicação

**Solução:**
- Temporariamente, configurar `spring.modulith.verification.enabled: false`
- Ou adicionar `@org.springframework.modulith.ApplicationModule(allowedDependencies = "*")` em módulos problemáticos
- Gradualmente habilitar verificação após refatorar módulos

---

## Próximos Passos

Após concluir esta sprint:
1. ✅ Validar que PoC trouxe os benefícios esperados
2. ✅ Documentar lições aprendidas
3. ➡️ Prosseguir para **Sprint 2: Módulos Foundation e Integration**

---

## Diretrizes para Agentes de IA

### Regras de Ouro
1. **Mudanças Incrementais:** Refatorar um módulo por vez
2. **Testar Continuamente:** Executar testes após cada mudança
3. **Commits Pequenos:** Um commit por módulo refatorado
4. **Preservar Funcionalidade:** Nenhuma mudança de comportamento

### Checklist por Módulo
- [ ] Criar `package-info.java`
- [ ] Criar pacote `api/` e mover DTOs
- [ ] Criar pacote `internal/` e mover implementações
- [ ] Atualizar todos os imports
- [ ] Executar `./gradlew :backend:compileJava`
- [ ] Executar `./gradlew :backend:test`
- [ ] Commit com mensagem: `refactor(modulo-X): adotar estrutura Spring Modulith`

### Comandos Essenciais
```bash
# Build incremental
./gradlew :backend:compileJava

# Testes rápidos (apenas módulo específico, se aplicável)
./gradlew :backend:test --tests sgc.alerta.*

# Verificar estrutura
./gradlew :backend:test --tests ModulithStructureTest
```

---

**Status Sprint 1**: 🟡 Pronto para Execução  
**Duração Estimada**: 1 semana  
**Complexidade**: Baixa-Média
