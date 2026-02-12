# Especificação: Árvore de Seleção de Unidades

## Visão Geral

A árvore de unidades permite selecionar unidades participantes de um processo usando checkboxes com **três estados** (
marcado, desmarcado, indeterminado). O comportamento deve ser intuitivo e consistente em todos os níveis da hierarquia.

## Tipos de Unidades

### OPERACIONAL

Unidades que não têm unidades subordinadas e têm uma equipe. Toda unidade OPERACIONAL terá um mapa de competência ao
final do seu processo de mapeamento.

**Exemplos**: SECAO_111, ASSESSORIA_11

### INTEROPERACIONAL

Unidade que tem uma equipe e também tem unidades subordinadas. Pode ter um mapa também.

**Exemplos**: SECRETARIA_1

### INTERMEDIARIA

Unidade que não tem uma equipe (além do responsável), e portanto não pode ter um mapa.

**Exemplos**: COORD_11

## Comportamento de Seleção (Três Estados)

### Estados do Checkbox

1. **Marcado (checked)**: Todas as filhas estão marcadas (recursivamente)
2. **Desmarcado (unchecked)**: Nenhuma filha está marcada
3. **Indeterminado (indeterminate)**: Algumas filhas marcadas, mas não todas

### Regras de Propagação

#### 1. Seleção Descendente (Pai → Filhas)

Quando o usuário **marca** uma unidade:

- Todas as filhas **elegíveis** são marcadas recursivamente
- Filhas **não elegíveis** permanecem desmarcadas
- O estado do pai reflete o resultado (marcado se todas elegíveis marcadas, indeterminado se algumas não elegíveis)

Quando o usuário **desmarca** uma unidade:

- Todas as filhas são desmarcadas recursivamente
- Não importa se são elegíveis ou não

#### 2. Propagação Ascendente (Filhas → Pai)

Após qualquer mudança nas filhas, o estado do pai é recalculado:

- **Todas filhas marcadas** → Pai fica **marcado**
- **Nenhuma filha marcada** → Pai fica **desmarcado**
- **Algumas filhas marcadas** → Pai fica **indeterminado**

**Importante**: Esta regra se aplica a **todos** os tipos de unidade, incluindo INTERMEDIARIA.

#### 3. Exceção: INTEROPERACIONAL

Unidades INTEROPERACIONAL podem estar marcadas mesmo que nem todas as filhas estejam marcadas, pois elas próprias podem
participar do processo.

## Elegibilidade

### Definição de Elegibilidade

Uma unidade é **elegível** para participar de um processo se:

1. **NÃO é INTERMEDIARIA** (unidades INTERMEDIARIA nunca são elegíveis)
2. **NÃO está participando de outro processo ativo**
3. Para REVISAO/DIAGNOSTICO: **possui mapa vigente**

**Importante**: Elegibilidade **não é recursiva**. Uma unidade é elegível ou não baseado apenas em suas próprias
características, independentemente de suas filhas.

### Habilitação vs Elegibilidade

Embora INTERMEDIARIA não seja elegível (não pode participar do processo), seu checkbox deve estar **habilitado** se
tiver pelo menos uma filha elegível. Isso permite ao usuário selecionar facilmente todas as filhas de uma vez.

**Regra de Habilitação**:

- **Unidade elegível**: Checkbox sempre habilitado
- **Unidade não elegível COM filhas elegíveis**: Checkbox habilitado (para facilitar seleção)
- **Unidade não elegível SEM filhas elegíveis**: Checkbox desabilitado

### Comportamento Visual

- **Habilitado**: Checkbox clicável (pode ser elegível ou não)
- **Desabilitado**: Checkbox cinza, não clicável (não elegível E sem filhas elegíveis)

### Exemplos

**Cenário 1**: COORD_11 (INTERMEDIARIA) com filhas elegíveis

- COORD_11: `isElegivel = false` (é INTERMEDIARIA)
- COORD_11: Checkbox **habilitado** (tem filhas elegíveis)
- SECAO_111: `isElegivel = true` (OPERACIONAL, não em outro processo)

**Cenário 2**: COORD_11 (INTERMEDIARIA) com todas filhas em outro processo

- COORD_11: `isElegivel = false` (é INTERMEDIARIA)
- COORD_11: Checkbox **desabilitado** (todas filhas não elegíveis)
- SECAO_111: `isElegivel = false` (em outro processo ativo)
- SECAO_112: `isElegivel = false` (em outro processo ativo)

**Cenário 3**: SECAO_111 (OPERACIONAL) em processo REVISAO sem mapa vigente

- SECAO_111: `isElegivel = false` (sem mapa vigente)
- SECAO_111: Checkbox **desabilitado**

## Filtro Transparente de INTERMEDIARIA

### Comportamento do Usuário

O usuário vê e interage com **todas** as unidades elegíveis, incluindo INTERMEDIARIA. A árvore funciona normalmente com
três estados.

### Filtro ao Enviar ao Backend

Quando o formulário é submetido, apenas unidades **não-INTERMEDIARIA** são enviadas ao backend. Este filtro é *
*transparente** para o usuário - ele não vê que INTERMEDIARIA não vai para o processo.

### Exemplo Prático

**Usuário seleciona**:

- ☑ COORD_11 (INTERMEDIARIA) - marcado porque todas filhas marcadas
- ☑ SECAO_111 (OPERACIONAL)
- ☑ SECAO_112 (OPERACIONAL)
- ☑ SECAO_113 (OPERACIONAL)

**Enviado ao backend**:

- SECAO_111
- SECAO_112
- SECAO_113

**COORD_11 NÃO é enviada** (filtrada automaticamente)

## Validação Backend

O backend possui validação defensiva que rejeita unidades INTERMEDIARIA. Esta validação nunca deve ser acionada em uso
normal, pois o frontend já filtra.

## Cenários

### Cenário 1: Selecionar Coordenadoria Inteira

1. Usuário clica em COORD_11 (INTERMEDIARIA)
2. Todas as filhas elegíveis são marcadas (SECAO_111, SECAO_112, SECAO_113)
3. COORD_11 fica **marcada** (todas filhas marcadas)
4. Ao salvar, apenas as SECAOs são enviadas ao backend

### Cenário 2: Selecionar Parcialmente

1. Usuário marca SECAO_111 e SECAO_112
2. COORD_11 fica **indeterminada** (nem todas filhas marcadas)
3. Ao salvar, apenas SECAO_111 e SECAO_112 são enviadas

### Cenário 3: Desmarcar Coordenadoria

1. COORD_11 está marcada (todas filhas marcadas)
2. Usuário clica em COORD_11 para desmarcar
3. Todas as filhas são desmarcadas
4. COORD_11 fica **desmarcada**

### Cenário 4: Unidade com Filhas Não Elegíveis

1. COORD_11 tem 3 filhas, mas apenas 1 é elegível (tem mapa vigente)
2. COORD_11 está **habilitada** (tem pelo menos uma filha elegível)
3. Usuário clica em COORD_11
4. Apenas a filha elegível é marcada
5. COORD_11 fica **indeterminada** (nem todas filhas marcadas)
