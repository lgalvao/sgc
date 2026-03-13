# Alinhamento CDU-30 - Manter administradores

## Cobertura atual do teste
O teste cobre:
- **Setup inicial**: Validação de acesso a página de administradores via botão de configurações.
- **Validação de UI**: Verificação de heading "Administradores", presença de tabela com dados (linha não vazia) e botão "Adicionar/Novo" visível e habilitado.

## Lacunas em relação ao requisito
**Lacunas críticas:**

1. **Falta teste de fluxo de adição completo**: O requisito descreve detalhadamente o fluxo de adição (passos 4-8), mas o teste não executa nenhuma adição. Não valida:
   - Clique no botão "Adicionar"
   - Abertura do modal com título "Adicionar administrador"
   - Preenchimento do campo de "título eleitoral"
   - Validação de existência do usuário
   - Validação de duplicação (se já é administrador)
   - Mensagem de sucesso "Administrador adicionado com sucesso!"

2. **Falta teste de fluxo de remoção completo**: O requisito descreve fluxo de remoção (passos 9-15), mas o teste não executa nenhuma remoção. Não valida:
   - Clique no ícone de exclusão
   - Abertura do modal com título "Confirmar remoção"
   - Mensagem de confirmação com nome do administrador
   - Validações de remoção (não remove a si mesmo, não é único)
   - Mensagens de erro correspondentes
   - Mensagem de sucesso "Administrador removido com sucesso!"

3. **Falta validação de campos da tabela**: O requisito (passo 2) especifica que a tabela mostra "nome, título de eleitor, matricula e unidade de lotação". O teste não valida a presença dessas colunas.

4. **Falta validação de modal de adição**: Não há validação do modal com:
   - Título "Adicionar administrador"
   - Campo de texto para título eleitoral
   - Botões "Cancelar" e "Adicionar"

5. **Falta validação de modal de remoção**: Não há validação do modal com:
   - Título "Confirmar remoção"
   - Mensagem dinâmica com nome do administrador
   - Botões "Cancelar" e "Remover"

6. **Falta validação de validações no fluxo de adição**: O requisito especifica (passo 7) que o sistema "valida se o usuário existe e se já é administrador. Se houver erro, exibe mensagem de erro". O teste não valida esses cenários de erro.

7. **Falta validação de validações no fluxo de remoção**: O requisito especifica (passo 12) validações de:
   - Não permitir remover a si mesmo
   - Não permitir remover se é o único administrador
   
   O teste não valida esses cenários de erro.

8. **Falta teste de cancelamento de adição**: Não há validação de clique em "Cancelar" no modal de adição.

9. **Falta teste de cancelamento de remoção**: Não há validação de clique em "Cancelar" no modal de remoção.

10. **Falta atualização dinâmica da lista**: Não há validação de que após adicionar/remover, a tabela é atualizada corretamente.

## Alterações necessárias no teste E2E
1. **Adicionar teste de adição de administrador**: 
   - Clicar em botão "Adicionar"
   - Validar que modal com título "Adicionar administrador" abre
   - Validar campo de texto para título eleitoral
   - Preenchimento de título eleitoral válido
   - Clique em "Adicionar"
   - Validar mensagem "Administrador adicionado com sucesso!"
   - Validar que novo administrador aparece na tabela

2. **Adicionar validação de colunas da tabela**: 
   - Validar presença de colunas: "Nome", "Título de eleitor", "Matrícula", "Unidade de lotação"

3. **Adicionar teste de erro ao adicionar usuário inexistente**: 
   - Preencher título eleitoral inválido
   - Clicar "Adicionar"
   - Validar mensagem de erro (usuário não existe)

4. **Adicionar teste de erro ao adicionar administrador duplicado**: 
   - Tentar adicionar usuário que já é administrador
   - Validar mensagem de erro (já é administrador)

5. **Adicionar teste de cancelamento de adição**: 
   - Abrir modal de adição
   - Preencher dados
   - Clicar "Cancelar"
   - Validar que modal fecha e nenhum administrador foi adicionado

6. **Adicionar teste de remoção de administrador**: 
   - Clicar ícone de remoção em um administrador
   - Validar que modal com título "Confirmar remoção" abre
   - Validar mensagem dinâmica com nome do administrador
   - Clicar "Remover"
   - Validar mensagem "Administrador removido com sucesso!"
   - Validar que administrador foi removido da tabela

7. **Adicionar teste de cancelamento de remoção**: 
   - Abrir modal de remoção
   - Clicar "Cancelar"
   - Validar que modal fecha e administrador continua na tabela

8. **Adicionar teste de validação: não remover a si mesmo**: 
   - Como ADMIN atualmente logado, tentar remover a si mesmo
   - Validar mensagem de erro específica (não pode remover a si mesmo)

9. **Adicionar teste de validação: não remover único administrador**: 
   - Em cenário com apenas um administrador, tentar removê-lo
   - Validar mensagem de erro (não pode remover único administrador)

## Notas e inconsistências do requisito
- **Ambiguidade em "título eleitoral" vs "título de eleitor"**: O requisito usa "título eleitoral" no passo de entrada (passo 6) mas "título de eleitor" na exibição (passo 2). Presume-se ser o mesmo campo.

- **Falta de clareza em "icone de exclusão"**: O requisito menciona (passo 9) "ícone de exclusão em um registro" mas não especifica qual ícone (X, lixeira, etc.).

- **Ambiguidade em validação de duplicação**: O requisito diz "valida se o usuário existe e se já é administrador", mas não especifica se essas são validações separadas (dois erros distintos) ou uma validação única.

- **Falta de informação sobre logout do usuário removido**: O requisito não especifica se um administrador removido é imediatamente deslogado ou se sua sessão continua válida até expirar.
