package models;

public interface Reportable {
    void generatePopularBooksReport();
    void generateActiveReadersReport();
    void generateOverdueReport();
    void generateMonthlyStatistics();
}
