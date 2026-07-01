import {AxeBuilder} from '@axe-core/playwright';
import {expect, type Page} from '@playwright/test';

type ContextoCaptura = Record<string, unknown>;

export interface OpcoesCapturaTela {
    fullPage?: boolean;
    extra?: ContextoCaptura;
    tags?: string[];
    auditarAcessibilidade?: boolean;
}

export async function capturarCheckpointA11y(page: Page, opcoes?: OpcoesCapturaTela): Promise<void> {
    await aguardarInterfaceEstavelParaCaptura(page);

    if (deveAuditarCheckpoint(opcoes)) {
        await auditarAcessibilidadeNosTemas(page);
    }
}

function deveAuditarCheckpoint(opcoes?: OpcoesCapturaTela): boolean {
    if (opcoes?.auditarAcessibilidade !== undefined) {
        return opcoes.auditarAcessibilidade;
    }

    if (opcoes?.fullPage) {
        return true;
    }

    const tags = new Set(opcoes?.tags ?? []);
    return ['modal', 'menu', 'dropdown', 'historico', 'a11y'].some((tag) => tags.has(tag));
}

export async function auditarAcessibilidadeNosTemas(page: Page): Promise<void> {
    const temaOriginal = await obterTemaAtual(page);

    await auditarAcessibilidade(page);
    await definirTemaTemporario(page, 'dark');
    await auditarAcessibilidade(page);
    await definirTemaTemporario(page, temaOriginal);
}

async function auditarAcessibilidade(page: Page): Promise<void> {
    const accessibilityScanResults = await new AxeBuilder({page})
        .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22a', 'wcag22aa'])
        .disableRules(['list'])
        .analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
}

async function obterTemaAtual(page: Page): Promise<'light' | 'dark'> {
    return page.evaluate(() => {
        return document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
    });
}

async function definirTemaTemporario(page: Page, tema: 'light' | 'dark'): Promise<void> {
    await page.evaluate((temaAtual) => {
        document.documentElement.setAttribute('data-bs-theme', temaAtual);
        document.documentElement.style.colorScheme = temaAtual;
    }, tema);
    await aguardarInterfaceEstavelParaCaptura(page);
}

export async function aguardarPinturaEstavel(page: Page, quadros = 2): Promise<void> {
    for (let indice = 0; indice < quadros; indice += 1) {
        await page.evaluate(() => new Promise<void>((resolve) => {
            globalThis.requestAnimationFrame(() => resolve());
        }));
    }
}

async function aguardarInterfaceEstavelParaCaptura(page: Page): Promise<void> {
    await aguardarPinturaEstavel(page);
    await aguardarModaisEstaveis(page);
    await aguardarTransicoesEspecificas(page);
    await aguardarPinturaEstavel(page);
}

async function aguardarModaisEstaveis(page: Page): Promise<void> {
    const duracaoMs = await page.evaluate(() => {
        const seletores = [
            '.modal.show',
            '.modal.show .modal-dialog',
            '.modal-backdrop.show',
            '[role="dialog"]:not([aria-hidden="true"])',
            '.tooltip.show',
            '.tooltip',
            '.dropdown-menu.show',
            '.dropdown-menu',
        ];

        const elementos = Array.from(document.querySelectorAll<HTMLElement>(seletores.join(',')))
            .filter((elemento) => {
                const estilo = globalThis.getComputedStyle(elemento);
                return estilo.display !== 'none' && estilo.visibility !== 'hidden';
            });

        if (elementos.length === 0) {
            return 0;
        }

        function converterTempoParaMs(valor: string): number {
            const valorTratado = valor.trim();
            if (valorTratado.endsWith('ms')) {
                return Number.parseFloat(valorTratado) || 0;
            }
            if (valorTratado.endsWith('s')) {
                return (Number.parseFloat(valorTratado) || 0) * 1000;
            }
            return Number.parseFloat(valorTratado) || 0;
        }

        function maiorTempo(listaCss: string): number {
            return Math.max(
                0,
                ...listaCss.split(',').map((valor) => converterTempoParaMs(valor))
            );
        }

        return Math.max(
            0,
            ...elementos.map((elemento) => {
                const estilo = globalThis.getComputedStyle(elemento);
                return maiorTempo(estilo.transitionDuration)
                    + maiorTempo(estilo.transitionDelay)
                    + maiorTempo(estilo.animationDuration)
                    + maiorTempo(estilo.animationDelay);
            })
        );
    });

    if (duracaoMs <= 0) {
        return;
    }

    await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
}

async function aguardarTransicoesEspecificas(page: Page): Promise<void> {
    const duracaoMs = await page.evaluate(() => {
        const seletores = [
            '.login-autorizacao-enter-active',
            '.login-autorizacao-leave-active',
            '.tree-row-transition-enter-active',
            '.tree-row-transition-leave-active',
        ];

        const elementos = Array.from(document.querySelectorAll<HTMLElement>(seletores.join(',')))
            .filter((elemento) => {
                const estilo = globalThis.getComputedStyle(elemento);
                return estilo.display !== 'none' && estilo.visibility !== 'hidden';
            });

        if (elementos.length === 0) {
            return 0;
        }

        function converterTempoParaMs(valor: string): number {
            const valorTratado = valor.trim();
            if (valorTratado.endsWith('ms')) {
                return Number.parseFloat(valorTratado) || 0;
            }
            if (valorTratado.endsWith('s')) {
                return (Number.parseFloat(valorTratado) || 0) * 1000;
            }
            return Number.parseFloat(valorTratado) || 0;
        }

        function maiorTempo(listaCss: string): number {
            return Math.max(
                0,
                ...listaCss.split(',').map((valor) => converterTempoParaMs(valor))
            );
        }

        return Math.max(
            0,
            ...elementos.map((elemento) => {
                const estilo = globalThis.getComputedStyle(elemento);
                return maiorTempo(estilo.transitionDuration)
                    + maiorTempo(estilo.transitionDelay)
                    + maiorTempo(estilo.animationDuration)
                    + maiorTempo(estilo.animationDelay);
            })
        );
    });

    if (duracaoMs <= 0) {
        return;
    }

    await page.waitForTimeout(Math.min(Math.ceil(duracaoMs) + 50, 1000));
}
