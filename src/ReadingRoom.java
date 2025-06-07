import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.annotation.SendMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class ReadingRoom {
    private HashMap<String, ArrayList<Book>> userBooks = new HashMap<>();

    @SendMessage(from = "ReadingRoom", to = "Book")
    @SendMessage(from = "ReadingRoom", to = "Student")
    public void addBook(String userId, Book book) {
        userBooks.putIfAbsent(userId, new ArrayList<>());
        userBooks.get(userId).add(book);
    }

    public Book getBook(String userId, LibraryBookId bookId) {
        if (userBooks.containsKey(userId)) {
            for (Book book : userBooks.get(userId)) {
                if (book.getBookId().equals(bookId)) {
                    return book;
                }
            }
        }
        return null;
    }

    public void removeBook(String userId, LibraryBookId bookId) {
        if (userBooks.containsKey(userId)) {
            ArrayList<Book> books = userBooks.get(userId);
            books.removeIf(book -> book.getBookId().equals(bookId));
        }
    }

    public boolean hasReadingBook(String userId) {
        return userBooks.containsKey(userId) && !userBooks.get(userId).isEmpty();
    }

    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> allBooks = new ArrayList<>();
        for (ArrayList<Book> bookList : userBooks.values()) {
            allBooks.addAll(bookList);
        }
        return allBooks;
    }

    public void clear() {
        userBooks.clear();
    }

    public ArrayList<String> getAllReaders() {
        ArrayList<String> readers = new ArrayList<>();
        for (String userId : userBooks.keySet()) {
            if (!userBooks.get(userId).isEmpty()) {
                readers.add(userId);
            }
        }
        return readers;
    }
}