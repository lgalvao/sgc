# Melhoria contínua do toolkit `toolkit`

## Objetivo desta rodada

Consolidar problemas de manutenção detectados no toolkit e iniciar correções com maior impacto imediato em
confiabilidade operacional e experiência de uso da CLI.

## Diagnóstico resumido

1. **Ajuda e exemplos desatualizados em scripts legados**
    - Alguns comandos exibiam `comandoSgc` incompleto (sem prefixo `backend`), extensão antiga (`.cjs`) e caminhos não
      existentes.
    - Isso dificultava execução correta e confundia quem usa o script fora do roteador principal.

2. **Comando de jornada de cobertura backend órfão e quebrado**
    - O script `backend/cobertura-jornada.js` referenciava scripts inexistentes (`cobertura-analisar.js`,
      `cobertura-lacunas.js`, `cobertura-plano.js`).
    - O fluxo não estava integrado no `sgc.js`, apesar de haver indicação de uso no próprio texto de ajuda.

3. **Mensagens de uso inconsistentes**
    - `frontend/telas-capturar.js` e `projeto/arvore-linhas.js` mostravam comandos antigos (`node scripts/...`), sem
      refletir o entrypoint oficial `node toolkit/sgc.js`.
    - `frontend/mensagens-extrair.js` apontava “próximo passo” para um arquivo não existente.

4. **Configuração de auditoria de dependências com ruído**
    - `knip.json` mantinha `ignoreBinaries`/`ignoreDependencies` que o próprio Knip já indica como desnecessários.

## Melhorias já iniciadas nesta tarefa

- Correção de metadados de ajuda em scripts backend:
    - `backend/testes-analisar.js`
    - `backend/testes-priorizar.js`
    - `backend/testes-gerar-stub.js`
    - `backend/java-corrigir-fqn.js`
    - `backend/java-auditar-null.js`
    - `backend/java-instalar-certificados.js`
- Reescrita da jornada de cobertura em `backend/cobertura-jornada.js` para usar apenas scripts e artefatos atuais do
  toolkit.
- Integração da jornada no roteador principal (`sgc.js`) via `backend cobertura jornada`.
- Ajuste de mensagens de uso/próximos passos:
    - `frontend/telas-capturar.js`
    - `frontend/mensagens-extrair.js`
    - `projeto/arvore-linhas.js`
- Atualização de `README.md` do toolkit com o comando de jornada.
- Limpeza de `knip.json` removendo ignores desnecessários reportados pelo Knip.

## Próximas melhorias recomendadas (backlog)

1. **Padronizar parsing de argumentos**
    - Extrair helper compartilhado para leitura de flags/opções (`--chave valor` e `--chave=valor`) e validação de
      opções obrigatórias.

2. **Padronizar saída e tratamento de erro**
    - Reduzir `console.log/error` dispersos em scripts antigos, migrando gradualmente para utilitários comuns
      (`lib/saida.js` e logger).

3. **Cobertura de testes do toolkit além da CLI raiz**
    - Adicionar testes focados para scripts de alto risco de regressão (ex.: `cobertura-jornada`, `telas-capturar`,
      `arvore-linhas`) usando fixtures temporárias.

4. **Catalogar e tratar scripts legados fora do roteador**
    - Decidir explicitamente entre:
        - migrar para subcomandos do `sgc.js`, ou
        - marcar como legados/deprecados com prazo de remoção.

5. **Governança de compatibilidade**
    - Definir política para mudanças de interface (flags, output e exit code) e manter checklist de compatibilidade para
      evitar regressões silenciosas em automações.

## Rodada 2 (nomenclatura e legado)

### Padronização aplicada

- `frontend/test-ids-duplicados.js` ➜ `frontend/test-ids-listar-duplicados.js`
- `frontend/views-auditar-validacoes.js` ➜ `frontend/views-validacoes-auditar.js`
- `projeto/sincronizar-versao.js` ➜ `projeto/versao-sincronizar.js`

### Ajustes de integração

- `sgc.js` atualizado para usar os nomes canônicos.
- Comando canônico de views passou a ser `frontend views validacoes-auditar`.
- Comando canônico de versão no projeto passou a ser `projeto versao-sincronizar`.
- `README.md` atualizado para refletir o comando canônico.
- `.release-it.json` atualizado para a nova nomenclatura de sincronização de versão.

### Diretório de legado criado

- `toolkit/legado/frontend/views-auditar-validacoes.js`
- `toolkit/legado/frontend/test-ids-duplicados.js`
- `toolkit/legado/projeto/sincronizar-versao.js`

Os aliases legados emitem aviso de depreciação e delegam para os scripts canônicos, preservando compatibilidade de
execução direta enquanto a migração é concluída.
