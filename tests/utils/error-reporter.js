// tests/utils/error-reporter.js - Utilitário para relatórios customizados
export class ErrorReporter {
    constructor() {
        this.errors = {
            console: [],
            javascript: [],
            network: [],
            vue: [],
            performance: []
        };
    }

    addConsoleError(message, location) {
        this.errors.console.push({message, location, timestamp: Date.now()});
    }

    addJavaScriptError(error) {
        this.errors.javascript.push({
            message: error.message,
            stack: error.stack,
            timestamp: Date.now()
        });
    }

    addNetworkError(url, error) {
        this.errors.network.push({url, error, timestamp: Date.now()});
    }

    addVueError(message, component) {
        this.errors.vue.push({message, component, timestamp: Date.now()});
    }

    generateReport() {
        const totalErrors = Object.values(this.errors).flat().length;

        if (totalErrors === 0) {
            console.log('✅ Nenhum erro encontrado!');
            return;
        }

        console.error(`\n🚨 RELATÓRIO DE ERROS - ${totalErrors} erro(s) encontrado(s)`);
        console.error('='.repeat(80));

        Object.entries(this.errors).forEach(([type, errors]) => {
            if (errors.length > 0) {
                console.error(`\n📋 ${type.toUpperCase()} (${errors.length}):`);
                errors.forEach((error, i) => {
                    console.error(`  ${i + 1}. ${error.message || error.error}`);
                    if (error.location) console.error(`     📍 ${error.location}`);
                    if (error.url) console.error(`     🔗 ${error.url}`);
                });
            }
        });

        console.error('\n' + '='.repeat(80) + '\n');
    }

    hasErrors() {
        return Object.values(this.errors).some(errorList => errorList.length > 0);
    }

    getCriticalErrors() {
        return [...this.errors.javascript, ...this.errors.vue];
    }
}
