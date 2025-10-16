package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.*;
import infra.*;

public class FinalizaJogoComSMSTest {

    private JogoDao daoFalso;
    private VencedorDao vencedorDaoFalso;
    private MessageSender senderFalso;
    private FinalizaJogo finalizador;
    private Participante vencedor;
    private Participante perdedor;

    @BeforeEach
    public void setup() {
        daoFalso = mock(JogoDao.class);
        vencedorDaoFalso = mock(VencedorDao.class);
        senderFalso = mock(MessageSender.class);
        finalizador = new FinalizaJogo(daoFalso, vencedorDaoFalso, senderFalso);
        vencedor = new Participante("Vencedor");
        perdedor = new Participante("Perdedor");
    }

    @Test
    public void deveSalvarJogosFinalizadosESalvarVencedorEEnviarMensagem() {
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);
        Jogo jogo1 = new CriadorDeJogo().para("Corrida Maluca").naData(dataAntiga)
            .resultado(vencedor, 100).resultado(perdedor, 50).constroi();
        when(daoFalso.emAndamento()).thenReturn(Arrays.asList(jogo1));

        finalizador.finaliza();

        verify(daoFalso).atualiza(jogo1);
        verify(vencedorDaoFalso).salva(any(Vencedor.class));
        verify(senderFalso).send(eq(vencedor), anyString());
    }

    @Test
    public void deveGarantirAOrdemCorretaDasOperacoes() {
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);
        Jogo jogo1 = new CriadorDeJogo().para("Corrida Maluca").naData(dataAntiga)
            .resultado(vencedor, 100).resultado(perdedor, 50).constroi();
        when(daoFalso.emAndamento()).thenReturn(Arrays.asList(jogo1));

        finalizador.finaliza();

        InOrder inOrder = inOrder(daoFalso, vencedorDaoFalso, senderFalso);
        inOrder.verify(daoFalso).atualiza(jogo1);
        inOrder.verify(vencedorDaoFalso).salva(any(Vencedor.class));
        inOrder.verify(senderFalso).send(eq(vencedor), anyString());
    }


    @Test
    public void naoDeveFinalizarJogosRecentes() {
      
        Jogo jogoRecente = new CriadorDeJogo().para("Jogo de Hoje")
            .naData(Calendar.getInstance())
            .resultado(vencedor, 100)
            .constroi();

        when(daoFalso.emAndamento()).thenReturn(Arrays.asList(jogoRecente));

        finalizador.finaliza();

        verify(daoFalso, never()).atualiza(any(Jogo.class));
        verify(vencedorDaoFalso, never()).salva(any(Vencedor.class));
        verify(senderFalso, never()).send(any(Participante.class), anyString());
    }
    
    @Test
    public void naoDeveSalvarVencedorNemEnviarMensagemSeJogoNaoTiverResultados() {
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);
        Jogo jogoSemResultados = new CriadorDeJogo().para("Jogo Vazio").naData(dataAntiga).constroi();
        when(daoFalso.emAndamento()).thenReturn(Arrays.asList(jogoSemResultados));
        
        finalizador.finaliza();

        verify(daoFalso).atualiza(jogoSemResultados);
        verify(vencedorDaoFalso, never()).salva(any(Vencedor.class));
        verify(senderFalso, never()).send(any(Participante.class), anyString());
    }

    @Test
    public void naoDeveFazerNadaEmCasoDeErroAoSalvarJogo() {
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);
        Jogo jogo1 = new CriadorDeJogo().para("Corrida Maluca").naData(dataAntiga)
            .resultado(vencedor, 100).resultado(perdedor, 50).constroi();
        when(daoFalso.emAndamento()).thenReturn(Arrays.asList(jogo1));
        doThrow(new RuntimeException("Falha ao salvar no banco")).when(daoFalso).atualiza(jogo1);

        assertThrows(RuntimeException.class, () -> finalizador.finaliza());
        
        verify(vencedorDaoFalso, never()).salva(any(Vencedor.class));
        verify(senderFalso, never()).send(any(Participante.class), anyString());
    }

     @Test
    public void naoDeveFazerNadaCasoNaoHajaJogos() {
        when(daoFalso.emAndamento()).thenReturn(Arrays.asList());

        finalizador.finaliza();

        verifyNoInteractions(vencedorDaoFalso, senderFalso);
        verify(daoFalso, never()).atualiza(any());
    }
}