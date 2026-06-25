# CDU-06 - Detalhar processo

Atores: ADMIN e GESTOR

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
   tipo de processo:
    - Perfil GESTOR
        - Processos de mapeamento/revisão:
            - `Aceitar cadastros em bloco`, habilitado se existirem unidades subordinadas na situação 'Cadastro
              disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão) e suprocesso localizado
              na unidade
            - `Aceitar mapas em bloco`, habilitado se existirem unidades subordinadas com subprocesso nas situações
              'Mapa validado' ou 'Mapa com sugestões' e suprocesso localizado na unidade.
        - Processos de diagnóstico:
            - `Aceitar diagnósticos em bloco`, se existirem unidades subordinadas com subprocesso na situação
              'Avaliação de consenso concluída' e suprocesso localizado na unidade.

    - Perfil ADMIN:
        - Processos de mapeamento/revisão:
            - `Homologar cadastros em bloco`, habilitado se existirem unidades participantes com subprocesso na situação
              'Cadastro disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão) e suprocesso
              localizado na unidade
            - `Homologar mapas em bloco`, habilitado se existirem unidades participantes com subprocesso nas situações
              'Mapa validado' ou 'Mapa com sugestões' e suprocesso localizado na unidade;
            - `Disponibilizar mapas em bloco`, habilitado se existirem unidades participantes com subprocesso nas
              situações 'Mapa criado' (mapeamento) ou 'Mapa ajustado' (revisão) e suprocesso localizado na unidade.

        - Processos de diagnóstico:
            - `Homologar diagnósticos em bloco`, habilitado se existirem unidades subordinadas com subprocesso na
              situação 'Concluído' e suprocesso localizado na unidade.

   3.3. Seção `Unidades participantes`:
    - Subárvore das unidades hierarquicamente inferiores, incluindo a hierarquia acima até a unidade logo abaixo da raiz.
        - Para cada unidade operacional e interoperacional da subárvore são exibidas, as informações de situação da
          unidade e a data limite para a conclusão da etapa atual do processo naquela unidade.
    - O usuário poderá acionar as unidades operacionais e interoperacionais para visualizar a tela `Detalhes do
        subprocesso`
