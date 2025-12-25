import os
import sys

def update_imports(root_dir, limit=30):
    replacements = {
        "sgc.processo.internal.model": "sgc.processo.api.model",
        "sgc.unidade.internal.model": "sgc.unidade.api.model",
        "sgc.sgrh.internal.model": "sgc.sgrh.api.model",
        "sgc.mapa.internal.model": "sgc.mapa.api.model",
        "sgc.atividade.internal.model": "sgc.atividade.api.model"
    }

    count = 0
    for subdir, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith(".java"):
                filepath = os.path.join(subdir, file)
                with open(filepath, "r") as f:
                    content = f.read()

                new_content = content
                for old, new in replacements.items():
                    new_content = new_content.replace(old, new)

                if new_content != content:
                    with open(filepath, "w") as f:
                        f.write(new_content)
                    print(f"Updated {filepath}")
                    count += 1
                    if count >= limit:
                        print(f"Limit of {limit} files reached.")
                        return

if __name__ == "__main__":
    update_imports("backend/src/test/java/sgc")
