import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import ProcessoSubprocessosTable from "../ProcessoSubprocessosTable.vue";
import {SituacaoSubprocesso} from "@/types/tipos";

describe("ProcessoSubprocessosTable.vue", () => {
    const participantesMock = [
        {
            codUnidade: 1,
            sigla: "ADMIN",
            nome: "Administração",
            codSubprocesso: 0, // Unidade sem subprocesso
            situacaoSubprocesso: null as any,
            dataLimite: null as any,
            filhos: [
                {
                    codUnidade: 2,
                    sigla: "SEC",
                    nome: "Secretaria",
                    codSubprocesso: 123, // Unidade com subprocesso
                    situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                    dataLimite: "2023-12-31T12:00:00",
                    filhos: []
                }
            ]
        }
    ];

    it("deve marcar unidades sem subprocesso como não clicáveis", () => {
        const wrapper = mount(ProcessoSubprocessosTable, {
            props: {
                participantesHierarquia: participantesMock
            }
        });

        // O mapeamentoHierarquia é uma computed, podemos verificar via vm (em teste unitário)
        // ou verificando as propriedades passadas para o TreeTable
        const treeTable = wrapper.findComponent({ name: 'TreeTable' });
        const data = treeTable.props('data');

        const admin = data.find((u: any) => u.sigla === "ADMIN");
        const sec = admin.children.find((u: any) => u.sigla === "SEC");

        expect(admin.clickable).toBe(false);
        expect(sec.clickable).toBe(true);
    });

    it("deve formatar corretamente os dados para a tabela", () => {
        const wrapper = mount(ProcessoSubprocessosTable, {
            props: {
                participantesHierarquia: participantesMock
            }
        });

        const treeTable = wrapper.findComponent({ name: 'TreeTable' });
        const data = treeTable.props('data');
        const sec = data[0].children[0];

        expect(sec.unidadeAtual).toBe("SEC - Secretaria");
        expect(sec.situacao).toBe("Cadastro em andamento");
        expect(sec.dataLimite).toBe("31/12/2023");
    });
});
