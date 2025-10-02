// tests/utils/error-reporter.ts - Utilitário para relatórios customizados

interface ConsoleError {
    message: string;
    location: string;
    timestamp: number;
}

interface JavascriptError {
    message: string;
    stack?: string;
    timestamp: number;
}

interface NetworkError {
    url: string;
    error: string;
    timestamp: number;
}

interface VueError {
    message: string;
    component: unknown;
    timestamp: number;
}

interface ErrorCollection {
    console: ConsoleError[];
    javascript: JavascriptError[];
    network: NetworkError[];
    vue: VueError[];
    performance: unknown[];
}

export class ErrorReporter {
    public errors: ErrorCollection = {
        console: [],
        javascript: [],
        network: [],
        vue: [],
        performance: []
    };

    public addConsoleError(message: string, location: string): void {
        this.errors.console.push({message, location, timestamp: Date.now()});
    }

    public addJavaScriptError(error: Error): void {
        this.errors.javascript.push({
            message: error.message,
            stack: error.stack,
            timestamp: Date.now()
        });
    }

    public addNetworkError(url: string, error: string): void {
        this.errors.network.push({url, error, timestamp: Date.now()});
    }

    public addVueError(message: string, component: unknown): void {
        this.errors.vue.push({message, component, timestamp: Date.now()});
    }

    public generateReport(): void {
        const totalErrors = Object.values(this.errors).flat().length;
        if (totalErrors === 0) return;

        console.error(`\n🚨 RELATÓRIO DE ERROS - ${totalErrors} erro(s) encontrado(s)`);
        console.error('='.repeat(80));
        Object.entries(this.errors).forEach(([type, errors]) => {
            if (errors.length > 0) {
                console.error(`\n📋 ${type.toUpperCase()} (${errors.length}):`);
                 
                errors.forEach((error: any, i: number) => {
                    let message = 'Erro desconhecido';
                    if ('message' in error && error.message) {
                        message = error.message;
                    } else if ('error' in error && error.error) {
                        message = String(error.error);
                    }
                    console.error(`  ${i + 1}. ${message}`);
                    if ('location' in error && typeof error.location === 'string') console.error(`     📍 ${error.location}`);
                    if ('url' in error && typeof error.url === 'string') console.error(`     🔗 ${error.url}`);
                });
            }
        });

        console.error('\n' + '='.repeat(80) + '\n');
    }

    public hasErrors(): boolean {
        return Object.values(this.errors).some(errorList => errorList.length > 0);
    }

    public getCriticalErrors(): (JavascriptError | VueError)[] {
        return [...this.errors.javascript, ...this.errors.vue];
    }
}
