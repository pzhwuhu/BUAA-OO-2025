import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.annotation.SendMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class HotBookShelf {
    private HashMap<LibraryBookIsbn, ArrayList<Book>> books = new HashMap<>();

    @SendMessage(from = "HotBookShelf", to = "Book")
    public void addBook(Book book) {
        books.putIfAbsent(book.getIsbn(), new ArrayList<>());
        books.get(book.getIsbn()).add(book);
    }

    @SendMessage(from = "HotBookShelf", to = "Book")
    public Book getBook(LibraryBookIsbn isbn) {
        if (books.containsKey(isbn) && !books.get(isbn).isEmpty()) {
            return books.get(isbn).get(0);
        }
        return null;
    }

    public boolean containsBook(LibraryBookIsbn isbn) {
        if (books.containsKey(isbn)) {
            return !books.get(isbn).isEmpty();
        }
        return false;
    }

    public void removeBook(LibraryBookId bookId) {
        ArrayList<Book> booksToRemove = books.get(bookId.getBookIsbn());
        if (booksToRemove != null) {
            for (Book book : booksToRemove) {
                if (book.getBookId().equals(bookId)) {
                    booksToRemove.remove(book);
                    return;
                }
            }
        }
    }

    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> allBooks = new ArrayList<>();
        for (ArrayList<Book> bookList : books.values()) {
            allBooks.addAll(bookList);
        }
        return allBooks;
    }

    public void clear() {
        books.clear();
    }
}