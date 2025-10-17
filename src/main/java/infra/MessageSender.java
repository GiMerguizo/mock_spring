package infra;

import br.com.valueprojects.mock_spring.model.Participante;

/**
 * Interface genérica para qualquer serviço de envio de mensagens.
 */
public interface MessageSender {
    void send(Participante participante, String mensagem);
}