# Lições Aprendidas - Correção dos Testes E2E CDU-20

## 1. Importância dos Dados de Mock Completos

**Problema**: Os testes falhavam porque faltavam mapas de competência e subprocessos nos arquivos de mock, causando cartões desabilitados e navegação interrompida.

**Solução**: Sempre verificar e completar os dados de mock antes de executar testes. Arquivos afetados:
- `src/mocks/mapas.json` - mapas de competência
- `src/mocks/subprocessos.json` - subprocessos por unidade

**Lição**: Dados de mock incompletos levam a falsos positivos em testes, mascarando problemas reais na aplicação.

## 2. Seleção Robusta de Elementos nos Testes

**Problema**: Seleção baseada em índices (`nth()`) era frágil e falhava quando a ordem dos elementos mudava ou quando havia elementos duplicados.

**Solução**: Implementar seleção baseada em texto/IDs únicos:
```typescript
// Antes (frágil)
linhasProcesso.nth(indiceProcesso).click()

// Depois (robusto)
linhasProcesso.filter({hasText: processDescription}).first().click()
```

**Lição**: Preferir seletores baseados em conteúdo textual ou data-testid em vez de posições numéricas.

## 4. Compreensão da Arquitetura da Aplicação

**Problema**: Dificuldade inicial em identificar por que os cartões estavam desabilitados.

**Solução**: Analisar a cadeia de dependências:
- Stores Pinia (`mapas.ts`, `processos.ts`)
- Componentes Vue (`SubprocessoCards.vue`, `VisMapa.vue`)
- Dados de mock (`mapas.json`, `subprocessos.json`)

**Lição**: Mapear a arquitetura completa ajuda a debugar problemas mais rapidamente.

## 5. Testes de Modal e Interações

**Problema**: Títulos de modal com espaços extras e conteúdo incorreto.

**Solução**: Verificar exatamente o que a aplicação renderiza:
- Comparar strings de modal com o código fonte
- Usar seletores precisos para elementos de modal
- Validar estados visuais (visível/oculto)

**Lição**: Testes de UI devem refletir exatamente o comportamento da aplicação, não suposições.

## 6. Testes Baseados em Perfil de Usuário

**Problema**: Comportamentos diferentes para GESTOR e ADMIN não estavam sendo testados adequadamente.

**Solução**: Criar cenários específicos para cada perfil:
- GESTOR: Testa aceite com observações
- ADMIN: Testa homologação sem observações
- Usar unidades apropriadas para cada perfil

**Lição**: Testes devem cobrir todas as variações de perfil e suas permissões específicas.

## 7. Consistência de Dados de Teste

**Problema**: Dados de teste não refletiam a lógica de negócio da aplicação.

**Solução**: Garantir que:
- Situações de subprocesso correspondam aos requisitos
- Mapas existam para subprocessos ativos
- Hierarquia organizacional seja respeitada

**Lição**: Dados de teste devem ser realistas e consistentes com as regras de negócio.

## 8. Debugging Sistemático

**Problema**: Múltiplas falhas simultâneas dificultavam a identificação da causa raiz.

**Solução**: Abordagem passo-a-passo:
1. Executar testes individualmente
2. Verificar logs do Playwright
3. Inspecionar elementos na aplicação
4. Validar dados de mock
5. Testar componentes isoladamente

**Lição**: Debugging sistemático previne correções superficiais e identifica problemas fundamentais.

## 9. Documentação e Requisitos

**Problema**: Interpretações incorretas dos requisitos levaram a testes inadequados.

**Solução**: Referenciar documentação oficial:
- `regras/regras-projeto.md` - arquitetura da aplicação
- `reqs/` - requisitos funcionais
- `regras/regras-playwright.md` - boas práticas de testes e2e no contexto deste projeto

**Lição**: Manter documentação atualizada e consultá-la regularmente durante o desenvolvimento.

## 10. Prevenção de Regressões

**Problema**: Correções pontuais podem quebrar outros testes.

**Solução**: 
- Executar suite completa após correções
- Verificar impacto em testes relacionados
- Manter dados de mock consistentes

**Lição**: Correções devem ser validadas no contexto completo da aplicação.

## Recomendações para Futuros Testes E2E

. **Dados de Mock**: Manter dados de mock atualizados e completos. Criar novos dados sempre que necessário para testes.
. **Seletores Robustos**: Preferir data-testid e texto sobre posições
. **Cobertura Completa**: Testar todas as variações de perfil e estado
. **Debugging Estruturado**: Seguir abordagem sistemática para problemas
. **Documentação**: Manter requisitos e regras atualizadas
.. **Validação Contínua**: Executar testes regularmente durante desenvolvimento

Essas lições ajudarão a criar testes E2E mais robustos e confiáveis no futuro.