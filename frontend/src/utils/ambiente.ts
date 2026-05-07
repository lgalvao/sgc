export function ehModoProducao(mode = import.meta.env.MODE): boolean {
    return mode === "production";
}
