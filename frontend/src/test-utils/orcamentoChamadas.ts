type MockComChamadas = {
    mock?: {
        calls: unknown[][];
    };
};

export function contarChamadas(...mocks: MockComChamadas[]): number {
    return mocks.reduce((total, mock) => total + (mock.mock?.calls.length ?? 0), 0);
}
