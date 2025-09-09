# Análise do Projeto SGC

## Visão Geral

O Sistema de Gestão de Competências (SGC) é um protótipo desenvolvido para o TRE-PE utilizando Vue 3 + Vite, Vue Router,
Bootstrap 5 e Pinia. O objetivo é simular os fluxos de mapeamento, revisão e diagnóstico de competências das unidades do
TRE-PE, centralizando todos os dados no front-end via mocks em JSON.

## Estrutura de Diretórios

- `/src/components/`: Componentes Vue reutilizáveis
- `/src/views/`: Páginas/rotas da aplicação
- `/src/stores/`: Gerenciamento de estado com Pinia
- `/src/mocks/`: Dados simulados em JSON
- `/src/composables/`: Lógica reutilizável
- `/src/constants/`: Constantes e enums centralizados
- `/src/utils/`: Utilitários auxiliares
- `/src/types/`: Definições de tipos

## Principais Funcionalidades

### 1. Cadastro de Atividades e Conhecimentos

Permite às unidades cadastrarem suas atividades e conhecimentos técnicos associados. As unidades podem importar
atividades de processos já finalizados.

### 2. Mapa de Competências

Funcionalidade central do sistema que permite criar mapas associando atividades às competências técnicas da unidade. O
mapa passa por etapas de validação hierárquica até ser homologado.

### 3. Atribuição Temporária

Permite designar temporariamente um servidor para assumir a responsabilidade de uma unidade, substituindo o titular.

### 4. Processos

O sistema trabalha com três tipos de processos:

- Mapeamento: Criação inicial do mapa de competências
- Revisão: Atualização do mapa já existente
- Diagnóstico: Avaliação do domínio das competências

## Gerenciamento de Estado

O sistema utiliza Pinia para gerenciar o estado da aplicação, com stores dedicadas para cada domínio:

- `processos.ts`: Gerencia processos e subprocessos
- `mapas.ts`: Gerencia os mapas de competência
- `atividades.ts`: Gerencia atividades e conhecimentos
- `atribuicaoTemporaria.ts`: Gerencia atribuições temporárias
- `perfil.ts`: Gerencia o usuário logado
- `servidores.ts`: Gerencia os dados dos servidores
- `unidades.ts`: Gerencia as unidades organizacionais
- `revisao.ts`: Gerencia mudanças durante revisão de mapas
- `alertas.ts`: Fornece dados mockados de alertas
- `notificacoes.ts`: Gerencia o sistema de notificações
- `analises.ts`: Gerencia análises de validação

## Perfis de Usuário

O sistema possui quatro perfis de usuário:

- `ADMIN`: Administrador do sistema
- `GESTOR`: Gestor (unidades intermediárias)
- `CHEFE`: Chefe de unidade operacional/interoperacional
- `SERVIDOR`: Servidor comum

O perfil é determinado dinamicamente com base na lotação do servidor logado.

## Componentização

O sistema segue um padrão de componentização com:

- Componentes reutilizáveis em `/src/components/`
- Views específicas em `/src/views/`
- Componentes hierárquicos para exibição de unidades
- Modais para ações específicas (disponibilização, aceite, etc)

## Navegação

A navegação é gerenciada pelo Vue Router com rotas específicas para cada funcionalidade. O sistema possui uma barra de
navegação superior com breadcrumbs contextuais.

## Dados Mockados

Todos os dados do sistema são mockados em arquivos JSON em `/src/mocks/`, incluindo:

- Processos
- Subprocessos
- Unidades
- Servidores
- Mapas
- Atividades
- Atribuições temporárias

## Regras de Negócio Principais

1. Unidades do tipo `INTERMEDIARIA` não devem ter `subprocessos` associados
2. Perfis são determinados pela função do servidor na unidade (titular, atribuição temporária)
3. Fluxo hierárquico de validação para mapas e cadastros
4. Processos passam por etapas com datas limite específicas
5. Mapas vigentes são definidos após finalização do processo