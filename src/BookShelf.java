import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;

public class BookShelf {
    private HashMap<LibraryBookIsbn, ArrayList<Book>> books = new HashMap<>();

    public void addBook(Book book) {
        books.putIfAbsent(book.getIsbn(), new ArrayList<>());
        books.get(book.getIsbn()).add(book);
    }

    public Book getBook(LibraryBookIsbn isbn) {
        if (books.get(isbn).size() == 0) {
            return null;
        }
        return books.get(isbn).get(0);
    }

    public boolean containsBook(LibraryBookIsbn isbn) {
        if (books.containsKey(isbn)) {
            return books.get(isbn).size() > 0;
        }
        return false;
    }

    public void removeBook(LibraryBookId bookId) {
        ArrayList<Book> booksToRemove = books.get(bookId.getBookIsbn());
        for (Book book : booksToRemove) {
            if (book.getBookId().equals(bookId)) {
                booksToRemove.remove(book);
                return;
            }
        }
    }

    public void print() {
        return;
    }
}
