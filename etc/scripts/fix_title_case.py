import os

REPORT_FILE = 'title-case-report.txt'
SEPARATOR = ' | '

def to_sentence_case(text):
    """Converte 'Título Eleitoral' para 'Título eleitoral' preservando identificadores isolados (A, B, X)"""
    words = text.split()
    if not words:
        return text
    
    first_word = words[0]
    processed_words = [first_word]
    
    for word in words[1:]:
        # Preserva identificadores (uma única letra maiúscula)
        if len(word) == 1 and word.isupper():
            processed_words.append(word)
        else:
            processed_words.append(word.lower())
    
    return " ".join(processed_words)

def apply_fixes():
    if not os.path.exists(REPORT_FILE):
        return

    fixes_by_file = {}
    with open(REPORT_FILE, 'r', encoding='utf-8') as f:
        for line in f:
            parts = line.strip().split(SEPARATOR)
            if len(parts) < 3:
                continue
            
            file_path = parts[0].strip()
            line_num = int(parts[1].strip())
            old_text = SEPARATOR.join(parts[2:]).strip()
            
            if file_path not in fixes_by_file:
                fixes_by_file[file_path] = []
            fixes_by_file[file_path].append((line_num, old_text))

    for file_path, fixes in fixes_by_file.items():
        if not os.path.exists(file_path):
            continue
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            modified = False
            for line_num, old_text in sorted(fixes, key=lambda x: x[0], reverse=True):
                idx = line_num - 1
                if idx < len(lines) and old_text in lines[idx]:
                    new_text = to_sentence_case(old_text)
                    if new_text != old_text:
                        lines[idx] = lines[idx].replace(old_text, new_text)
                        modified = True
            
            if modified:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.writelines(lines)
                print(f"Corrigido com segurança: {file_path}")
        except Exception as e:
            print(f"Erro em {file_path}: {e}")

if __name__ == "__main__":
    apply_fixes()
