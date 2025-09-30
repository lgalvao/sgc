### Guia de Refatoração para Testes Semânticos com Playwright

**Objetivo:** transformar os testes E2E em narrativas de usuário claras, concentrando detalhes técnicos nas camadas inferiores de abstração.

---

#### Arquitetura em 3 camadas

- **Camada 1 – Constantes (`e2e/cdu/helpers/dados/constantes-teste.ts`)**
  - Centraliza seletores, textos visíveis, URLs e rótulos.
  - Nunca use strings literais de UI diretamente em ações, verificações ou testes.
  - Organize por objetos (`SELETORES`, `SELETORES_CSS`, `TEXTOS`, `URLS`, `ROTULOS`) e mantenha o idioma consistente.

- **Camada 2 – Linguagem de Domínio (`e2e/cdu/helpers/`)**
  - Subdiretórios: `acoes/`, `verificacoes/`, `navegacao/`, `dados/`, `utils/`.
  - Cada subdiretório possui um `index.ts` e `helpers/index.ts` reexporta tudo.
  - Diretrizes principais:
    - Ações descrevem intenções de negócio (ex.: `registrarAceiteRevisao`, `acessarAnaliseRevisaoComoGestor`).
    - Verificações encapsulam `expect` e expõem estados (“cadastro devolvido”, “histórico visível”).
    - Funções compostas reúnem fluxos de ponta a ponta (login, navegação e ação principal).
    - Todo identificador permanece em português, direto e sem preposições desnecessárias.

- **Camada 3 – Especificações (`e2e/cdu/cdu-XX.spec.ts`)**
  - Os testes chamam exclusivamente helpers semânticos da Camada 2.
  - Cada caso de uso deve ser lido como uma história curta sem ruído técnico.
  - Setup repetitivo vai para `test.beforeEach` ou helpers específicos.

---

#### Fluxo de refatoração

1. **Inspecionar o arquivo**
   - Mapear cenários de usuário e anti-padrões (IFs, locators diretos, `expect` técnicos, duplicações).

2. **Criar ou ajustar abstrações**
   - Adicionar strings ausentes em `TEXTOS`.
  - Implementar ações/verificações específicas no domínio apropriado.
  - Reexportar novas funções nos `index.ts` correspondentes.

3. **Reescrever o teste**
   - Substituir chamadas técnicas por helpers semânticos.
   - Garantir narrativa linear: pré-condição → ação → verificação.

4. **Revisar**
   - Remover imports não usados.
   - Ler o teste em voz alta e confirmar que parece um roteiro de usuário.
   - Confirmar ausência de condicionais, seletores e `expect` nos testes.

---

#### Convenções essenciais

- **Nomenclatura:** prefixos claros (`clicar`, `abrir`, `verificar`, `aguardar`); sufixos para contexto quando necessário (`...ComSucesso`, `...ComoGestor`).
- **Assinaturas:** `page: Page` sempre como primeiro parâmetro.
- **Modais:** padronizar helpers (`abrirDialogo...`, `confirmarModal`, `cancelarModal`) e reutilizá-los.
- **Strings:** toda mensagem exibida deve vir de `TEXTOS`; se a UI mudar, apenas a constante é atualizada.

---

#### Anti-padrões a eliminar

1. **Condicionais nos testes** → mover lógica para helpers.
2. **Verificações técnicas expostas** → encapsular em verificações semânticas.
3. **Uso direto de locators ou seletores** → criar ação/seleção na camada 2.
4. **Duplicação de expect** → consolidar em uma verificação composta.
5. **Importar `expect` nos testes** → proibido; expectativas devem viver nos helpers.

---

#### Checklist rápido

- [ ] Todos os valores de UI vêm de `TEXTOS/SELETORES`.
- [ ] Ações e verificações novas estão reexportadas nos índices.
- [ ] Testes chamam apenas funções semânticas.
- [ ] Não restam condicionais, loops ou verificações técnicas nos arquivos `spec`.
- [ ] Código lido em voz alta soa como comportamento de usuário.

---

#### Como acelerar futuras refatorações

1. **Checklist de preparação**
   Antes de editar o `spec`, confirme se as strings necessárias já estão em `TEXTOS`, liste os helpers que precisam ser criados/ajustados e planeje as reexportações. Isso evita correções posteriores de imports e reduz retrabalho.

2. **Batching disciplinado**
   Agrupe alterações por domínio (constantes → navegação → ações → verificações) e aplique cada grupo em um único diff, fazendo os `index.ts` logo após criar o helper. Só então reescreva o teste, garantindo fluxo linear.

3. **Templates/snippets prontos**
   Mantenha snippets para helpers de modal, navegação de cenário e verificações compostas, além de um esqueleto de teste semântico. O trabalho passa a ser “preencher lacunas”, acelerando a escrita e padronizando resultados.

---

#### Aprendizados recentes

- Verifique sempre os `index.ts` ao adicionar helpers (problemas de importação surgem quando a reexportação é esquecida).
- Prefira funções de cenário para perfis específicos (ex.: `acessarAnaliseRevisaoComoGestor/Admin`) para encapsular login + navegação.
- Centralize novos textos (`ACEITE_REGISTRADO`, `FECHAR`, `ACEITE_REVISAO_TITULO`) em `TEXTOS` antes de criar verificações.
- Utilize verificações compostas para mensagens + redirecionamentos (ex.: `verificarAceiteRegistradoComSucesso`).
- `cancelarModal` cobre múltiplos botões de fechamento; reutilize-o em vez de criar interações diretas com o modal.
- Fluxos de finalização exigem helpers dedicados para modal, bloqueios e notificações (ex.: `verificarModalFinalizacaoProcesso`, `verificarProcessoFinalizadoNoPainel`); centralize novos textos (`FINALIZACAO_BLOQUEADA`, `CONFIRMACAO_VIGENCIA_MAPAS`) em `TEXTOS`.
- Exemplo: CDU-21 (Finalização de processo) — Exemplo de refatoração completa: o arquivo `spec` foi convertido em narrativa semântica, todas as interações e verificações estão em helpers reexportados, e as strings de UI necessárias foram centralizadas em `TEXTOS`. Use este caso como padrão para futuras refatorações de fluxos de finalização.