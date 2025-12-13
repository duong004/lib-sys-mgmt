package models.books;

public abstract class Book {
    protected String ISBN;
    protected String title;
    protected String author;
    protected String publisher;
    protected int publishYear;
    protected String category;
    protected int totalCopies;
    protected int availableCopies;
    protected double price;

    public Book(String ISBN, String title, String author, int totalCopies) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public abstract void displayDetails();

    public String getInfo() {
        return String.format("ISBN: %s | Tên: %s | Tác giả: %s | Có sẵn: %d/%d",
                ISBN, title, author, availableCopies, totalCopies);
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public boolean borrowBook() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public double calculateLateFee(int daysLate) {
        return daysLate * 5000; // 5000 VND per day
    }

    // Getters and Setters
    public String getISBN() { return ISBN; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getAvailableCopies() { return availableCopies; }
    public int getTotalCopies() { return totalCopies; }
    public void setCategory(String category) { this.category = category; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setPublishYear(int year) { this.publishYear = year; }
    public void setPrice(double price) { this.price = price; }
    public String getPublisher() { return publisher; }
    public int getPublishYear() { return publishYear; }
    public double getPrice() { return price; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
}