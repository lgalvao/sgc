# CDU-06 - Detalhar processo

## Atores

- ADMIN
- GESTOR

## Pré-condições

- Usuário ter feito login com os perfis ADMIN ou GESTOR
- Ao menos um processo na situação 'Em andamento'.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do processo` com os dados do processo acionado, compondo a tela como a seguir:

   3.1. Cabeçalho de dados do processo, com:
    - Descrição, tipo e situação do processo;
    - Para perfil ADMIN, botão `Finalizar processo` no topo da tela, sempre habilitado.

   3.2. Botão *drop-down* `Ações em bloco`, no topo da tela, sempre habilitado, com ações que variam com o perfil e o
   tipo do processo:
    - Perfil GESTOR
        - Processos de mapeamento/revisão:
            - `Aceitar cadastros em bloco`, habilitado se existirem unidades subordinadas na situação 'Cadastro
              disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão), localizado na unidade
            - `Aceitar mapas em bloco`, habilitado se existirem unidades subordinadas com subprocesso nas situações
              'Mapa validado' ou 'Mapa com sugestões' e localizados na unidade.
        - Processos de diagnóstico:
            - `Aceitar em bloco`, habilitado se existirem unidades no processo com situação 'Concluído' e subprocesso
              localizado na unidade.

    - Perfil ADMIN:
        - Processos de mapeamento/revisão:
            - `Homologar cadastros em bloco`, habilitado se existirem unidades no processo com subprocesso na situação
              'Cadastro disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão), localizados na
              unidade
            - `Homologar mapas em bloco`, habilitado se existirem unidades no processo com subprocesso nas situações
              'Mapa validado' ou 'Mapa com sugestões' e localizados na unidade;
            - `Disponibilizar mapas em bloco`, habilitado se existirem unidades no processo com subprocesso nas
              situações 'Mapa criado' (mapeamento) ou 'Mapa ajustado' (revisão) e localizados na unidade.
        - Processos de diagnóstico:
            - `Homologar em bloco`, habilitado se existirem unidades no processo com subprocesso na situação
              'Concluído', localizados na unidade.

   3.3. Seção `Unidades participantes`:
    - Subárvore das unidades hierarquicamente inferiores, incluindo a cadeia hierárquica da raiz até a unidade atual.
        - Para cada unidade operacional e interoperacional da subárvore são exibidas a situação da unidade e a data
          limite para a conclusão da etapa atual do processo naquela unidade.
    - O usuário poderá acionar as unidades operacionais e interoperacionais para visualizar a tela `Detalhes do
        subprocesso`