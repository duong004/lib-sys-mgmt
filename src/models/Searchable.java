package models;

import java.util.List;

interface Searchable {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    Book searchByISBN(String isbn);
    List<Book> searchByCategory(String category);
}