# Sprint 2: Módulos Foundation e Integration

**Baseado em:** `modulith-report.md` - Seção 6.2 (Sprint 2)

## Contexto

Esta sprint foca na refatoração dos módulos de **infraestrutura** e **integração**, que possuem poucas ou nenhuma dependência cíclica, tornando-os candidatos ideais para consolidar o padrão Spring Modulith.

### Módulos Alvo
1. **`unidade`** - Foundation layer (estrutura organizacional)
2. **`sgrh`** - Integration layer (integração com sistema de RH externo)

### Status do Projeto
Após Sprint 1:
- ✅ Spring Modulith configurado
- ✅ Teste `ModulithStructureTest` funcionando
- ✅ Módulos `alerta` e `analise` refatorados (PoC validada)
- ✅ Documentação automatizada gerada

---

## Objetivo da Sprint

Refatorar módulos de **baixa complexidade** e **sem dependências cíclicas** para consolidar o padrão Spring Modulith e preparar o terreno para módulos de domínio.

### Entregáveis
1. ✅ Módulo `unidade` refatorado para estrutura Spring Modulith
2. ✅ Módulo `sgrh` refatorado para estrutura Spring Modulith
3. ✅ Documentação dos módulos atualizada
4. ✅ Diagramas PlantUML gerados e revisados
5. ✅ Build e testes passando

---

## Tarefas Detalhadas

### Tarefa 1: Refatorar Módulo `unidade`

#### Contexto do Módulo

O módulo **`unidade`** é um módulo **Foundation** que gerencia a estrutura organizacional:
- Unidades organizacionais (departamentos, setores)
- Hierarquia de unidades
- Sem dependências de domínio complexas
- Usado por vários outros módulos (`processo`, `sgrh`, `mapa`)

**Localização:** `backend/src/main/java/sgc/unidade/`

#### Estrutura Atual (exemplo)
```
sgc/unidade/
├── UnidadeController.java
├── UnidadeService.java
├── README.md
├── dto/
│   ├── UnidadeDto.java
│   └── UnidadeMapper.java
├── model/
│   ├── Unidade.java
│   ├── UnidadeRepo.java
│   └── TipoUnidade.java
└── erros/
    └── ErroUnidade.java
```

#### Estrutura Alvo
```
sgc/unidade/
├── UnidadeService.java             # API pública
├── package-info.java
├── api/
│   └── UnidadeDto.java
└── internal/
    ├── UnidadeController.java
    ├── UnidadeMapper.java
    ├── model/
    │   ├── Unidade.java
    │   ├── UnidadeRepo.java
    │   └── TipoUnidade.java
    └── erros/
        └── ErroUnidade.java
```

#### Passo 1.1: Criar package-info.java

**Arquivo:** `backend/src/main/java/sgc/unidade/package-info.java`

```java
/**
 * Módulo de Estrutura Organizacional (Foundation).
 * 
 * <p>Gerencia unidades organizacionais, sua hierarquia e tipos.
 * Este é um módulo foundation usado por diversos módulos de domínio.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.unidade.UnidadeService} - Facade para operações de unidades</li>
 *   <li>{@link sgc.unidade.api.UnidadeDto} - DTO para transferência de dados</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos</h2>
 * <p>Este módulo não publica eventos, mas pode ser estendido no futuro
 * para notificar mudanças em hierarquias organizacionais.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Estrutura Organizacional",
    allowedDependencies = {"comum"}
)
package sgc.unidade;
```

#### Passo 1.2: Executar Refatoração

**Criar estrutura:**
```bash
mkdir -p backend/src/main/java/sgc/unidade/api
mkdir -p backend/src/main/java/sgc/unidade/internal/model
mkdir -p backend/src/main/java/sgc/unidade/internal/erros
```

**Mover DTOs para api/:**
```bash
git mv backend/src/main/java/sgc/unidade/dto/UnidadeDto.java backend/src/main/java/sgc/unidade/api/
```

**Mover implementações para internal/:**
```bash
git mv backend/src/main/java/sgc/unidade/UnidadeController.java backend/src/main/java/sgc/unidade/internal/
git mv backend/src/main/java/sgc/unidade/dto/UnidadeMapper.java backend/src/main/java/sgc/unidade/internal/
git mv backend/src/main/java/sgc/unidade/model/* backend/src/main/java/sgc/unidade/internal/model/
git mv backend/src/main/java/sgc/unidade/erros/* backend/src/main/java/sgc/unidade/internal/erros/

# Remover diretórios vazios
rmdir backend/src/main/java/sgc/unidade/dto
rmdir backend/src/main/java/sgc/unidade/model
rmdir backend/src/main/java/sgc/unidade/erros
```

**Atualizar imports:**
```bash
# DTOs
find backend/src -name "*.java" -exec sed -i 's/sgc\.unidade\.dto\.UnidadeDto/sgc.unidade.api.UnidadeDto/g' {} +

# Model
find backend/src -name "*.java" -exec sed -i 's/sgc\.unidade\.model\./sgc.unidade.internal.model./g' {} +

# Erros
find backend/src -name "*.java" -exec sed -i 's/sgc\.unidade\.erros\./sgc.unidade.internal.erros./g' {} +

# Controller
find backend/src -name "*.java" -exec sed -i 's/sgc\.unidade\.UnidadeController/sgc.unidade.internal.UnidadeController/g' {} +

# Mapper
find backend/src -name "*.java" -exec sed -i 's/sgc\.unidade\.dto\.UnidadeMapper/sgc.unidade.internal.UnidadeMapper/g' {} +
```

**Atualizar declarações de package:**
```bash
# Controller
sed -i 's/package sgc.unidade;/package sgc.unidade.internal;/g' backend/src/main/java/sgc/unidade/internal/UnidadeController.java

# Mapper
sed -i 's/package sgc.unidade.dto;/package sgc.unidade.internal;/g' backend/src/main/java/sgc/unidade/internal/UnidadeMapper.java

# Model
find backend/src/main/java/sgc/unidade/internal/model -name "*.java" -exec sed -i 's/package sgc.unidade.model;/package sgc.unidade.internal.model;/g' {} +

# Erros
find backend/src/main/java/sgc/unidade/internal/erros -name "*.java" -exec sed -i 's/package sgc.unidade.erros;/package sgc.unidade.internal.erros;/g' {} +

# API
sed -i 's/package sgc.unidade.dto;/package sgc.unidade.api;/g' backend/src/main/java/sgc/unidade/api/UnidadeDto.java
```

#### Passo 1.3: Validar Compilação e Testes

```bash
# Compilar
./gradlew :backend:compileJava

# Executar testes do módulo (se existirem)
./gradlew :backend:test --tests sgc.unidade.*

# Executar todos os testes
./gradlew :backend:test
```

**Critério de Aceite:**
- ✅ Build sem erros
- ✅ Testes passam
- ✅ Estrutura de diretórios correta

---

### Tarefa 2: Refatorar Módulo `sgrh`

#### Contexto do Módulo

O módulo **`sgrh`** é um módulo de **Integration** que integra com o sistema de RH externo:
- Sincronização de usuários
- Consulta de informações de RH
- Pode ter dependências mínimas com `unidade`

**Localização:** `backend/src/main/java/sgc/sgrh/`

#### Estrutura Atual (exemplo)
```
sgc/sgrh/
├── SgrhController.java
├── SgrhService.java
├── README.md
├── dto/
│   ├── UsuarioSgrhDto.java
│   └── SgrhMapper.java
├── model/
│   ├── UsuarioSgrh.java
│   └── UsuarioSgrhRepo.java
└── erros/
    └── ErroSgrh.java
```

#### Estrutura Alvo
```
sgc/sgrh/
├── SgrhService.java                # API pública
├── package-info.java
├── api/
│   └── UsuarioSgrhDto.java
└── internal/
    ├── SgrhController.java
    ├── SgrhMapper.java
    ├── model/
    │   ├── UsuarioSgrh.java
    │   └── UsuarioSgrhRepo.java
    └── erros/
        └── ErroSgrh.java
```

#### Passo 2.1: Criar package-info.java

**Arquivo:** `backend/src/main/java/sgc/sgrh/package-info.java`

```java
/**
 * Módulo de Integração com Sistema de Gestão de RH (SGRH).
 * 
 * <p>Responsável por integrar com o sistema externo de RH,
 * sincronizando informações de usuários e estrutura organizacional.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.sgrh.SgrhService} - Facade para integração com SGRH</li>
 *   <li>{@link sgc.sgrh.api.UsuarioSgrhDto} - DTO de usuário do SGRH</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>unidade - Para sincronizar estrutura organizacional</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>EventoUsuarioSincronizado - Quando usuário é sincronizado do SGRH</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Integração SGRH",
    allowedDependencies = {"unidade", "comum"}
)
package sgc.sgrh;
```

#### Passo 2.2: Executar Refatoração

**Aplicar os mesmos passos da Tarefa 1:**
1. Criar estrutura de diretórios
2. Mover DTOs para `api/`
3. Mover implementações para `internal/`
4. Atualizar imports
5. Validar build e testes

**Comandos (adaptados para sgrh):**
```bash
# Criar estrutura
mkdir -p backend/src/main/java/sgc/sgrh/api
mkdir -p backend/src/main/java/sgc/sgrh/internal/model
mkdir -p backend/src/main/java/sgc/sgrh/internal/erros

# Mover arquivos (exemplo - adaptar conforme estrutura real)
git mv backend/src/main/java/sgc/sgrh/dto/UsuarioSgrhDto.java backend/src/main/java/sgc/sgrh/api/
git mv backend/src/main/java/sgc/sgrh/SgrhController.java backend/src/main/java/sgc/sgrh/internal/
git mv backend/src/main/java/sgc/sgrh/dto/SgrhMapper.java backend/src/main/java/sgc/sgrh/internal/
git mv backend/src/main/java/sgc/sgrh/model/* backend/src/main/java/sgc/sgrh/internal/model/
git mv backend/src/main/java/sgc/sgrh/erros/* backend/src/main/java/sgc/sgrh/internal/erros/

# Remover diretórios vazios
rmdir backend/src/main/java/sgc/sgrh/dto
rmdir backend/src/main/java/sgc/sgrh/model
rmdir backend/src/main/java/sgc/sgrh/erros

# Atualizar imports
find backend/src -name "*.java" -exec sed -i 's/sgc\.sgrh\.dto\./sgc.sgrh.api./g' {} +
find backend/src -name "*.java" -exec sed -i 's/sgc\.sgrh\.model\./sgc.sgrh.internal.model./g' {} +
find backend/src -name "*.java" -exec sed -i 's/sgc\.sgrh\.erros\./sgc.sgrh.internal.erros./g' {} +
find backend/src -name "*.java" -exec sed -i 's/sgc\.sgrh\.SgrhController/sgc.sgrh.internal.SgrhController/g' {} +

# Atualizar declarações de package
sed -i 's/package sgc.sgrh;/package sgc.sgrh.internal;/g' backend/src/main/java/sgc/sgrh/internal/SgrhController.java
sed -i 's/package sgc.sgrh.dto;/package sgc.sgrh.internal;/g' backend/src/main/java/sgc/sgrh/internal/SgrhMapper.java
sed -i 's/package sgc.sgrh.dto;/package sgc.sgrh.api;/g' backend/src/main/java/sgc/sgrh/api/UsuarioSgrhDto.java
find backend/src/main/java/sgc/sgrh/internal/model -name "*.java" -exec sed -i 's/package sgc.sgrh.model;/package sgc.sgrh.internal.model;/g' {} +
find backend/src/main/java/sgc/sgrh/internal/erros -name "*.java" -exec sed -i 's/package sgc.sgrh.erros;/package sgc.sgrh.internal.erros;/g' {} +
```

**Validar:**
```bash
./gradlew :backend:compileJava
./gradlew :backend:test
```

**Critério de Aceite:**
- ✅ Build sem erros
- ✅ Testes passam

---

### Tarefa 3: Atualizar Documentação dos Módulos

#### Tarefa 3.1: README.md do Módulo `unidade`

**Arquivo:** `backend/src/main/java/sgc/unidade/README.md`

**Adicionar/Atualizar seção:**

```markdown
# Módulo: Estrutura Organizacional (Unidade)

## Visão Geral

Módulo **Foundation** responsável por gerenciar unidades organizacionais, hierarquias e tipos.

## Responsabilidades

- Gerenciar unidades organizacionais (departamentos, setores, áreas)
- Manter hierarquia de unidades (unidade pai/filha)
- Fornecer consultas sobre estrutura organizacional

## Estrutura Spring Modulith

Este módulo segue a convenção Spring Modulith:

### API Pública
- **`UnidadeService`** - Facade principal para operações de unidades
- **`UnidadeDto`** (em `api/`) - DTO para transferência de dados

### Implementação Interna
- `UnidadeController` - REST endpoints
- `UnidadeMapper` - Mapeamento entidade ↔ DTO
- Model: `Unidade`, `TipoUnidade`, `UnidadeRepo`
- Erros customizados

**⚠️ Importante:** Outros módulos **NÃO** devem acessar classes em `internal/`.

## Dependências

### Módulos que este módulo depende
- `comum` - Componentes transversais

### Módulos que dependem deste módulo
- `processo`
- `sgrh`
- `mapa`
- Outros módulos de domínio

## Endpoints REST

- `GET /api/unidades` - Listar unidades
- `GET /api/unidades/{codigo}` - Buscar unidade por código
- `POST /api/unidades` - Criar unidade
- `POST /api/unidades/{codigo}/atualizar` - Atualizar unidade

## Eventos

Este módulo atualmente **não publica eventos**, mas pode ser estendido para notificar:
- Criação de unidade
- Mudança em hierarquia organizacional
```

#### Tarefa 3.2: README.md do Módulo `sgrh`

**Arquivo:** `backend/src/main/java/sgc/sgrh/README.md`

**Adicionar/Atualizar seção:**

```markdown
# Módulo: Integração SGRH

## Visão Geral

Módulo **Integration** responsável por integrar o SGC com o Sistema de Gestão de Recursos Humanos (SGRH) externo.

## Responsabilidades

- Sincronizar usuários do SGRH
- Consultar informações de RH
- Mapear estrutura organizacional do SGRH para SGC

## Estrutura Spring Modulith

### API Pública
- **`SgrhService`** - Facade para integração com SGRH
- **`UsuarioSgrhDto`** (em `api/`) - DTO de usuário do SGRH

### Implementação Interna
- `SgrhController` - REST endpoints (se houver)
- `SgrhMapper` - Mapeamento SGRH ↔ SGC
- Model: `UsuarioSgrh`, `UsuarioSgrhRepo`
- Client/Integração com API externa do SGRH

## Dependências

### Módulos que este módulo depende
- `unidade` - Para mapear estrutura organizacional
- `comum` - Componentes transversais

### Módulos que dependem deste módulo
- `processo` (pode consultar informações de usuários)
- `alerta` (para notificar usuários)

## Eventos Publicados

- `EventoUsuarioSincronizado` - Quando usuário é sincronizado do SGRH

## Eventos Consumidos

Nenhum evento consumido atualmente.
```

---

### Tarefa 4: Gerar e Revisar Diagramas PlantUML

#### Executar Teste de Documentação

```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

**Localização dos arquivos gerados:**
```
backend/build/spring-modulith-docs/
├── index.html                      # Índice da documentação
├── components.puml                 # Diagrama geral de componentes
├── module-sgc.alerta.puml          # Diagrama do módulo alerta
├── module-sgc.analise.puml         # Diagrama do módulo analise
├── module-sgc.unidade.puml         # Diagrama do módulo unidade (NOVO)
├── module-sgc.sgrh.puml            # Diagrama do módulo sgrh (NOVO)
└── ...
```

#### Revisar Diagramas

**Abrir e validar:**
1. Abrir `index.html` em navegador
2. Verificar que módulos `unidade` e `sgrh` aparecem
3. Verificar dependências mostradas nos diagramas

**Opcional:** Converter PlantUML para PNG/SVG:
```bash
# Se plantuml estiver instalado
plantuml backend/build/spring-modulith-docs/*.puml
```

**Critério de Aceite:**
- ✅ Diagramas gerados para `unidade` e `sgrh`
- ✅ Dependências corretas mostradas

---

### Tarefa 5: Validação Final da Sprint

#### Executar Suite Completa de Testes

```bash
# Clean build
./gradlew clean :backend:build

# Todos os testes
./gradlew :backend:test

# Teste de estrutura modular
./gradlew :backend:test --tests ModulithStructureTest
```

#### Verificar Acessos Indevidos

**Nenhum módulo deve importar `internal/` de outros módulos:**

```bash
# Verificar que nenhum módulo acessa internal/ do módulo unidade
grep -r "import sgc.unidade.internal" backend/src/main/java/ --exclude-dir=unidade

# Verificar que nenhum módulo acessa internal/ do módulo sgrh
grep -r "import sgc.sgrh.internal" backend/src/main/java/ --exclude-dir=sgrh

# Verificar que nenhum módulo acessa internal/ dos módulos da Sprint 1
grep -r "import sgc.alerta.internal" backend/src/main/java/ --exclude-dir=alerta
grep -r "import sgc.analise.internal" backend/src/main/java/ --exclude-dir=analise
```

**Critério de Aceite:**
- ✅ Nenhum resultado encontrado (ou apenas em testes, que podem ser ajustados)

---

## Comandos de Verificação

### Listar módulos detectados
```bash
./gradlew :backend:test --tests ModulithStructureTest.deveDetectarModulosCorretamente
```

### Verificar dependências
```bash
./gradlew :backend:test --tests ModulithStructureTest.naoDevemExistirDependenciasCiclicas
```

**Nota:** Ainda pode falhar devido a outros módulos. Será resolvido nas próximas sprints.

### Gerar documentação atualizada
```bash
./gradlew :backend:test --tests ModulithStructureTest.gerarDocumentacaoDosModulos
```

---

## Critérios de Aceite da Sprint

### Obrigatórios
- ✅ Módulo `unidade` refatorado para estrutura Spring Modulith
- ✅ Módulo `sgrh` refatorado para estrutura Spring Modulith
- ✅ `package-info.java` criado para ambos os módulos
- ✅ READMEs atualizados com estrutura Spring Modulith
- ✅ Build completo sem erros: `./gradlew clean :backend:build`
- ✅ Todos os testes passam: `./gradlew :backend:test`
- ✅ Diagramas PlantUML gerados e revisados
- ✅ Nenhum acesso a `internal/` de outros módulos

### Opcionais
- ⚙️ Configurar `spring.modulith.verification.enabled: true` e validar que aplicação inicia
- ⚙️ Adicionar testes de integração usando `@ApplicationModuleTest`

---

## Próximos Passos

Após concluir esta sprint:
1. ✅ Validar que refatorações não causaram regressões
2. ✅ Revisar lições aprendidas do processo de refatoração
3. ➡️ Prosseguir para **Sprint 3: Módulos Core Domain** (mais complexa - 2 semanas)

---

## Diretrizes para Agentes de IA

### Regras de Ouro
1. **Um módulo por vez** - Completar refatoração de `unidade` antes de `sgrh`
2. **Testar após cada mudança** - Executar testes após mover cada conjunto de arquivos
3. **Commits incrementais** - Um commit por módulo concluído
4. **Preservar funcionalidade** - Nenhuma mudança de comportamento

### Checklist por Módulo
- [ ] Criar `package-info.java`
- [ ] Criar pacotes `api/` e `internal/`
- [ ] Mover DTOs para `api/`
- [ ] Mover implementações para `internal/`
- [ ] Atualizar imports em todo o projeto
- [ ] Atualizar declarações de package
- [ ] Executar `./gradlew :backend:compileJava`
- [ ] Executar `./gradlew :backend:test`
- [ ] Atualizar README.md do módulo
- [ ] Commit: `refactor(modulo): adotar estrutura Spring Modulith`

### Comandos Essenciais
```bash
# Build incremental
./gradlew :backend:compileJava

# Testes completos
./gradlew :backend:test

# Verificar estrutura
./gradlew :backend:test --tests ModulithStructureTest

# Buscar acessos indevidos
grep -r "import sgc.{modulo}.internal" backend/src/main/java/ --exclude-dir={modulo}
```

### Troubleshooting

**Problema:** Erro de compilação após mover arquivos
- **Solução:** Verificar que todos os imports foram atualizados
- **Comando:** `grep -r "{ClasseMovida}" backend/src --include="*.java"`

**Problema:** Testes falhando
- **Solução:** Atualizar imports em arquivos de teste
- **Considerar:** Mover testes para pacote `internal` se testam classes internas

**Problema:** CircularDependencyException
- **Solução:** Documentar para resolver na Sprint 3
- **Workaround temporário:** Configurar `allowedDependencies` no `package-info.java`

---

**Status Sprint 2**: 🟡 Pronto para Execução  
**Duração Estimada**: 1 semana  
**Complexidade**: Baixa  
**Dependências**: Sprint 1 concluída
