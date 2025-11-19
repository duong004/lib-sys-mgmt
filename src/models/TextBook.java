class TextBook extends Book {
    private String subject;
    private int grade;
    private int edition;

    public TextBook(String ISBN, String title, String author, int totalCopies, String subject, int grade) {
        super(ISBN, title, author, totalCopies);
        this.subject = subject;
        this.grade = grade;
        this.category = "Sách giáo khoa";
    }

    @Override
    public void displayDetails() {
        System.out.println("=== SÁCH GIÁO KHOA ===");
        System.out.println(getInfo());
        System.out.println("Môn học: " + subject);
        System.out.println("Lớp: " + grade);
    }

    public String getSubject() { return subject; }
    public int getGrade() { return grade; }
}