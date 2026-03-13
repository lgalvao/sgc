# Alinhamento CDU-29 - Consultar histórico de processos

## Cobertura atual do teste
O teste cobre:
- **Cenário 1**: Navegação do ADMIN para página de histórico via link "Histórico" na navbar e validação de URL e heading.
- **Cenário 2**: Navegação do GESTOR para página de histórico e validação de URL e heading.
- **Cenário 3**: Navegação do CHEFE para página de histórico e validação de URL e heading.
- **Cenário 4**: Verificação de que a tabela de histórico contém colunas "Processo/Descrição" e "Tipo".

## Lacunas em relação ao requisito
**Lacunas críticas:**

1. **Falta validação de pré-condição de acesso**: O requisito especifica acesso apenas para "ADMIN/GESTOR/CHEFE", excluindo SERVIDOR. O teste valida três perfis mas não testa exclusão de SERVIDOR.

2. **Falta validação de filtro de processos finalizados**: O requisito (passo 2) especifica "tabela com **todos os processos com situação 'Finalizado'**". O teste não valida que apenas processos finalizados são exibidos ou que processos em andamento não aparecem.

3. **Falta de dados de teste com processos finalizados**: O teste não cria processos finalizados para exibir. Navegadores locais podem ter histórico vazio, invalidando o teste real.

4. **Falta validação de colunas específicas**: O requisito especifica quatro colunas:
   - `Processo` (descrição do processo)
   - `Tipo` (tipo do processo)
   - `Finalizado em` (data de finalização)
   - `Unidades participantes` (lista agregada)
   
   O teste apenas valida presença de "Processo/Descrição" e "Tipo", faltam validações de "Finalizado em" e "Unidades participantes".

5. **Falta validação de agregação de unidades**: O requisito especifica que "Unidades participantes" deve agregar unidades por hierarquia (mesma forma usada no Painel). O teste não valida isso.

6. **Falta cenário de clique em processo**: O requisito (passos 3-4) descreve que ao clicar em um processo, o sistema apresenta "Detalhes do processo" **sem permitir mudanças ou mostrar botões de ação**. O teste não valida isso.

7. **Falta validação de visualização read-only**: O teste não valida que a página de detalhes do histórico é read-only (sem botões de ação como "Aceitar mapas", "Alterar data", etc.).

8. **Falta teste com dados reais de histórico**: O teste só valida navegação e presença de headings, não valida conteúdo de processos finalizados reais.

## Alterações necessárias no teste E2E
1. **Adicionar teste de exclusão de SERVIDOR**: 
   - Fazer login como SERVIDOR
   - Tentar acessar `/historico`
   - Validar que é redirecionado ou acesso é negado

2. **Adicionar setup de processo finalizado**: 
   - Antes dos testes de histórico, criar e finalizar um processo
   - Garantir que há dados de teste reais na tabela de histórico

3. **Adicionar validação de todas as colunas**: 
   - Validar presença das colunas: "Processo", "Tipo", "Finalizado em", "Unidades participantes"

4. **Adicionar validação de agregação de unidades**: 
   - Criar processo com múltiplas unidades
   - Finalizar e verificar se aparecem agregadas conforme descrito

5. **Adicionar teste de clique em processo**: 
   - Clicar em um processo da tabela de histórico
   - Validar que navegou para página de detalhes
   - Validar que URL está correta (provavelmente `/processo/{codigo}/...`)

6. **Adicionar validação de read-only**: 
   - Na página de detalhes de processo finalizado, validar que:
     - Botões de ação ("Aceitar mapas", "Homologar", "Alterar data", etc.) **não aparecem**
     - Formulários/campos não são editáveis

7. **Adicionar validação de filtragem por situação**: 
   - Criar processo em andamento
   - Validar que não aparece no histórico
   - Finalizar processo
   - Validar que aparece no histórico

## Notas e inconsistências do requisito
- **Falta de clareza em "agregação de unidades"**: O requisito menciona agregação "da mesma forma usada no Painel" mas não especifica exatamente como é feita. Presumem-se agrupamentos por nível hierárquico.

- **Ambiguidade em "sem permitir mudanças"**: Não fica claro se "sem permitir mudanças" significa apenas leitura ou se inclui desabilitar interações secundárias (como expandir/colapsar unidades).

- **Falta de informação sobre paginação/scroll**: O requisito não especifica se há limite de processos exibidos ou se há paginação.
