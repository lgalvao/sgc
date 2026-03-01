import os
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # 1. Remove visual separator lines like // ======== or // --------
    content = re.sub(r'^[ \t]*//[-=]{3,}[ \t]*\n', '', content, flags=re.MULTILINE)
    content = re.sub(r'^[ \t]*/\*[-=]{3,}\*/[ \t]*\n', '', content, flags=re.MULTILINE)

    # 2. Obvious @param and @return
    # This regex deletes `@param` and `@return` lines and ANY subsequent continuation lines
    # (lines starting with `* ` but not `* @` and not `*/`)
    # We use a pattern to match the `@param`/`@return` line AND optionally any following lines
    # that just contain `* ` and text (but not another tag or the end of comment).
    # A continuation line typically looks like `[ \t]*\*[ \t]+[^@\n/].*?\n`
    chunk_pattern = r'^[ \t]*\*[ \t]*@(param|return(s)?)[ \t]+.*?\n(?:[ \t]*\*[ \t]+(?:[^@ \n].*?)?\n)*'
    content = re.sub(chunk_pattern, '', content, flags=re.MULTILINE)

    # We remove empty lines at the end of javadoc blocks before */
    content = re.sub(r'(\n[ \t]*\*[ \t]*)+\n([ \t]*)\*/', r'\n\2*/', content)
    # We remove empty lines at the start of javadoc blocks after /**
    content = re.sub(r'/\*\*[ \t]*\n([ \t]*\*[ \t]*\n)+', '/**\n', content)
    # Remove empty JSDoc/Javadoc blocks
    content = re.sub(r'^[ \t]*/\*\*[ \t]*\n([ \t]*\*[ \t]*\n)*[ \t]*\*/[ \t]*\n', '', content, flags=re.MULTILINE)

    # 3. Conversational and obvious single line comments
    phrases = [
        "This function", "This class", "This method", "Here we",
        "We need to", "The purpose of", "As requested", "This component",
        "Essa função", "Essa classe", "Esse método", "Aqui nós", "Nós precisamos",
        "O objetivo desse", "Como solicitado", "Esse componente"
    ]

    obvious_starts = [
        "cria um", "retorna um", "retorna a", "retorna o", "atualiza o", "atualiza a",
        "busca um", "busca o", "busca a", "deleta o", "deleta a", "salva o", "salva a",
        "verifica se", "checa se", "valida o", "valida a", "valida se",
        "função para", "método para", "classe que", "componente que",
        "import", "imports", "define", "definição de", "declaração de",
        "inicializa", "inicia o", "inicia a", "configura o", "configura a",
        "renderiza o", "renderiza a", "estado de", "variável que",
        "adiciona o", "adiciona a", "remove o", "remove a",
        "lida com", "trata o", "trata a", "trata erro",
        "exporta", "export default", "export", "interface", "tipo",
        "chama o", "chama a", "executa o", "executa a",
        "define a props", "define as props", "define as propriedades", "propriedades",
        "importa", "importa os", "importa as"
    ]

    lines = content.split('\n')
    new_lines = []

    for line in lines:
        stripped = line.strip()

        if stripped.startswith('//'):
            comment_text = stripped[2:].strip()

            # Skip jacoco ignores and eslint/ts/ide directives
            ct_lower = comment_text.lower()
            if ct_lower.startswith("jacoco") or ct_lower.startswith("eslint") or ct_lower.startswith("@ts") or ct_lower.startswith("noinspection"):
                new_lines.append(line)
                continue

            if len(comment_text.split()) <= 2:
                continue

            is_bad = False
            for phrase in phrases:
                if ct_lower.startswith(phrase.lower()):
                    is_bad = True
                    break

            if is_bad:
                continue

            for phrase in obvious_starts:
                if ct_lower.startswith(phrase):
                    is_bad = True
                    break

            if is_bad:
                continue

        new_lines.append(line)

    content = '\n'.join(new_lines)

    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

modified_files = []

for root, dirs, files in os.walk('.'):
    if 'node_modules' in root or '.git' in root or 'build' in root or 'dist' in root or 'test' in root or '__tests__' in root or 'stories' in root:
        continue
    for file in files:
        if file.endswith(('.ts', '.js', '.vue', '.java')):
            if process_file(os.path.join(root, file)):
                modified_files.append(os.path.join(root, file))

print(f"Modified {len(modified_files)} files.")
