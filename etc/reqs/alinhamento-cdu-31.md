# Alinhamento CDU-31 - Configurar sistema

## Cobertura atual do teste
O teste E2E (cdu-31.spec.ts) cobre:
- Navegação para página de parâmetros via botão `btn-configuracoes`
- Verificação da URL `/parametros` e heading "Parâmetros"
- Visualização dos dois campos editáveis: "Dias para inativação de processos" e "Dias para indicação de alerta como novo"
- Verificação do botão "Salvar configurações"
- Preenchimento dos campos com valores (30 e 3)
- Clique em salvar e validação da mensagem "Configurações salvas."

## Lacunas em relação ao requisito
- **Falta validação da efetividade imediata**: O requisito especifica que "O efeito das configurações deve ser imediato", mas o teste não valida que as configurações alteradas (30 e 3 dias) foram realmente aplicadas no sistema ou persistidas para uso.
- **Falta teste de valores inválidos**: O requisito menciona que DIAS_INATIVACAO_PROCESSO deve ser "Valor inteiro, 1 ou mais". Não há validação de entrada de valores negativos, zero, decimais ou não-inteiros.
- **Falta verificação de valores iniciais**: O teste não verifica quais eram os valores das configurações ANTES da alteração.
- **Falta teste de múltiplas alterações**: Apenas um cenário de alteração é testado; não há validação de alterações subsequentes.

## Alterações necessárias no teste E2E
- Adicionar verificação do estado inicial dos campos de entrada antes da alteração
- Adicionar cenário de tentativa de preenchimento com valores inválidos (zero, negativo, letra) para confirmar validação
- Adicionar verificação de persistência: após salvar, recarregar a página e confirmar que os valores salvos (30 e 3) aparecem nos campos
- Adicionar verificação de que alterações subsequentes também funcionam
- Considerar adicionar cenário de teste com valores mínimos e máximos válidos

## Notas e inconsistências do requisito
- O requisito menciona "mostra mensagem de confirmação", mas a forma exata não é especificada (tooltip, snackbar, modal, etc.). O teste valida "Configurações salvas.", confirmando que é uma mensagem de texto simples.
- O requisito não especifica onde está o botão de configurações ("engrenagem na barra de navegação"), apenas que é um botão. O teste usa `btn-configuracoes`, que pode estar no menu de navegação, não especificando sua localização exata na UI.
- Não está claro se as duas configurações são as ÚNICAS configurações do sistema ou se há outras que não devem ser alteradas pelo usuário.
