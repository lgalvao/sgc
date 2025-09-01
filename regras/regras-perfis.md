# Regras de Perfis de Usuários no Sistema de Gestão de Competências

O sistema de Gestão de Competências opera com os seguintes perfis de usuários, cujas atribuições e acessos são
automaticamente reconhecidos com base na condição de responsabilidade ou lotação em uma unidade, ou por atribuição de
responsabilidade temporária realizada no próprio sistema.

Os perfis são definidos no enum `Perfil` em `src/types/tipos.ts` e são: `ADMIN`, `GESTOR`, `CHEFE` e `SERVIDOR`.

Caso um usuário acumule mais de um perfil ou seja responsável por mais de uma unidade, será necessário selecionar o
perfil e a unidade de trabalho após o login.

## Definição dos Perfis e Regras de Atribuição:

### 1. ADMIN

*   **Definição:** Administrador da SEDOC. É responsável por criar, configurar e monitorar processos, além de
    criar/ajustar os mapas de competências das unidades.
*   **Unidade Associada:** A unidade SEDOC é tratada como unidade raiz da estrutura organizacional para efeito dos
    processos de mapeamento, de revisão e de diagnóstico.
*   **Regra de Atribuição:** Atribuído a servidores que são titulares da unidade SEDOC.

### 2. GESTOR

*   **Definição:** Responsável por uma unidade intermediária (exemplo: Coordenador). Pode visualizar e validar as
    informações cadastradas pelas unidades sob sua gestão, submetendo para análise da unidade superior, ou devolver à
    unidade subordinada para realização de retificações.
*   **Regra de Atribuição:**
    *   Atribuído a servidores que são titulares de uma unidade de tipo 'INTERMEDIARIA'.
    *   **OU** Atribuído a servidores que possuem uma atribuição temporária em uma unidade de tipo 'INTERMEDIARIA'. Neste
        caso, o perfil `GESTOR` é concedido para a unidade da atribuição temporária.

### 3. CHEFE

*   **Definição:** Responsável por uma unidade operacional ou interoperacional. Pode cadastrar as informações de sua
    unidade em cada processo e submeter essas informações para validação pela unidade superior.
*   **Regra de Atribuição:**
    *   Atribuído a servidores que são titulares de uma unidade de tipo 'OPERACIONAL' ou 'INTEROPERACIONAL'.
    *   **OU** Atribuído a servidores que possuem uma atribuição temporária em uma unidade de tipo 'OPERACIONAL' ou
        'INTEROPERACIONAL'. Neste caso, o perfil `CHEFE` é concedido para a unidade da atribuição temporária.

### 4. SERVIDOR

*   **Definição:** Servidor lotado em uma unidade operacional ou interoperacional. Este papel só atua nos processos de
    diagnóstico.
*   **Regra de Atribuição:**
    *   Atribuído a servidores para a sua unidade de lotação principal, **SOMENTE SE**:
        *   A unidade de lotação principal for de tipo 'OPERACIONAL' ou 'INTEROPERACIONAL'.
        *   **E** o servidor não for titular dessa unidade.
        *   **E** o servidor não tiver uma atribuição temporária para essa unidade (que o tornaria `CHEFE` nela).

## Geração de Pares (Perfil, Unidade) para Seleção:

A lista de pares `(perfil, unidade)` disponíveis para um servidor é construída da seguinte forma:

1.  **Coleta de Perfis de Titularidade:** Para cada unidade em que o servidor é titular, o perfil correspondente (
    `ADMIN`, `GESTOR`, `CHEFE`) e a sigla da unidade são adicionados à lista.
2.  **Coleta de Perfis por Atribuição Temporária:** Para cada atribuição temporária do servidor, o perfil (`GESTOR` se a
    unidade for intermediária, ou `CHEFE` se for operacional/interoperacional) e a sigla da unidade da atribuição são
    adicionados à lista.
3.  **Coleta do Perfil SERVIDOR:** O perfil `SERVIDOR` é adicionado para a unidade de lotação principal do servidor,
    seguindo as regras específicas do perfil `SERVIDOR` (conforme item 4 acima).
4.  **Remoção de Duplicatas:** Após a coleta de todos os pares, a lista é filtrada para remover quaisquer pares
    `(perfil, unidade)` que sejam exatamente iguais. Isso garante que cada combinação única de perfil e unidade seja
    apresentada ao usuário para seleção.

Esta documentação reflete o entendimento atual das regras de negócio para a atribuição e seleção de perfis no sistema.