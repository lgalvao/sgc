package sesel.sgc.processounidade;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.atividades.Atividade;
import sesel.sgc.processo.Processo;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessoUnidadeTest {
    @Test
    void testGettersAndSetters() {
        ProcessoUnidade pu = new ProcessoUnidade();
        Unidade unidade = new Unidade();
        Processo processo = new Processo();
        List<Atividade> atividades = new ArrayList<>();
        Atividade atividade = new Atividade();
        atividades.add(atividade);
        MapaUnidadeSituacao situacao = MapaUnidadeSituacao.EM_ANDAMENTO;
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        LocalDateTime validacao = LocalDateTime.now().plusDays(2);
        LocalDateTime homologacao = LocalDateTime.now().plusDays(3);
        UUID id = UUID.randomUUID();

        pu.setUnidade(unidade);
        pu.setProcesso(processo);
        pu.setAtividades(atividades);
        pu.setSituacao(situacao);
        pu.setDataHoraInicioRegistro(inicio);
        pu.setDataHoraFimRegistro(fim);
        pu.setDataHoraValidacao(validacao);
        pu.setDataHoraHomologacao(homologacao);
        pu.setId(id);

        assertEquals(unidade, pu.getUnidade());
        assertEquals(processo, pu.getProcesso());
        assertEquals(atividades, pu.getAtividades());
        assertEquals(situacao, pu.getSituacao());
        assertEquals(inicio, pu.getDataHoraInicioRegistro());
        assertEquals(fim, pu.getDataHoraFimRegistro());
        assertEquals(validacao, pu.getDataHoraValidacao());
        assertEquals(homologacao, pu.getDataHoraHomologacao());
        assertEquals(id, pu.getId());
    }

    @Test
    void deveRespeitarContratoEqualsHashCode() {
        EqualsVerifier.forClass(ProcessoUnidade.class)
                .usingGetClass()
                .withRedefinedSuperclass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.IDENTICAL_COPY_FOR_VERSIONED_ENTITY)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }
}