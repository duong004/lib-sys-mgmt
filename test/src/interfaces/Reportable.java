package interfaces;

public interface Reportable {
    void generatePopularBooksReport();
    void generateActiveReadersReport();
    void generateOverdueReport();
    void generateMonthlyStatistics();
}
