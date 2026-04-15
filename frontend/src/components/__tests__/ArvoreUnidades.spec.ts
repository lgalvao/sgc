import {describe, expect, it} from "vitest";
import {mount, RouterLinkStub} from "@vue/test-utils";
import ArvoreUnidades from "../unidade/ArvoreUnidades.vue";
import type {Unidade} from "@/types/tipos";

describe("ArvoreUnidades.vue", () => {
    const mockUnidades: Unidade[] = [
        {
            codigo: 1,
            sigla: "ROOT",
            nome: "Raiz",
            filhas: [
                {
                    codigo: 10,
                    sigla: "FILHA1",
                    nome: "Filha 1",
                    isElegivel: true,
                    filhas: [],
                    tipo: "OPERACIONAL"
                },
                {
                    codigo: 20,
                    sigla: "FILHA2",
                    nome: "Filha 2",
                    isElegivel: false,
                    filhas: [
                        {
                            codigo: 21,
                            sigla: "NETA1",
                            nome: "Neta 1",
                            isElegivel: true,
                            filhas: [],
                            tipo: "OPERACIONAL"
                        }
                    ],
                    tipo: "INTERMEDIARIA"
                }
            ],
            isElegivel: false,
            tipo: "ADMINISTRATIVA"
        }
    ];

    const createWrapper = (props = {}) => {
        return mount(ArvoreUnidades, {
            props: {
                unidades: mockUnidades,
                modelValue: [],
                ocultarRaiz: false, // Show root to make traversal easier to test
                ...props
            },
            global: {
                stubs: {
                    UnidadeTreeNode: true,
                    RouterLink: RouterLinkStub
                }
            }
        });
    };

    it("deve renderizar as unidades", () => {
        const wrapper = createWrapper();
        expect(wrapper.findComponent({name: "UnidadeTreeNode"}).exists()).toBe(true);
    });

    it("deve agrupar zonas eleitorais em um no visual", () => {
        const unidades: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 10, sigla: "SJ", nome: "SECRETARIA JUDICIARIA", filhas: [], tipo: "ADMINISTRATIVA"},
                    {codigo: 101, sigla: "1ª Z.E.", nome: "1ª ZONA ELEITORAL", filhas: []},
                    {codigo: 102, sigla: "2ª Z.E.", nome: "2ª ZONA ELEITORAL", filhas: []},
                ],
            }
        ];
        const wrapper = createWrapper({unidades, ocultarRaiz: true});

        expect((wrapper.vm as any).unidadesExibidas).toEqual([
            {
                codigo: 10,
                sigla: "SJ",
                nome: "SECRETARIA JUDICIARIA",
                filhas: [],
                tipo: "ADMINISTRATIVA",
            },
            {
                codigo: -1999,
                sigla: "",
                nome: "ZONAS ELEITORAIS",
                tipo: "AGRUPADOR_VISUAL",
                isElegivel: false,
                agrupadorVisual: true,
                filhas: [
                    {codigo: 101, sigla: "1ª Z.E.", nome: "1ª ZONA ELEITORAL", filhas: []},
                    {codigo: 102, sigla: "2ª Z.E.", nome: "2ª ZONA ELEITORAL", filhas: []},
                ],
            }
        ]);
    });

    it("deve ordenar em blocos: secretarias, zonas eleitorais e demais por ordem alfabetica", () => {
        const unidades: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 1_1, sigla: "GP", nome: "GABINETE DA PRESIDENCIA", filhas: [], tipo: "ADMINISTRATIVA"},
                    {codigo: 1_2, sigla: "SJ", nome: "SECRETARIA JUDICIARIA", filhas: [], tipo: "ADMINISTRATIVA"},
                    {codigo: 1_3, sigla: "2ª Z.E.", nome: "2ª ZONA ELEITORAL", filhas: [], tipo: "OPERACIONAL"},
                    {codigo: 1_4, sigla: "CAE01", nome: "CENTRAL DE ATENDIMENTO AO ELEITOR DA CAPITAL", filhas: [], tipo: "OPERACIONAL"},
                    {codigo: 1_5, sigla: "SGP", nome: "SECRETARIA DE GESTAO DE PESSOAS", filhas: [], tipo: "ADMINISTRATIVA"},
                    {codigo: 1_6, sigla: "AAA", nome: "ASSESSORIA DE APOIO", filhas: [], tipo: "OPERACIONAL"},
                    {codigo: 1_7, sigla: "1ª Z.E.", nome: "1ª ZONA ELEITORAL", filhas: [], tipo: "OPERACIONAL"},
                ],
            }
        ];
        const wrapper = createWrapper({unidades, ocultarRaiz: true});

        expect((wrapper.vm as any).unidadesExibidas.map((item: Unidade) => item.codigo)).toEqual([
            1_5,
            1_2,
            -1999,
            1_6,
            1_4,
            1_1,
        ]);
    });

    it("deve aplicar a ordenacao especial tambem nas filhas", () => {
        const unidades: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {
                        codigo: 10,
                        sigla: "SGP",
                        nome: "SECRETARIA DE GESTAO DE PESSOAS",
                        tipo: "INTEROPERACIONAL",
                        filhas: [
                            {codigo: 101, sigla: "GP", nome: "GABINETE", filhas: [], tipo: "ADMINISTRATIVA"},
                            {codigo: 102, sigla: "SA", nome: "SECRETARIA ADMINISTRATIVA", filhas: [], tipo: "ADMINISTRATIVA"},
                            {codigo: 103, sigla: "2ª Z.E.", nome: "2ª ZONA ELEITORAL", filhas: [], tipo: "OPERACIONAL"},
                            {codigo: 104, sigla: "AAA", nome: "ASSESSORIA DE APOIO", filhas: [], tipo: "OPERACIONAL"},
                            {codigo: 105, sigla: "1ª Z.E.", nome: "1ª ZONA ELEITORAL", filhas: [], tipo: "OPERACIONAL"},
                        ],
                    },
                ],
            }
        ];
        const wrapper = createWrapper({unidades, ocultarRaiz: true});
        const secretaria = (wrapper.vm as any).unidadesExibidas[0];

        expect(secretaria.filhas.map((item: Unidade) => item.codigo)).toEqual([
            102,
            -10999,
            104,
            101,
        ]);
    });

    it("deve ordenar por sigla dentro dos blocos, e nao pelo nome", () => {
        const unidades: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 10, sigla: "ZZZ", nome: "SECRETARIA ALFA", filhas: [], tipo: "ADMINISTRATIVA"},
                    {codigo: 11, sigla: "AAA", nome: "SECRETARIA ZULU", filhas: [], tipo: "ADMINISTRATIVA"},
                    {codigo: 12, sigla: "ZZ", nome: "ASSESSORIA ALFA", filhas: [], tipo: "OPERACIONAL"},
                    {codigo: 13, sigla: "AA", nome: "GABINETE ZULU", filhas: [], tipo: "OPERACIONAL"},
                ],
            }
        ];

        const wrapper = createWrapper({unidades, ocultarRaiz: true});

        expect((wrapper.vm as any).unidadesExibidas.map((item: Unidade) => item.codigo)).toEqual([
            11,
            10,
            13,
            12,
        ]);
    });

    it("deve marcar o grupo de zonas eleitorais como indeterminado quando apenas parte das zonas estiver selecionada", () => {
        const unidades: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 101, sigla: "1ª Z.E.", nome: "1ª ZONA ELEITORAL", isElegivel: true, filhas: []},
                    {codigo: 102, sigla: "2ª Z.E.", nome: "2ª ZONA ELEITORAL", isElegivel: true, filhas: []},
                ],
            }
        ];
        const wrapper = createWrapper({unidades, ocultarRaiz: true, modelValue: [101]});
        const grupo = (wrapper.vm as any).unidadesExibidas[0];

        expect((wrapper.vm as any).getEstadoSelecao(grupo)).toBe("indeterminate");
    });

    it("deve selecionar todas as zonas ao marcar o grupo visual", async () => {
        const unidades: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                filhas: [
                    {codigo: 101, sigla: "1ª Z.E.", nome: "1ª ZONA ELEITORAL", isElegivel: true, filhas: []},
                    {codigo: 102, sigla: "2ª Z.E.", nome: "2ª ZONA ELEITORAL", isElegivel: true, filhas: []},
                ],
            }
        ];
        const wrapper = createWrapper({unidades, ocultarRaiz: true, modelValue: [], modoSelecao: true});
        const grupo = (wrapper.vm as any).unidadesExibidas[0];

        (wrapper.vm as any).toggle(grupo, true);
        await wrapper.vm.$nextTick();

        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        expect(emitted![emitted!.length - 1][0]).toEqual([101, 102]);
    });

    it("deve iniciar com as unidades recolhidas", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});
        expect(root.props("isExpanded")(mockUnidades[0])).toBe(false);
    });

    it("deve alternar expansão", async () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        await root.props("onToggleExpand")(mockUnidades[0]);
        expect(root.props("isExpanded")(mockUnidades[0])).toBe(true);

        await root.props("onToggleExpand")(mockUnidades[0]);
        expect(root.props("isExpanded")(mockUnidades[0])).toBe(false);
    });

    it("deve calcular estado de seleção (checked)", async () => {
        const wrapper = createWrapper({modelValue: [10]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        expect(root.props("isChecked")(10)).toBe(true);
        expect(root.props("isChecked")(20)).toBe(false);
    });

    it("deve selecionar todas as unidades elegíveis", async () => {
        const wrapper = createWrapper({modoSelecao: true});

        await wrapper.find('button[aria-label="Selecionar todas as unidades elegíveis"]').trigger("click");

        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        const selection = emitted![0][0] as number[];
        expect(selection).toContain(10);
        expect(selection).toContain(21);
        expect(selection).not.toContain(1);
        expect(selection).not.toContain(20);
    });

    it("deve limpar seleção", async () => {
        const wrapper = createWrapper({modelValue: [10, 21], modoSelecao: true});

        await wrapper.find('button[aria-label="Desmarcar todas as unidades"]').trigger("click");

        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        expect(emitted![0][0]).toEqual([]);
    });

    it("deve lidar com toggle de unidade (selecionar)", async () => {
        const wrapper = createWrapper({modelValue: [], modoSelecao: true});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const node20 = mockUnidades[0].filhas![1];
        await root.props("onToggle")(node20, true);

        const emitted = wrapper.emitted("update:modelValue");
        const selection = emitted![emitted!.length - 1][0] as number[];

        expect(selection).toContain(21);
        expect(selection).not.toContain(20);
    });

    it("deve lidar com toggle de unidade (deselecionar)", async () => {
        const wrapper = createWrapper({modelValue: [10], modoSelecao: true});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const node10 = mockUnidades[0].filhas![0];
        await root.props("onToggle")(node10, false);

        const emitted = wrapper.emitted("update:modelValue");
        const selection = emitted![emitted!.length - 1][0] as number[];
        expect(selection).not.toContain(10);
    });

    it("deve calcular estado indeterminado", () => {
        const wrapper = createWrapper({modelValue: [21]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const node20 = mockUnidades[0].filhas![1];
        expect(root.props("getEstadoSelecao")(node20)).toBe(true);

        expect(root.props("getEstadoSelecao")(mockUnidades[0])).toBe("indeterminate");
    });

    it("isHabilitado deve retornar false se não elegível e sem filhas", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const leafNotEligible: Unidade = {codigo: 99, sigla: "X", nome: "X", isElegivel: false, filhas: []};
        expect(root.props("isHabilitado")(leafNotEligible)).toBe(false);
    });

    it("isHabilitado deve retornar true se elegível", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const leafEligible: Unidade = {codigo: 99, sigla: "X", nome: "X", isElegivel: true, filhas: []};
        expect(root.props("isHabilitado")(leafEligible)).toBe(true);
    });

    it("deve atualizar ancestrais corretamente (selecionar pai se todos filhos selecionados)", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 100,
                sigla: "PAI",
                nome: "Pai",
                isElegivel: true, // Pai elegível
                filhas: [
                    {codigo: 101, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL"},
                    {codigo: 102, sigla: "F2", nome: "F2", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                ],
                tipo: "ADMINISTRATIVA"
            }
        ];

        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [101]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const child2 = unidadesTeste[0].filhas![1];
        await root.props("onToggle")(child2, true);

        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];

        expect(lastEmission).toContain(101);
        expect(lastEmission).toContain(102);
        expect(lastEmission).toContain(100);
    });

    it("não deve deselecionar pai INTEROPERACIONAL se filhos desmarcados", async () => {

        const unidadesTeste: Unidade[] = [
            {
                codigo: 200,
                sigla: "INTER",
                nome: "Inter",
                isElegivel: true,
                tipo: "INTEROPERACIONAL",
                filhas: [
                    {codigo: 201, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                ]
            }
        ];

        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [200, 201]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const child = unidadesTeste[0].filhas![0];
        await root.props("onToggle")(child, false);

        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];

        expect(lastEmission).not.toContain(201);
        expect(lastEmission).toContain(200);
    });

    it("deve emitir a unidade INTEROPERACIONAL ao selecionar a secretaria com subordinadas", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 700,
                sigla: "STIC",
                nome: "Secretaria de Tecnologia",
                isElegivel: true,
                tipo: "INTEROPERACIONAL",
                filhas: [
                    {codigo: 701, sigla: "COSIS", nome: "Coordenadoria de Sistemas", isElegivel: false, filhas: [
                        {codigo: 702, sigla: "SEDIA", nome: "Secao de Dados", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                    ], tipo: "INTERMEDIARIA"},
                    {codigo: 703, sigla: "COSINF", nome: "Coordenadoria de Infra", isElegivel: false, filhas: [
                        {codigo: 704, sigla: "SENIC", nome: "Secao de Infra", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                    ], tipo: "INTERMEDIARIA"}
                ]
            }
        ];

        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [], modoSelecao: true});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        await root.props("onToggle")(unidadesTeste[0], true);

        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];

        expect(lastEmission).toContain(700);
        expect(lastEmission).toContain(702);
        expect(lastEmission).toContain(704);
    });

    it("deve atualizar ancestrais: não selecionar pai se não for elegível, mesmo se todos filhos selecionados", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 300,
                sigla: "PAI_INELEGIVEL",
                nome: "Pai inelegivel",
                isElegivel: false,
                filhas: [
                    {codigo: 301, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                ],
                tipo: "ADMINISTRATIVA"
            }
        ];

        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: []});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        const child = unidadesTeste[0].filhas![0];
        await root.props("onToggle")(child, true);

        const emitted = wrapper.emitted("update:modelValue");
        const lastEmission = emitted![emitted!.length - 1][0] as number[];

        expect(lastEmission).toContain(301);
        expect(lastEmission).not.toContain(300);
    });

    it("selecionarTodas deve lidar com nós sem filhas definidas", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 400,
                sigla: "SOLITARIA",
                nome: "Solitaria",
                isElegivel: true,
                // filhas undefined
                tipo: "OPERACIONAL"
            }
        ];

        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [], modoSelecao: true});

        await wrapper.find('button[aria-label="Selecionar todas as unidades elegíveis"]').trigger("click");

        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted).toBeTruthy();
        expect(emitted![0][0]).toContain(400);
    });

    it("deve exibir a raiz quando ocultarRaiz é false", () => {
        const wrapper = createWrapper({ocultarRaiz: false});
        expect((wrapper.vm as any).unidadesExibidas[0].codigo).toBe(1);
    });

    it("deve filtrar unidades", () => {
        const wrapper = createWrapper({
            filtrarPor: (u: Unidade) => u.sigla === "ROOT",
            ocultarRaiz: false
        });
        expect((wrapper.vm as any).unidadesExibidas).toHaveLength(1);
        expect((wrapper.vm as any).unidadesExibidas[0].sigla).toBe("ROOT");
    });

    it("não deve permitir seleção se modoSelecao é false", async () => {
        const wrapper = createWrapper({modoSelecao: false});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        expect(root.props("isChecked")(10)).toBe(false);
        expect(root.props("isHabilitado")(mockUnidades[0].filhas![0])).toBe(false);
        expect(root.props("getEstadoSelecao")(mockUnidades[0].filhas![0])).toBe(false);

        // toggle should do nothing
        await root.props("onToggle")(mockUnidades[0].filhas![0], true);
        expect(wrapper.emitted("update:modelValue")).toBeFalsy();
    });

    it("deve atualizar unidadesSelecionadasLocal quando modelValue muda", async () => {
        const wrapper = createWrapper({modelValue: [10]});
        await wrapper.setProps({modelValue: [20]});
        expect((wrapper.vm as any).unidadesSelecionadasLocal).toEqual([]);
    });

    it("deve atualizar expandedUnits quando unidades muda", async () => {
        const wrapper = createWrapper();
        const novasUnidades = [{codigo: 999, sigla: 'N', nome: 'N'}];
        await wrapper.setProps({unidades: novasUnidades});
        expect((wrapper.vm as any).isExpanded(novasUnidades[0])).toBe(false);
    });

    it("deve calcular estado de seleção para INTEROPERACIONAL", () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 500,
                sigla: "INTER",
                nome: "Inter",
                isElegivel: true,
                tipo: "INTEROPERACIONAL",
                filhas: [
                    {codigo: 501, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL"},
                    {codigo: 502, sigla: "F2", nome: "F2", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                ]
            }
        ];

        // INTEROPERACIONAL self selected but no children selected -> should return true
        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [500]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});
        expect(root.props("getEstadoSelecao")(unidadesTeste[0])).toBe(true);

        // INTEROPERACIONAL not selected but some children selected -> indeterminate
        const wrapper2 = createWrapper({unidades: unidadesTeste, modelValue: [501]});
        const root2 = wrapper2.findComponent({name: "UnidadeTreeNode"});
        expect(root2.props("getEstadoSelecao")(unidadesTeste[0])).toBe("indeterminate");
    });

    it("deve deselecionar pai não INTEROPERACIONAL se um filho for desmarcado", async () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 600,
                sigla: "NORMAL",
                nome: "Normal",
                isElegivel: true,
                tipo: "OPERACIONAL",
                filhas: [
                    {codigo: 601, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
                ]
            }
        ];

        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [600, 601]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});

        await root.props("onToggle")(unidadesTeste[0].filhas![0], false);
        const emitted = wrapper.emitted("update:modelValue");
        expect(emitted![emitted!.length - 1][0]).not.toContain(600);
    });

    it("deve cobrir watcher de modelValue quando não há mudança real", async () => {
        const wrapper = createWrapper({modelValue: [10]});
        await wrapper.setProps({modelValue: [10]});
        expect((wrapper.vm as any).unidadesSelecionadasLocal).toEqual([10]);
    });

    it("deve remover seleção inelegível quando unidades muda", async () => {
        const wrapper = createWrapper({modelValue: [10, 21]});
        const unidadesAtualizadas: Unidade[] = [
            {
                codigo: 1,
                sigla: "ROOT",
                nome: "Raiz",
                isElegivel: false,
                tipo: "ADMINISTRATIVA",
                filhas: [
                    {
                        codigo: 10,
                        sigla: "FILHA1",
                        nome: "Filha 1",
                        isElegivel: false,
                        filhas: [],
                        tipo: "OPERACIONAL"
                    },
                    {
                        codigo: 20,
                        sigla: "FILHA2",
                        nome: "Filha 2",
                        isElegivel: false,
                        filhas: [
                            {
                                codigo: 21,
                                sigla: "NETA1",
                                nome: "Neta 1",
                                isElegivel: true,
                                filhas: [],
                                tipo: "OPERACIONAL"
                            }
                        ],
                        tipo: "INTERMEDIARIA"
                    }
                ]
            }
        ];

        await wrapper.setProps({unidades: unidadesAtualizadas});
        await wrapper.vm.$nextTick();

        const emissoes = wrapper.emitted("update:modelValue");
        expect(emissoes).toBeTruthy();
        const ultimaEmissao = emissoes![emissoes!.length - 1][0] as number[];
        expect(ultimaEmissao).toEqual([21]);
    });

    it("deve lidar com unidades sem propriedade filhas", async () => {
        const unidadesSemFilhas: Unidade[] = [
            {codigo: 700, sigla: "SOLO", nome: "Solo", isElegivel: true} as any
        ];
        const wrapper = createWrapper({unidades: unidadesSemFilhas, modelValue: [700]});
        expect((wrapper.vm as any).isHabilitado(unidadesSemFilhas[0])).toBe(true);
    });

    it("getEstadoSelecao deve retornar selfSelected se não houver filhas", () => {
        const wrapper = createWrapper();
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});
        const leaf: Unidade = {codigo: 999, sigla: "L", nome: "L", isElegivel: true, filhas: undefined};
        expect(root.props("getEstadoSelecao")(leaf)).toBe(false);
    });

    it("getEstadoSelecao deve retornar selfSelected se não houver descendentes elegíveis", () => {
        const unidadesTeste: Unidade[] = [
            {
                codigo: 800,
                sigla: "PAI",
                nome: "Pai",
                isElegivel: true,
                filhas: [
                    {codigo: 801, sigla: "F1", nome: "F1", isElegivel: false, filhas: []}
                ]
            }
        ];
        const wrapper = createWrapper({unidades: unidadesTeste, modelValue: [800]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});
        expect(root.props("getEstadoSelecao")(unidadesTeste[0])).toBe(true);
    });

    it("watch unidades deve lidar com lista vazia", async () => {
        const wrapper = createWrapper();
        await wrapper.setProps({unidades: []});
    });

    it("unidadesExibidas lida com ocultarRaiz true e unidade sem filhas", () => {
        const units: Unidade[] = [{codigo: 1, sigla: "R", nome: "R", filhas: undefined}];
        const wrapper = createWrapper({unidades: units, ocultarRaiz: true});
        expect((wrapper.vm as any).unidadesExibidas).toHaveLength(0);
    });

    it("isHabilitado deve ser recursivo", () => {
        const units: Unidade[] = [{
            codigo: 1, sigla: "R", nome: "R", isElegivel: false,
            filhas: [{codigo: 2, sigla: "F", nome: "F", isElegivel: true, filhas: []}]
        }];
        const wrapper = createWrapper({unidades: units, modoSelecao: true});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});
        expect(root.props("isHabilitado")(units[0])).toBe(true);
    });

    it("getEstadoSelecao para INTEROPERACIONAL com filhos parcial selecionados", () => {
        const units: Unidade[] = [{
            codigo: 1000, sigla: "INTER", nome: "Inter", isElegivel: true, tipo: "INTEROPERACIONAL",
            filhas: [
                {codigo: 1001, sigla: "F1", nome: "F1", isElegivel: true, filhas: [], tipo: "OPERACIONAL"},
                {codigo: 1002, sigla: "F2", nome: "F2", isElegivel: true, filhas: [], tipo: "OPERACIONAL"}
            ]
        }];
        const wrapper = createWrapper({unidades: units, modelValue: [1000, 1001]});
        const root = wrapper.findComponent({name: "UnidadeTreeNode"});
        // Self selected AND some children selected -> true
        expect(root.props("getEstadoSelecao")(units[0])).toBe(true);
    });

    describe("Busca e Filtragem", () => {
        const mockUnidadesBusca: Unidade[] = [
            {
                codigo: 1,
                sigla: "STI",
                nome: "Secretaria de TI",
                isElegivel: true,
                filhas: [
                    {
                        codigo: 11,
                        sigla: "COSIS",
                        nome: "Coordenadoria de Sistemas",
                        isElegivel: true,
                        filhas: [
                            {codigo: 111, sigla: "SEDIA", nome: "Seção de Dados", isElegivel: true, filhas: []}
                        ]
                    },
                    {
                        codigo: 12,
                        sigla: "COINF",
                        nome: "Coordenadoria de Infra",
                        isElegivel: true,
                        filhas: []
                    }
                ]
            },
            {
                codigo: 2,
                sigla: "SGP",
                nome: "Secretaria de Pessoas",
                isElegivel: true,
                filhas: []
            }
        ];

        it("deve renderizar o campo de busca", () => {
            const wrapper = createWrapper({unidades: mockUnidadesBusca});
            expect(wrapper.find('input[type="search"]').exists()).toBe(true);
        });

        it("deve filtrar unidades por sigla", async () => {
            const wrapper = createWrapper({unidades: mockUnidadesBusca, ocultarRaiz: false});
            const input = wrapper.find('input[type="search"]');

            await input.setValue("COSIS");

            const exibidas = (wrapper.vm as any).unidadesExibidas;
            // Deve mostrar STI (pai) e COSIS
            expect(exibidas).toHaveLength(1);
            expect(exibidas[0].sigla).toBe("STI");
            expect(exibidas[0].filhas).toHaveLength(1);
            expect(exibidas[0].filhas[0].sigla).toBe("COSIS");
            // COINF e SGP devem sumir
            expect(exibidas[0].filhas.find((u: any) => u.sigla === "COINF")).toBeUndefined();
        });

        it("deve filtrar unidades por nome", async () => {
            const wrapper = createWrapper({unidades: mockUnidadesBusca, ocultarRaiz: false});
            const input = wrapper.find('input[type="search"]');

            await input.setValue("Sistemas");

            const exibidas = (wrapper.vm as any).unidadesExibidas;
            expect(exibidas[0].filhas[0].sigla).toBe("COSIS");
        });

        it("deve expandir automaticamente as unidades ao pesquisar", async () => {
            const wrapper = createWrapper({unidades: mockUnidadesBusca, ocultarRaiz: false});
            const input = wrapper.find('input[type="search"]');

            expect((wrapper.vm as any).isExpanded(mockUnidadesBusca[0])).toBe(false);

            await input.setValue("SEDIA");

            expect((wrapper.vm as any).isExpanded(mockUnidadesBusca[0])).toBe(true);
            expect((wrapper.vm as any).isExpanded(mockUnidadesBusca[0].filhas![0])).toBe(true);
        });

        it("deve restaurar a árvore original ao limpar a busca", async () => {
            const wrapper = createWrapper({unidades: mockUnidadesBusca, ocultarRaiz: false});
            const input = wrapper.find('input[type="search"]');

            await input.setValue("COSIS");
            expect((wrapper.vm as any).unidadesExibidas).toHaveLength(1);

            await input.setValue("");
            expect((wrapper.vm as any).unidadesExibidas).toHaveLength(2);
        });

        it("não deve renderizar o campo de busca se a árvore estiver vazia", () => {
            const wrapper = createWrapper({unidades: []});
            expect(wrapper.find('input[type="search"]').exists()).toBe(false);
        });
    });
});
