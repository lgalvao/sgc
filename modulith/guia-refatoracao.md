# Guia de Refatoração de Módulos para Spring Modulith

Este guia fornece um **passo a passo detalhado** para refatorar um módulo existente do SGC para a estrutura Spring Modulith.

---

## Visão Geral do Processo

### Etapas Principais

1. **Análise** - Entender estrutura atual e dependências
2. **Planejamento** - Definir API pública e privada
3. **Criação** - Criar nova estrutura de diretórios
4. **Migração** - Mover arquivos para novos locais
5. **Atualização** - Corrigir imports e packages
6. **Validação** - Testar compilação e funcionamento
7. **Documentação** - Atualizar READMEs e package-info

### Tempo Estimado por Módulo

- **Módulo Simples** (alerta, analise): 2-4 horas
- **Módulo Médio** (unidade, sgrh): 4-6 horas
- **Módulo Complexo** (processo, subprocesso): 8-12 horas

---

## Passo 1: Análise do Módulo

### 1.1. Identificar Estrutura Atual

**Localização:** `backend/src/main/java/sgc/{modulo}/`

**Listar arquivos:**
```bash
tree backend/src/main/java/sgc/{modulo}/
```

**Exemplo de estrutura típica:**
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
├── listeners/                      # Pode ou não existir
│   └── ProcessoListener.java
└── model/
    ├── Alerta.java
    ├── AlertaRepo.java
    └── TipoAlerta.java
```

### 1.2. Identificar Dependências

**Buscar imports de outros módulos:**
```bash
grep -r "import sgc\." backend/src/main/java/sgc/{modulo}/ --include="*.java" | \
  grep -v "import sgc.{modulo}" | \
  grep -v "import sgc.comum" | \
  sort | uniq
```

**Exemplo de saída:**
```
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.sgrh.SgrhService;
```

**Documentar dependências:**
- `processo` (via eventos)
- `sgrh` (via service)

### 1.3. Identificar Classes Públicas vs Privadas

**Classes Públicas** (devem ir para pacote raiz ou `api/`):
- Services (facades)
- DTOs expostos a outros módulos
- Eventos publicados

**Classes Privadas** (devem ir para `internal/`):
- Controllers
- Mappers
- Entidades JPA
- Repositories
- Listeners
- Exceções internas

**Checklist:**
- [ ] Service identificado
- [ ] DTOs identificados
- [ ] Eventos identificados (se houver)
- [ ] Implementações identificadas
- [ ] Dependências documentadas

---

## Passo 2: Planejamento

### 2.1. Definir Estrutura Alvo

**Template:**
```
sgc/{modulo}/
├── {Modulo}Service.java           # API pública (manter no raiz)
├── package-info.java              # CRIAR
├── api/                           # CRIAR
│   ├── {Modulo}Dto.java          # Mover de dto/
│   └── eventos/                   # CRIAR (se necessário)
│       └── Evento{Modulo}{Acao}.java
└── internal/                      # CRIAR
    ├── {Modulo}Controller.java    # Mover de raiz
    ├── {Modulo}Mapper.java        # Mover de dto/
    ├── listeners/                 # Mover ou criar
    │   └── {OutroModulo}Listener.java
    ├── model/                     # Mover
    │   ├── {Modulo}.java
    │   └── {Modulo}Repo.java
    └── erros/                     # Mover
        └── Erro{Modulo}.java
```

### 2.2. Definir Dependências Permitidas

**Listar módulos dos quais este módulo depende:**

Exemplo para `alerta`:
```java
allowedDependencies = {"sgrh", "comum"}
```

Exemplo para `processo`:
```java
allowedDependencies = {"subprocesso", "mapa", "atividade", "unidade", "comum"}
```

**Regras:**
- Não incluir dependências transitivas (Spring Modulith resolve)
- Apenas módulos diretamente utilizados
- Usar sintaxe `"modulo::subpacote"` para limitar a subpacotes específicos

### 2.3. Criar Checklist de Ações

```markdown
- [ ] Criar `package-info.java`
- [ ] Criar diretório `api/`
- [ ] Criar diretório `internal/`
- [ ] Mover {X} DTOs para `api/`
- [ ] Mover Controller para `internal/`
- [ ] Mover Mapper para `internal/`
- [ ] Mover Model para `internal/model/`
- [ ] Mover Erros para `internal/erros/`
- [ ] Atualizar imports em arquivos movidos
- [ ] Atualizar imports em outros módulos
- [ ] Compilar
- [ ] Executar testes
- [ ] Atualizar README.md
```

---

## Passo 3: Criação da Estrutura

### 3.1. Criar Diretórios

```bash
# Definir variável com nome do módulo
MODULO="alerta"  # Substituir pelo módulo sendo refatorado

# Criar estrutura
mkdir -p backend/src/main/java/sgc/$MODULO/api
mkdir -p backend/src/main/java/sgc/$MODULO/api/eventos
mkdir -p backend/src/main/java/sgc/$MODULO/internal
mkdir -p backend/src/main/java/sgc/$MODULO/internal/model
mkdir -p backend/src/main/java/sgc/$MODULO/internal/erros
mkdir -p backend/src/main/java/sgc/$MODULO/internal/listeners

# Verificar criação
tree backend/src/main/java/sgc/$MODULO/
```

### 3.2. Criar package-info.java

**Arquivo:** `backend/src/main/java/sgc/{modulo}/package-info.java`

**Template:**
```java
/**
 * Módulo de {Descrição do Módulo}.
 * 
 * <p>{Descrição detalhada da responsabilidade do módulo}</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.{modulo}.{Modulo}Service} - {Descrição}</li>
 *   <li>{@link sgc.{modulo}.api.{Modulo}Dto} - {Descrição}</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>{modulo1} - {Motivo}</li>
 *   <li>{modulo2} - {Motivo}</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>{EventoNome} - {Descrição}</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>{EventoNome} - {Descrição}</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "{Nome de Exibição}",
    allowedDependencies = {"{modulo1}", "{modulo2}", "comum"}
)
package sgc.{modulo};
```

**Preencher:**
- `{Descrição do Módulo}`
- `{Modulo}Service`
- `allowedDependencies`
- Eventos (se aplicável)

---

## Passo 4: Migração de Arquivos

### 4.1. Mover DTOs para api/

**Identificar DTOs:**
```bash
ls backend/src/main/java/sgc/$MODULO/dto/*.java
```

**Mover:**
```bash
MODULO="alerta"

# Mover DTO principal
git mv backend/src/main/java/sgc/$MODULO/dto/${MODULO^}Dto.java \
       backend/src/main/java/sgc/$MODULO/api/

# Se houver outros DTOs públicos, mover também
# git mv backend/src/main/java/sgc/$MODULO/dto/OutroDto.java \
#        backend/src/main/java/sgc/$MODULO/api/
```

**Nota:** `${MODULO^}` converte primeira letra para maiúscula (bash 4+).

### 4.2. Mover Controller para internal/

```bash
git mv backend/src/main/java/sgc/$MODULO/${MODULO^}Controller.java \
       backend/src/main/java/sgc/$MODULO/internal/
```

### 4.3. Mover Mapper para internal/

```bash
# Se estiver em dto/
if [ -f backend/src/main/java/sgc/$MODULO/dto/${MODULO^}Mapper.java ]; then
    git mv backend/src/main/java/sgc/$MODULO/dto/${MODULO^}Mapper.java \
           backend/src/main/java/sgc/$MODULO/internal/
fi

# Se estiver no raiz
if [ -f backend/src/main/java/sgc/$MODULO/${MODULO^}Mapper.java ]; then
    git mv backend/src/main/java/sgc/$MODULO/${MODULO^}Mapper.java \
           backend/src/main/java/sgc/$MODULO/internal/
fi
```

### 4.4. Mover Model para internal/model/

```bash
git mv backend/src/main/java/sgc/$MODULO/model/* \
       backend/src/main/java/sgc/$MODULO/internal/model/
```

### 4.5. Mover Erros para internal/erros/

```bash
git mv backend/src/main/java/sgc/$MODULO/erros/* \
       backend/src/main/java/sgc/$MODULO/internal/erros/
```

### 4.6. Mover Listeners (se existirem)

```bash
# Se listeners estão em subdiretório
if [ -d backend/src/main/java/sgc/$MODULO/listeners ]; then
    git mv backend/src/main/java/sgc/$MODULO/listeners/* \
           backend/src/main/java/sgc/$MODULO/internal/listeners/
fi

# Se listeners estão no raiz (procurar por *Listener.java)
find backend/src/main/java/sgc/$MODULO -maxdepth 1 -name "*Listener.java" \
    -exec git mv {} backend/src/main/java/sgc/$MODULO/internal/listeners/ \;
```

### 4.7. Remover Diretórios Vazios

```bash
rmdir backend/src/main/java/sgc/$MODULO/dto 2>/dev/null || true
rmdir backend/src/main/java/sgc/$MODULO/model 2>/dev/null || true
rmdir backend/src/main/java/sgc/$MODULO/erros 2>/dev/null || true
rmdir backend/src/main/java/sgc/$MODULO/listeners 2>/dev/null || true
```

---

## Passo 5: Atualização de Imports e Packages

### 5.1. Atualizar Declarações de Package

**DTOs em api/:**
```bash
sed -i "s/package sgc.$MODULO.dto;/package sgc.$MODULO.api;/g" \
    backend/src/main/java/sgc/$MODULO/api/*.java
```

**Controller:**
```bash
sed -i "s/package sgc.$MODULO;/package sgc.$MODULO.internal;/g" \
    backend/src/main/java/sgc/$MODULO/internal/${MODULO^}Controller.java
```

**Mapper:**
```bash
sed -i "s/package sgc.$MODULO.dto;/package sgc.$MODULO.internal;/g" \
    backend/src/main/java/sgc/$MODULO/internal/${MODULO^}Mapper.java 2>/dev/null || \
sed -i "s/package sgc.$MODULO;/package sgc.$MODULO.internal;/g" \
    backend/src/main/java/sgc/$MODULO/internal/${MODULO^}Mapper.java
```

**Model:**
```bash
find backend/src/main/java/sgc/$MODULO/internal/model -name "*.java" \
    -exec sed -i "s/package sgc.$MODULO.model;/package sgc.$MODULO.internal.model;/g" {} +
```

**Erros:**
```bash
find backend/src/main/java/sgc/$MODULO/internal/erros -name "*.java" \
    -exec sed -i "s/package sgc.$MODULO.erros;/package sgc.$MODULO.internal.erros;/g" {} +
```

**Listeners:**
```bash
find backend/src/main/java/sgc/$MODULO/internal/listeners -name "*.java" \
    -exec sed -i "s/package sgc.$MODULO.listeners;/package sgc.$MODULO.internal.listeners;/g" {} +
```

### 5.2. Atualizar Imports em TODO o Projeto

**DTOs:**
```bash
find backend/src -name "*.java" \
    -exec sed -i "s/sgc\.$MODULO\.dto\.${MODULO^}Dto/sgc.$MODULO.api.${MODULO^}Dto/g" {} +
```

**Model:**
```bash
find backend/src -name "*.java" \
    -exec sed -i "s/sgc\.$MODULO\.model\./sgc.$MODULO.internal.model./g" {} +
```

**Erros:**
```bash
find backend/src -name "*.java" \
    -exec sed -i "s/sgc\.$MODULO\.erros\./sgc.$MODULO.internal.erros./g" {} +
```

**Controller:**
```bash
find backend/src -name "*.java" \
    -exec sed -i "s/sgc\.$MODULO\.${MODULO^}Controller/sgc.$MODULO.internal.${MODULO^}Controller/g" {} +
```

**Mapper:**
```bash
find backend/src -name "*.java" \
    -exec sed -i "s/sgc\.$MODULO\.dto\.${MODULO^}Mapper/sgc.$MODULO.internal.${MODULO^}Mapper/g" {} +
```

**Listeners:**
```bash
find backend/src -name "*.java" \
    -exec sed -i "s/sgc\.$MODULO\.listeners\./sgc.$MODULO.internal.listeners./g" {} +
```

---

## Passo 6: Validação

### 6.1. Compilar

```bash
./gradlew :backend:compileJava
```

**Se houver erros:**
1. Revisar mensagens de erro
2. Identificar imports não atualizados
3. Corrigir manualmente
4. Tentar novamente

### 6.2. Executar Testes

```bash
# Todos os testes
./gradlew :backend:test

# Apenas testes do módulo (se isolados)
./gradlew :backend:test --tests sgc.$MODULO.*
```

**Se houver falhas:**
1. Revisar testes que falharam
2. Atualizar imports em arquivos de teste
3. Considerar mover testes para `internal` se testam classes internas

### 6.3. Verificar Estrutura Modular

```bash
./gradlew :backend:test --tests ModulithStructureTest.deveDetectarModulosCorretamente
```

**Verificar logs:**
- Módulo é detectado?
- Dependências estão corretas?

### 6.4. Buscar Acessos Indevidos

```bash
# Nenhum outro módulo deve importar internal/ deste módulo
grep -r "import sgc.$MODULO.internal" backend/src/main/java/ --include="*.java" | \
    grep -v "backend/src/main/java/sgc/$MODULO/"

# Se houver resultados, CORRIGIR
```

---

## Passo 7: Documentação

### 7.1. Atualizar README.md do Módulo

**Arquivo:** `backend/src/main/java/sgc/{modulo}/README.md`

**Adicionar seção:**
```markdown
## Estrutura Spring Modulith

Este módulo segue a convenção Spring Modulith:

### API Pública
- **`{Modulo}Service`** - Facade principal
- **`{Modulo}Dto`** (em `api/`) - DTO exposto

### Implementação Interna
- `{Modulo}Controller` - REST endpoints
- `{Modulo}Mapper` - Mapeamento
- Model: Entidades e Repositories
- Listeners: Event listeners

**⚠️ Importante:** Outros módulos **NÃO** devem acessar classes em `internal/`.

## Dependências

### Módulos que este módulo depende
- `{modulo1}` - {motivo}
- `comum` - Componentes compartilhados

### Módulos que dependem deste módulo
- `{moduloX}`
- `{moduloY}`

## Eventos

### Publicados
- `{EventoNome}` - {descrição}

### Consumidos
- `{EventoNome}` - {descrição}
```

### 7.2. Commit

```bash
git add .
git commit -m "refactor($MODULO): adotar estrutura Spring Modulith

- Criado package-info.java com metadados do módulo
- Movido DTOs para api/
- Movido implementações para internal/
- Atualizado imports em todo o projeto
- Atualizado README.md
"
```

---

## Checklist Final

Antes de considerar o módulo completo:

- [ ] `package-info.java` criado e preenchido
- [ ] Estrutura `api/` e `internal/` criada
- [ ] DTOs movidos para `api/`
- [ ] Implementações movidas para `internal/`
- [ ] Todos os imports atualizados (package declarations)
- [ ] Imports em outros módulos atualizados
- [ ] Build compila sem erros
- [ ] Testes passam
- [ ] Nenhum acesso a `internal/` de outros módulos
- [ ] `ModulithStructureTest` detecta o módulo
- [ ] README.md atualizado
- [ ] Commit realizado

---

## Script Completo (Exemplo para módulo 'alerta')

```bash
#!/bin/bash

# Definir módulo
MODULO="alerta"
MODULO_UPPER="${MODULO^}"

echo "=== Refatorando módulo: $MODULO ==="

# Passo 3: Criar estrutura
mkdir -p backend/src/main/java/sgc/$MODULO/api
mkdir -p backend/src/main/java/sgc/$MODULO/internal/model
mkdir -p backend/src/main/java/sgc/$MODULO/internal/erros
mkdir -p backend/src/main/java/sgc/$MODULO/internal/listeners

# Passo 4: Mover arquivos
git mv backend/src/main/java/sgc/$MODULO/dto/${MODULO_UPPER}Dto.java backend/src/main/java/sgc/$MODULO/api/
git mv backend/src/main/java/sgc/$MODULO/${MODULO_UPPER}Controller.java backend/src/main/java/sgc/$MODULO/internal/
git mv backend/src/main/java/sgc/$MODULO/dto/${MODULO_UPPER}Mapper.java backend/src/main/java/sgc/$MODULO/internal/
git mv backend/src/main/java/sgc/$MODULO/model/* backend/src/main/java/sgc/$MODULO/internal/model/
git mv backend/src/main/java/sgc/$MODULO/erros/* backend/src/main/java/sgc/$MODULO/internal/erros/

# Remover diretórios vazios
rmdir backend/src/main/java/sgc/$MODULO/dto
rmdir backend/src/main/java/sgc/$MODULO/model
rmdir backend/src/main/java/sgc/$MODULO/erros

# Passo 5.1: Atualizar package declarations
sed -i "s/package sgc.$MODULO.dto;/package sgc.$MODULO.api;/g" backend/src/main/java/sgc/$MODULO/api/*.java
sed -i "s/package sgc.$MODULO;/package sgc.$MODULO.internal;/g" backend/src/main/java/sgc/$MODULO/internal/${MODULO_UPPER}Controller.java
sed -i "s/package sgc.$MODULO.dto;/package sgc.$MODULO.internal;/g" backend/src/main/java/sgc/$MODULO/internal/${MODULO_UPPER}Mapper.java
find backend/src/main/java/sgc/$MODULO/internal/model -name "*.java" -exec sed -i "s/package sgc.$MODULO.model;/package sgc.$MODULO.internal.model;/g" {} +
find backend/src/main/java/sgc/$MODULO/internal/erros -name "*.java" -exec sed -i "s/package sgc.$MODULO.erros;/package sgc.$MODULO.internal.erros;/g" {} +

# Passo 5.2: Atualizar imports
find backend/src -name "*.java" -exec sed -i "s/sgc\.$MODULO\.dto\.${MODULO_UPPER}Dto/sgc.$MODULO.api.${MODULO_UPPER}Dto/g" {} +
find backend/src -name "*.java" -exec sed -i "s/sgc\.$MODULO\.model\./sgc.$MODULO.internal.model./g" {} +
find backend/src -name "*.java" -exec sed -i "s/sgc\.$MODULO\.erros\./sgc.$MODULO.internal.erros./g" {} +

# Passo 6: Compilar
echo "=== Compilando ==="
./gradlew :backend:compileJava

if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
else
    echo "❌ Erro na compilação. Revisar manualmente."
    exit 1
fi

echo "=== Refatoração completa para módulo $MODULO ==="
```

---

**Use este guia como referência durante a execução das sprints!**
