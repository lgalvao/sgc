// noinspection JSUnusedGlobalSymbols

declare module '*.vue' {
    import type {DefineComponent} from 'vue'
    const component: DefineComponent<Record<string, never>, Record<string, never>, Record<string, unknown>>
    export default component
}
