/** @type {import('dependency-cruiser').IConfiguration} */
module.exports = {
    forbidden: [
        {
            name: "view-sem-service-direto",
            severity: "error",
            comment: "Views (.vue) nao devem importar services diretamente — use composables ou stores",
            from: {path: "^src/views/.*\\.vue$"},
            to: {path: "^src/services/"},
        },
        {
            name: "component-sem-service-direto",
            severity: "error",
            comment: "Componentes nao devem importar services diretamente",
            from: {path: "^src/components/"},
            to: {path: "^src/services/"},
        },
        {
            name: "store-sem-composable",
            severity: "error",
            comment: "Stores Pinia nao devem depender de composables — sentido errado",
            from: {path: "^src/stores/"},
            to: {path: "^src/composables/"},
        },
        {
            name: "service-sem-store",
            severity: "error",
            comment: "Services nao devem depender de stores",
            from: {path: "^src/services/"},
            to: {path: "^src/stores/"},
        },
        {
            name: "sem-ciclos",
            severity: "error",
            comment: "Dependencias circulares sao proibidas",
            from: {},
            to: {circular: true},
        },
    ],
    options: {
        doNotFollow: {path: "node_modules"},
        exclude: {path: "(__tests__|__mocks__|\\.spec\\.|\\.test\\.|\\.stories\\.)"},
        tsConfig: {fileName: "./tsconfig.json"},
        enhancedResolveOptions: {
            exportsFields: ["exports"],
            conditionNames: ["import", "require", "node", "default"],
            extensions: [".ts", ".vue", ".js", ".mjs"],
        },
    },
};
