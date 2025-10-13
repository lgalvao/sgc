package sgc.subprocesso.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class MapperTest {
    @Test
    void subprocessoMapper_MapsEntityToDtoCorrectly() {
        SubprocessoMapper mapper = Mappers.getMapper(SubprocessoMapper.class);

        Processo processo = new Processo();
        processo.setCodigo(100L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(200L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);

        Subprocesso entity = new Subprocesso();
        entity.setCodigo(1L);
        entity.setProcesso(processo);
        entity.setUnidade(unidade);
        entity.setMapa(mapa);
        entity.setDataLimiteEtapa1(LocalDate.now());
        entity.setDataFimEtapa1(LocalDateTime.now());
        entity.setDataLimiteEtapa2(LocalDate.now().plusDays(10));
        entity.setDataFimEtapa2(LocalDateTime.now().plusHours(1));
        entity.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);

        SubprocessoDto dto = mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getProcessoCodigo());
        assertEquals(200L, dto.getUnidadeCodigo());
        assertEquals(300L, dto.getMapaCodigo());
        assertEquals(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, dto.getSituacao());
    }

    @Test
    void subprocessoMapper_MapsDtoToEntityCorrectly() {
        SubprocessoMapper mapper = Mappers.getMapper(SubprocessoMapper.class);

        SubprocessoDto dto = new SubprocessoDto(
            1L,
            100L,
            200L,
            300L,
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDate.now().plusDays(10),
            LocalDateTime.now().plusHours(1),
            SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO
        );

        Subprocesso entity = mapper.toEntity(dto);

        assertNotNull(entity);
        // Note: The mapping expressions in the mapper create new Processo/Unidade/Mapa objects
        // based on the code in the mapper, so we check that the objects are initialized
        assertNotNull(entity.getProcesso());
        assertEquals(100L, entity.getProcesso().getCodigo());
        assertNotNull(entity.getUnidade());
        assertEquals(200L, entity.getUnidade().getCodigo());
        assertNotNull(entity.getMapa());
        assertEquals(300L, entity.getMapa().getCodigo());
        assertEquals(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, entity.getSituacao());
    }

    @Test
    void movimentacaoMapper_MapsEntityToDtoCorrectly() {
        MovimentacaoMapper mapper = Mappers.getMapper(MovimentacaoMapper.class);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);

        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(100L);
        unidadeOrigem.setSigla("ORIGEM");
        unidadeOrigem.setNome("Unidade Origem");

        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(200L);
        unidadeDestino.setSigla("DESTINO");
        unidadeDestino.setNome("Unidade Destino");

        Movimentacao entity = new Movimentacao();
        entity.setCodigo(1L);
        entity.setSubprocesso(subprocesso);
        entity.setDataHora(LocalDateTime.now());
        entity.setUnidadeOrigem(unidadeOrigem);
        entity.setUnidadeDestino(unidadeDestino);
        entity.setDescricao("Descrição da movimentação");

        MovimentacaoDto dto = mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.codigo());
        assertEquals(100L, dto.unidadeOrigemCodigo());
        assertEquals("ORIGEM", dto.unidadeOrigemSigla());
        assertEquals("Unidade Origem", dto.unidadeOrigemNome());
        assertEquals(200L, dto.unidadeDestinoCodigo());
        assertEquals("DESTINO", dto.unidadeDestinoSigla());
        assertEquals("Unidade Destino", dto.unidadeDestinoNome());
        assertEquals("Descrição da movimentação", dto.descricao());
    }
}
