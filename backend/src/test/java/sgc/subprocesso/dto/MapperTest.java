package sgc.subprocesso.dto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.comum.repo.RepositorioComum;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.mapper.MovimentacaoMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class MapperTest {
    private final SubprocessoMapper subprocessoMapper = Mappers.getMapper(SubprocessoMapper.class);
    private final MovimentacaoMapper movimentacaoMapper =
            Mappers.getMapper(MovimentacaoMapper.class);
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private RepositorioComum repo;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field processoRepoField =
                SubprocessoMapper.class.getDeclaredField("processoRepo");
        processoRepoField.setAccessible(true);
        processoRepoField.set(subprocessoMapper, processoRepo);

        java.lang.reflect.Field unidadeRepoField =
                SubprocessoMapper.class.getDeclaredField("unidadeRepo");
        unidadeRepoField.setAccessible(true);
        unidadeRepoField.set(subprocessoMapper, unidadeRepo);

        java.lang.reflect.Field mapaRepoField =
                SubprocessoMapper.class.getDeclaredField("mapaRepo");
        mapaRepoField.setAccessible(true);
        mapaRepoField.set(subprocessoMapper, mapaRepo);

        java.lang.reflect.Field repoField =
                SubprocessoMapper.class.getDeclaredField("repo");
        repoField.setAccessible(true);
        repoField.set(subprocessoMapper, repo);
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
        entity.setDataLimiteEtapa1(LocalDateTime.now());
        entity.setDataFimEtapa1(LocalDateTime.now());
        entity.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));
        entity.setDataFimEtapa2(LocalDateTime.now().plusHours(1));
        entity.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        SubprocessoDto dto = subprocessoMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getCodProcesso());
        assertEquals(200L, dto.getCodUnidade());
        assertEquals(300L, dto.getCodMapa());
        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, dto.getSituacao());
    }

    // REMOVIDO: Teste do método toEntity() que não é mais usado após simplificação
    // O método toEntity(SubprocessoDto) foi removido do mapper por não ser necessário

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

        MovimentacaoDto dto = movimentacaoMapper.toDto(entity);

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
