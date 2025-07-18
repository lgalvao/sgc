# Especificação do Fluxo de Mapeamento de Competências Técnicas — SGC

## Visão Geral

O SGC é um sistema para apoiar a SEDOC (Seção de Desenvolvimento Organizacional e Capacitação) no mapeamento de competências técnicas das unidades do TRE-PE. O sistema organiza o fluxo desde o cadastro de atividades/conhecimentos pelas unidades até a criação, validação e disponibilização dos mapas de competências técnicas.

---

## Perfis de Usuário
- **SEDOC (Admin):**
  - Cria processos de mapeamento.
  - Analisa atividades/conhecimentos cadastrados pelas unidades.
  - Cria e associa competências às atividades de cada unidade.
  - Disponibiliza mapas de competências para validação.
  - Pode reabrir etapas, validar ou devolver cadastros.
  - Gerencia o fluxo completo e acompanha o andamento pelo dashboard.
- **CHEFE:**
  - Cadastra atividades e conhecimentos da sua unidade.
  - Pode importar atividades/conhecimentos de outras unidades.
  - Indica a finalização do cadastro.
  - Recebe notificações sobre prazos e devoluções.
- **GESTOR:**
  - Visualiza informações da sua unidade e das subordinadas.
  - Não cadastra atividades/conhecimentos.
  - Pode validar (subir para unidade superior) ou devolver (descer para unidade subordinada) os cadastros.
  - Atua como elo de validação e acompanhamento do fluxo.

---

## Etapas do Fluxo de Mapeamento

### 1. Criação do Processo
- SEDOC cria um novo processo de mapeamento, seleciona unidades participantes e define data limite para cadastro de atividades/conhecimentos.
- O sistema permite múltiplos processos simultâneos.
- SEDOC acompanha o andamento de todas as unidades em um dashboard hierárquico.

### 2. Cadastro de Atividades e Conhecimentos
- SEDOC inicia a etapa e notifica as unidades participantes.
- CHEFE cadastra atividades e conhecimentos da sua unidade (ou importa de outras unidades).
- CHEFE indica finalização do cadastro, bloqueando a edição.
- Sistema notifica a unidade superior sobre a conclusão.
- GESTOR acompanha o andamento das subordinadas, podendo validar ou devolver cadastros.
- O fluxo de validação sobe na hierarquia até chegar à SEDOC.
- SEDOC pode reabrir etapas para ajustes, com notificações para unidades superiores.

### 3. Criação do Mapa de Competências
- SEDOC analisa todas as atividades e conhecimentos cadastrados por cada unidade.
- Com base nessa análise, SEDOC cria uma lista de competências para cada unidade, associando cada competência a uma ou mais atividades.
- O trabalho da SEDOC é intelectual: envolve conversão das informações técnicas em competências gerenciais voltadas à capacitação/desenvolvimento.

### 4. Disponibilização e Validação do Mapa de Competências

- SEDOC disponibiliza o mapa de competências para validação pelas unidades, informando prazos.
- Chefes podem validar ou sugerir melhorias no mapa.
- Gestores acompanham e ratificam ou devolvem para ajustes.
- SEDOC analisa sugestões, faz ajustes e redisponibiliza o mapa até que todos estejam validados.
- Após validação de todos, o processo é concluído e os mapas ficam disponíveis para as unidades.

---

## Situações e Estados
- **Processos e unidades**: Não iniciado, Em andamento, Finalizado (calculados pelo sistema).
- **Validação do mapa**: Não iniciada, Aguardando validação, Com sugestões, Validado.
- O sistema controla bloqueios, notificações e liberações conforme o estado de cada etapa.

---

## Implicações para o Protótipo
- **Painel.vue** deve evoluir para um painel de monitoramento real, mostrando situações e alertas.
- **Controle de permissões**: cada tela/ação deve ser acessível apenas ao perfil correto (SEDOC, CHEFE, GESTOR), mesmo que simulado.
- **Validação e devolução**: telas e ações para GESTOR validar ou devolver cadastros, com fluxo subindo/descendo na hierarquia.
- **Notificações e situações**: devem ser simulados no protótipo para ilustrar o fluxo real.
- **Dados de amostra**: organizados em arquivos JSON para facilitar ajuste fino e simulação de dados reais.

---

## Observações
- Todo o código e comentários estão em português.
- O sistema é um protótipo, focado em simular o fluxo e experiência de uso, mas já preparado para evoluir para um sistema real. 