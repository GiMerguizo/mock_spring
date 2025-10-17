package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.*;
import infra.*;

public class FinalizaJogoTest {

    @Test
    public void deveFinalizarJogosDaSemanaAnterior() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        Jogo jogo1 = new CriadorDeJogo().para("Caça moedas").naData(antiga).constroi();
        Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();
        List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

        JogoDao daoFalso = mock(JogoDao.class);
        VencedorDao vencedorDaoFalso = mock(VencedorDao.class);
        MessageSender senderFalso = mock(MessageSender.class);

        when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

        FinalizaJogo finalizador = new FinalizaJogo(daoFalso, vencedorDaoFalso, senderFalso);
        finalizador.finaliza();

        assertTrue(jogo1.isFinalizado());
        assertTrue(jogo2.isFinalizado());
        assertEquals(2, finalizador.getTotalFinalizados());
    }

    @Test
    public void deveVerificarSeMetodoAtualizaFoiInvocado() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);
        Jogo jogo1 = new CriadorDeJogo().para("Cata moedas").naData(antiga).constroi();
        Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();
        List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

        JogoDao daoFalso = mock(JogoDao.class);
        VencedorDao vencedorDaoFalso = mock(VencedorDao.class);
        MessageSender senderFalso = mock(MessageSender.class);

        when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

        FinalizaJogo finalizador = new FinalizaJogo(daoFalso, vencedorDaoFalso, senderFalso);
        finalizador.finaliza();

        verify(daoFalso, times(1)).atualiza(jogo1);
        verify(daoFalso, times(1)).atualiza(jogo2);
    }
}