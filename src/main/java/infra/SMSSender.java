package infra;

import br.com.valueprojects.mock_spring.model.Participante;

public interface SMSSender {
    void send(Participante participante);
}