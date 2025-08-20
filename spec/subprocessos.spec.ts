import {expect, test} from "@playwright/test";
import {login} from "./utils/auth";
import {useMapasStore} from "../src/stores/mapas";
import {createPinia, setActivePinia} from "pinia";

test.describe('Detalhes da Unidade no Processo', () => {
    test.setTimeout(5000);

    test.beforeEach(async ({page}) => {
        await login(page);

        const pinia = createPinia(); // Inicializa o Pinia
        setActivePinia(pinia); // Ativa a instância do Pinia
        const mapasStore = useMapasStore();
        mapasStore.mapas = [{
            id: 1,
            idProcesso: 1,
            unidadeSigla: 'SESEL',
            vigente: true,
            dataInicio: new Date(),
            dataFim: null,
            descricao: 'Mapa de Teste',
            situacao: 'Ativo',
            tipo: 'MAPEAMENTO',
            subprocessos: []
        }];

        // Navegar para a página de detalhes da unidade no processo (ID 1) no novo padrão
        await page.goto(`/unidade/SESEL`);
        await page.waitForLoadState('networkidle');
    });

    test('deve exibir os detalhes da unidade e os cards de funcionalidade', async ({page}) => {
        await expect(page.getByText('Responsável:')).toBeVisible();

        await expect(page.getByTestId('card-atividades-conhecimentos')).toBeVisible();
        await expect(page.getByRole('heading', {name: 'Mapa de Competências'})).toBeVisible();
    });

    test('deve navegar para a página de atividades ao clicar no card', async ({page}) => {
        await page.getByTestId('card-atividades-conhecimentos').click();
        await page.waitForURL(/.*\/processo\/\d+\/SESEL\/cadastro/);
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });


});