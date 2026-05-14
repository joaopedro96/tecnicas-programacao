package biblioteca.servico;

import biblioteca.modelo.Emprestimo;
import biblioteca.modelo.Livro;
import biblioteca.repositorio.AutorRepositorio;
import biblioteca.repositorio.EmprestimoRepositorio;
import biblioteca.repositorio.LivroRepositorio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RelatorioServico {

    private final LivroRepositorio livroRepo;
    private final AutorRepositorio autorRepo;
    private final EmprestimoRepositorio emprestimoRepo;

    public RelatorioServico(LivroRepositorio livroRepo,
            AutorRepositorio autorRepo,
            EmprestimoRepositorio emprestimoRepo) {
        this.livroRepo = livroRepo;
        this.autorRepo = autorRepo;
        this.emprestimoRepo = emprestimoRepo;
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 3c — Streams (Módulo 3)
    // -------------------------------------------------------------------------

    /**
     * Retorna os 5 livros mais emprestados de todos os tempos.
     *
     * Passos:
     * 1. emprestimoRepo.buscarTodos().stream()
     * 2. .collect(Collectors.groupingBy(Emprestimo::getLivroId,
     * Collectors.counting()))
     * → produz Map<Long, Long>: livroId → quantidade de empréstimos
     * 3. .entrySet().stream()
     * 4. .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
     * 5. .limit(5)
     * 6. .map(entry -> livroRepo.buscarPorId(entry.getKey()))
     * 7. .filter(Optional::isPresent).map(Optional::get)
     * 8. .collect(Collectors.toList())
     *
     * @return lista de até 5 livros, do mais para o menos emprestado
     */
    public List<Livro> top5LivrosMaisEmprestados() {
        return emprestimoRepo.buscarTodos().stream()
                .collect(Collectors.groupingBy(
                        Emprestimo::getLivroId,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> livroRepo.buscarPorId(entry.getKey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 3d — Streams (Módulo 3)
    // -------------------------------------------------------------------------

    /**
     * Retorna o total de multas pendentes agrupado por usuário.
     * Considera apenas empréstimos atrasados e ainda não devolvidos.
     *
     * Passos:
     * 1. emprestimoRepo.buscarTodos().stream()
     * 2. .filter(e -> e.estaAtrasado())
     * 3. .collect(Collectors.toMap(
     * Emprestimo::getUsuarioId,
     * Emprestimo::calcularMulta,
     * BigDecimal::add ← merge: soma quando o mesmo usuário aparece mais de uma vez
     * ))
     *
     * @return Map<Long, BigDecimal> de usuarioId → soma das multas pendentes
     */
    public Map<Long, BigDecimal> multasPendentesPorUsuario() {
        return emprestimoRepo.buscarTodos().stream()
                .filter(Emprestimo::estaAtrasado)
                .collect(Collectors.toMap(
                        Emprestimo::getUsuarioId,
                        Emprestimo::calcularMulta,
                        BigDecimal::add));
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 7 ⭐ BÔNUS — Programação Paralela (Módulo 7)
    // -------------------------------------------------------------------------

    /**
     * Gera um relatório completo da biblioteca executando as três consultas em
     * paralelo.
     *
     * Cada consulta é independente das outras — rodá-las em paralelo com
     * CompletableFuture reduz o tempo total de geração do relatório.
     *
     * Passos:
     * 1. Crie um ExecutorService com Executors.newFixedThreadPool(3)
     *
     * 2. Dispare as três consultas em paralelo:
     * CompletableFuture<List<Livro>> futureTop5 =
     * CompletableFuture.supplyAsync(() -> top5LivrosMaisEmprestados(), executor);
     *
     * CompletableFuture<Map<Long, BigDecimal>> futureMultas =
     * CompletableFuture.supplyAsync(() -> multasPendentesPorUsuario(), executor);
     *
     * CompletableFuture<Map<String, List<Livro>>> futureGeneros =
     * CompletableFuture.supplyAsync(() -> livroRepo.agruparPorGenero(), executor);
     *
     * 3. Aguarde todas terminarem:
     * CompletableFuture.allOf(futureTop5, futureMultas, futureGeneros).join();
     *
     * 4. Colete os resultados com .join() em cada future
     *
     * 5. Encerre o executor: executor.shutdown()
     *
     * 6. Retorne new RelatorioCompleto(top5, multas, generos)
     */
    public RelatorioCompleto gerarRelatorioCompleto() {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        CompletableFuture<List<Livro>> futureTop5 = CompletableFuture.supplyAsync(
                () -> top5LivrosMaisEmprestados(),
                executor);

        CompletableFuture<Map<Long, BigDecimal>> futureMultas = CompletableFuture.supplyAsync(
                () -> multasPendentesPorUsuario(),
                executor);

        CompletableFuture<Map<String, List<Livro>>> futureGeneros = CompletableFuture.supplyAsync(
                () -> livroRepo.agruparPorGenero(),
                executor);

        CompletableFuture.allOf(
                futureTop5,
                futureMultas,
                futureGeneros).join();

        List<Livro> top5 = futureTop5.join();

        Map<Long, BigDecimal> multas = futureMultas.join();

        Map<String, List<Livro>> generos = futureGeneros.join();

        executor.shutdown();

        return new RelatorioCompleto(
                top5,
                multas,
                generos);
    }

    // -------------------------------------------------------------------------
    // Record de resultado — fornecido
    // -------------------------------------------------------------------------

    public record RelatorioCompleto(
            List<Livro> top5MaisEmprestados,
            Map<Long, BigDecimal> multasPendentes,
            Map<String, List<Livro>> livrosPorGenero) {
    }
}
