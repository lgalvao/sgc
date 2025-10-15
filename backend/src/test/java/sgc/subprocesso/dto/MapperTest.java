package sgc.subprocesso.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapperTest {
    private final SubprocessoMapper subprocessoMapper = Mappers.getMapper(SubprocessoMapper.class);
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private MapaRepo mapaRepo;

    private final MovimentacaoMapper movimentacaoMapper = Mappers.getMapper(MovimentacaoMapper.class);

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field processoRepoField = SubprocessoMapper.class.getDeclaredField("processoRepo");
        processoRepoField.setAccessible(true);
        processoRepoField.set(subprocessoMapper, processoRepo);

        java.lang.reflect.Field unidadeRepoField = SubprocessoMapper.class.getDeclaredField("unidadeRepo");
        unidadeRepoField.setAccessible(true);
        unidadeRepoField.set(subprocessoMapper, unidadeRepo);

        java.lang.reflect.Field mapaRepoField = SubprocessoMapper.class.getDeclaredField("mapaRepo");
        mapaRepoField.setAccessible(true);
        mapaRepoField.set(subprocessoMapper, mapaRepo);
    }

    @Test
    void subprocessoMapper_MapsEntityToDtoCorrectly() {
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

        SubprocessoDto dto = subprocessoMapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getProcessoCodigo());
        assertEquals(200L, dto.getUnidadeCodigo());
        assertEquals(300L, dto.getMapaCodigo());
        assertEquals(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, dto.getSituacao());
    }

    @Test
    void subprocessoMapper_MapsDtoToEntityCorrectly() {
        SubprocessoDto dto = new SubprocessoDto(
                1L,
                100L,
                200L,
                300L,
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDate.now().plusDays(10),
                SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO
        );

        Processo processo = new Processo();
        processo.setCodigo(100L);
        when(processoRepo.findById(100L)).thenReturn(Optional.of(processo));

        Unidade unidade = new Unidade();
        unidade.setCodigo(200L);
        when(unidadeRepo.findById(200L)).thenReturn(Optional.of(unidade));

        Mapa mapa = new Mapa();
        mapa.setCodigo(300L);
        when(mapaRepo.findById(300L)).thenReturn(Optional.of(mapa));

        Subprocesso entity = subprocessoMapper.toEntity(dto);

        assertNotNull(entity);
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

        MovimentacaoDto dto = movimentacaoMapper.toDTO(entity);

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