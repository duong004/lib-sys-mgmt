class Librarian extends Person {
    private String employeeId;
    private String position;
    private LocalDate hireDate;
    private double salary;
    private List<String> permissions;

    public Librarian(String id, String name, String email, String phone, String position) {
        super(id, name, email, phone);
        this.employeeId = id;
        this.position = position;
        this.hireDate = LocalDate.now();
        this.permissions = new ArrayList<>();
        initializePermissions();
    }

    private void initializePermissions() {
        permissions.add("ADD_BOOK");
        permissions.add("REMOVE_BOOK");
        permissions.add("REGISTER_READER");
        permissions.add("PROCESS_BORROW");
        permissions.add("GENERATE_REPORT");
    }

    @Override
    public String getInfo() {
        return String.format("Mã NV: %s | Tên: %s | Chức vụ: %s",
                employeeId, name, position);
    }

    public boolean hasPermission(String action) {
        return permissions.contains(action);
    }
}