package models.books;

import java.time.LocalDate;

public class Magazine extends Book {
    private int issueNumber;
    private LocalDate publishDate;
    private String frequency;

    public Magazine(String ISBN, String title, String author, int totalCopies, int issueNumber) {
        super(ISBN, title, author, totalCopies);
        this.issueNumber = issueNumber;
        this.publishDate = LocalDate.now();
        this.category = "Tạp chí";
    }

    @Override
    public void displayDetails() {
        System.out.println("=== TẠP CHÍ ===");
        System.out.println(getInfo());
        System.out.println("Số phát hành: " + issueNumber);
        System.out.println("Ngày xuất bản: " + publishDate);
    }

    public int getIssueNumber() { return issueNumber; }
    public boolean isLatestIssue() {
        return publishDate.isAfter(LocalDate.now().minusMonths(1));
    }
    public LocalDate getPublishDate() { return publishDate; }
}