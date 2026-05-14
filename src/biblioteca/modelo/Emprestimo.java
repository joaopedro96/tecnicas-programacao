package biblioteca.modelo;

import simplodb.Persistivel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Emprestimo implements Persistivel {

    private static final long serialVersionUID = 1L;

    public static final int PRAZO_DIAS = 14;
    public static final BigDecimal MULTA_POR_DIA = BigDecimal.ONE;

    private Long id;
    private Long usuarioId;
    private Long livroId;
    private LocalDateTime dataEmprestimo;
    private LocalDateTime dataDevolucaoPrevista;
    private LocalDateTime dataDevolvido;

    public Emprestimo() {
    }

    public Emprestimo(Long usuarioId, Long livroId) {
        this.usuarioId = usuarioId;
        this.livroId = livroId;
        this.dataEmprestimo = LocalDateTime.now();
        this.dataDevolucaoPrevista = this.dataEmprestimo.plusDays(PRAZO_DIAS);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getLivroId() {
        return livroId;
    }

    public void setLivroId(Long livroId) {
        this.livroId = livroId;
    }

    public LocalDateTime getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDateTime dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDateTime getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public void setDataDevolucaoPrevista(LocalDateTime dataDevolucaoPrevista) {
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    }

    public LocalDateTime getDataDevolvido() {
        return dataDevolvido;
    }

    public void setDataDevolvido(LocalDateTime dataDevolvido) {
        this.dataDevolvido = dataDevolvido;
    }

    public boolean isDevolvido() {
        return dataDevolvido != null;
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 1a — Datas (Módulo 1)
    // -------------------------------------------------------------------------

    /**
     * Retorna true se o empréstimo está atrasado.
     *
     * Um empréstimo está atrasado quando:
     * - ainda não foi devolvido (dataDevolvido == null), E
     * - o instante atual é posterior à dataDevolucaoPrevista
     *
     * Dica: use LocalDateTime.now() e o método isAfter()
     */
    public boolean estaAtrasado() {
        // TODO Exercício 1a
        boolean passouDataEntrega = LocalDateTime.now().isAfter(dataDevolucaoPrevista);
        boolean livroNaoDevolvido = dataDevolvido == null;

        return livroNaoDevolvido && passouDataEntrega;
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 1b — Datas (Módulo 1)
    // -------------------------------------------------------------------------

    /**
     * Calcula a multa acumulada em reais (R$ 1,00 por dia de atraso).
     *
     * Regras:
     * - Se não estiver atrasado → retorna BigDecimal.ZERO
     * - Se já foi devolvido → calcula com base em dataDevolvido
     * - Se ainda não devolvido → calcula com base em LocalDateTime.now()
     *
     * Passos:
     * 1. Se !estaAtrasado(), retorne BigDecimal.ZERO
     * 2. Determine a data de referência:
     * - isDevolvido() → use dataDevolvido
     * - senão → use LocalDateTime.now()
     * 3. Calcule os dias de atraso:
     * long dias = ChronoUnit.DAYS.between(dataDevolucaoPrevista, referencia)
     * 4. Retorne MULTA_POR_DIA.multiply(BigDecimal.valueOf(dias))
     */
    public BigDecimal calcularMulta() {
        if (!estaAtrasado()) {
            return BigDecimal.ZERO;
        }

        LocalDateTime referencia = isDevolvido() ? dataDevolvido : LocalDateTime.now();

        long dias = ChronoUnit.DAYS.between(
                dataDevolucaoPrevista,
                referencia);

        return MULTA_POR_DIA.multiply(BigDecimal.valueOf(dias));
    }

    // -------------------------------------------------------------------------
    // TODO Exercício 1c — Datas (Módulo 1)
    // -------------------------------------------------------------------------

    /**
     * Formata um resumo legível do empréstimo.
     *
     * Formato esperado para empréstimo em aberto no prazo:
     * "Empréstimo #3 | Livro: 7 | Usuário: 2 | Vence: 20/05/2026 14:30"
     *
     * Formato esperado para empréstimo atrasado:
     * "Empréstimo #3 | Livro: 7 | Usuário: 2 | Vence: 20/05/2026 14:30 | ATRASADO |
     * Multa: R$ 3,00"
     *
     * Formato esperado para empréstimo devolvido:
     * "Empréstimo #3 | Livro: 7 | Usuário: 2 | Vence: 20/05/2026 14:30 | Devolvido:
     * 18/05/2026 09:00"
     *
     * Passos:
     * 1. Crie um DateTimeFormatter com padrão "dd/MM/yyyy HH:mm"
     * 2. Formate dataDevolucaoPrevista com o formatter
     * 3. Monte a String base: "Empréstimo #" + id + " | Livro: " + livroId + ...
     * 4. Se isDevolvido(), acrescente " | Devolvido: " + dataDevolvido formatada
     * 5. Se estaAtrasado(), acrescente " | ATRASADO | Multa: R$ " + calcularMulta()
     * Dica: String.format("%.2f", calcularMulta()) formata com 2 casas decimais
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String base = "Empréstimo #" + id +
                " | Livro: " + livroId +
                " | Usuário: " + usuarioId +
                " | Vence: " + dataDevolucaoPrevista.format(formatter);

        if (isDevolvido()) {
            return base + " | Devolvido: "
                    + dataDevolvido.format(formatter);
        }

        if (estaAtrasado()) {
            return base + " | ATRASADO | Multa: R$ "
                    + calcularMulta();
        }

        return base;
    }
}
