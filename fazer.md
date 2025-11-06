## Problemas possíveis no diretório `helpers`

- Diretórios `/e2e/helpers/dados` e `/e2e/helpers/navegacao` e `/e2e/helpers/dados` contêm apenas um .ts. O outro é o index.ts, que reexporta. Isso é necessário?

- Avaliar se é possível reduzir o tempo de execução de testes do backend. Estão demorando cerca de 2 minutos

- A classe `UnidadeProcesso` parece não ser necessária. Por que nao fazer uma relacao many-to-many entre Processo e Unidade?

- Procurar pelo sistema se ainda há chamadas explícitas a 'sanitize' ou 'sanitizar' e evitar.

- Incluir o JSpecify no projeto e ativar o 'NullMarked' para todos os pacotes. Tratar questões de nulabilidade
  
- O uso do termo em ingles 'snapshot' está tirando a clareza. E tambem me parecem estranhos o uso de snapshots nos pontos em que estao sendo usados.
  
- Procurar a lançamento de 'IllegalArgumentException' e outras exceções do Java. Só deveria ser lançada exceções do sistema.

## Investigação realizada (2025-11-06)

### 1. Diretórios helpers ✅ Confirmado
- `/e2e/helpers/dados/`: contém `constantes.ts` + `index.ts`
- `/e2e/helpers/navegacao/`: contém `navegacao.ts` + `index.ts`
- Padrão comum mas poderia ser simplificado

### 2. Testes backend demorando ~2 minutos ⚠️ Necessário diagnosticar
- Executar com `./gradlew test --info` para profile detalhado

### 3. UnidadeProcesso como many-to-many 🔍 Merece refatoração
- Classe usada em: `PainelService`, `ProcessoDetalheMapperCustom`, `UnidadeProcessoRepo`
- Potencial eliminar entidade intermediária se sem atributos específicos

### 4. Chamadas sanitize/sanitizar ✅ Encontradas 7+ ocorrências
- `AlertaService`: `sanitizeHtml(descricao)`
- `RestExceptionHandler`: `sanitizar()` em 3+ locais
- `MapaController`: `HTML_SANITIZER_POLICY.sanitize()` em 2+ locais
- `HtmlSanitizingDeserializer`: Sanitização automática
- **TODO**: Centralizar via anotações/interceptadores

### 5. JSpecify + NullMarked ❌ Não implementado
- Nenhuma implementação detectada no projeto

### 6. Uso de "snapshot" confuso ✅ Confirmado
- `ProcessoService.java`: "Salvar snapshot das unidades participantes"
- `CDU21IntegrationTest.java`: "Create UnidadeProcesso snapshots"
- **TODO**: Renomear para "cópia" ou "estado"

### 7. IllegalArgumentException e exceções Java ✅ Encontradas
- `AtividadeService`: `IllegalStateException` ✅ FIXADO
- `EventoProcessoListener`: `IllegalStateException` ✅ FIXADO
- `RestExceptionHandler`: Handler para `IllegalArgumentException`
- **TODO**: Substituir por exceções customizadas do sistema

## Execução de tarefas (2025-11-06)

### ✅ 1. Renomeação de "snapshot" para "participação/associação"
- `ProcessoService.java`: Renomeado `criarSnapshotUnidadeProcesso()` → `criarAssociacaoUnidadeProcesso()`
- `ProcessoService.java`: Comentários atualizados de "snapshot" para "associação"
- `CDU21IntegrationTest.java`: Comentário atualizado

### ✅ 2. Substituição de exceções Java por customizadas
- Criada nova exceção: `ErroSituacaoInvalida.java`
- `AtividadeService.java`: `IllegalStateException` → `ErroSituacaoInvalida`
- `EventoProcessoListener.java`: `IllegalStateException` → `ErroEntidadeNaoEncontrada`
- `AtividadeServiceTest.java`: Teste atualizado para nova exceção
- ✅ Testes backend passando (335 testes) - BUILD SUCCESSFUL

### ✅ 3. Remoção de sanitização manual
- `AlertaService.java`: Removido método `sanitizeHtml()` (dados gerados internamente são seguros)
- `MapaController.java`: Removido método `sanitizarEMapearMapaDto()` e lógica de sanitização no controlador
- Sanitização centralizada permanece em `RestExceptionHandler` (para mensagens de erro) e `HtmlSanitizingDeserializer` (para DTOs de entrada)
- ✅ BUILD SUCCESSFUL