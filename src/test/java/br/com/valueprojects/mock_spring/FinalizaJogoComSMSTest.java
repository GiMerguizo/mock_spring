package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.FinalizaJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import infra.JogoDao;
import infra.SMSSender;

public class FinalizaJogoComSMSTest {

    private JogoDao daoFalso;
    private SMSSender smsSenderFalso;
    private FinalizaJogo finalizador;
    private Participante vencedor;
    private Participante perdedor;

    @BeforeEach
    public void setup() {
        daoFalso = mock(JogoDao.class);
        smsSenderFalso = mock(SMSSender.class);
        finalizador = new FinalizaJogo(daoFalso, smsSenderFalso);
        vencedor = new Participante("Vencedor");
        perdedor = new Participante("Perdedor");
    }

    @Test
    public void deveSalvarJogosFinalizadosEEnviarSMSParaVencedor() {
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);

        Jogo jogo1 = new CriadorDeJogo()
            .para("Corrida Maluca")
            .naData(dataAntiga)
            .resultado(vencedor, 100)
            .resultado(perdedor, 50)
            .constroi();

        List<Jogo> jogos = Arrays.asList(jogo1);

        when(daoFalso.emAndamento()).thenReturn(jogos);

        finalizador.finaliza();

        verify(daoFalso, times(1)).atualiza(jogo1);
        verify(smsSenderFalso, times(1)).send(vencedor);
    }

    @Test
    public void deveGarantirAOrdemDeSalvarPrimeiroEEnviarSMSDepois() {

        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);

        Jogo jogo1 = new CriadorDeJogo()
            .para("Corrida Maluca")
            .naData(dataAntiga)
            .resultado(vencedor, 100)
            .resultado(perdedor, 50)
            .constroi();

        List<Jogo> jogos = Arrays.asList(jogo1);

        when(daoFalso.emAndamento()).thenReturn(jogos);

        finalizador.finaliza();

        InOrder inOrder = inOrder(daoFalso, smsSenderFalso);
        inOrder.verify(daoFalso).atualiza(jogo1);
        inOrder.verify(smsSenderFalso).send(vencedor);
    }
    
    
    @Test
    public void deveFinalizarJogoSemResultadosSemEnviarSMS() {
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);

        Jogo jogoSemResultados = new CriadorDeJogo()
            .para("Jogo Vazio")
            .naData(dataAntiga)
            .constroi();

        List<Jogo> jogos = Arrays.asList(jogoSemResultados);

        when(daoFalso.emAndamento()).thenReturn(jogos);

        finalizador.finaliza();

        verify(daoFalso).atualiza(jogoSemResultados);
        verify(smsSenderFalso, never()).send(any(Participante.class));
    }


    @Test
    public void naoDeveEnviarSMSEmCasoDeErroAoSalvar() {

        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);

        Jogo jogo1 = new CriadorDeJogo()
            .para("Corrida Maluca")
            .naData(dataAntiga)
            .resultado(vencedor, 100)
            .resultado(perdedor, 50)
            .constroi();

        List<Jogo> jogos = Arrays.asList(jogo1);

        when(daoFalso.emAndamento()).thenReturn(jogos);
        doThrow(new RuntimeException("Falha ao salvar no banco")).when(daoFalso).atualiza(jogo1);

        assertThrows(RuntimeException.class, () -> {
            finalizador.finaliza();
        });

        verify(smsSenderFalso, never()).send(any(Participante.class));
    }

     @Test
    public void naoDeveFazerNadaCasoNaoHajaJogos() {

        when(daoFalso.emAndamento()).thenReturn(Arrays.asList());

        finalizador.finaliza();

        verifyNoInteractions(smsSenderFalso);
        verify(daoFalso, never()).atualiza(any());
    }
}