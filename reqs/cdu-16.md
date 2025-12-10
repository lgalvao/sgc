# CDU-16 - Ajustar mapa de competências

Ator: ADMIN

Pré-condições:

- Processo de Revisão, com ao menos uma unidade com subprocesso nas situações 'Revisão do cadastro homologada' ou 'Mapa
  ajustado'.

Fluxo principal:

1. No Painel, ADMIN escolhe o processo de revisão desejado.

2. O sistema mostra tela `Detalhes do processo`.

3. ADMIN clica em uma unidade operacional ou interoperacional com subprocesso nas situações 'Revisão do cadastro
   homologada' ou 'Mapa ajustado'.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.

5. ADMIN clica no card `Mapa de Competências`.

6. O sistema mostra a tela `Edição de mapa` preenchida com o mapa do subprocesso da unidade (ver detalhes sobre a tela
   no caso de uso _Manter mapa de competências_, com os botões Impactos no mapa e `Disponibilizar`.

7. ADMIN clica em Impactos no mapa.

8. O sistema mostra o modal `Impactos no mapa`. Ver caso de uso _Verificar impactos no mapa de competências_.

9. ADMIN usa como base as informações de impactos mostradas nesta tela para alterar o mapa, podendo alterar descrições
   de competências, de atividades e de conhecimentos; remover ou criar novas competências; e ajustar a associação das
   atividades às competências do mapa, conforme descrito no caso de uso _Manter mapa de competências_.

   9.1. ADMIN deve associar a uma competência todas as atividades ainda não associadas.

10. Quando concluir os ajustes, ADMIN clica em `Disponibilizar` e o sistema segue o fluxo descrito no caso de uso
    _Disponibilizar mapa de competências_.
