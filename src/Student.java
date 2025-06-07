import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.util.HashMap;

public class Student {
    private final String id;
    private HashMap<LibraryBookIsbn, Book> heldBooks = new HashMap<>();
    private boolean reservedNotfetch = false;

    public Student(String id) {
        this.id = id;
    }

    public void addBook(LibraryBookIsbn isbn, Book book) {
        heldBooks.put(isbn, book);
    }

    public void removeBook(LibraryBookIsbn isbn) {
        heldBooks.remove(isbn);
    }

    public Book getHeldBook(LibraryBookIsbn isbn) {
        return heldBooks.get(isbn);
    }

    public boolean canBorrowBook(Book book) {
        LibraryBookId bookId = book.getBookId();
        if (bookId.isTypeA()) {
            return false;
        } else if (bookId.isTypeB()) {
            return !hadBookB();
        } else if (bookId.isTypeC()) {
            return !hadBookC(book);
        }
        System.err.println("Book's type is wrong!");
        return false;
    }

    public boolean canReserve(Book book) {
        return canBorrowBook(book) && !reservedNotfetch;
    }

    public boolean hadBookB() {
        for (Book book : heldBooks.values()) {
            if (book.getBookId().isTypeB()) { return true; }
        }
        return false;
    }

    public boolean hadBookC(Book book) {
        LibraryBookIsbn bookIsbn = book.getBookId().getBookIsbn();
        return heldBooks.containsKey(bookIsbn);
    }

    public void setReservedNotfetch(boolean reservedNotfetch) {
        this.reservedNotfetch = reservedNotfetch;
    }
}
