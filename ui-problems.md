# Relatório de Problemas de UI/UX

Este relatório documenta inconsistências e comportamentos estranhos identificados na interface do usuário através da análise de capturas de tela e do código-fonte.

## 1. Sobreposição de Notificações (Toast)

**Severidade:** Alta
**Screenshots:** `04-subprocesso--02-cadastro-atividades-vazio.png`, `04-subprocesso--20-cadastro-vazio-com-label-obrigatorio.png`
**Arquivo:** `frontend/src/App.vue`

### Problema
As notificações do sistema ("Toasts"), como "Processo iniciado", aparecem fixadas no topo da tela e centralizadas. Devido à margem superior (`mt-3`) e ao posicionamento `fixed-top`, elas sobrepõem diretamente a barra de navegação (breadcrumbs), impedindo que o usuário clique em links de navegação ou veja onde está enquanto a mensagem estiver visível.

### Código Responsável
Em `frontend/src/App.vue`:
```html
<div class="fixed-top w-100 d-flex justify-content-center mt-3" style="z-index: 2000; pointer-events: none;">
  <BAlert ... >
```

### Recomendação
Mover o container de alertas para baixo da barra de navegação ou para um canto da tela (ex: superior direito) para evitar obstruir a navegação principal. Alternativamente, garantir que o `z-index` e o posicionamento não conflitem com a área clicável dos breadcrumbs.

## 2. Estado Visual de Botões Desabilitados

**Severidade:** Média
**Screenshots:** `03-processo--10-botoes-desativados-form-vazio.png`
**Arquivo:** `frontend/src/views/CadProcesso.vue`

### Problema
Na tela de criação de processo, o botão "Iniciar processo" deve estar desabilitado quando o formulário está vazio. O código implementa a lógica `:disabled="isFormInvalid"`, porém, visualmente, o botão na captura de tela parece estar ativo (cor verde sólida), com pouca distinção visual de um botão habilitado. Isso pode confundir o usuário, que tentará clicar em um botão que não responde.

### Recomendação
Verificar se o estilo Bootstrap para botões desabilitados (`disabled`) está sendo aplicado corretamente ou se é necessário CSS personalizado para reduzir a opacidade e indicar claramente a inatividade (ex: cursor `not-allowed`).

## 3. Visibilidade da Ação "Nova Atividade"

**Severidade:** Baixa (UX)
**Screenshots:** `04-subprocesso--02-cadastro-atividades-vazio.png`, `04-subprocesso--20-cadastro-vazio-com-label-obrigatorio.png`
**Arquivo:** `frontend/src/views/CadAtividades.vue`

### Problema
Em um estado vazio (sem atividades cadastradas), a única indicação de como prosseguir é um campo de entrada de texto "Nova atividade" com um botão `+` pequeno. Embora funcional, falta um "Call to Action" mais evidente para guiar o usuário na primeira interação.

### Recomendação
Para listas vazias, exibir um estado vazio ilustrativo (ex: ícone centralizado com texto "Nenhuma atividade cadastrada") e um botão de ação mais proeminente, ou destacar o input de criação.

## 4. Inconsistência no Layout de Botões

**Severidade:** Baixa
**Screenshots:** `04-subprocesso--02-cadastro-atividades-vazio.png` vs `04-subprocesso--20-cadastro-vazio-com-label-obrigatorio.png`
**Arquivo:** `frontend/src/views/CadAtividades.vue`

### Problema
A barra de ferramentas superior exibe botões condicionalmente ("Histórico de análise", "Importar", "Disponibilizar"). A aparição dinâmica desses botões causa deslocamento dos demais, o que pode ser uma experiência visual inconsistente.

### Recomendação
Manter uma ordem fixa e previsível ou agrupar ações secundárias em um menu "Mais ações" (dropdown) para manter a interface limpa e estável.

## 5. Feedback de Erro de Validação

**Severidade:** Média
**Screenshots:** `04-subprocesso--25-detalhe-card-com-erro.png`

### Problema
O erro "Esta atividade não possui conhecimentos associados" é exibido como um alerta vermelho dentro do card. Embora visível, o campo para corrigir o erro ("Novo conhecimento") é um input padrão abaixo. A associação visual entre o erro e a ação de correção poderia ser reforçada.

### Recomendação
Garantir que o foco seja levado ao campo de correção ou destacar a borda do input de "Novo conhecimento" em vermelho para guiar o usuário diretamente à solução.
