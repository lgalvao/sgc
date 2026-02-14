# Índice de Especificações de Views do Sistema

Este documento consolida as especificações de requisitos das 6 views principais do SGC, que fornecem dados essenciais de unidades organizacionais, usuários, responsabilidades e perfis de acesso.

## Visão Geral

As views do sistema integram dados do **Sistema de Gestão de Recursos Humanos (SGRH)** e do **Sistema CORAU (SIGMA)** com dados próprios do SGC, fornecendo uma camada de abstração que:

1. Isola o SGC das estruturas internas dos sistemas externos
2. Implementa regras de negócio complexas de forma centralizada
3. Garante consistência de dados em todo o sistema
4. Facilita manutenção e evolução do modelo de dados

## Views Documentadas

### 1. VW_VINCULACAO_UNIDADE
**Arquivo:** [view-01-vinculacao-unidade.md](view-01-vinculacao-unidade.md)

**Finalidade:** Mapeamento histórico de vinculações e sucessões entre unidades organizacionais.

**Colunas principais:**
- `unidade_atual_codigo`: Código da unidade atual
- `unidade_anterior_codigo`: Código da unidade predecessora imediata
- `demais_unidades_historicas`: Lista de unidades históricas no caminho de sucessão

**Uso principal:**
- Rastreamento de mudanças organizacionais
- Auditoria de processos históricos
- Migração de dados de unidades extintas

---

### 2. VW_ZONA_RESP_CENTRAL
**Arquivo:** [view-02-zona-resp-central.md](view-02-zona-resp-central.md)

**Finalidade:** Mapeamento entre Centrais de Atendimento ao Eleitor (CAEs) e Zonas Eleitorais sob sua responsabilidade.

**Colunas principais:**
- `codigo_central`: Código da CAE
- `sigla_central`: Sigla da CAE
- `codigo_zona_resp`: Código da zona eleitoral sob responsabilidade
- `sigla_zona_resp`: Sigla da zona
- `data_inicio_resp` / `data_fim_resp`: Período de vigência da responsabilidade

**Uso principal:**
- Determinação da hierarquia de CAEs em `VW_UNIDADE`
- Validação de atribuição de processos
- Relatórios de distribuição territorial

**Integração:** Combina dados do SGRH (`UNIDADE_TSE`) com dados do CORAU (`CT_CENTRAL`, `CT_ZONA`, `EVENTO`, `RESP_CENTRAL`)

---

### 3. VW_UNIDADE
**Arquivo:** [view-03-unidade.md](view-03-unidade.md)

**Finalidade:** **View principal** de unidades organizacionais, consolidando estrutura hierárquica, classificação, titularidade e situação operacional.

**Colunas principais:**
- `codigo`: Código único da unidade
- `nome`, `sigla`: Identificação da unidade
- `matricula_titular`, `titulo_titular`: Identificação do titular
- `data_inicio_titularidade`: Início da titularidade atual
- `tipo`: Classificação (`RAIZ`, `OPERACIONAL`, `INTEROPERACIONAL`, `INTERMEDIARIA`, `SEM_EQUIPE`)
- `situacao`: Situação (`ATIVA`, `INATIVA`)
- `unidade_superior_codigo`: Código da unidade hierarquicamente superior

**Uso principal:**
- Construção da árvore de unidades
- Validação de perfis de usuários
- Seleção de unidades para processos
- Snapshot em `UNIDADE_PROCESSO`

**Características especiais:**
- Inclui unidade virtual `ADMIN` (código 1)
- Hierarquia ajustada para CAEs (usa `VW_ZONA_RESP_CENTRAL`)
- Classificação automática baseada em servidores lotados e estrutura hierárquica

---

### 4. VW_USUARIO
**Arquivo:** [view-04-usuario.md](view-04-usuario.md)

**Finalidade:** Informações de todos os servidores com lotação ativa, que são usuários potenciais do sistema.

**Colunas principais:**
- `titulo`: Título de eleitor (identificador único no SGC)
- `matricula`: Matrícula funcional
- `nome`: Nome completo
- `email`: E-mail institucional
- `ramal`: Ramal telefônico principal
- `unidade_lot_codigo`: Unidade de lotação formal
- `unidade_comp_codigo`: Unidade de competência (onde atua no sistema)

**Uso principal:**
- Autenticação e login
- Determinação de perfis (via `VW_USUARIO_PERFIL_UNIDADE`)
- Exibição de informações de usuários
- Validação de responsáveis
- Envio de notificações

**Característica especial:**
- `unidade_comp_codigo` pode diferir de `unidade_lot_codigo` para servidores lotados em unidades `SEM_EQUIPE`

---

### 5. VW_RESPONSABILIDADE
**Arquivo:** [view-05-responsabilidade.md](view-05-responsabilidade.md)

**Finalidade:** Consolidação de todas as formas de responsabilidade sobre unidades, com hierarquia de precedência.

**Colunas principais:**
- `unidade_codigo`: Código da unidade
- `usuario_matricula`, `usuario_titulo`: Identificação do responsável atual
- `tipo`: Tipo de responsabilidade (`TITULAR`, `SUBSTITUTO`, `ATRIBUICAO_TEMPORARIA`)
- `data_inicio`, `data_fim`: Período de vigência

**Uso principal:**
- Determinação de perfis GESTOR e CHEFE
- Notificação de responsáveis
- Validação de ações em subprocessos
- Auditoria de mudanças de responsabilidade

**Hierarquia de precedência:**
1. **ATRIBUICAO_TEMPORARIA** (maior prioridade) - cadastrada no SGC
2. **SUBSTITUTO** (prioridade intermediária) - do SGRH
3. **TITULAR** (menor prioridade) - de `VW_UNIDADE`

---

### 6. VW_USUARIO_PERFIL_UNIDADE
**Arquivo:** [view-06-usuario-perfil-unidade.md](view-06-usuario-perfil-unidade.md)

**Finalidade:** **Coração do sistema de autorização**, estabelecendo mapeamento completo entre usuários, perfis e unidades.

**Colunas principais:**
- `usuario_titulo`: Título de eleitor do usuário
- `perfil`: Perfil do usuário (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`)
- `unidade_codigo`: Código da unidade onde o perfil se aplica

**Chave primária composta:** (`usuario_titulo`, `perfil`, `unidade_codigo`)

**Uso principal:**
- Seleção de perfil/unidade no login
- Validação de acesso a funcionalidades
- Filtragem de processos visíveis
- Controle de menu e botões
- Auditoria e logs

**Perfis implementados:**
- **ADMIN**: Unidade ADMIN (código 1), acesso total ao sistema
- **GESTOR**: Unidades `INTERMEDIARIA` ou `INTEROPERACIONAL`, validação de subordinadas
- **CHEFE**: Unidades `OPERACIONAL` ou `INTEROPERACIONAL`, cadastro de atividades
- **SERVIDOR**: Qualquer unidade operacional, participa de diagnósticos

---

## Diagrama de Dependências

```
┌─────────────────────────────────────────────────────────────┐
│                         SGRH / CORAU                        │
│  (UNIDADE_TSE, LOTACAO, SERVIDOR, QFC_*, CT_*, EVENTO, ...) │
└────────────┬─────────────────────────────────┬──────────────┘
             │                                 │
             ↓                                 ↓
┌──────────────────────┐          ┌──────────────────────────┐
│ VW_VINCULACAO_UNIDADE│          │  VW_ZONA_RESP_CENTRAL    │
└──────────────────────┘          └───────────┬──────────────┘
                                              │
             ┌────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────────────────────┐
│                        VW_UNIDADE                           │
│  (Unidades + Titulares + Classificação + Hierarquia)       │
└───────────┬─────────────────────────────────┬───────────────┘
            │                                 │
            ↓                                 ↓
┌───────────────────────┐         ┌─────────────────────────┐
│     VW_USUARIO        │         │  ATRIBUICAO_TEMPORARIA  │
│  (Servidores ativos)  │         │   (Tabela do SGC)       │
└───────┬───────────────┘         └──────────┬──────────────┘
        │                                    │
        └────────────┬───────────────────────┘
                     ↓
        ┌────────────────────────────┐
        │   VW_RESPONSABILIDADE      │
        │ (Titular/Substituto/Atrib.)│
        └─────────┬──────────────────┘
                  │
                  ↓
        ┌──────────────────────────────────┐
        │  VW_USUARIO_PERFIL_UNIDADE       │
        │  (Usuários × Perfis × Unidades)  │
        └──────────────────────────────────┘
```

## Fluxo de Dados no Login

1. **Autenticação:** Sistema Acesso TRE-PE valida título e senha
2. **Verificação de lotação:** Consulta `VW_USUARIO` para confirmar lotação ativa
3. **Determinação de perfis:** Consulta `VW_USUARIO_PERFIL_UNIDADE`
4. **Seleção de contexto:** Usuário escolhe perfil e unidade (se múltiplas opções)
5. **Estabelecimento de sessão:** Sistema armazena perfil e unidade selecionados

## Sincronização e Atualização

### Dados do SGRH (Tempo Real)
- **Unidades:** `VW_UNIDADE`, `VW_VINCULACAO_UNIDADE`
- **Servidores:** `VW_USUARIO`
- **Titularidades:** `VW_UNIDADE`, `VW_RESPONSABILIDADE`
- **Substituições:** `VW_RESPONSABILIDADE`

### Dados do CORAU (Tempo Real)
- **CAEs e Zonas:** `VW_ZONA_RESP_CENTRAL`
- **Responsabilidades territoriais:** `VW_ZONA_RESP_CENTRAL`

### Dados do SGC (Mantidos pelo Sistema)
- **Administradores:** `ADMINISTRADOR` (manual)
- **Atribuições temporárias:** `ATRIBUICAO_TEMPORARIA` (cadastro via ADMIN)

## Permissões Necessárias

### No SGRH (SRH2)
```sql
GRANT SELECT ON SRH2.UNIDADE_TSE TO SGC;
GRANT SELECT ON SRH2.LOTACAO TO SGC;
GRANT SELECT ON SRH2.QFC_OCUP_COM TO SGC;
GRANT SELECT ON SRH2.QFC_VAGAS_COM TO SGC;
GRANT SELECT ON SRH2.SERVIDOR TO SGC;
GRANT SELECT ON SRH2.LOT_RAMAIS_SERVIDORES TO SGC;
GRANT SELECT ON SRH2.QFC_SUBST_COM TO SGC;
```

### No CORAU (SIGMA)
```sql
GRANT SELECT ON CORAU.RESP_CENTRAL TO SGC;
GRANT SELECT ON CORAU.EVENTO TO SGC;
GRANT SELECT ON CORAU.CT_CENTRAL TO SGC;
GRANT SELECT ON CORAU.CT_ZONA TO SGC;
```

## Considerações de Performance

### Candidatas a Materialização
- **VW_USUARIO_PERFIL_UNIDADE:** Consultada em cada login e em validações frequentes
- **VW_UNIDADE:** Base para árvore de unidades e múltiplas operações
- **VW_USUARIO:** Consultada frequentemente para exibição de dados

### Views de Consulta Ocasional
- **VW_VINCULACAO_UNIDADE:** Usada principalmente para auditoria e relatórios históricos
- **VW_ZONA_RESP_CENTRAL:** Consultada na construção de `VW_UNIDADE`

### Índices Recomendados

Nas tabelas base do SGRH (se possível):
- `LOTACAO(mat_servidor, cod_unid_tse)` WHERE `dt_fim_lotacao IS NULL`
- `SERVIDOR(num_tit_ele)`
- `UNIDADE_TSE(cd, cod_unid_super, sigla_unid_tse)`
- `QFC_OCUP_COM(mat_servidor, dt_dispensa, titular_com)`

Nas tabelas do SGC:
- `ATRIBUICAO_TEMPORARIA(unidade_codigo, data_inicio, data_termino)`
- `ADMINISTRADOR(usuario_titulo)`

## Casos de Uso que Utilizam as Views

### Login e Autorização (CDU-01)
- `VW_USUARIO`: Validação de lotação ativa
- `VW_USUARIO_PERFIL_UNIDADE`: Determinação de perfis disponíveis

### Painel (CDU-02)
- `VW_USUARIO_PERFIL_UNIDADE`: Filtragem de processos visíveis
- `VW_UNIDADE`: Hierarquia de unidades para navegação

### Gestão de Processos
- `VW_UNIDADE`: Seleção de unidades participantes, snapshot em `UNIDADE_PROCESSO`
- `VW_RESPONSABILIDADE`: Notificação de responsáveis

### Cadastro e Validação
- `VW_USUARIO_PERFIL_UNIDADE`: Autorização de ações
- `VW_RESPONSABILIDADE`: Identificação de responsáveis

### Relatórios e Consultas
- Todas as views: Fornecimento de dados consolidados para relatórios

## Manutenção e Evolução

### Responsabilidades
- **Views 01, 02, 03, 04, 05, 06:** Mantidas pelo time de desenvolvimento do SGC
- **Permissões SGRH/CORAU:** Mantidas pelo time de infraestrutura/DBA
- **Dados em ADMINISTRADOR e ATRIBUICAO_TEMPORARIA:** Mantidos por usuários ADMIN

### Alterações Permitidas
- ✅ Adicionar colunas às views (com valores DEFAULT ou NULL)
- ✅ Otimizar queries internas (desde que resultado seja idêntico)
- ✅ Criar views materializadas para performance

### Alterações Restritas
- ⚠️ Renomear colunas: Requer atualização de todo código que usa a view
- ⚠️ Mudar tipos de dados: Pode quebrar aplicações dependentes
- ⚠️ Alterar lógica de cálculo: Pode afetar regras de negócio

### Alterações Proibidas
- ❌ Remover colunas: Quebra compatibilidade
- ❌ Mudar significado de colunas existentes: Corrompe lógica de negócio

## Documentação Relacionada

- **Glossário:** [_intro-glossario.md](_intro-glossario.md) - Definições de termos usados
- **Introdução:** [_intro.md](_intro.md) - Visão geral do sistema e perfis
- **DDL de Views:** `/backend/etc/sql/ddl_views.sql` - Código SQL das views
- **DDL de Tabelas:** `/backend/etc/sql/ddl_tabelas.sql` - Tabelas do sistema

## Versionamento

**Versão:** 1.0  
**Data:** Fevereiro/2024  
**Status:** Documentação inicial completa  
**Autores:** Equipe de desenvolvimento SGC
