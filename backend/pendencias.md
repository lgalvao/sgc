# Pendências da Padronização para Português-BR

Este documento lista as tarefas restantes para concluir a tradução completa do módulo de backend para o português brasileiro idiomático.

## Status Atual
- **Pacotes Concluídos:** `comum`, `alerta`, `notificacao`.
- **Pacotes Parcialmente Alterados:** `unidade`, `sgrh`, `processo`, `subprocesso` (apenas para corrigir erros de compilação).
- **Estado dos Testes:** Atualmente, a suíte de testes **não compila** devido às alterações parciais. É necessário corrigir os testes dos pacotes já traduzidos e dos que foram alterados para corrigir dependências.

## Tarefas Pendentes

### 1. Corrigir a Compilação dos Testes
- **Objetivo:** Fazer a suíte de testes (`./gradlew :backend:test`) compilar e passar.
- **Ações:**
  - Corrigir todas as referências a classes e métodos renomeados nos arquivos de teste (`*Test.java`).
  - Atualizar os nomes das classes de teste e dos métodos de teste para refletir as traduções.

### 2. Concluir a Padronização dos Pacotes Restantes
Os seguintes pacotes ainda precisam ser traduzidos:

- **`unidade` e `sgrh`**:
  - [ ] Concluir a revisão e tradução de todas as classes, DTOs e serviços.
  - [ ] Atualizar o `README.md` do pacote `sgrh` para refletir as renomeações.

- **`atividade`**:
  - [ ] Traduzir `AnaliseCadastro`, `AnaliseValidacao`, `AtividadeController`, `AtividadeDTO`, etc.
  - [ ] Atualizar o `README.md` do pacote.

- **`competencia`**:
  - [ ] Traduzir `Competencia`, `CompetenciaAtividade`, `CompetenciaController`, `CompetenciaDTO`, etc.
  - [ ] Atualizar o `README.md` do pacote.

- **`conhecimento`**:
  - [ ] Traduzir `Conhecimento`, `ConhecimentoController`, `ConhecimentoDTO`, etc.
  - [ ] Atualizar o `README.md` do pacote.

- **`mapa`**:
  - [ ] Traduzir `Mapa`, `MapaController`, `MapaService`, `CopiaMapaService`, DTOs, etc.
  - [ ] Atualizar o `README.md` do pacote.

- **`processo` e `subprocesso`**:
  - [ ] Realizar uma tradução completa e sistemática desses pacotes, que foram alterados apenas para corrigir a compilação.
  - [ ] Garantir que todas as interações entre os serviços estejam consistentes.
  - [ ] Atualizar os `README.md` desses pacotes.

### 3. Revisão Geral
- **Objetivo:** Após a conclusão de todas as traduções, fazer uma revisão geral para garantir a consistência da terminologia em todo o backend.
- **Ações:**
  - Verificar a consistência de termos técnicos (e.g., `DTO`, `Service`, `Repository`).
  - Garantir que a nomenclatura de métodos e variáveis seja uniforme.

### 4. Execução Final dos Testes
- **Objetivo:** Rodar a suíte de testes completa para garantir que a aplicação está estável após todas as alterações.

Com a conclusão dessas tarefas, a padronização do backend estará completa.