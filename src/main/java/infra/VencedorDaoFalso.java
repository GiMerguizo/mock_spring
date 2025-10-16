package infra;

import java.util.ArrayList;
import java.util.List;
import br.com.valueprojects.mock_spring.model.Vencedor;

public class VencedorDaoFalso implements VencedorDao {

    private static List<Vencedor> vencedores = new ArrayList<>();

    @Override
    public void salva(Vencedor vencedor) {
        vencedores.add(vencedor);
    }

    public List<Vencedor> todos() {
        return vencedores;
    }
}