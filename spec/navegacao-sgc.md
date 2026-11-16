# Manual de Navegação - SGC

## Índice

- [Estrutura Principal](#estrutura-principal)
- [Fluxo de Autenticação](#fluxo-de-autenticação)
- [Navegação entre Processos e Unidades](#navegação-entre-processos-e-unidades)
- [Gerenciamento de Atividades](#gerenciamento-de-atividades)
- [Padrões de Interface](#padrões-de-interface)
- [Boas Práticas](#boas-práticas)

## Estrutura Principal

- **URL Base**: `http://localhost:8080`
- **Páginas Principais**:
    - Dashboard (`/`)
    - Processos (`/processo`)
    - Unidades (`/unidade`)
    - Atividades por Unidade (`/processo-unidade/{id}/atividades`)

## Fluxo de Autenticação

1. **Página de Login** (`/login`)
    - Campos obrigatórios:
        - Usuário
        - Senha
    - Ação: Clicar em "Entrar"
    - Comportamento: Redireciona para o dashboard após autenticação bem-sucedida

## Navegação entre Processos e Unidades

### Lista de Processos

- Acesso: Menu "Processos"
- Informações exibidas por processo:
    - Nome
    - Data limite
    - Número de unidades participantes
    - Progresso (%)
- Ação: Clicar em um processo para ver detalhes

### Detalhes do Processo

- Informações exibidas:
    - Nome do processo
    - Data limite
    - Situação
    - Estatísticas (Total de Unidades, Progresso)
- Lista de unidades participantes com:
    - Nome da unidade
    - Situação
    - Última atualização
- Ação: Clicar em uma unidade para acessar atividades

## Gerenciamento de Atividades

### Acesso às Atividades

- URL: `/processo-unidade/{id}/atividades`
- Exibe lista de atividades da unidade
- Botão "Adicionar Atividade" para nova atividade

### Adicionar Nova Atividade

1. Clicar em "Adicionar Atividade" (botão com `value="iniciarAdicionarAtividade"`)
2. Preencher o campo de descrição (seletor `input#novaAtividadeDescricao`)
3. Clicar em "Salvar" (botão com `value="salvarNovaAtividade"`)
4. Após salvar, a atividade aparece na lista com um input contendo a descrição

### Adicionar Conhecimentos

1. Para cada atividade, há um link "Adicionar Conhecimento" (seletor `a[data-testid^="btn-adicionar-conhecimento-"]`)
2. Clicar no link para expandir o formulário
3. Preencher a descrição do conhecimento (seletor `input[placeholder*="conhecimento"]`)
4. Clicar em "Salvar" (botão com texto "Salvar")
5. É possível adicionar múltiplos conhecimentos por atividade

## Estrutura do DOM e Seletores

### Atividades

- **Lista de Atividades**: Cada atividade é um item em uma lista
- **Input da Atividade**: `input[name^="atividades"][name$="descricao"]`
- **Botão Adicionar Atividade**: `button[value="iniciarAdicionarAtividade"]`
- **Botão Salvar Atividade**: `button[value="salvarNovaAtividade"]`

### Conhecimentos

- **Link Adicionar Conhecimento**: `a[data-testid^="btn-adicionar-conhecimento-"]`
- **Input do Conhecimento**: `input[placeholder*="conhecimento"]`
- **Botão Salvar Conhecimento**: `button:has-text("Salvar")`

## Padrões de Interface

### Cores

- **Azul (primary)**: Ações principais, links
- **Verde (success)**: Confirmações, ações bem-sucedidas
- **Vermelho (danger)**: Remoção, erros

### Componentes

- **Cards**: Agrupamento de informações relacionadas
- **Tabelas**: Listagem de itens com múltiplas propriedades
- **Formulários**: Campos de entrada com validação

### Botões de Ação

| Botão    | Cor      | Função                              |
|----------|----------|-------------------------------------|
| Salvar   | Azul     | Confirma alterações                 |
| Limpar   | Cinza    | Limpa os campos do formulário       |
| Cancelar | Vermelho | Retorna à tela anterior             |
| Remover  | Vermelho | Remove itens (solicita confirmação) |

## Boas Práticas

1. **Fluxo de Navegação**
    - Acesse Processos
    - Selecione um Processo
    - Escolha uma Unidade
    - Gerencie Atividades

2. **Gerenciamento de Dados**
    - Sempre clique em "Salvar" após fazer alterações
    - Verifique mensagens de confirmação
    - Utilize a opção "Limpar" para recomeçar um formulário

3. **Dicas de Uso**
    - Utilize os `data-testid` para automação de testes
    - Observe as mensagens de feedback do sistema
    - Em caso de erros, verifique os campos obrigatórios

## Boas Práticas para Testes

1. **Verificação de Atividades**
    - Após adicionar uma atividade, verifique se o input correspondente está visível:
      ```typescript
      await expect(page.locator(`input[value="${nomeDaAtividade}"]`).first()).toBeVisible();
      ```

2. **Seletores**
    - Prefira seletores por atributos específicos (`value`, `data-testid`) em vez de texto visível
    - Use seletores que sejam menos suscetíveis a mudanças na interface

3. **Fluxo de Adição**
    - Sempre aguarde a conclusão de uma operação antes de prosseguir
    - Verifique se os elementos estão visíveis/interagíveis antes de interagir com eles

4. **Testes Independentes**
    - Cada teste deve começar do zero, navegando até a página de atividades
    - Não dependa do estado de testes anteriores

## Boas Práticas Observadas

1. **Fluxo de Navegação**
    - Sempre seguir a hierarquia: Processo → Unidade → Atividades
    - Aguardar o carregamento completo de cada página antes de interagir

2. **Interação com Formulários**
    - Preencher todos os campos obrigatórios antes de salvar
    - Clicar no botão de confirmação apropriado (Salvar, Adicionar, etc.)
    - Aguardar a conclusão da operação antes de prosseguir

3. **Seletores**
    - Preferir `data-testid` para elementos interativos
    - Usar seletores por atributos específicos quando `data-testid` não estiver disponível
    - Evitar seletores muito genéricos que podem quebrar facilmente

4. **Verificações**
    - Verificar se os elementos estão visíveis antes de interagir
    - Confirmar que as ações tiveram o efeito esperado
    - Usar timeouts adequados para elementos que podem demorar para carregar

5. **Manutenção**
    - Manter os seletores organizados e documentados
    - Atualizar os testes quando a interface for alterada
    - Usar variáveis para valores reutilizáveis (como textos de botões, classes CSS)

---
*Documentação atualizada em: 09/07/2025*
