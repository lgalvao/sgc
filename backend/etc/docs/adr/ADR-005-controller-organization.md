# ADR-005: Organização de Controllers

**Status:** 🔄 Em Revisão (Atualizado 2026-02-24)

---

## Contexto e Problema

O módulo `subprocesso` possui 4 controllers distintos organizados por workflow phase:

1. **SubprocessoCrudController** — CRUD básico, permissões, busca
2. **SubprocessoCadastroController** — Disponibilizar, devolver, aceitar, homologar
3. **SubprocessoMapaController** — Edição de mapa, impactos, salvamento
4. **SubprocessoValidacaoController** — Validação, sugestões, homologação

Todos usam o mesmo `SubprocessoFacade`.

---

## Decisão Original (2026-02-16)

✅ Manter 4 controllers separados por workflow phase, priorizando SRP and navigability.

---

## Reavaliação (2026-02-24)

Após diagnóstico de sobre-engenharia no sistema (ver ADR-008), reavaliamos esta decisão considerando a escala real: **5-10 usuários simultâneos, equipe única**.

### Problemas Identificados com a Separação

1. **Fragmentação desnecessária** — 4 controllers + facade + 8+ services para um único domínio
2. **Overhead cognitivo** — Desenvolvedores precisam saber em qual controller cada endpoint está
3. **Controllers "thin"** — Cada controller só delega para a facade, sem lógica própria
4. **Não justifica SRP** — Controllers REST são "handlers de rota" — agrupá-los por domínio é igualmente válido
5. **Escala não justifica** — Para uma equipe de 1-3 devs, navegabilidade por arquivo não é gargalo

### Nova Direção

Para módulos onde os controllers são thin (apenas delegam para facade/services sem lógica significativa):

- ✅ **1 controller por domínio** é aceitável e preferível
- ✅ Organizar endpoints em seções com comentários quando necessário
- ✅ Manter arquivos com até ~400 linhas (limite pragmático)

Para módulos com lógica significativa nos controllers:

- ✅ Separar continua sendo válido se cada controller tem responsabilidade comprovadamente distinta
- ❌ Não separar arbitrariamente apenas para "ter arquivos pequenos"

---

## Consequências da Mudança

### Positivas ✅

- Menos arquivos para navegar
- Menos indireção
- Mais fácil encontrar "onde está o endpoint X"
- Consistente com a simplificação geral (ADR-008)

### Negativas ❌

- Controller resultante pode ter ~300-400 linhas
  - *Mitigação:* Ainda gerenciável; prefira seções claras com comentários

---

## Referências

- ADR-001: Facade Pattern
- ADR-008: Decisões de Simplificação
