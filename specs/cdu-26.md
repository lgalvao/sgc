# CDU-26 - Homologar validação de mapas em bloco

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização na unidade ADMIN.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento/revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo`, que inclui no cabeçalho o botão `Homologar mapas em bloco`.

3. O usuário aciona `Homologar mapas em bloco`.

4. O sistema identifica as unidades aptas à homologação do mapa; ou seja, unidades subordinadas com subprocesso nas
   situações 'Mapa validado' ou 'Mapa com sugestões'.

5. O sistema abre um modal de confirmação com título "Homologação de mapa em bloco" e texto "Selecione as unidades cujos
   mapas deverão ser homologados:", além dos elementos a seguir:
    - lista das unidades aptas, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o
      nome da unidade;
    - botões `Cancelar` e `Homologar em bloco`.

6. O usuário aciona `Homologar em bloco`.

7. Para cada unidade selecionada o sistema:

   7.1. Registra uma movimentação para o subprocesso:
    - `Descrição`: "Mapa homologado"
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: ADMIN
    - `Unidade destino`: ADMIN

   7.2. Altera a situação do subprocesso da unidade para 'Mapa homologado'.

8. O sistema redireciona para o `Painel` e mostra *toast* "Mapas homologados em bloco".
