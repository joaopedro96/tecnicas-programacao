package biblioteca.servico;

import biblioteca.modelo.Emprestimo;
import biblioteca.modelo.Livro;
import biblioteca.modelo.Usuario;
import biblioteca.repositorio.EmprestimoRepositorio;
import biblioteca.repositorio.LivroRepositorio;
import biblioteca.repositorio.UsuarioRepositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BibliotecaServico {

    public static final int LIMITE_EMPRESTIMOS = 3;

    private final LivroRepositorio livroRepo;
    private final UsuarioRepositorio usuarioRepo;
    private final EmprestimoRepositorio emprestimoRepo;

    public BibliotecaServico(LivroRepositorio livroRepo,
            UsuarioRepositorio usuarioRepo,
            EmprestimoRepositorio emprestimoRepo) {
        this.livroRepo = livroRepo;
        this.usuarioRepo = usuarioRepo;
        this.emprestimoRepo = emprestimoRepo;
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 6 — Optional (Módulo 6)
    // -------------------------------------------------------------------------

    /**
     * Registra um novo empréstimo aplicando todas as regras de negócio.
     *
     * Regras:
     *   - O usuário deve existir
     *   - O livro deve existir
     *   - O usuário não pode ter mais de 3 empréstimos ativos
     *   - O livro não pode estar emprestado no momento
     *
     * Passos:
     *   1. Recupere o usuário usando usuarioRepo.buscarPorId(usuarioId)
     *      Use .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioId))
     *
     *   2. Recupere o livro usando livroRepo.buscarPorId(livroId)
     *      Use .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado: " + livroId))
     *
     *   3. Verifique o limite de empréstimos com validarLimiteEmprestimos(usuarioId)
     *      Se retornar false, lance: new IllegalStateException("Limite de empréstimos atingido para o usuário: " + usuarioId)
     *
     *   4. Verifique se o livro já está emprestado:
     *      emprestimoRepo.buscarAbertos().stream()
     *          .filter(e -> e.getLivroId().equals(livroId))
     *          .findFirst()
     *          .ifPresent(e -> { throw new IllegalStateException("Livro já está emprestado: " + livroId); });
     *
     *   5. Crie, salve e retorne: emprestimoRepo.salvar(new Emprestimo(usuarioId, livroId))
     *
     * @throws IllegalArgumentException se usuário ou livro não existir
     * @throws IllegalStateException    se alguma regra de negócio for violada
     */
    // TODO Exercício 6

    public Emprestimo registrarEmprestimo(Long usuarioId, Long livroId) {
        Usuario usuario = usuarioRepo.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioId));

        Livro livro = livroRepo.buscarPorId(livroId)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado: " + livroId));

        if (!validarLimiteEmprestimos(usuarioId)) {
            throw new IllegalStateException(
                    "Limite de empréstimos atingido para o usuário: " + usuarioId);
        }

        emprestimoRepo.buscarAbertos().stream()
                .filter(e -> e.getLivroId().equals(livroId))
                .findFirst()
                .ifPresent(e -> {
                    throw new IllegalStateException(
                            "Livro já está emprestado: " + livroId);
                });

        return emprestimoRepo.salvar(new Emprestimo(usuarioId, livroId));
    }

    // -------------------------------------------------------------------------
    // Fornecido — devolverLivro (exemplo de Optional encadeado)
    // -------------------------------------------------------------------------

    /**
     * Registra a devolução de um empréstimo.
     *
     * Observe como Optional.orElseThrow() é usado para garantir que o empréstimo
     * existe antes de operar sobre ele — padrão que você repetirá no Exercício 6.
     */
    public Emprestimo devolverLivro(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepo.buscarPorId(emprestimoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empréstimo não encontrado: " + emprestimoId));

        if (emprestimo.isDevolvido()) {
            throw new IllegalStateException("Empréstimo já foi devolvido: " + emprestimoId);
        }

        emprestimo.setDataDevolvido(LocalDateTime.now());
        return emprestimoRepo.salvar(emprestimo);
    }

    // -------------------------------------------------------------------------
    // Fornecido — validarLimiteEmprestimos
    // -------------------------------------------------------------------------

    /**
     * Retorna true se o usuário ainda pode fazer novos empréstimos.
     */
    public boolean validarLimiteEmprestimos(Long usuarioId) {
        return emprestimoRepo.buscarAbertosDoUsuario(usuarioId).size() < LIMITE_EMPRESTIMOS;
    }

    // -------------------------------------------------------------------------
    // Fornecido — calcularMultaTotal
    // -------------------------------------------------------------------------

    /**
     * Calcula o total de multas em aberto de um usuário.
     */
    public BigDecimal calcularMultaTotal(Long usuarioId) {
        return emprestimoRepo.buscarAbertosDoUsuario(usuarioId).stream()
                .filter(Emprestimo::estaAtrasado)
                .map(Emprestimo::calcularMulta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
