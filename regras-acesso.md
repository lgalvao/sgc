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
- ✅ **Acesso Global (Perfil)**: Tem acesso visual global a processos e subprocessos porque possui o perfil ADMIN.
- ✅ **Limitado por AÇÃO**: Ações operacionais (ex: editar cadastro de unidade) são bloqueadas na Camada 1.
- 📌 **Unidade RAIZ (id=1)**: Vinculação técnica para consistência do sistema.
  - **Internamente**: Unidade ID=1, Sigla='ADMIN', Tipo='RAIZ'.
  - **Externamente (Usuário)**: Apresentada como **"SEDOC"** em movimentações, alertas e históricos.

**Papel:** Gerencia processos e mapas (nível estratégico), faz homologações finais quando o fluxo chega à SEDOC.

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
- **HOMOLOGAÇÃO** de cadastros e mapas (aprovação final - ação exclusiva)

**AÇÕES COMPARTILHADAS (papel específico de ADMIN):**
- **CDU-06**: Detalhar processo
  - ✅ Visualiza qualquer processo (inclusive situação CRIADO)
  - ✅ Botão "Finalizar processo"
  
- **CDU-07**: Detalhar subprocesso
  - ✅ Visualiza detalhes de qualquer subprocesso
  
- **CDU-12**: Verificar impactos no mapa
  - ✅ Acessa em múltiplas situações ('Revisão homologada', 'Mapa ajustado')
  
- **CDU-13**: Analisar cadastro de atividades (mapeamento)
  - ✅ **HOMOLOGAR** cadastro (aprovação final → 'Cadastro homologado') **[EXCLUSIVO]**
  - ✅ Devolver para ajustes
  - ✅ Ver histórico de análise
  
- **CDU-14**: Analisar revisão de cadastro
  - ✅ **HOMOLOGAR** revisão (aprovação final → 'Revisão homologada') **[EXCLUSIVO]**
  
- **CDU-20**: Analisar validação de mapa
  - ✅ **HOMOLOGAR** validação (aprovação final do mapa) **[EXCLUSIVO]**

**NÃO PODE:**
- ❌ **CDU-08**: Manter/Editar cadastro de atividades (trabalho operacional do CHEFE)
- ❌ **CDU-09**: Disponibilizar cadastro (decisão do CHEFE titular)
- ❌ **CDU-10**: Disponibilizar revisão do cadastro (decisão do CHEFE titular)
- ❌ **CDU-19**: Validar mapa de competências (decisão do CHEFE titular)
- ❌ **Criar/Editar/Excluir Atividades** (Responsabilidade do CHEFE)

---

#### GESTOR

**Características:**
- ✅ **Limitado por AÇÃO**: Pode validar/homologar trabalhos de unidades subordinadas
- ✅ **Limitado por HIERARQUIA**: Só atua em sua unidade e subordinadas

**Papel:** Análise e aceite intermediário de trabalhos de unidades subordinadas

**AÇÕES DESTAQUE:**
- **Aceite** de cadastros e mapas (CDU-13, CDU-14, CDU-20)
- **Devolver** para ajustes
- Visualizar processos de sua hierarquia

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
| **NENHUM** | Não verifica hierarquia | ADMIN criando processo, ou Ações Globais de Admin |
| **MESMA_UNIDADE** | Usuário na mesma unidade do recurso | CHEFE editando atividade da própria unidade |
| **MESMA_OU_SUBORDINADA** | Usuário na mesma unidade ou superior | GESTOR visualizando cadastro de subordinada |
| **SUPERIOR_IMEDIATA** | Usuário na unidade imediatamente superior | Validação hierárquica específica |
| **TITULAR_UNIDADE** | Usuário é o titular da unidade | Ações que requerem titular (não é verificação hierárquica puramente espacial) |

**Observação sobre ADMIN:** 
ADMIN ignora a verificação espacial padrão (`AbstractAccessPolicy`) porque seus privilégios de visualização são globais devido à natureza do perfil, não por ele estar "acima" na árvore.

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

### 4.2. Dupla Camada de Controle

O sistema implementa duas camadas independentes:

**CAMADA 1 - AÇÃO (O que pode fazer):**
Verifica se o perfil do usuário está na lista de perfis permitidos para a ação.
- Se NÃO está → **NEGADO** (não prossegue).
- *Exemplo*: ADMIN tentando `EDITAR_CADASTRO` é negado aqui.

**CAMADA 2 - HIERARQUIA (Onde pode fazer):**  
Verifica se o usuário atende ao requisito de hierarquia (unidade).
- Aplica-se a GESTOR, CHEFE, SERVIDOR.
- ADMIN bypassa esta verificação na maioria dos casos (Acesso Global).
- *Exceção*: `TITULAR_UNIDADE` é verificado para todos (incluindo ADMIN, se aplicável).

### 4.3. Implementação do Bypass de ADMIN

**Conceito:** O perfil ADMIN possui visualização global do sistema.

**Implementação:** Na classe `AbstractAccessPolicy`, se o usuário tem perfil ADMIN, a verificação de hierarquia espacial (ex: `MESMA_OU_SUBORDINADA`) retorna automaticamente `true`.

**RAIZ vs SEDOC:**
Embora o ADMIN esteja vinculado tecnicamente à unidade RAIZ (id=1), o sistema apresenta essa unidade como **"SEDOC"** para o usuário final em fluxos de trabalho (movimentações, alertas), mantendo a integridade técnica interna (RAIZ) e a familiaridade de negócio (SEDOC).

### 4.4. Componentes de Suporte

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

**Regra:** `[ADMIN, GESTOR, CHEFE, SERVIDOR]` + `MESMA_OU_SUBORDINADA`

- **GESTOR (Unidade 10) → Unidade 20 (Subordinada)**: 
  1. Perfil OK. 
  2. Hierarquia OK (20 é filha de 10). 
  3. **PERMITIDO**.
- **CHEFE (Unidade 20) → Unidade 10 (Superior)**: 
  1. Perfil OK. 
  2. Hierarquia FALHA (10 não é subordinada a 20). 
  3. **NEGADO**.
- **ADMIN → Qualquer Unidade**: 
  1. Perfil OK. 
  2. Hierarquia BYPASS (Global). 
  3. **PERMITIDO**.

### 5.2. CRIAR_ATIVIDADE

**Regra:** `[CHEFE]` + `MESMA_UNIDADE`

- **ADMIN → Qualquer Unidade**: 
  1. Perfil FALHA (ADMIN não está na lista). 
  2. **NEGADO**.
- **CHEFE (Unidade 10) → Unidade 10**: 
  1. Perfil OK. 
  2. Hierarquia OK (Mesma). 
  3. **PERMITIDO**.
- **CHEFE (Unidade 10) → Unidade 20**: 
  1. Perfil OK. 
  2. Hierarquia FALHA. 
  3. **NEGADO**.

### 5.3. HOMOLOGAR_CADASTRO

**Regra:** `[ADMIN]` + `NENHUM (Hierarquia)`

- **ADMIN → Subprocesso (Logicamente na SEDOC)**: 
  1. Perfil OK. 
  2. Hierarquia NENHUM. 
  3. **PERMITIDO**.
- **GESTOR**: 
  1. Perfil FALHA. 
  2. **NEGADO**.

---

## 6. Referências Técnicas

**Código-fonte:**
- `backend/src/main/java/sgc/seguranca/acesso/AccessControlService.java` - Serviço centralizador
- `backend/src/main/java/sgc/seguranca/acesso/AbstractAccessPolicy.java` - Lógica base e bypass de ADMIN.
- `backend/src/main/java/sgc/seguranca/acesso/*AccessPolicy.java` - Políticas específicas por recurso
- `backend/src/main/java/sgc/seguranca/acesso/Acao.java` - Enum de todas as ações do sistema
- `backend/src/main/java/sgc/organizacao/service/HierarchyService.java` - Serviço de hierarquia
- `backend/src/main/java/sgc/seguranca/acesso/SubprocessoAccessPolicy.java` - Regras detalhadas de permissão.

**Requisitos:**
- `/etc/reqs/cdu-*.md` - Casos de uso detalhando permissões por perfil
- `/etc/reqs/_intro.md` - Introdução e definição dos perfis

**Banco de dados:**
- `VW_USUARIO` - Usuários do sistema
- `VW_USUARIO_PERFIL_UNIDADE` - Mapeamento usuário ↔ perfil ↔ unidade
- `VW_UNIDADE` - Unidades e hierarquia
- Unidade RAIZ (id=1): Unidade especial para consistência técnica (ADMIN)
