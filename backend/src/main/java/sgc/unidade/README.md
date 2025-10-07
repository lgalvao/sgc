# Módulo de Unidade - SGC

## Visão Geral
O pacote `unidade` é responsável por definir a estrutura organizacional do sistema. Ele contém as entidades JPA que modelam as **Unidades Organizacionais**, suas hierarquias e os relacionamentos entre elas.

Este pacote é, em grande parte, um módulo de modelo de dados. Ele não contém serviços com lógica de negócio complexa ou controladores REST. Em vez disso, suas entidades servem como a base sobre a qual os outros módulos (como `processo` e `subprocesso`) operam. Ele forma o "esqueleto" organizacional da aplicação.

## Arquivos Principais

### 1. `Unidade.java`
**Localização:** `backend/src/main/java/sgc/unidade/Unidade.java`
- **Descrição:** A entidade mais importante do pacote. Representa uma unidade organizacional dentro da instituição (ex: uma secretaria, uma seção, uma zona eleitoral).
- **Campos e Relacionamentos Chave:**
  - `nome` e `sigla`: Identificadores da unidade.
  - `titular`: Um relacionamento `ManyToOne` com a entidade `Usuario`, designando o responsável pela unidade.
  - `unidadeSuperior`: Um auto-relacionamento (`ManyToOne` com `Unidade`) que cria a estrutura hierárquica. Se for nulo, a unidade está no topo da hierarquia.
  - `tipo`: Categoriza a unidade (ex: `OPERACIONAL`, `INTERMEDIARIA`), o que pode direcionar diferentes lógicas de negócio em outros pacotes.
  - `situacao`: Indica se a unidade está `ATIVA` ou `INATIVA`.

### 2. `AtribuicaoTemporaria.java`
**Localização:** `backend/src/main/java/sgc/unidade/AtribuicaoTemporaria.java`
- **Descrição:** Entidade que representa uma atribuição de responsabilidade temporária sobre uma unidade. Isso pode ser usado para cenários como férias ou licenças, onde um usuário substitui o titular por um período determinado.
- **Campos Importantes:**
  - `unidade`: A unidade que está recebendo a atribuição temporária.
  - `usuario`: O usuário que está assumindo a responsabilidade temporariamente.
  - `dataInicio` e `dataFim`: O período da atribuição.

### 3. `VinculacaoUnidade.java`
**Localização:** `backend/src/main/java/sgc/unidade/VinculacaoUnidade.java`
- **Descrição:** Entidade que parece modelar um tipo de vínculo ou agrupamento entre unidades, possivelmente para fins de processos ou relatórios específicos.

### 4. `UnidadeRepository.java`
**Localização:** `backend/src/main/java/sgc/unidade/UnidadeRepository.java`
- **Descrição:** A interface Spring Data JPA para fornecer acesso aos dados da entidade `Unidade`. É utilizada por vários serviços em toda a aplicação para carregar informações sobre a estrutura organizacional.

## Como as Entidades são Utilizadas

As entidades deste pacote são fundamentais para o funcionamento de todo o sistema:
- O **`ProcessoService`** utiliza o `UnidadeRepository` para selecionar as unidades que participarão de um novo processo.
- O **`SubprocessoService`** depende da hierarquia (`unidadeSuperior`) para determinar para onde um cadastro deve ser enviado para análise ou aprovação.
- O **`SgrhService`** (módulo de integração de RH) é responsável por popular e manter os dados da entidade `Unidade` sincronizados com o sistema de RH oficial.
- O controle de acesso e as permissões frequentemente dependem da unidade à qual o usuário está associado.

## Notas Importantes
- **Fonte da Verdade Organizacional**: Este pacote é a "fonte da verdade" para a estrutura organizacional dentro do SGC.
- **Sincronização Externa**: É crucial que os dados das unidades sejam mantidos atualizados. Em um ambiente de produção, espera-se que um processo de sincronização (provavelmente via `SgrhService`) alimente os dados da tabela `UNIDADE` a partir de um sistema de RH externo.
- **Ausência de Controller**: A ausência de um `UnidadeController` é proposital. A gestão das unidades não é feita diretamente pelos usuários através de uma tela de CRUD, mas sim através de uma integração com um sistema externo, o que garante a consistência e a autoridade dos dados.