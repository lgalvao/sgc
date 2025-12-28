# Plano de Adoção de Testes Baseados em Propriedade (PBT)

## 1. Visão Geral e Objetivos

Este documento descreve a estratégia para introduzir **Testes Baseados em Propriedade (Property-Based Testing - PBT)** no projeto SGC. O objetivo é aumentar a robustez dos testes unitários, saindo da verificação baseada em exemplos fixos para a verificação de **invariantes** e **comportamentos** que devem ser verdadeiros para qualquer entrada válida gerada.

### Benefícios Esperados
- **Cobertura de Casos de Borda:** Detecção de erros em cenários não previstos (ex: strings vazias, números negativos, datas bissextas, caracteres unicode) que testes baseados em exemplos costumam perder.
- **Documentação Executável:** As propriedades descrevem *o que* o código deve fazer em um nível mais abstrato (regras de negócio).
- **Redução de Código de Teste:** Um único teste de propriedade pode substituir dezenas de testes de exemplo parametrizados.

---

## 2. Ferramentas e Dependências

As ferramentas já foram identificadas e instaladas.

### 2.1 Backend (Java)
- **Biblioteca:** [JQwik](https://jqwik.net/)
- **Status:** Instalado (`net.jqwik:jqwik:1.9.2`).
- **Integração:** Funciona nativamente com JUnit 5.

### 2.2 Frontend (TypeScript/Vue)
- **Biblioteca:** [fast-check](https://fast-check.dev/)
- **Status:** Instalado (`fast-check@4.5.2`).

---

## 3. Estratégia de Implementação (Backend)

A implementação será gradual, focando primeiro em utilitários puros e lógica de domínio central.

### 3.1 Candidatos Iniciais

| Categoria | Classe/Componente | Propriedade a Testar (Exemplo) | Prioridade |
|-----------|-------------------|--------------------------------|------------|
| **Utilitários** | `sgc.comum.util.FormatadorData` | Formatação de nulos retorna "-"; Data válida nunca retorna nulo/vazio. (Implementado) | Concluído |
| **Domínio** | `sgc.mapa.mapper.MapaCompletoMapper` | Preservação de dados na conversão Entidade <-> DTO (Round Trip). (Implementado) | Concluído |
| **Negócio** | `sgc.processo.service.ProcessoService` (Regras de Estado) | Transições de `SituacaoProcesso` inválidas devem sempre falhar, independente dos dados do processo. (POC Implementada) | Concluído |
| **Segurança** | `sgc.seguranca.Sanitizacao` (HtmlPolicy) | Idempotência: `sanitizar(sanitizar(x)) == sanitizar(x)`; Tags proibidas nunca estão presentes na saída. | Alta |

---

## 4. Estratégia de Implementação (Frontend)

No frontend, o foco será em validadores de formulário e helpers de exibição.

### 4.1 Candidatos Iniciais

| Categoria | Componente/Função | Propriedade a Testar | Prioridade |
|-----------|-------------------|----------------------|------------|
| **Helpers** | Formatação de CPF/CNPJ | `format(strip(x)) == format(x)`; Strings aleatórias não quebram o formatter. (Implementado) | Concluído |
| **Validação** | Regras de validação customizadas | Entradas inválidas (ex: emails malformados gerados) sempre retornam `false`. | Alta |
| **Componentes** | Componentes de Tabela/Lista | Renderização com arrays de tamanhos variados (0 a 1000 itens) não quebra a UI. | Média |

---

## 5. Plano de Execução

### Fase 1: Configuração e POC (Concluído)
1.  **Frontend:** Instalar `fast-check`. (Feito)
2.  **Backend:** Criar teste de propriedade para `sgc.comum.util.FormatadorData`. (Feito)
3.  **Frontend:** Criar teste de propriedade para `formatarCpf`. (Feito)

### Fase 2: Domínio Crítico (Concluído)
1.  Identificar lógica de validação complexa nos Services de `Processo` e `Mapa`.
2.  Implementar testes gerativos para transições de estado de `Processo` (Feito em `ProcessoServicePropertyTest`).
3.  Implementar teste de propriedade para invariantes de `Mapa` (Feito em `MapaCompletoMapperPropertyTest` cobrindo consistência de DTOs).

### Fase 3: Integração no CI/CD
1.  Garantir que os testes de propriedade rodem com os comandos padrão (`./gradlew test`, `npm run test:unit`). (Verificado: ambos rodam com os comandos padrão)
2.  Configurar o "Seed" do gerador aleatório nos logs de falha para permitir reprodução determinística de erros encontrados.

## 6. Boas Práticas Adotadas

1.  **Separação:** Testes de propriedade podem ficar na mesma pasta dos testes unitários, mas recomenda-se o sufixo `PropertyTest` (Java) ou `.prop.spec.ts` (JS) se forem muito extensos.
2.  **Determinismo:** Sempre logar a semente (seed) usada na geração para reproduzir falhas.
3.  **Performance:** Testes de propriedade podem ser lentos se gerarem milhares de casos. Manter o número de execuções (samples) razoável para testes de CI (ex: 100 execuções).

## 7. Próximos Passos
- Implementar PBT para `sgc.seguranca.Sanitizacao`.
- Implementar PBT para validações de formulário no Frontend.
