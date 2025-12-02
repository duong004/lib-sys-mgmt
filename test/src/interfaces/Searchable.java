package interfaces;

import models.books.Book;
import java.util.List;

public interface Searchable {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    Book searchByISBN(String isbn);
    List<Book> searchByCategory(String category);
}