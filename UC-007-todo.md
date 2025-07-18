# UC-007 - Manter Mapa de Competências: Lista de Tarefas Pendentes

## Tarefas a Implementar

1. **Visualização do mapa em modo somente leitura (CHEFE/GESTOR)**

   - Implementar tela para exibir o mapa de competências sem permitir edição.
   - Integrar o botão "Visualizar mapa" em DetalheUnidade.vue para navegar para essa tela.

2. **Histórico de mapas de competências**

   - Criar tela/listagem para consultar versões anteriores do mapa por unidade.
   - Permitir navegação entre mapas atuais e históricos.

3. **Fluxo de validação do mapa pela unidade**

   - Simular ação de validação (ex: botão "Validar" para CHEFE).
   - Atualizar status do mapa para "validado" e registrar data/usuário.

4. **Disponibilização e notificação em lote**

   - Permitir disponibilizar mapas para múltiplas unidades de uma vez.
   - Simular envio de notificação consolidada para unidades superiores.

5. **Aprimorar tela Mapas.vue**

   - Exibir mapas reais do store, com filtros por unidade e status.
   - Permitir acesso rápido ao histórico e à visualização.

6. **Aprimorar feedbacks e mensagens**

   - Exibir mensagens claras de sucesso, erro e status em todas as etapas do fluxo.

7. **Simular permissões por perfil**
   - Restringir ações de edição/validação conforme o perfil (SEDOC, CHEFE, GESTOR).
   - Exibir apenas as opções permitidas para cada perfil em cada tela.
