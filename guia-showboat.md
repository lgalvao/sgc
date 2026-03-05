# Guia de Uso: Showboat

O **Showboat** é uma ferramenta de linha de comando projetada para criar documentos de demonstração executáveis em formato Markdown. Ele combina comentários, blocos de código e a captura automática de suas saídas, servindo tanto como documentação quanto como prova de trabalho reproduzível.

## Comandos Principais

### Gerenciamento de Documento
- `showboat init <arquivo> <título>`: Inicia um novo documento de demonstração com o título fornecido.
- `showboat note <arquivo> [texto]`: Adiciona um comentário ou explicação (texto livre) ao documento.
- `showboat pop <arquivo>`: Remove a entrada mais recente (nota, execução ou imagem) do documento. Útil para corrigir erros.

### Execução de Código
- `showboat exec <arquivo> <linguagem> [código]`: Executa o código na linguagem especificada (ex: `bash`, `python`, `node`), captura a saída e a anexa ao documento. A saída também é exibida no terminal em tempo real.

### Mídia e Imagens
- `showboat image <arquivo> <caminho>`: Copia uma imagem para o diretório do documento e adiciona uma referência a ela no Markdown.
- `showboat image <arquivo> '![texto alt](caminho)'`: Adiciona a imagem preservando o texto alternativo especificado.

### Verificação e Extração
- `showboat verify <arquivo>`: Reexecuta todos os blocos de código do documento e compara as novas saídas com as registradas originalmente. Retorna erro se houver divergências.
- `showboat extract <arquivo>`: Exibe a sequência exata de comandos CLI necessários para recriar o documento do zero.

---

## Exemplos Práticos

### Criando uma demonstração básica
```bash
# 1. Iniciar o documento
showboat init demo.md "Configuração de Ambiente"

# 2. Adicionar uma nota explicativa
showboat note demo.md "Primeiro, vamos verificar as versões das ferramentas instaladas."

# 3. Executar comandos e capturar saídas
showboat exec demo.md bash "node -v"
showboat exec demo.md bash "npm -v"

# 4. Adicionar um script em Python
showboat exec demo.md python "print('Hello from Showboat!')"

# 5. Adicionar um screenshot do resultado final
showboat image demo.md '![Resultado do Terminal](screenshot.png)'
```

### Validando a reprodutibilidade
Para garantir que o seu guia ainda funciona conforme esperado em um novo ambiente ou após alterações no código:
```bash
showboat verify demo.md
```

## Opções Globais
- `--workdir <dir>`: Define o diretório de trabalho onde os códigos serão executados (o padrão é o diretório atual).
- `--help, -h`: Exibe a ajuda detalhada.
