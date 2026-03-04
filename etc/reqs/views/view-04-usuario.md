# VIEW-04 - VW_USUARIO - Usuários do Sistema

## Finalidade

Esta view fornece as informações essenciais sobre todos os servidores do TRE-PE que são usuários potenciais do SGC.
Consolida dados pessoais, de contato e de lotação, estabelecendo também a unidade de competência de cada servidor, que é
a unidade operacional ou interoperacional à qual o servidor está efetivamente vinculado para fins de atuação no sistema.

## Origem dos Dados

**Sistema de Gestão de Recursos Humanos (SRH2):**

- `SRH2.SERVIDOR`: Dados cadastrais dos servidores
- `SRH2.LOTACAO`: Lotações ativas dos servidores
- `SRH2.LOT_RAMAIS_SERVIDORES`: Ramais telefônicos

**Views do Sistema:**

- `VW_UNIDADE`: Para determinar a unidade de competência

## Estrutura da View

| Coluna                | Tipo         | Descrição                                                          | Origem                                 |
|-----------------------|--------------|--------------------------------------------------------------------|----------------------------------------|
| `titulo`              | VARCHAR2(12) | Título de eleitor do servidor (PK, identificador único no sistema) | `SERVIDOR.NUM_TIT_ELE`                 |
| `matricula`           | VARCHAR2(8)  | Matrícula funcional do servidor                                    | `SERVIDOR.MAT_SERVIDOR`                |
| `nome`                | VARCHAR2     | Nome completo do servidor                                          | `SERVIDOR.NOM`                         |
| `email`               | VARCHAR2     | Endereço de e-mail institucional                                   | `SERVIDOR.E_MAIL`                      |
| `ramal`               | VARCHAR2     | Ramal telefônico principal                                         | `LOT_RAMAIS_SERVIDORES.RAMAL_SERVIDOR` |
| `unidade_lot_codigo`  | NUMBER       | Código da unidade de lotação (onde está formalmente lotado)        | `LOTACAO.COD_UNID_TSE`                 |
| `unidade_comp_codigo` | NUMBER       | Código da unidade de competência (onde atua no sistema)            | Calculado (ver RN-VIEW04-03)           |

## Regras de Negócio

### RN-VIEW04-01: Identificador Único - Título de Eleitor

O título de eleitor (`titulo`) é o identificador único dos usuários no SGC:

**Justificativa:**

- Matrícula pode mudar em casos de reintegração ou mudança de regime
- Título de eleitor é permanente e único
- Integração com sistema de autenticação (Acesso TRE-PE) usa título de eleitor

**Implicações:**

- Todas as tabelas que referenciam usuários usam `titulo` como FK
- Login no sistema é feito com título de eleitor e senha
- Perfis e permissões são associados ao título

### RN-VIEW04-02: Lotação Ativa

A view considera apenas servidores com lotação ativa:

```sql
FROM srh2.servidor s
JOIN srh2.lotacao l 
    ON s.mat_servidor = l.mat_servidor 
   AND l.dt_fim_lotacao IS NULL
```

**Critérios:**

- Servidor deve ter registro ativo em `SERVIDOR`
- Lotação com `dt_fim_lotacao IS NULL` (não encerrada)
- Um servidor pode ter apenas uma lotação ativa por vez

**Exclusões:**

- Servidores aposentados sem lotação ativa
- Servidores cedidos/afastados sem lotação ativa
- Servidores com lotação encerrada

### RN-VIEW04-03: Determinação da Unidade de Competência

A unidade de competência (`unidade_comp_codigo`) é a unidade operacional ou interoperacional onde o servidor
efetivamente atua no sistema. Difere da unidade de lotação em casos específicos:

```sql
SELECT DECODE(tipo, 'SEM_EQUIPE',
       DECODE(unidade_superior_codigo,
              1, CASE
                     WHEN sigla = 'GP' THEN (SELECT codigo FROM vw_unidade 
                                             WHERE sigla = 'ASPRE' 
                                               AND tipo = 'OPERACIONAL')
                     ELSE (SELECT codigo FROM vw_unidade 
                           WHERE sigla = 'SEDOC' 
                             AND tipo = 'OPERACIONAL') 
                 END,
              unidade_superior_codigo),
       codigo)
FROM vw_unidade
WHERE codigo = l.cod_unid_tse
```

#### Caso 1: Unidade de Lotação é Operacional ou Interoperacional

```sql
WHEN tipo NOT IN ('SEM_EQUIPE', ...) 
THEN codigo  -- unidade_comp_codigo = unidade_lot_codigo
```

O servidor atua na própria unidade de lotação.

#### Caso 2: Unidade de Lotação é SEM_EQUIPE

Servidor lotado em unidade sem equipe deve ser vinculado a uma unidade operacional:

**Sub-caso 2.1: Unidade SEM_EQUIPE diretamente subordinada à ADMIN (código 1)**

Tratamento especial para unidades específicas:

```sql
WHEN sigla = 'GP' 
    THEN codigo_da_unidade_ASPRE_operacional
ELSE codigo_da_unidade_SEDOC_operacional
```

- **Gabinete da Presidência (GP)**: Servidor vinculado à Assessoria da Presidência (ASPRE)
- **Outras unidades diretas da ADMIN**: Servidor vinculado à SEDOC

**Justificativa:** Estas são unidades estratégicas que necessitam de vinculação específica para fins de competências.

**Sub-caso 2.2: Unidade SEM_EQUIPE subordinada a outra unidade (não ADMIN)**

```sql
ELSE unidade_superior_codigo  -- usa a unidade superior como competência
```

Servidor atua no contexto da unidade hierarquicamente superior.

**Exemplo prático:**

- Servidor lotado em "Seção de Arquivo" (SEM_EQUIPE)
- Unidade superior: "Coordenadoria de Documentação" (OPERACIONAL)
- `unidade_comp_codigo` = código da Coordenadoria

#### Caso 3: Unidade de Lotação é INTERMEDIARIA

```sql
ELSE codigo  -- unidade_comp_codigo = unidade_lot_codigo
```

Embora unidades intermediárias não cadastrem atividades, seus titulares têm perfil GESTOR e atuam no contexto da própria
unidade intermediária.

### RN-VIEW04-04: Ramal Telefônico Principal

```sql
LEFT JOIN srh2.lot_ramais_servidores r
    ON s.mat_servidor = r.mat_servidor 
   AND l.cod_unid_tse = r.unid_lot 
   AND r.ramal_principal = 1
```

**Regras:**

- Considera apenas o ramal marcado como principal (`ramal_principal = 1`)
- Ramal deve estar associado à lotação atual
- Se não houver ramal, a coluna fica NULL
- Se houver múltiplos ramais, apenas o principal é retornado

### RN-VIEW04-05: E-mail Institucional

O e-mail vem diretamente do campo `SERVIDOR.E_MAIL`:

**Características:**

- Deve ser e-mail institucional (@tre-pe.jus.br)
- Pode estar NULL se não cadastrado no SGRH
- É o e-mail usado para notificações do sistema

## Casos de Uso da View

### CU-VIEW04-01: Autenticação e Login (CDU-01)

**Contexto:** Usuário realiza login no sistema.

**Fluxo:**

1. Sistema valida título e senha via API Acesso TRE-PE
2. Após autenticação, consulta `VW_USUARIO` para verificar se usuário existe:
   ```sql
   SELECT titulo, matricula, nome, email, unidade_comp_codigo
   FROM VW_USUARIO
   WHERE titulo = :titulo_informado
   ```
3. Se não encontrado, usuário não tem lotação ativa no TRE-PE

**Validações:**

- Usuário deve existir em `VW_USUARIO` para acessar o sistema
- Se lotação for encerrada, usuário sai automaticamente da view e perde acesso

### CU-VIEW04-02: Determinação de Perfis

**Contexto:** Após login, sistema determina perfis disponíveis através de `VW_USUARIO_PERFIL_UNIDADE`.

**Utilização de dados:**

- `titulo`: Chave para buscar perfis em `VW_USUARIO_PERFIL_UNIDADE`
- `unidade_comp_codigo`: Unidade base para determinação de perfil SERVIDOR

### CU-VIEW04-03: Exibição de Informações do Usuário

**Contexto:** Telas que mostram informações do usuário logado ou de responsáveis.

**Aplicações:**

- Barra de navegação: Nome e perfil do usuário
- Detalhes de unidade: Nome e contato do titular
- Histórico de análises: Nome do usuário que realizou a ação
- Notificações: Destinatários de alertas

### CU-VIEW04-04: Validação de Responsáveis

**Contexto:** Ao atribuir responsabilidade temporária, validar se usuário existe e está ativo.

**Implementação:**

```sql
-- Validar se título existe em VW_USUARIO
SELECT COUNT(*) FROM VW_USUARIO WHERE titulo = :titulo_informado

-- Validar se usuário pode ser responsável de unidade específica
SELECT u.nome, u.email
FROM VW_USUARIO u
WHERE u.titulo = :titulo_informado
  AND u.unidade_comp_codigo IN (
      SELECT codigo FROM VW_UNIDADE 
      WHERE tipo IN ('OPERACIONAL', 'INTEROPERACIONAL', 'INTERMEDIARIA')
  )
```

### CU-VIEW04-05: Listagem de Servidores de uma Unidade

**Contexto:** Visualizar todos os servidores que atuam em uma unidade específica.

**Implementação:**

```sql
SELECT titulo, nome, matricula, email, ramal
FROM VW_USUARIO
WHERE unidade_comp_codigo = :codigo_unidade
ORDER BY nome;
```

**Aplicação:** Tela de detalhes da unidade, listando equipe.

### CU-VIEW04-06: Envio de Notificações

**Contexto:** Sistema precisa enviar e-mail para usuários específicos.

**Implementação:**

```sql
-- E-mail para responsável de unidade
SELECT u.email, u.nome
FROM VW_USUARIO u
JOIN VW_RESPONSABILIDADE r ON u.titulo = r.usuario_titulo
WHERE r.unidade_codigo = :codigo_unidade
  AND r.data_fim IS NULL;

-- E-mail para todos servidores de unidade
SELECT email, nome
FROM VW_USUARIO
WHERE unidade_comp_codigo = :codigo_unidade
  AND email IS NOT NULL;
```

## Relação com Outras Views e Tabelas

### Views que Dependem de VW_USUARIO

**VW_RESPONSABILIDADE:**

- Valida que `usuario_titulo` existe em `VW_USUARIO`
- Usa dados do titular da unidade de `VW_UNIDADE` combinados com `VW_USUARIO`

**VW_USUARIO_PERFIL_UNIDADE:**

- Filtra administradores: `FROM administrador a JOIN vw_usuario u ON u.titulo = a.usuario_titulo`
- Usa `titulo` como chave para todos os perfis
- Vincula perfil SERVIDOR através de `unidade_comp_codigo`

### Tabelas que Referenciam VW_USUARIO

**ADMINISTRADOR:**

- `usuario_titulo` deve existir em `VW_USUARIO.titulo`
- FK implícita

**ALERTA_USUARIO:**

- `usuario_titulo` referencia `VW_USUARIO.titulo`

**ANALISE:**

- `usuario_titulo` referencia `VW_USUARIO.titulo`

**ATRIBUICAO_TEMPORARIA:**

- `usuario_titulo` referencia `VW_USUARIO.titulo`
- `usuario_matricula` deve corresponder a `VW_USUARIO.matricula`

**MOVIMENTACAO:**

- `usuario_titulo` referencia `VW_USUARIO.titulo`

### Uso em Conjunto com VW_UNIDADE

```sql
-- Usuário com informações da sua unidade de competência
SELECT u.titulo, u.nome, u.email,
       un.codigo, un.nome AS unidade_nome, un.tipo
FROM VW_USUARIO u
JOIN VW_UNIDADE un ON u.unidade_comp_codigo = un.codigo
WHERE u.titulo = :titulo_usuario;
```

## Dependências

### Permissões Necessárias

```sql
GRANT SELECT ON SRH2.SERVIDOR TO SGC;
GRANT SELECT ON SRH2.LOTACAO TO SGC;
GRANT SELECT ON SRH2.LOT_RAMAIS_SERVIDORES TO SGC;
```

### Tabelas de Origem

- `SRH2.SERVIDOR`: Dados cadastrais
- `SRH2.LOTACAO`: Lotações ativas
- `SRH2.LOT_RAMAIS_SERVIDORES`: Ramais telefônicos

### Views Necessárias

- `VW_UNIDADE`: Para cálculo de `unidade_comp_codigo`

## Considerações de Performance

### Índices Recomendados

Para otimizar consultas frequentes:

```sql
-- No SGRH (se possível)
CREATE INDEX idx_lotacao_ativa ON LOTACAO(mat_servidor, cod_unid_tse) 
WHERE dt_fim_lotacao IS NULL;

CREATE INDEX idx_servidor_titulo ON SERVIDOR(num_tit_ele);

CREATE INDEX idx_ramais_principal ON LOT_RAMAIS_SERVIDORES(mat_servidor, unid_lot)
WHERE ramal_principal = 1;
```

### Consultas Frequentes

A view é consultada em praticamente todas as operações do sistema:

1. **Login:** Validação de usuário (1 consulta por login)
2. **Determinação de perfis:** Via `VW_USUARIO_PERFIL_UNIDADE` (1 consulta por login)
3. **Exibição de dados:** Múltiplas consultas em telas de listagem
4. **Validações:** Ao cadastrar responsáveis, análises, movimentações

**Recomendação:** Considerar materialização da view se o número de consultas for muito alto.

### Subconsulta de Unidade de Competência

A subconsulta para calcular `unidade_comp_codigo` é executada para cada servidor:

```sql
(SELECT ... FROM vw_unidade WHERE codigo = l.cod_unid_tse)
```

**Otimização possível:**

- Converter em JOIN se performance for crítica
- Ou usar CTE para pré-carregar dados de `VW_UNIDADE`

## Exemplo de Registros

```
+----------------+------------+-------------------------+---------------------------+-------+-----------------+------------------+
| titulo         | matricula  | nome                    | email                     | ramal | unidade_lot_cod | unidade_comp_cod |
+----------------+------------+-------------------------+---------------------------+-------+-----------------+------------------+
| 001234567890   | 00012345   | João da Silva Santos    | joao.silva@tre-pe.jus.br  | 1234  | 200             | 200              |
| 002345678901   | 00023456   | Maria Oliveira Costa    | maria.costa@tre-pe.jus.br | 2345  | 100             | 100              |
| 003456789012   | 00034567   | Pedro Souza Lima        | pedro.lima@tre-pe.jus.br  | NULL  | 201             | 150              |
| 004567890123   | 00045678   | Ana Paula Rodrigues     | ana.rodrigues@tre-pe.jus  | 3456  | 550             | 551              |
| 005678901234   | 00056789   | Carlos Alberto Ferreira | NULL                      | 4567  | 1001            | 1001             |
+----------------+------------+-------------------------+---------------------------+-------+-----------------+------------------+
```

**Interpretação:**

1. **João da Silva Santos:**
    - Lotado e atua na unidade 200 (OPERACIONAL)
    - Tem ramal e e-mail cadastrados
    - `unidade_comp_codigo` = `unidade_lot_codigo` (caso padrão)

2. **Maria Oliveira Costa:**
    - Lotada e atua na unidade 100 (INTEROPERACIONAL)
    - Tem ramal e e-mail cadastrados
    - Caso padrão de lotação operacional

3. **Pedro Souza Lima:**
    - Lotado na unidade 201 (SEM_EQUIPE)
    - Atua na unidade 150 (superior da 201, OPERACIONAL)
    - Sem ramal cadastrado
    - `unidade_comp_codigo` ≠ `unidade_lot_codigo` (caso especial)

4. **Ana Paula Rodrigues:**
    - Lotada na unidade 550, mas atua na 551
    - Possível caso de unidade SEM_EQUIPE com superior operacional
    - E-mail incompleto/inválido

5. **Carlos Alberto Ferreira:**
    - Lotado em CAE (1001)
    - Atua na própria CAE (OPERACIONAL)
    - Sem e-mail cadastrado
    - Tem ramal

## Notas de Implementação

### Sincronização com SGRH

A view reflete sempre o estado atual do SGRH:

**Mudanças automáticas:**

- Servidor encerra lotação → desaparece da view → perde acesso ao sistema
- Servidor transferido → `unidade_lot_codigo` e possivelmente `unidade_comp_codigo` mudam
- Atualização de e-mail/ramal → reflete imediatamente
- Mudança de nome → atualiza automaticamente

**Impactos no sistema:**

- Perda de lotação ativa = perda de acesso imediata
- Transferência = mudança de perfis e permissões
- Responsabilidades em `ATRIBUICAO_TEMPORARIA` não são afetadas (são por título, não por lotação)

### Validações de Integridade

**Dados obrigatórios:**

- `titulo`, `matricula`, `nome`: Sempre presentes (obrigatórios no SGRH)
- `unidade_lot_codigo`: Sempre presente (lotação ativa requerida)
- `unidade_comp_codigo`: Sempre presente (calculado a partir de lotação)

**Dados opcionais:**

- `email`: Pode ser NULL se não cadastrado no SGRH
- `ramal`: Pode ser NULL se não há ramal principal

**Validações no sistema:**

- Ao enviar e-mails, verificar se `email IS NOT NULL`
- Ao exibir contatos, considerar que ramal pode estar ausente
- Garantir que toda referência a usuário use `titulo`, não `matricula`

### Casos Especiais

**Usuário com múltiplas funções:**

- Servidor pode ser titular de unidade E estar lotado em outra
- Pode ter perfil ADMIN (cadastrado em `ADMINISTRADOR`) E outros perfis
- Sistema deve permitir seleção de perfil/unidade após login

**Substituição e Afastamentos:**

- `VW_RESPONSABILIDADE` gerencia substituições formais
- Servidor afastado com lotação ativa ainda aparece em `VW_USUARIO`
- Servidor afastado sem lotação ativa desaparece da view

**Servidores de unidades extintas:**

- Unidade extinta → servidor deve ser transferido
- Enquanto lotação não atualizada, pode haver `unidade_lot_codigo` apontando para unidade INATIVA
- Sistema deve validar unidade ativa ao usar `unidade_comp_codigo`
