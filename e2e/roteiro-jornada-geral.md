# Roteiro da Jornada Geral do SGC

Este documento descreve uma jornada ponta a ponta do sistema, escrita para ser compreendida por pessoas técnicas e não técnicas. O objetivo é servir como base para um futuro teste e2e mais semântico, cobrindo o ciclo completo desde a criação de um processo de mapeamento até a finalização do processo correspondente de revisão.

## Objetivo

Validar que o SGC permite:

- iniciar um processo de mapeamento para a `SECAO_111`;
- realizar o cadastro de atividades e conhecimentos da `SECAO_111`;
- criar, disponibilizar, validar e homologar o mapa de competências;
- finalizar o processo de mapeamento, tornando o mapa vigente;
- iniciar um processo de revisão da mesma seção;
- revisar o cadastro, verificar impactos no mapa, ajustar o mapa, validá-lo novamente e finalizar a revisão.

## Princípios deste roteiro

- O roteiro privilegia o fluxo principal de negócio, sem entrar em detalhes de implementação.
- Sempre que possível, cada passo informa também o resultado esperado.
- Validações importantes do sistema aparecem como 'checkpoints' ao longo da jornada.
- Fluxos alternativos importantes, mas mutuamente exclusivos com o fluxo principal, ficam listados ao final como cenários complementares.

## Premissas do cenário

- O cenário adotado neste roteiro é a hierarquia `SECAO_111` -> `COORD_11` -> `SECRETARIA_1`.
- O cadastro e a validação do mapa devem percorrer toda a hierarquia antes de chegar ao `ADMIN`.
- O processo de revisão deve usar a mesma `SECAO_111` que acabou de concluir o mapeamento, para garantir a existência de mapa vigente.
- Na revisão, a alteração no cadastro deve produzir impacto real no mapa. Para isso, incluir uma nova atividade ou alterar uma atividade já associada a competência, em vez de usar um caso sem impacto.
- O fluxo deve passar por dois níveis reais de gestão antes da homologação final: primeiro `COORD_11`, depois `SECRETARIA_1`.

## Preparação do cenário

Antes de automatizar esta jornada, o plano deve incluir a escolha explícita das unidades e dos usuários do cenário.

### Escolha das unidades

Devem ser definidos:

- uma `SECAO` que será a unidade principal do subprocesso;
- uma `COORDENADORIA` imediatamente superior à seção;
- uma `SECRETARIA` imediatamente superior à coordenadoria.

Critérios para essa escolha:

- a cadeia hierárquica deve existir de forma estável no `seed` usado pelos testes e2e;
- a `SECAO` deve participar diretamente do processo;
- a `COORDENADORIA` e a `SECRETARIA` devem atuar como níveis sucessivos de análise;
- a cadeia escolhida deve ser simples de localizar e entender por quem estiver lendo ou mantendo o teste.

### Escolha dos usuários

Devem ser definidos:

- um usuário `CHEFE` da `SECAO`;
- um usuário `GESTOR` da `COORDENADORIA`;
- um usuário `GESTOR` da `SECRETARIA`;
- um usuário `ADMIN`.

Critérios para essa escolha:

- cada usuário precisa ter o perfil esperado no `seed`;
- deve ficar claro, no nome das variáveis e do roteiro, qual papel cada usuário representa;
- se algum usuário acumular mais de um perfil, o teste deve deixar explícito qual perfil será selecionado no login.

### Resultado esperado desta preparação

Ao final dessa definição, o roteiro deve registrar nominalmente:

- a sigla da `SECAO`;
- a sigla da `COORDENADORIA`;
- a sigla da `SECRETARIA`;
- o usuário de `CHEFE`;
- o usuário de `GESTOR` da coordenadoria;
- o usuário de `GESTOR` da secretaria;
- o usuário `ADMIN`.

### Escolha proposta para este roteiro

Para a primeira versão do teste geral, a escolha recomendada é:

- `SECAO`: `SECAO_111`
- `COORDENADORIA`: `COORD_11`
- `SECRETARIA`: `SECRETARIA_1`
- `CHEFE da seção`: `CHEFE_SECAO_111`
- `GESTOR da coordenadoria`: `GESTOR_COORD`
- `GESTOR da secretaria`: `GESTOR_SECRETARIA_1`
- `ADMIN`: `ADMIN_1_PERFIL`

Justificativas para esta escolha:

- a cadeia hierárquica está presente de forma clara no `seed`;
- os usuários já existem e já são usados em outros testes e capturas;
- o usuário da `SECRETARIA_1` já está mapeado para login com seleção explícita de perfil `GESTOR`, o que ajuda a tornar o cenário mais representativo;
- essa combinação evita depender de novos dados ou de ampliação imediata dos helpers.

## Papéis envolvidos

- `ADMIN_1_PERFIL`: cria processos, homologa cadastros, cria ou ajusta mapas, disponibiliza mapas e finaliza processos.
- `CHEFE_SECAO_111`: cadastra e revisa atividades e conhecimentos da `SECAO_111`; valida o mapa disponibilizado.
- `GESTOR_COORD`: faz a primeira análise do cadastro e da validação do mapa da `SECAO_111`, no contexto da `COORD_11`.
- `GESTOR_SECRETARIA_1`: faz a segunda análise do cadastro e da validação do mapa, no contexto da `SECRETARIA_1`, antes da homologação final pelo `ADMIN_1_PERFIL`.

## Roteiro principal

### Fase 1 - Criar e iniciar o processo de mapeamento

1. O `ADMIN_1_PERFIL` cria um novo processo do tipo `Mapeamento` para a `SECAO_111`.
   Resultado esperado: o processo nasce na situação `Criado`.

2. O `ADMIN_1_PERFIL` inicia o processo de mapeamento.
   Resultado esperado: o processo passa para `Em andamento` e o subprocesso da `SECAO_111` passa a existir na situação `Não iniciado`.

3. O `CHEFE_SECAO_111` acessa o subprocesso da sua unidade.
   Resultado esperado: o card `Atividades e conhecimentos` está disponível; o card `Mapa de competências` ainda não está liberado para uso normal pela `SECAO_111`.

### Fase 2 - Cadastrar atividades e conhecimentos no mapeamento

4. O `CHEFE_SECAO_111` abre a área de `Atividades e conhecimentos` e registra o cadastro inicial da `SECAO_111`.
   Resultado esperado: a seção consegue criar atividades e conhecimentos, com salvamento automático a cada alteração.

5. O `CHEFE_SECAO_111` conclui o cadastro e o disponibiliza para análise.
   Resultado esperado: o subprocesso muda para `Cadastro disponibilizado` e sai da edição do `CHEFE_SECAO_111`.

6. O `GESTOR_COORD` acessa, pela `COORD_11`, o cadastro disponibilizado da `SECAO_111` e registra aceite.
   Resultado esperado: o cadastro segue para a `SECRETARIA_1` para nova análise.

7. O `GESTOR_SECRETARIA_1` acessa o mesmo cadastro e registra novo aceite.
   Resultado esperado: o cadastro segue para homologação final pelo `ADMIN_1_PERFIL`.

8. O `ADMIN_1_PERFIL` acessa o cadastro e faz a homologação.
   Resultado esperado: o subprocesso muda para `Cadastro homologado` e o card `Mapa de competências` passa a ficar disponível para o `ADMIN_1_PERFIL`.

Checkpoint importante desta fase:

- o sistema deve impedir a disponibilização se existir atividade sem conhecimento associado;
- o histórico e as movimentações do subprocesso devem refletir o percurso entre `SECAO_111`, `COORD_11`, `SECRETARIA_1` e `ADMIN`;
- o `CHEFE_SECAO_111` não precisa executar ação de salvar manualmente.

### Fase 3 - Criar, disponibilizar e homologar o mapa do mapeamento

9. O `ADMIN_1_PERFIL` abre o card `Mapa de competências` e monta o primeiro mapa da `SECAO_111`.
   Resultado esperado: o `ADMIN_1_PERFIL` consegue criar ao menos uma competência e associá-la às atividades cadastradas.

10. O `ADMIN_1_PERFIL` disponibiliza o mapa para validação da `SECAO_111`.
   Resultado esperado: o subprocesso muda para `Mapa disponibilizado`.

11. O `CHEFE_SECAO_111` acessa o mapa disponibilizado e faz a validação.
    Resultado esperado: o subprocesso muda para `Mapa validado`.

12. O `GESTOR_COORD` acessa a validação do mapa da `SECAO_111` e registra aceite.
    Resultado esperado: a validação segue para a `SECRETARIA_1`.

13. O `GESTOR_SECRETARIA_1` acessa a validação do mapa e registra novo aceite.
    Resultado esperado: a validação segue para a análise final do `ADMIN_1_PERFIL`.

14. O `ADMIN_1_PERFIL` homologa o mapa.
    Resultado esperado: o subprocesso muda para `Mapa homologado`.

15. O `ADMIN_1_PERFIL` finaliza o processo de mapeamento.
    Resultado esperado: o processo passa para `Finalizado` e o mapa homologado se torna o mapa vigente da `SECAO_111`.

Checkpoint importante desta fase:

- o sistema deve impedir a disponibilização do mapa se houver competência sem atividade associada;
- o sistema deve impedir a disponibilização do mapa se houver atividade sem competência associada;
- após a disponibilização do mapa, `CHEFE_SECAO_111`, `GESTOR_COORD` e `GESTOR_SECRETARIA_1` podem visualizá-lo.

### Fase 4 - Criar e iniciar o processo de revisão correspondente

16. O `ADMIN_1_PERFIL` cria um novo processo do tipo `Revisão` para a mesma `SECAO_111`.
    Resultado esperado: a criação é permitida porque a `SECAO_111` já passou por um mapeamento finalizado e possui mapa vigente.

17. O `ADMIN_1_PERFIL` inicia o processo de revisão.
    Resultado esperado: o processo passa para `Em andamento` e o subprocesso nasce com uma cópia do mapa vigente da `SECAO_111`.

18. O `CHEFE_SECAO_111` acessa a revisão da `SECAO_111`.
    Resultado esperado: a tela de `Atividades e conhecimentos` já aparece preenchida com o cadastro vigente e exibe o botão `Impactos no mapa`.

### Fase 5 - Revisar o cadastro e homologar a revisão do cadastro

19. O `CHEFE_SECAO_111` altera o cadastro de modo a produzir impacto real no mapa.
    Resultado esperado: a revisão entra em andamento e o sistema registra as alterações automaticamente.

20. O `CHEFE_SECAO_111` consulta `Impactos no mapa` para entender o efeito das mudanças.
    Resultado esperado: o sistema mostra as atividades inseridas e/ou as competências impactadas.

21. O `CHEFE_SECAO_111` disponibiliza a revisão do cadastro.
    Resultado esperado: o subprocesso muda para `Revisão do cadastro disponibilizada`.

22. O `GESTOR_COORD` acessa, pela `COORD_11`, a revisão disponibilizada da `SECAO_111`, consulta os impactos e registra aceite.
    Resultado esperado: a revisão segue para a `SECRETARIA_1`.

23. O `GESTOR_SECRETARIA_1` acessa a revisão disponibilizada, consulta os impactos e registra novo aceite.
    Resultado esperado: a revisão segue para análise final do `ADMIN_1_PERFIL`.

24. O `ADMIN_1_PERFIL` acessa a revisão disponibilizada, consulta os impactos e homologa o cadastro revisado.
    Resultado esperado: o subprocesso muda para `Revisão do cadastro homologada`.

Checkpoint importante desta fase:

- o sistema deve impedir a disponibilização da revisão quando não houver mudança real, salvo no caso explícito de disponibilização sem mudanças;
- o botão `Impactos no mapa` deve estar disponível para `CHEFE_SECAO_111`, `GESTOR_COORD`, `GESTOR_SECRETARIA_1` e `ADMIN_1_PERFIL` nos pontos previstos do fluxo;
- a revisão não deve seguir direto para conclusão do processo se houver impacto no mapa.

### Fase 6 - Ajustar, disponibilizar e homologar o mapa revisado

25. O `ADMIN_1_PERFIL` abre o card `Mapa de competências` da revisão e consulta novamente `Impactos no mapa`.
    Resultado esperado: o impacto identificado no cadastro revisado aparece também como subsídio para o ajuste do mapa.

26. O `ADMIN_1_PERFIL` ajusta o mapa para refletir o novo cadastro revisado.
    Resultado esperado: novas atividades ficam associadas a competências e o mapa passa a representar corretamente a revisão.

27. O `ADMIN_1_PERFIL` disponibiliza o mapa ajustado.
    Resultado esperado: o subprocesso muda para `Mapa disponibilizado`.

28. O `CHEFE_SECAO_111` acessa o mapa revisado e realiza a validação.
    Resultado esperado: o subprocesso muda para `Mapa validado`.

29. O `GESTOR_COORD` acessa a validação do mapa revisado da `SECAO_111` e registra aceite.
    Resultado esperado: a validação segue para a `SECRETARIA_1`.

30. O `GESTOR_SECRETARIA_1` acessa a validação do mapa revisado e registra novo aceite.
    Resultado esperado: a validação segue para o `ADMIN_1_PERFIL`.

31. O `ADMIN_1_PERFIL` homologa o mapa revisado.
    Resultado esperado: o subprocesso muda para `Mapa homologado`.

### Fase 7 - Finalizar o processo de revisão

32. O `ADMIN_1_PERFIL` finaliza o processo de revisão.
    Resultado esperado: o processo passa para `Finalizado` e o mapa revisado se torna o novo mapa vigente da `SECAO_111`.

33. O `CHEFE_SECAO_111`, o `GESTOR_COORD`, o `GESTOR_SECRETARIA_1` e o `ADMIN_1_PERFIL` podem consultar o resultado final do processo.
    Resultado esperado: o processo e o mapa final ficam acessíveis em modo de consulta, respeitando as permissões de cada perfil.

## Resultado final esperado da jornada

Ao fim do roteiro:

- o processo de mapeamento está `Finalizado`;
- o processo de revisão correspondente também está `Finalizado`;
- o mapa vigente da `SECAO_111` reflete o conteúdo revisado;
- todas as transições principais de cadastro e mapa foram exercitadas com participação de `CHEFE_SECAO_111`, `GESTOR_COORD`, `GESTOR_SECRETARIA_1` e `ADMIN_1_PERFIL`.

## Lacunas atuais em relação ao teste `jornada.spec.ts`

O teste atual já cobre partes importantes desta jornada, mas ainda não cobre o ciclo completo de revisão. Em especial, ainda falta consolidar de forma explícita:

- a finalização do processo de mapeamento como pré-condição da revisão;
- o uso de uma revisão com impacto real no mapa;
- o ajuste do mapa durante a revisão;
- a disponibilização, validação e homologação do mapa revisado;
- a finalização do processo de revisão.

## Cenários complementares importantes

Os itens abaixo são relevantes para a cobertura funcional do sistema, mas não precisam fazer parte do mesmo roteiro principal:

- devolução do cadastro por `GESTOR_COORD`, `GESTOR_SECRETARIA_1` ou `ADMIN_1_PERFIL` para ajustes;
- apresentação de sugestões no mapa, em vez de validação direta;
- devolução da validação do mapa para ajustes;
- disponibilização da revisão sem mudanças;
- operações em bloco;
- reabertura de cadastro ou de revisão;
- painel administrativo de notificações e reenvio de e-mail.

## Referências de requisitos usadas neste roteiro

- [Informações gerais](/C:/sgc/etc/reqs/_intro.md)
- [CDU-04 - Iniciar processo de mapeamento](/C:/sgc/etc/reqs/cdu-04.md)
- [CDU-05 - Iniciar processo de revisão](/C:/sgc/etc/reqs/cdu-05.md)
- [CDU-07 - Detalhar subprocesso](/C:/sgc/etc/reqs/cdu-07.md)
- [CDU-08 - Manter cadastro de atividades e conhecimentos](/C:/sgc/etc/reqs/cdu-08.md)
- [CDU-09 - Disponibilizar cadastro de atividades e conhecimentos](/C:/sgc/etc/reqs/cdu-09.md)
- [CDU-10 - Disponibilizar revisão do cadastro](/C:/sgc/etc/reqs/cdu-10.md)
- [CDU-12 - Verificar impactos no mapa de competências](/C:/sgc/etc/reqs/cdu-12.md)
- [CDU-13 - Analisar cadastro de atividades e conhecimentos](/C:/sgc/etc/reqs/cdu-13.md)
- [CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos](/C:/sgc/etc/reqs/cdu-14.md)
- [CDU-15 - Manter mapa de competências](/C:/sgc/etc/reqs/cdu-15.md)
- [CDU-16 - Ajustar mapa de competências](/C:/sgc/etc/reqs/cdu-16.md)
- [CDU-17 - Disponibilizar mapa de competências](/C:/sgc/etc/reqs/cdu-17.md)
- [CDU-19 - Validar mapa de competências](/C:/sgc/etc/reqs/cdu-19.md)
- [CDU-20 - Analisar validação de mapa de competências](/C:/sgc/etc/reqs/cdu-20.md)
- [CDU-21 - Finalizar processo de mapeamento ou de revisão](/C:/sgc/etc/reqs/cdu-21.md)
