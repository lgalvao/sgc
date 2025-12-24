# Sprint 4: Módulos Supporting e Finalização

**Baseado em:** `modulith-report.md` - Seção 6.2 (Sprint 4)

## Contexto

Esta é a **sprint final** da adoção do Spring Modulith. Foca em:
- Refatorar módulos **supporting** restantes
- Consolidar padrões estabelecidos
- Finalizar migração de eventos
- Validar métricas de sucesso
- Documentação completa

### Módulos Alvo
1. **`notificacao`** - Orquestração de notificações e eventos
2. **`painel`** - Dashboards e painéis (read-only)

### Status do Projeto
Após Sprint 3:
- ✅ 8 módulos refatorados
- ✅ Todos os ciclos quebrados
- ✅ Event Publication Registry funcionando
- ✅ Teste `naoDevemExistirDependenciasCiclicas()` passando

---

## Objetivo da Sprint

Concluir a adoção do Spring Modulith refatorando os **módulos supporting** restantes e consolidando toda a implementação.

### Entregáveis
1. ✅ Módulos `notificacao` e `painel` refatorados
2. ✅ 100% dos módulos com estrutura Spring Modulith
3. ✅ Todos os listeners migrados para `@ApplicationModuleListener`
4. ✅ Documentação completa atualizada
5. ✅ Testes de integração usando `@ApplicationModuleTest`
6. ✅ Diagramas e documentação gerados e publicados
7. ✅ Validação de métricas de sucesso
8. ✅ Configuração de `verification.enabled: true` em produção

---

## Tarefas Detalhadas

### Tarefa 1: Refatorar Módulo `notificacao`

#### Contexto do Módulo

O módulo **`notificacao`** é um módulo **supporting** que:
- Orquestra envio de notificações (email, SMS, push)
- Escuta **múltiplos eventos** de domínio
- Publica eventos de notificação enviada

**Características:**
- Alto acoplamento via eventos (escuta muitos módulos)
- Sem dependências diretas (apenas via eventos)
- Candidato ideal para `@ApplicationModuleListener` assíncrono

**Localização:** `backend/src/main/java/sgc/notificacao/`

#### Estrutura Atual (exemplo)
```
sgc/notificacao/
├── NotificacaoController.java
├── NotificacaoService.java
├── README.md
├── dto/
│   ├── NotificacaoDto.java
│   └── NotificacaoMapper.java
├── listeners/                      # Múltiplos listeners
│   ├── ProcessoListener.java
│   ├── SubprocessoListener.java
│   └── AlertaListener.java
├── model/
│   ├── Notificacao.java
│   ├── NotificacaoRepo.java
│   └── TipoNotificacao.java
└── erros/
    └── ErroNotificacao.java
```

#### Estrutura Alvo
```
sgc/notificacao/
├── NotificacaoService.java         # API pública
├── package-info.java
├── api/
│   ├── NotificacaoDto.java
│   └── eventos/
│       └── EventoNotificacaoEnviada.java
└── internal/
    ├── NotificacaoController.java
    ├── NotificacaoMapper.java
    ├── listeners/                  # Todos os listeners aqui
    │   ├── ProcessoListener.java
    │   ├── SubprocessoListener.java
    │   └── AlertaListener.java
    ├── model/
    │   ├── Notificacao.java
    │   ├── NotificacaoRepo.java
    │   └── TipoNotificacao.java
    └── erros/
        └── ErroNotificacao.java
```

#### Passo 1.1: Criar package-info.java

**Arquivo:** `backend/src/main/java/sgc/notificacao/package-info.java`

```java
/**
 * Módulo de Orquestração de Notificações.
 * 
 * <p>Responsável por gerenciar e enviar notificações via diferentes canais
 * (email, SMS, push) em resposta a eventos do sistema.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.notificacao.NotificacaoService} - Facade de notificações</li>
 *   <li>{@link sgc.notificacao.api.NotificacaoDto} - DTO de notificação</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>processo::api.eventos - Eventos de processo</li>
 *   <li>subprocesso::api.eventos - Eventos de subprocesso</li>
 *   <li>alerta - Para notificar sobre alertas</li>
 *   <li>sgrh - Para informações de usuários</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>EventoNotificacaoEnviada - Quando notificação é enviada com sucesso</li>
 *   <li>EventoNotificacaoFalhou - Quando envio falha</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>EventoProcessoIniciado - Notifica início de processo</li>
 *   <li>EventoProcessoFinalizado - Notifica conclusão</li>
 *   <li>EventoSubprocessoCriado - Notifica criação de subprocesso</li>
 *   <li>EventoAlertaCriado - Notifica alertas</li>
 * </ul>
 * 
 * <h2>Nota sobre Assincronicidade</h2>
 * <p>Todos os listeners deste módulo usam {@code @ApplicationModuleListener}
 * com {@code @Async}, garantindo que falhas de notificação não afetem
 * transações de outros módulos.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Orquestração de Notificações",
    allowedDependencies = {"processo::api.eventos", "subprocesso::api.eventos", "alerta", "sgrh", "comum"}
)
package sgc.notificacao;
```

#### Passo 1.2: Executar Refatoração

**Criar estrutura:**
```bash
mkdir -p backend/src/main/java/sgc/notificacao/api
mkdir -p backend/src/main/java/sgc/notificacao/api/eventos
mkdir -p backend/src/main/java/sgc/notificacao/internal/model
mkdir -p backend/src/main/java/sgc/notificacao/internal/erros
mkdir -p backend/src/main/java/sgc/notificacao/internal/listeners
```

**Mover arquivos:**
```bash
# DTOs para api/
git mv backend/src/main/java/sgc/notificacao/dto/NotificacaoDto.java backend/src/main/java/sgc/notificacao/api/

# Eventos (se houver)
if [ -d backend/src/main/java/sgc/notificacao/eventos ]; then
    git mv backend/src/main/java/sgc/notificacao/eventos/* backend/src/main/java/sgc/notificacao/api/eventos/
fi

# Controller e Mapper
git mv backend/src/main/java/sgc/notificacao/NotificacaoController.java backend/src/main/java/sgc/notificacao/internal/
git mv backend/src/main/java/sgc/notificacao/dto/NotificacaoMapper.java backend/src/main/java/sgc/notificacao/internal/

# Listeners (se já não estiverem em subdiretório)
if [ -d backend/src/main/java/sgc/notificacao/listeners ]; then
    git mv backend/src/main/java/sgc/notificacao/listeners/* backend/src/main/java/sgc/notificacao/internal/listeners/
fi

# Model
git mv backend/src/main/java/sgc/notificacao/model/* backend/src/main/java/sgc/notificacao/internal/model/

# Erros
git mv backend/src/main/java/sgc/notificacao/erros/* backend/src/main/java/sgc/notificacao/internal/erros/

# Remover diretórios vazios
rmdir backend/src/main/java/sgc/notificacao/dto 2>/dev/null
rmdir backend/src/main/java/sgc/notificacao/model 2>/dev/null
rmdir backend/src/main/java/sgc/notificacao/erros 2>/dev/null
rmdir backend/src/main/java/sgc/notificacao/listeners 2>/dev/null
rmdir backend/src/main/java/sgc/notificacao/eventos 2>/dev/null
```

**Atualizar imports:**
```bash
# DTOs
find backend/src -name "*.java" -exec sed -i 's/sgc\.notificacao\.dto\.NotificacaoDto/sgc.notificacao.api.NotificacaoDto/g' {} +

# Model
find backend/src -name "*.java" -exec sed -i 's/sgc\.notificacao\.model\./sgc.notificacao.internal.model./g' {} +

# Erros
find backend/src -name "*.java" -exec sed -i 's/sgc\.notificacao\.erros\./sgc.notificacao.internal.erros./g' {} +

# Controller
find backend/src -name "*.java" -exec sed -i 's/sgc\.notificacao\.NotificacaoController/sgc.notificacao.internal.NotificacaoController/g' {} +

# Mapper
find backend/src -name "*.java" -exec sed -i 's/sgc\.notificacao\.dto\.NotificacaoMapper/sgc.notificacao.internal.NotificacaoMapper/g' {} +

# Listeners
find backend/src -name "*.java" -exec sed -i 's/sgc\.notificacao\.listeners\./sgc.notificacao.internal.listeners./g' {} +
```

**Atualizar declarações de package:**
```bash
# API
sed -i 's/package sgc.notificacao.dto;/package sgc.notificacao.api;/g' backend/src/main/java/sgc/notificacao/api/NotificacaoDto.java

# Internal
sed -i 's/package sgc.notificacao;/package sgc.notificacao.internal;/g' backend/src/main/java/sgc/notificacao/internal/NotificacaoController.java
sed -i 's/package sgc.notificacao.dto;/package sgc.notificacao.internal;/g' backend/src/main/java/sgc/notificacao/internal/NotificacaoMapper.java

# Model
find backend/src/main/java/sgc/notificacao/internal/model -name "*.java" -exec sed -i 's/package sgc.notificacao.model;/package sgc.notificacao.internal.model;/g' {} +

# Erros
find backend/src/main/java/sgc/notificacao/internal/erros -name "*.java" -exec sed -i 's/package sgc.notificacao.erros;/package sgc.notificacao.internal.erros;/g' {} +

# Listeners
find backend/src/main/java/sgc/notificacao/internal/listeners -name "*.java" -exec sed -i 's/package sgc.notificacao.listeners;/package sgc.notificacao.internal.listeners;/g' {} +
```

#### Passo 1.3: Garantir Listeners Assíncronos

**Verificar que todos os listeners usam `@ApplicationModuleListener`:**
```bash
grep -r "@EventListener" backend/src/main/java/sgc/notificacao/ --include="*.java"
```

**Se houver `@EventListener`, migrar para `@ApplicationModuleListener`.**

**Exemplo de listener:**
```java
package sgc.notificacao.internal.listeners;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.api.eventos.EventoProcessoIniciado;
import sgc.notificacao.NotificacaoService;

@Component
public class ProcessoListener {
    
    private final NotificacaoService notificacaoService;
    
    public ProcessoListener(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }
    
    @ApplicationModuleListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        notificacaoService.notificarInicioProcesso(evento.getCodigoProcesso());
    }
}
```

**Critério de Aceite:**
- ✅ Estrutura criada
- ✅ Todos os listeners usam `@ApplicationModuleListener`
- ✅ Build compila: `./gradlew :backend:compileJava`
- ✅ Testes passam: `./gradlew :backend:test`

---

### Tarefa 2: Refatorar Módulo `painel`

#### Contexto do Módulo

O módulo **`painel`** é um módulo **supporting** que:
- Fornece dashboards e painéis de visualização
- **Read-only** (não modifica dados de domínio)
- Pode consultar dados de múltiplos módulos

**Características:**
- Sem lógica de escrita
- Pode ter dependências de leitura para vários módulos
- Simples de refatorar

**Localização:** `backend/src/main/java/sgc/painel/`

#### Estrutura Alvo
```
sgc/painel/
├── PainelService.java              # API pública
├── package-info.java
├── api/
│   ├── DashboardDto.java
│   └── EstatisticasDto.java
└── internal/
    ├── PainelController.java
    ├── PainelMapper.java
    ├── queries/                    # Queries de leitura
    │   ├── ProcessoQuery.java
    │   └── SubprocessoQuery.java
    └── model/                      # Se houver entidades próprias
        └── ...
```

#### Passo 2.1: Criar package-info.java

**Arquivo:** `backend/src/main/java/sgc/painel/package-info.java`

```java
/**
 * Módulo de Dashboards e Painéis de Visualização.
 * 
 * <p>Responsável por agregar e apresentar informações de múltiplos módulos
 * em dashboards e painéis de visualização. Este módulo é read-only,
 * não modificando dados de domínio.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.painel.PainelService} - Facade para dashboards</li>
 *   <li>{@link sgc.painel.api.DashboardDto} - DTO de dashboard</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>processo - Para estatísticas de processos</li>
 *   <li>subprocesso - Para estatísticas de subprocessos</li>
 *   <li>mapa - Para estatísticas de mapas</li>
 *   <li>atividade - Para estatísticas de atividades</li>
 *   <li>unidade - Para filtros organizacionais</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos</h2>
 * <p>Este módulo não publica nem consome eventos. É puramente read-only.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Dashboards e Painéis",
    allowedDependencies = {"processo", "subprocesso", "mapa", "atividade", "unidade", "comum"}
)
package sgc.painel;
```

#### Passo 2.2: Executar Refatoração

**Aplicar mesmos passos da Tarefa 1:**
- Criar estrutura `api/` e `internal/`
- Mover DTOs para `api/`
- Mover implementações para `internal/`
- Atualizar imports
- Validar build

**Comandos (resumido):**
```bash
# Criar estrutura
mkdir -p backend/src/main/java/sgc/painel/api
mkdir -p backend/src/main/java/sgc/painel/internal

# Mover e atualizar (adaptar conforme estrutura real)
# ... seguir padrão estabelecido ...

# Compilar e testar
./gradlew :backend:compileJava
./gradlew :backend:test
```

**Critério de Aceite:**
- ✅ Estrutura Spring Modulith aplicada
- ✅ Build e testes passam

---

### Tarefa 3: Criar Testes de Integração Modulares

**Objetivo:** Usar `@ApplicationModuleTest` para testar módulos isoladamente.

#### Criar Teste para Módulo `alerta`

**Arquivo:** `backend/src/test/java/sgc/alerta/AlertaModuleTest.java`

```java
package sgc.alerta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import sgc.alerta.api.AlertaDto;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração modular para o módulo de alertas.
 * 
 * Carrega apenas o módulo 'alerta' e suas dependências diretas,
 * resultando em testes mais rápidos e focados.
 */
@ApplicationModuleTest
class AlertaModuleTest {
    
    @Autowired
    private AlertaService alertaService;
    
    @Test
    void deveCarregarContextoDoModuloAlerta() {
        assertThat(alertaService).isNotNull();
    }
    
    @Test
    void deveCriarAlerta() {
        // Arrange
        AlertaDto dto = AlertaDto.builder()
            .titulo("Teste")
            .descricao("Teste de alerta")
            .build();
        
        // Act
        AlertaDto criado = alertaService.criar(dto);
        
        // Assert
        assertThat(criado).isNotNull();
        assertThat(criado.getCodigo()).isNotNull();
    }
}
```

#### Criar Testes para Outros Módulos

**Replicar padrão para:**
- `backend/src/test/java/sgc/analise/AnaliseModuleTest.java`
- `backend/src/test/java/sgc/unidade/UnidadeModuleTest.java`
- `backend/src/test/java/sgc/sgrh/SgrhModuleTest.java`
- `backend/src/test/java/sgc/processo/ProcessoModuleTest.java`
- Etc.

**Benefícios:**
- Testes mais rápidos (contexto Spring menor)
- Validação de isolamento de módulos
- Identificação clara de dependências

**Critério de Aceite:**
- ✅ Ao menos 3 testes modulares criados
- ✅ Testes passam
- ✅ Tempo de execução reduzido comparado a `@SpringBootTest`

---

### Tarefa 4: Atualizar Documentação Completa

#### Atualizar backend/README.md

**Adicionar seção completa sobre Spring Modulith:**

```markdown
## Arquitetura Modular com Spring Modulith

### Estrutura de Módulos

O SGC adota **Spring Modulith** para garantir limites arquiteturais claros entre módulos.

#### Módulos do Sistema

| Módulo | Tipo | Responsabilidade |
|--------|------|------------------|
| `processo` | Core Domain | Orquestrador central de processos |
| `subprocesso` | Core Domain | Máquina de estados e workflow |
| `mapa` | Core Domain | Mapas de competências |
| `atividade` | Core Domain | Atividades e conhecimentos |
| `alerta` | Supporting | Gestão de alertas internos |
| `notificacao` | Supporting | Orquestração de notificações |
| `analise` | Supporting | Auditoria e análise |
| `painel` | Supporting | Dashboards e painéis |
| `sgrh` | Integration | Integração com sistema de RH |
| `unidade` | Foundation | Estrutura organizacional |

### Convenção de Pacotes

Cada módulo segue a estrutura:

```
sgc/{modulo}/
├── {Modulo}Service.java           # API pública (facade)
├── package-info.java              # Metadados e dependências
├── api/                           # API pública exportada
│   ├── {Modulo}Dto.java
│   └── eventos/                   # Eventos publicados
└── internal/                      # Implementação privada
    ├── {Modulo}Controller.java
    ├── {Modulo}Mapper.java
    ├── listeners/                 # Event listeners
    ├── model/                     # Entidades JPA
    └── erros/                     # Exceções
```

**Regra:** Outros módulos **NÃO** devem acessar `internal/`.

### Eventos de Domínio

O sistema usa **Spring Events** com Spring Modulith para comunicação assíncrona entre módulos.

#### Event Publication Registry

Eventos são **persistidos** antes de serem entregues, garantindo que:
- Nenhum evento é perdido em caso de falha
- Retry automático para falhas transientes
- Auditoria completa de eventos

#### Exemplo de Event Listener

```java
@Component
public class NotificacaoListener {
    
    @ApplicationModuleListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        // Processa evento assincronamente
    }
}
```

### Verificação de Módulos

#### Executar Teste de Estrutura
```bash
./gradlew :backend:test --tests ModulithStructureTest
```

#### Gerar Documentação Automatizada
```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

Documentação gerada em: `backend/build/spring-modulith-docs/`

#### Testes Modulares

Testes de integração podem carregar apenas módulos específicos:

```java
@ApplicationModuleTest
class AlertaModuleTest {
    // Carrega apenas módulo 'alerta' e dependências
}
```

**Benefício:** Testes ~30% mais rápidos.

### Dependências Entre Módulos

Dependências são explicitamente declaradas em `package-info.java`:

```java
@ApplicationModule(
    displayName = "Gestão de Alertas",
    allowedDependencies = {"sgrh", "comum"}
)
package sgc.alerta;
```

Violações são detectadas **em tempo de compilação**.

### Endpoints de Monitoramento

Spring Modulith expõe endpoint Actuator:

```
GET /actuator/modulith
```

Retorna informações sobre módulos e eventos.
```

#### Atualizar READMEs dos Módulos Refatorados

**Para cada módulo restante (`notificacao`, `painel`), adicionar seção Spring Modulith.**

**Critério de Aceite:**
- ✅ `backend/README.md` atualizado
- ✅ READMEs dos módulos atualizados

---

### Tarefa 5: Gerar e Publicar Documentação Automatizada

#### Executar Geração de Documentação

```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

#### Revisar Documentação Gerada

**Localização:** `backend/build/spring-modulith-docs/`

**Arquivos gerados:**
- `index.html` - Índice navegável
- `components.puml` - Diagrama geral PlantUML
- `module-sgc.*.puml` - Diagrama de cada módulo
- `module-canvas-sgc.*.adoc` - Canvas de módulos (AsciiDoc)

#### Converter PlantUML para Imagens (Opcional)

**Se PlantUML estiver instalado:**
```bash
cd backend/build/spring-modulith-docs
plantuml *.puml
```

**Gera arquivos PNG/SVG.**

#### Copiar Documentação para Repositório

**Criar diretório de docs:**
```bash
mkdir -p docs/modulith
cp -r backend/build/spring-modulith-docs/* docs/modulith/
```

**Adicionar ao Git:**
```bash
git add docs/modulith
git commit -m "docs: adicionar documentação automatizada Spring Modulith"
```

**Critério de Aceite:**
- ✅ Documentação gerada
- ✅ Diagramas revisados manualmente
- ✅ Documentação copiada para `docs/modulith/` (opcional)

---

### Tarefa 6: Validar Métricas de Sucesso

**Referência:** `modulith/modulith-report.md` - Seção 11.3

#### Métrica 1: Violações de Limites de Módulos

**Comando:**
```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```

**Critério:** ✅ **0 violações** (teste passa)

#### Métrica 2: Estrutura de Módulos

**Validar:** 100% de módulos com `api/` e `internal/`

**Comando:**
```bash
for modulo in processo subprocesso mapa atividade alerta analise notificacao painel sgrh unidade; do
    echo "=== $modulo ==="
    ls -d backend/src/main/java/sgc/$modulo/api 2>/dev/null && echo "  ✅ api/" || echo "  ❌ api/ ausente"
    ls -d backend/src/main/java/sgc/$modulo/internal 2>/dev/null && echo "  ✅ internal/" || echo "  ❌ internal/ ausente"
done
```

**Critério:** ✅ Todos os módulos têm ambos os diretórios

#### Métrica 3: Documentação Gerada

**Validar:** Documentação existe e está atualizada

**Comando:**
```bash
ls backend/build/spring-modulith-docs/index.html
```

**Critério:** ✅ Arquivo existe

#### Métrica 4: Event Publication Registry

**Validar:** Tabela criada e funcional

**Comando:**
```bash
# Executar aplicação e verificar logs
./gradlew :backend:bootRun | grep "EVENT_PUBLICATION"
```

**Ou consultar banco:**
```sql
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'EVENT_PUBLICATION';
```

**Critério:** ✅ Tabela existe

#### Métrica 5: Tempo de Testes (Comparação)

**Medir tempo ANTES da adoção (baseline):**
```bash
time ./gradlew :backend:test
```

**Medir tempo DEPOIS (com testes modulares):**
```bash
time ./gradlew :backend:test
```

**Critério:** ⚙️ Redução de ~30% (meta, não obrigatório)

**Critério de Aceite:**
- ✅ Métricas 1-4 validadas e passando
- ⚙️ Métrica 5 documentada (comparação antes/depois)

---

### Tarefa 7: Habilitar Verificação em Inicialização

**Objetivo:** Configurar `spring.modulith.verification.enabled: true` para produção.

#### Atualizar application.yml

**Arquivo:** `backend/src/main/resources/application.yml`

**Atualizar:**
```yaml
spring:
  modulith:
    verification:
      enabled: true  # ← Habilitar
    events:
      externalization:
        enabled: true
      completion-mode: DELETE
      delete-completion-after: 7d
```

#### Testar Inicialização

```bash
./gradlew :backend:bootRun
```

**Logs esperados:**
```
INFO  : Bootstrapping @ApplicationModule 'processo'...
INFO  : Bootstrapping @ApplicationModule 'subprocesso'...
INFO  : Verifying module structure...
INFO  : ✓ No violations found
```

**Se houver violações:**
- Aplicação **não inicia**
- Logs mostram violações específicas
- Corrigir antes de prosseguir

**Critério de Aceite:**
- ✅ `verification.enabled: true`
- ✅ Aplicação inicia sem erros
- ✅ Logs confirmam verificação bem-sucedida

---

## Comandos de Verificação

### Listar todos os módulos detectados
```bash
./gradlew :backend:test --tests ModulithStructureTest.deveDetectarModulosCorretamente
```

### Verificar ausência de violações
```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```

### Gerar documentação
```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

### Validar estrutura de todos os módulos
```bash
for modulo in processo subprocesso mapa atividade alerta analise notificacao painel sgrh unidade; do
    echo "=== Verificando $modulo ==="
    test -d backend/src/main/java/sgc/$modulo/api && echo "  ✅ api/" || echo "  ❌ api/ ausente"
    test -d backend/src/main/java/sgc/$modulo/internal && echo "  ✅ internal/" || echo "  ❌ internal/ ausente"
    test -f backend/src/main/java/sgc/$modulo/package-info.java && echo "  ✅ package-info.java" || echo "  ❌ package-info.java ausente"
done
```

---

## Critérios de Aceite da Sprint

### Obrigatórios
- ✅ Módulos `notificacao` e `painel` refatorados
- ✅ 100% dos módulos com estrutura Spring Modulith (`api/`, `internal/`, `package-info.java`)
- ✅ Todos os listeners migrados para `@ApplicationModuleListener`
- ✅ Testes de integração modulares criados (ao menos 3)
- ✅ Documentação completa atualizada (`backend/README.md`, READMEs dos módulos)
- ✅ Documentação automatizada gerada e revisada
- ✅ Métricas de sucesso validadas
- ✅ `verification.enabled: true` e aplicação inicia sem erros
- ✅ Build completo sem erros
- ✅ Todos os testes passam

### Opcionais
- ⚙️ Documentação publicada em `docs/modulith/`
- ⚙️ Diagramas PlantUML convertidos para imagens
- ⚙️ Endpoint `/actuator/modulith` documentado
- ⚙️ Comparação de performance de testes (antes/depois)

---

## Conclusão da Adoção

### Resultados Alcançados

Após esta sprint, o projeto SGC terá:
- ✅ **10 módulos** refatorados para Spring Modulith
- ✅ **0 violações** de limites de módulos
- ✅ **0 dependências cíclicas**
- ✅ **Event Publication Registry** funcionando
- ✅ **Documentação automatizada** sincronizada com código
- ✅ **Testes modulares** mais rápidos
- ✅ **Enforcement arquitetural** em tempo de compilação

### Próximas Ações

1. **Monitoramento Contínuo**
   - Revisar eventos não completados
   - Monitorar performance
   - Ajustar configurações se necessário

2. **Evolução Futura**
   - Considerar extração de módulos para microserviços (se necessário)
   - Adicionar mais eventos de domínio
   - Expandir testes modulares

3. **Treinamento da Equipe**
   - Workshop sobre Spring Modulith
   - Revisão de código focada em limites de módulos
   - Documentação de best practices

---

## Diretrizes para Agentes de IA

### Regras de Ouro
1. **Atenção aos detalhes** - Última sprint, garantir qualidade máxima
2. **Validação completa** - Executar todos os testes e verificações
3. **Documentação impecável** - Atualizar toda a documentação
4. **Commits organizados** - Histórico limpo e descritivo

### Checklist Final
- [ ] Módulos `notificacao` e `painel` refatorados
- [ ] Testes modulares criados
- [ ] Documentação completa atualizada
- [ ] Documentação automatizada gerada
- [ ] Métricas validadas
- [ ] `verification.enabled: true` configurado
- [ ] Build completo sem erros
- [ ] Testes passam (100%)
- [ ] Aplicação inicia sem erros
- [ ] Commits bem organizados

### Comandos Essenciais
```bash
# Compilar
./gradlew :backend:compileJava

# Testar
./gradlew :backend:test

# Verificar estrutura
./gradlew :backend:test --tests ModulithStructureTest

# Gerar documentação
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos

# Iniciar aplicação
./gradlew :backend:bootRun

# Validar todos os módulos
for modulo in processo subprocesso mapa atividade alerta analise notificacao painel sgrh unidade; do
    test -d backend/src/main/java/sgc/$modulo/api && \
    test -d backend/src/main/java/sgc/$modulo/internal && \
    test -f backend/src/main/java/sgc/$modulo/package-info.java && \
    echo "✅ $modulo" || echo "❌ $modulo INCOMPLETO"
done
```

---

**Status Sprint 4**: 🟡 Pronto para Execução  
**Duração Estimada**: 1 semana  
**Complexidade**: Baixa-Média  
**Dependências**: Sprints 1, 2 e 3 concluídas  
**Resultado Final**: ✅ Adoção Completa do Spring Modulith
