# Plano de Implementação - Validação de Data na Disponibilização do Mapa

Garantir que a data limite para validação do mapa seja posterior à data de criação do processo, tanto no frontend quanto no backend.

## Mudanças Propostas

### Backend

---

#### [MODIFY] [Mensagens.java](file:///c:/sgc/backend/src/main/java/sgc/comum/Mensagens.java)
- Adicionar a constante `DATA_LIMITE_APOS_CRIACAO_PROCESSO = "A data limite deve ser posterior à data de criação do processo."`.

#### [MODIFY] [SubprocessoTransicaoService.java](file:///c:/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java)
- No método [executarDisponibilizacaoMapa](file:///c:/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java#204-243), adicionar uma verificação:
  ```java
  LocalDate dataCriacaoProcesso = sp.getProcesso().getDataCriacao().toLocalDate();
  if (!request.dataLimite().isAfter(dataCriacaoProcesso)) {
      throw new ErroValidacao(Mensagens.DATA_LIMITE_APOS_CRIACAO_PROCESSO);
  }
  ```

### Frontend

---

#### [MODIFY] [MapaView.vue](file:///c:/sgc/frontend/src/views/MapaView.vue)
- Passar a data de criação do processo para o componente `DisponibilizarMapaModal`.
  - Adicionar um computed `dataCriacaoProcesso`.
  - Atualizar a tag `<DisponibilizarMapaModal>`.

#### [MODIFY] [DisponibilizarMapaModal.vue](file:///c:/sgc/frontend/src/components/mapa/DisponibilizarMapaModal.vue)
- Adicionar a prop `dataCriacaoProcesso: string`.
- Atualizar a lógica do `watch` em `dataLimiteValidacao` para validar se a data é posterior à `dataCriacaoProcesso`.
- Exibir a mensagem de erro apropriada.

## Plano de Verificação

### Testes Automatizados
- Executar o teste de integração:
  `./gradlew :backend:test --tests sgc.integracao.CDU17IntegrationTest`
- Adicionar um novo caso de teste em [CDU17IntegrationTest.java](file:///c:/sgc/backend/src/test/java/sgc/integracao/CDU17IntegrationTest.java) que tenta disponibilizar um mapa com data anterior ou igual à criação do processo.

### Verificação Manual
1. Acessar a tela de mapa de um subprocesso.
2. Clicar em "Disponibilizar".
3. Tentar inserir uma data válida (futura) mas que seja anterior à criação do processo (se o processo foi criado hoje, o `@Future` já impede datas passadas, mas a validação deve ser consistente).
4. Verificar se a mensagem de erro aparece corretamente e o botão de confirmação é desabilitado.
