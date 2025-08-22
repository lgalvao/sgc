import {expect, test} from "@playwright/test";
import {login} from "./utils/auth";
import {useMapasStore} from "../src/stores/mapas";
import {createPinia, setActivePinia} from "pinia";

test.describe('Detalhes da Unidade no Processo', () => {
    test.beforeEach(async ({page, context}) => {
        await login(page);

        // Intercepta a requisição para unidades.json e fornece dados mockados
        await context.route('**/unidades.json', route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify([
                    {
                        "sigla": "SEDOC",
                        "nome": "Seção de Desenvolvimento Organizacional e Capacitação",
                        "tipo": "ADMINISTRATIVA",
                        "idServidorTitular": 1,
                        "responsavel": {
                            "idServidorResponsavel": 8,
                            "tipo": 0,
                            "dataInicio": "2025-01-01",
                            "dataFim": "2025-12-31"
                        },
                        "filhas": [
                            {
                                "sigla": "SGP",
                                "nome": "Secretaria de Gestao de Pessoas",
                                "tipo": "INTERMEDIARIA",
                                "idServidorTitular": 2,
                                "responsavel": {
                                    "idServidorResponsavel": 13,
                                    "tipo": 1,
                                    "dataInicio": "2025-01-01",
                                    "dataFim": "2025-12-31"
                                },
                                "filhas": [
                                    {
                                        "sigla": "COEDE",
                                        "nome": "Coordenadoria de Educação Especial",
                                        "tipo": "INTERMEDIARIA",
                                        "idServidorTitular": 3,
                                        "responsavel": null,
                                        "filhas": [
                                            {
                                                "sigla": "SEMARE",
                                                "nome": "Seção Magistrados e Requisitados",
                                                "tipo": "OPERACIONAL",
                                                "idServidorTitular": 4,
                                                "responsavel": null,
                                                "filhas": [],
                                                "id": 5
                                            }
                                        ],
                                        "id": 4
                                    }
                                ],
                                "id": 3
                            },
                            {
                                "sigla": "STIC",
                                "nome": "Secretaria de Informática e Comunicações",
                                "tipo": "INTEROPERACIONAL",
                                "idServidorTitular": 5,
                                "responsavel": null,
                                "filhas": [
                                    {
                                        "sigla": "COSIS",
                                        "nome": "Coordenadoria de Sistemas",
                                        "tipo": "INTERMEDIARIA",
                                        "idServidorTitular": 6,
                                        "responsavel": null,
                                        "filhas": [
                                            {
                                                "sigla": "SEDESENV",
                                                "nome": "Seção de Desenvolvimento de Sistemas",
                                                "tipo": "OPERACIONAL",
                                                "idServidorTitular": 7,
                                                "responsavel": {
                                                    "idServidorResponsavel": 8,
                                                    "tipo": 0,
                                                    "dataInicio": "2025-01-01",
                                                    "dataFim": "2025-12-31"
                                                },
                                                "filhas": [],
                                                "id": 8
                                            },
                                            {
                                                "sigla": "SEDIA",
                                                "nome": "Seção de Dados e Inteligência Artificial",
                                                "tipo": "OPERACIONAL",
                                                "idServidorTitular": 9,
                                                "responsavel": null,
                                                "filhas": [],
                                                "id": 9
                                            },
                                            {
                                                "sigla": "SESEL",
                                                "nome": "Seção de Sistemas Eleitorais",
                                                "tipo": "OPERACIONAL",
                                                "idServidorTitular": 10,
                                                "responsavel": null,
                                                "filhas": [],
                                                "id": 10
                                            }
                                        ],
                                        "id": 7
                                    },
                                    {
                                        "sigla": "COSINF",
                                        "nome": "Coordenadoria de Suporte e Infraestrutura",
                                        "tipo": "OPERACIONAL",
                                        "idServidorTitular": 12,
                                        "responsavel": null,
                                        "filhas": [
                                            {
                                                "sigla": "SENIC",
                                                "nome": "Seção de Infraestrutura",
                                                "tipo": "OPERACIONAL",
                                                "idServidorTitular": 1,
                                                "responsavel": null,
                                                "filhas": [],
                                                "id": 12
                                            }
                                        ],
                                        "id": 11
                                    }
                                ],
                                "id": 6
                            }
                        ],
                        "id": 1
                    }
                ])
            });
        });

        const pinia = createPinia();
        setActivePinia(pinia);

        const mapasStore = useMapasStore();
        mapasStore.mapas = [{
            id: 1,
            idProcesso: 1,
            unidade: 'SESEL',
            vigente: true,
            dataInicio: new Date(),
            dataFim: null,
            descricao: 'Mapa de Teste',
            situacao: 'Ativo',
            tipo: 'MAPEAMENTO',
            subprocessos: []
        }];

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
});