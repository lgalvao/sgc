# Controle de Acesso - SGC

**Sumário:**
1. [Processo de Login](#1-processo-de-login-fluxo-completo)
2. [Perfis do Sistema](#2-perfis-do-sistema)
3. [Tipos de Requisito de Hierarquia](#3-tipos-de-requisito-de-hierarquia)
4. [Arquitetura de Controle de Acesso](#4-arquitetura-de-controle-de-acesso)
5. [Exemplos Práticos](#5-exemplos-práticos)
6. [Referências Técnicas](#6-referências-técnicas)

---

## Visão Geral

O Sistema de Gestão de Competências (SGC) implementa um controle de acesso baseado em:

1. **Perfis** (ADMIN, GESTOR, CHEFE, SERVIDOR) - Definem o papel do usuário no sistema
2. **Unidades** - Definem o escopo hierárquico de atuação (exceto ADMIN)
3. **Ações** - Operações específicas que podem ser executadas sobre recursos
4. **Hierarquia** - Verificações baseadas na estrutura organizacional

O sistema utiliza **dupla camada de controle**:
- **Camada 1 (AÇÃO)**: Verifica se o perfil do usuário está autorizado para a ação
- **Camada 2 (HIERARQUIA)**: Verifica se o usuário tem permissão espacial (unidade) para executar a ação

---

## 1. Processo de Login (Fluxo Completo)

O login no SGC é um processo em **3 etapas**:

### Etapa 1: Autenticar (`POST /autenticar`)
- **Entrada**: Título do eleitor do usuário
- **Processamento**: 
  - Valida se título do eleitor existe em VW_USUARIO
  - Cria token temporário (5min) para escolha de perfil
- **Saída**: Token temporário + lista de perfis disponíveis
- **Exemplo**: Título do eleitor "123456789012" → tem perfis [ADMIN, CHEFE]

### Etapa 2: Autorizar (`POST /autorizar`)
- **Entrada**: Token temporário + perfil escolhido
- **Processamento**:
  - Para GESTOR/CHEFE/SERVIDOR: consulta VW_USUARIO_PERFIL_UNIDADE e retorna lista de unidades onde o usuário tem esse perfil
  - Para ADMIN: **NÃO deveria retornar unidades** (ADMIN não escolhe unidade)
- **Saída**: Lista de unidades disponíveis para o perfil escolhido
- **Exemplo**: 
  - CHEFE → [{"codigo": 10, "nome": "Zona 001"}]
  - ADMIN → [] (vazio - não escolhe unidade)

### Etapa 3: Entrar (`POST /entrar`)
- **Entrada**: 
  - **GESTOR/CHEFE/SERVIDOR**: Token temporário + perfil + unidade
  - **ADMIN**: Token temporário + perfil (sem unidade)
- **Processamento**:
  - **Para GESTOR/CHEFE/SERVIDOR**: Valida que usuário tem esse perfil nessa unidade
  - **Para ADMIN**: Não requer unidade, atribui automaticamente unidade RAIZ (id=1)
  - Cria token JWT final com: título eleitor, perfil, unidade
- **Saída**: Token JWT válido para requisições autenticadas
- **Exemplo**:
  - CHEFE na unidade 10 → JWT com {titulo: "...", perfil: CHEFE, unidade: 10}
  - ADMIN (sem unidade) → JWT com {titulo: "...", perfil: ADMIN, unidade: 1}

---

## 2. Perfis do Sistema

#### ADMIN (Administrador)

**Características:**
- ❌ **NÃO limitado por HIERARQUIA**: Tem acesso global, não depende de unidade
- ✅ **MUITO limitado por AÇÃO**: NÃO é um administrador clássico que pode tudo!
- 📌 **Unidade RAIZ (id=1)**: Vinculação técnica apenas para consistência do sistema

**Papel:** Gerencia processos e mapas (nível estratégico), faz homologações finais

**AÇÕES EXCLUSIVAS:**
- **CDU-03**: Manter processo (criar, editar, excluir processos)
- **CDU-04**: Iniciar processo de mapeamento
- **CDU-05**: Iniciar processo de revisão
- **CDU-15**: Manter mapa de competências (criar/editar mapa após homologação)
- **CDU-16**: Ajustar mapa de competências (processo de revisão)
- **CDU-17**: Disponibilizar mapa de competências
- **CDU-21**: Finalizar processo de mapeamento ou revisão
- **CDU-30**: Manter Administradores
- **Visualizar processos em situação CRIADO** (único perfil com acesso)
- **Alterar datas limite** de etapas de subprocessos
- **Enviar lembretes** sobre processos
- **Reabrir processo**
- **Criar atribuição temporária**
- **HOMOLOGAÇÃO** de cadastros e mapas (aprovação final - ação exclusiva, GESTOR só faz aceite)

**AÇÕES COMPARTILHADAS (papel específico de ADMIN):**
- **CDU-06**: Detalhar processo
  - ✅ Visualiza qualquer processo (inclusive situação CRIADO)
  - ✅ Botão "Finalizar processo"
  - ✅ Pode alterar datas limite e situações de subprocessos
  
- **CDU-07**: Detalhar subprocesso
  - ✅ Visualiza detalhes de qualquer subprocesso
  - ✅ Pode alterar datas limite e situações
  
- **CDU-12**: Verificar impactos no mapa
  - ✅ Acessa em múltiplas situações ('Revisão homologada', 'Mapa ajustado')
  - ✅ Acessa pela tela "Edição de mapa"
  
- **CDU-13**: Analisar cadastro de atividades (mapeamento)
  - ✅ **HOMOLOGAR** cadastro (aprovação final → 'Cadastro homologado') **[EXCLUSIVO]**
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  
- **CDU-14**: Analisar revisão de cadastro
  - ✅ **HOMOLOGAR** revisão (aprovação final → 'Revisão homologada') **[EXCLUSIVO]**
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  - ✅ Ver impactos no mapa
  
- **CDU-20**: Analisar validação de mapa
  - ✅ **HOMOLOGAR** validação (aprovação final do mapa) **[EXCLUSIVO]**
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  - ✅ Ver sugestões (se houver)

**IMPORTANTE:** 
- 🔒 **HOMOLOGAÇÃO** (aprovar definitivamente cadastros e mapas) é **EXCLUSIVA** de ADMIN
- GESTOR pode apenas fazer **ACEITE** (análise intermediária), nunca homologação

**NÃO PODE:**
- ❌ **CDU-08**: Manter cadastro de atividades (trabalho operacional da unidade)
- ❌ **CDU-09**: Disponibilizar cadastro (decisão do CHEFE titular)
- ❌ **CDU-10**: Disponibilizar revisão do cadastro (decisão do CHEFE titular)
- ❌ **CDU-19**: Validar mapa de competências (decisão do CHEFE titular)

---

#### GESTOR

**Características:**
- ✅ **Limitado por AÇÃO**: Pode validar/homologar trabalhos de unidades subordinadas
- ✅ **Limitado por HIERARQUIA**: Só atua em sua unidade e subordinadas

**Papel:** Análise e aceite intermediário de trabalhos de unidades subordinadas

**AÇÕES EXCLUSIVAS:**
- *(Nenhuma - GESTOR sempre atua em conjunto com outros perfis)*

**AÇÕES COMPARTILHADAS (papel específico de GESTOR):**
- **CDU-06**: Detalhar processo
  - ✅ Visualiza processos de sua unidade e subordinadas
  - ✅ Botões de homologação em bloco (cadastro/mapa)
  - ❌ Não pode alterar datas/situações administrativamente
  
- **CDU-12**: Verificar impactos no mapa
  - ✅ Acessa quando subprocesso está em sua unidade ('Revisão disponibilizada')
  
- **CDU-13**: Analisar cadastro de atividades (mapeamento)
  - ✅ **ACEITAR** cadastro (análise intermediária → envia para superior)
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  - ❌ Não pode homologar (aprovação final)
  
- **CDU-14**: Analisar revisão de cadastro
  - ✅ **ACEITAR** revisão (análise intermediária → envia para superior)
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  - ✅ Ver impactos no mapa
  - ❌ Não pode homologar (aprovação final)
  
- **CDU-20**: Analisar validação de mapa
  - ✅ **ACEITAR** validação (análise intermediária → envia para superior)
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  - ✅ Ver sugestões (se houver)
  - ❌ Não pode homologar (aprovação final)

---

#### CHEFE

**Características:**
- ✅ **Limitado por AÇÃO**: Pode gerenciar dados, fazer análises, criar atividades de sua unidade
- ✅ **Limitado por HIERARQUIA**: Só atua em sua unidade operacional

**Papel:** Trabalho operacional da unidade (cadastro de atividades, validação de mapa)

**AÇÕES EXCLUSIVAS:**
- **CDU-08**: Manter cadastro de atividades e conhecimentos
- **CDU-09**: Disponibilizar cadastro de atividades
- **CDU-10**: Disponibilizar revisão do cadastro
- **CDU-19**: Validar mapa de competências

**AÇÕES COMPARTILHADAS:**
- **CDU-07**: Detalhar subprocesso (com SERVIDOR)
  - ✅ Visualiza dados da sua unidade
  
- **CDU-12**: Verificar impactos no mapa
  - ✅ Acessa durante 'Revisão em andamento' na sua unidade
  - ✅ Acessa pela tela "Cadastro de atividades"

---

#### SERVIDOR

**Características:**
- ✅ **Muito limitado por AÇÃO**: Pode apenas visualizar diagnósticos
- ✅ **Limitado por HIERARQUIA**: Só atua em sua unidade

**Papel:** Visualização de informações para diagnósticos

**AÇÕES:**
- **CDU-07**: Detalhar subprocesso (com CHEFE)
  - ✅ Visualiza dados da sua unidade (somente leitura)
  
- **CDU-11**: Visualizar cadastro de atividades
  - ✅ Visualiza cadastro (somente leitura)
  
- **CDU-18**: Visualizar mapa de competências
  - ✅ Visualiza mapa (somente leitura)

---

## 3. Tipos de Requisito de Hierarquia

O sistema define 5 tipos de verificação hierárquica (enum `RequisitoHierarquia`):

| Requisito | Descrição | Exemplo de Uso |
|-----------|-----------|----------------|
| **NENHUM** | Não verifica hierarquia | ADMIN criando processo |
| **MESMA_UNIDADE** | Usuário na mesma unidade do recurso | CHEFE editando atividade da própria unidade |
| **MESMA_OU_SUBORDINADA** | Usuário na mesma unidade ou superior | GESTOR visualizando cadastro de subordinada |
| **SUPERIOR_IMEDIATA** | Usuário na unidade imediatamente superior | Validação hierárquica específica |
| **TITULAR_UNIDADE** | Usuário é o titular da unidade | Ações que requerem titular (não é verificação hierárquica) |

**Observação sobre ADMIN:** 
ADMIN bypassa verificações de hierarquia (exceto TITULAR_UNIDADE) porque seus privilégios são globais, não vinculados a unidade específica.

---

## 4. Arquitetura de Controle de Acesso

---

## 4. Arquitetura de Controle de Acesso

### 4.1. Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│ Controller                                                   │
│  - Recebe requisição HTTP                                   │
│  - Extrai usuário autenticado do contexto de segurança      │
│  - Chama AccessControlService.verificarPermissao()          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AccessControlService (Centralizador)                        │
│  - Seleciona a Policy correta (Processo, Subprocesso, etc) │
│  - Delega verificação para a Policy                        │
│  - Registra auditoria (AccessAuditService)                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ AccessPolicy (Específica do Recurso)                        │
│  - ProcessoAccessPolicy                                     │
│  - SubprocessoAccessPolicy                                  │
│  - AtividadeAccessPolicy                                    │
│  - MapaAccessPolicy                                         │
│                                                              │
│  Verifica 3 aspectos:                                       │
│  1. Perfil permitido (CAMADA AÇÃO)                         │
│  2. Situação do recurso (se aplicável)                     │
│  3. Hierarquia (CAMADA ESPAÇO) via AbstractAccessPolicy    │
└─────────────────────────────────────────────────────────────┘
```

### 4.2. Fluxo de Verificação

**Passo a passo:**

1. **Controller recebe requisição**
   ```java
   Usuario usuario = obterUsuarioAutenticado();
   accessControlService.verificarPermissao(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso);
   ```

2. **AccessControlService seleciona policy**
   ```java
   SubprocessoAccessPolicy policy = getPolicy(Subprocesso.class);
   boolean permitido = policy.canExecute(usuario, acao, subprocesso);
   ```

3. **Policy verifica em ordem:**
   - ✅ Usuário tem perfil permitido? (lista de perfis)
   - ✅ Recurso está em situação permitida? (enum de situações)
   - ✅ Usuário atende requisito de hierarquia? (chamada para AbstractAccessPolicy)

4. **AbstractAccessPolicy verifica hierarquia**
   ```java
   // Tratamento especial para ADMIN (não limitado por hierarquia)
   if (usuario.getPerfil() == ADMIN && requisito != TITULAR_UNIDADE) {
       return true;
   }
   
   // Para outros perfis, verifica hierarquia normalmente
   switch (requisito) {
       case MESMA_UNIDADE -> unidadeUsuario.equals(unidadeRecurso)
       case MESMA_OU_SUBORDINADA -> hierarquiaService.isSubordinada(...)
       // ...
   }
   ```

### 4.3. Dupla Camada de Controle

O sistema implementa **duas camadas independentes**:

**CAMADA 1: Controle por AÇÃO (Quem pode fazer O QUÊ)**
```java
// Exemplo: VISUALIZAR_SUBPROCESSO
perfisPermitidos: [ADMIN, GESTOR, CHEFE, SERVIDOR]
```
- Se o perfil NÃO está na lista → **NEGADO** (não prossegue para camada 2)
- Esta camada já limita fortemente o que cada perfil pode fazer

**CAMADA 2: Controle por HIERARQUIA (ONDE pode fazer)**
```java
// Exemplo: VISUALIZAR_SUBPROCESSO
requisitoHierarquia: MESMA_OU_SUBORDINADA
```
- Aplica-se apenas a perfis hierárquicos (GESTOR, CHEFE, SERVIDOR)
- ADMIN bypassa esta camada (privilégios globais)

**Exemplo Concreto:**

**CRIAR_ATIVIDADE:**
```java
perfisPermitidos: [CHEFE]           // Camada 1
requisitoHierarquia: MESMA_UNIDADE  // Camada 2
```
- ADMIN: ❌ **NEGADO na Camada 1** (não está na lista de perfis)
- GESTOR: ❌ **NEGADO na Camada 1** (não está na lista de perfis)
- CHEFE unidade 10 criando na unidade 10: ✅ **PERMITIDO** (passa nas duas camadas)
- CHEFE unidade 10 criando na unidade 20: ❌ **NEGADO na Camada 2** (hierarquia)

**VISUALIZAR_SUBPROCESSO:**
```java
perfisPermitidos: [ADMIN, GESTOR, CHEFE, SERVIDOR]  // Camada 1
requisitoHierarquia: MESMA_OU_SUBORDINADA            // Camada 2
```
- ADMIN: ✅ **PERMITIDO** (passa Camada 1, bypassa Camada 2)
- CHEFE unidade 10 visualizando unidade 200: ❌ **NEGADO na Camada 2** (não é subordinada)
- GESTOR unidade 2 visualizando unidade 10 (subordinada): ✅ **PERMITIDO** (passa ambas)

### 4.4. Por Que ADMIN Bypassa Hierarquia?

**Conceito:** ADMIN tem privilégios especiais **por ser ADMIN**, não por estar em unidade específica.

**Implementação Pragmática:**
- Ações compartilhadas (ex: VISUALIZAR_SUBPROCESSO) têm múltiplos perfis permitidos
- Para GESTOR/CHEFE/SERVIDOR, hierarquia importa (limitação espacial)
- Para ADMIN, hierarquia NÃO importa (acesso global)
- Criar regras duplicadas seria verboso e redundante

**Trade-off aceito:**
- ✅ **Código DRY**: Uma regra serve para todos os perfis
- ✅ **Manutenção simples**: Bypass em um único lugar (AbstractAccessPolicy)
- ⚠️ **Acoplamento**: AbstractAccessPolicy conhece ADMIN especificamente
- ⚠️ **Documentação importante**: Regras dizem "MESMA_OU_SUBORDINADA", mas ADMIN ignora

**Alternativas consideradas e rejeitadas:**
1. Criar enum `MESMA_OU_SUBORDINADA_EXCETO_ADMIN` → ainda seria acoplamento
2. Duplicar regras (ex: `VISUALIZAR_SUBPROCESSO_ADMIN` vs `VISUALIZAR_SUBPROCESSO_OUTROS`) → muito verboso
3. Usar `RequisitoHierarquia.NENHUM` para ADMIN → não funciona em ações compartilhadas

### 4.5. Componentes de Suporte

**HierarchyService**
- Verifica relações hierárquicas entre unidades
- Métodos: `isSubordinada()`, `isSuperiorImediata()`
- Usado por AbstractAccessPolicy na Camada 2

**AccessAuditService**
- Registra todas as decisões de acesso (permitido/negado)
- Log com: usuário, ação, recurso, resultado, motivo
- Permite rastreabilidade e análise de segurança

**UsuarioPerfilRepo**
- Consulta perfis e unidades de usuários
- Acessa view VW_USUARIO_PERFIL_UNIDADE

---

## 5. Exemplos Práticos

### 5.1. VISUALIZAR_SUBPROCESSO

**Regra definida em SubprocessoAccessPolicy:**
```java
VISUALIZAR_SUBPROCESSO = {
    perfisPermitidos: [ADMIN, GESTOR, CHEFE, SERVIDOR],
    situacoesPermitidas: TODAS,
    requisitoHierarquia: MESMA_OU_SUBORDINADA
}
```

**Cenário 1: CHEFE na unidade 10 tenta visualizar subprocesso na unidade 10**
1. ✅ Perfil CHEFE está na lista de permitidos
2. ✅ Situação do subprocesso é permitida
3. ✅ Hierarquia: unidade 10 (usuário) == unidade 10 (recurso)
4. **Resultado: PERMITIDO**

**Cenário 2: CHEFE na unidade 10 tenta visualizar subprocesso na unidade 200**
1. ✅ Perfil CHEFE está na lista de permitidos
2. ✅ Situação do subprocesso é permitida
3. ❌ Hierarquia: unidade 10 não é superior de unidade 200
4. **Resultado: NEGADO**

**Cenário 3: ADMIN tenta visualizar subprocesso na unidade 200**
1. ✅ Perfil ADMIN está na lista de permitidos
2. ✅ Situação do subprocesso é permitida
3. ✅ Hierarquia: **ADMIN BYPASSA verificação de hierarquia**
4. **Resultado: PERMITIDO**

### 5.2. CRIAR_ATIVIDADE

**Regra definida em AtividadeAccessPolicy:**
```java
CRIAR_ATIVIDADE = {
    perfisPermitidos: [CHEFE],
    situacoesPermitidas: [CADASTRO_EM_ANDAMENTO, REVISAO_EM_ANDAMENTO],
    requisitoHierarquia: MESMA_UNIDADE
}
```

**Cenário 1: ADMIN tenta criar atividade**
1. ❌ Perfil ADMIN **NÃO** está na lista de permitidos
2. **Resultado: NEGADO** (nem chega a verificar hierarquia)

**Cenário 2: CHEFE unidade 10 cria atividade na unidade 10**
1. ✅ Perfil CHEFE está permitido
2. ✅ Situação do subprocesso é permitida
3. ✅ Hierarquia: unidade 10 (usuário) == unidade 10 (recurso)
4. **Resultado: PERMITIDO**

**Cenário 3: CHEFE unidade 10 tenta criar atividade na unidade 20**
1. ✅ Perfil CHEFE está permitido
2. ✅ Situação do subprocesso é permitida
3. ❌ Hierarquia: unidade 10 ≠ unidade 20
4. **Resultado: NEGADO**

### 5.3. HOMOLOGAR_CADASTRO

**Regra definida em SubprocessoAccessPolicy:**
```java
HOMOLOGAR_CADASTRO = {
    perfisPermitidos: [ADMIN],  // Apenas ADMIN
    situacoesPermitidas: [CADASTRO_DISPONIBILIZADO],
    requisitoHierarquia: NENHUM
}
```

**Cenário 1: ADMIN homologa cadastro de qualquer unidade**
1. ✅ Perfil ADMIN está permitido
2. ✅ Situação é CADASTRO_DISPONIBILIZADO
3. ✅ Hierarquia: NENHUM (não verifica)
4. **Resultado: PERMITIDO**

**Cenário 2: GESTOR tenta homologar cadastro**
1. ❌ Perfil GESTOR **NÃO** está na lista
2. **Resultado: NEGADO** (GESTOR só pode ACEITAR, não HOMOLOGAR)

---

## 6. Referências Técnicas

**Código-fonte:**
- `backend/src/main/java/sgc/seguranca/acesso/AccessControlService.java` - Serviço centralizador
- `backend/src/main/java/sgc/seguranca/acesso/AbstractAccessPolicy.java` - Classe base com verificação de hierarquia
- `backend/src/main/java/sgc/seguranca/acesso/*AccessPolicy.java` - Políticas específicas por recurso
- `backend/src/main/java/sgc/seguranca/acesso/Acao.java` - Enum de todas as ações do sistema
- `backend/src/main/java/sgc/organizacao/service/HierarchyService.java` - Serviço de hierarquia

**Requisitos:**
- `/etc/reqs/cdu-*.md` - Casos de uso detalhando permissões por perfil
- `/etc/reqs/_intro.md` - Introdução e definição dos perfis

**Banco de dados:**
- `VW_USUARIO` - Usuários do sistema
- `VW_USUARIO_PERFIL_UNIDADE` - Mapeamento usuário ↔ perfil ↔ unidade
- `VW_UNIDADE` - Unidades e hierarquia
- Unidade RAIZ (id=1): Unidade especial para consistência técnica (ADMIN)
5. **Bypass de hierarquia é pragmático**: Evita duplicação de regras

### Exemplo Concreto

**CRIAR_ATIVIDADE** (CDU-08):
```java
perfisPermitidos: [CHEFE]
requisitoHierarquia: MESMA_UNIDADE
```
- ADMIN **NÃO está na lista** → ADMIN **NÃO PODE** criar atividades (em nenhuma unidade!)
- Hierarquia nem chega a ser verificada para ADMIN

**VISUALIZAR_SUBPROCESSO** (CDU-11):
```java
perfisPermitidos: [ADMIN, GESTOR, CHEFE, SERVIDOR]
requisitoHierarquia: MESMA_OU_SUBORDINADA
```
- ADMIN **ESTÁ na lista** → ADMIN **PODE** visualizar
- Hierarquia é bypassada para ADMIN → pode visualizar de QUALQUER unidade
- Hierarquia é verificada para GESTOR/CHEFE/SERVIDOR → podem visualizar apenas de suas unidades

### Próximas Ações Necessárias

1. ✅ **Login**: ADMIN não escolhe unidade (sempre RAIZ)
2. ⏳ **Políticas**: Verificar se todas as ações de ADMIN usam RequisitoHierarquia.NENHUM OU têm bypass
3. ⏳ **Frontend**: Esconder seleção de unidade para ADMIN
4. ⏳ **Testes**: Validar que ADMIN tem acesso global mas respeita limitações de ação

---

**Documento criado em:** 2026-02-11  
**Contexto:** Refatoração de arquitetura de ADMIN após descoberta de bug em testes de integração
