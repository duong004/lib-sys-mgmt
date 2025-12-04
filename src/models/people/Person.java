package models.people;
import java.time.LocalDate;

public abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected String address;
    protected LocalDate dateOfBirth;

    public Person(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public abstract String getInfo();

    public boolean validateEmail() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean validatePhone() {
        return phone != null && phone.matches("^[0-9]{10}$");
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
}