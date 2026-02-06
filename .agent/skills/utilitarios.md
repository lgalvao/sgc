# Skill: Utilitários e Ambiente

## Descrição
Scripts para manutenção do ambiente de desenvolvimento, correção de código e captura de evidências.

## Comandos Relacionados
- `node frontend/etc/scripts/capturar-telas.cjs`: Captura screenshots automatizadas via Playwright.
- `backend/etc/scripts/instalar-certs.sh`: Auxilia na instalação de certificados SSL necessários para conexões seguras.
- `python3 backend/etc/scripts/fix_fqn.py`: Corrige referências de Fully Qualified Names após refatorações.

## Fluxo de Uso
- Para screenshots: `node frontend/etc/scripts/capturar-telas.cjs [categoria]`.
- As evidências são salvas no diretório `screenshots/` na raiz.
