package models.books;

public class ReferenceBook extends Book {
    private String topic;
    private boolean canBorrow;
    private String referenceNumber;

    public ReferenceBook(String ISBN, String title, String author, int totalCopies, String topic) {
        super(ISBN, title, author, totalCopies);
        this.topic = topic;
        this.canBorrow = true;
        this.category = "Sách tham khảo";
    }

    @Override
    public void displayDetails() {
        System.out.println("=== SÁCH THAM KHẢO ===");
        System.out.println(getInfo());
        System.out.println("Chủ đề: " + topic);
        System.out.println("Có thể mượn: " + (canBorrow ? "Có" : "Không"));
    }

    public boolean canBeBorrowed() { return canBorrow; }
    public void setCanBorrow(boolean canBorrow) { this.canBorrow = canBorrow; }
    public String getTopic() { return topic; }
}