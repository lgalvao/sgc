# Plano de Reorganização dos Diretórios de Teste

## 1. Contexto

Atualmente, a estrutura de testes do projeto está dividida em três locais (`spec/`, `tests/`, e `__tests__/`), o que a torna confusa e dificulta a compreensão da finalidade de cada tipo de teste.

- `spec/`: Contém os testes End-to-End (E2E).
- `tests/`: Contém arquivos auxiliares para os testes E2E.
- `__tests__/`: Contém os testes unitários (este padrão está correto e será mantido).

O objetivo desta refatoração é unificar e esclarecer a organização dos testes E2E.

## 2. Estrutura Proposta

A nova estrutura centralizará tudo relacionado aos testes E2E em um único diretório chamado `e2e`, que é um padrão de mercado.

### Estrutura Atual:
```
.
├── spec/
│   └── meu-teste.e2e.ts
└── tests/
    └── arquivo-auxiliar.ts
```

### Nova Estrutura:
```
.
└── e2e/
    ├── support/
    │   └── arquivo-auxiliar.ts
    └── meu-teste.e2e.ts
```

- **`e2e/`**: Conterá todos os arquivos de teste E2E.
- **`e2e/support/`**: Conterá todos os arquivos auxiliares, como *helpers*, *fixtures* ou comandos customizados.

## 3. Plano de Execução (Ambiente Windows)

1.  **Criar os novos diretórios:**
    ```cmd
    mkdir e2e\support
    ```

2.  **Mover os testes E2E:** Mover todo o conteúdo de `spec\` para `e2e\`. O comando `git mv` é ideal, pois já atualiza o rastreamento do Git.
    ```cmd
    git mv spec\* e2e\
    ```
    *(Nota: O caractere `*` pode não funcionar como esperado no `cmd.exe` padrão. Recomenda-se executar este comando em um terminal como Git Bash ou mover os arquivos manualmente.)*

3.  **Mover os arquivos auxiliares:** Mover todo o conteúdo de `tests\` para `e2e\support\`.
    ```cmd
    git mv tests\* e2e\support\
    ```

4.  **Remover os diretórios antigos:** Após mover os arquivos, os diretórios `spec` e `tests` estarão vazios e podem ser removidos.
    ```cmd
    rmdir spec tests
    ```

5.  **Atualizar a Configuração do Playwright:** Edite o arquivo `playwright.config.ts` para que ele aponte para o novo diretório de testes.

    **Mude esta linha:**
    ```typescript
    // Linha a ser alterada (o valor pode ser './spec' ou 'spec/')
    testDir: './spec'
    ```

    **Para esta:**
    ```typescript
    // Novo valor
    testDir: './e2e'
    ```

6.  **Verificação Final:** Rode a suíte de testes E2E para garantir que a nova configuração está funcionando corretamente.
    ```cmd
    npx playwright test
    ```

## 4. Benefícios

- **Clareza:** Qualquer desenvolvedor entenderá imediatamente que o diretório `e2e` contém os testes End-to-End.
- **Organização:** Todo o código relacionado a E2E (testes e arquivos de suporte) fica centralizado em um único local.
- **Padrão de Mercado:** Alinha o projeto com as convenções mais comuns da indústria de desenvolvimento.