import com.oocourse.library1.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentCounter {
    private HashMap<String, HashMap<LibraryBookIsbn, Book>> userBooks = new HashMap<>();
    private ArrayList<LibraryReqCmd> requests = new ArrayList<>();

    public void addRequest(LibraryReqCmd req) { requests.add(req); }

    public ArrayList<LibraryMoveInfo> moveFromShelf(BookShelf shelf, LocalDate date, boolean isOpen) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        for (LibraryReqCmd req : requests) {
            String userId = req.getStudentId();
            Book book = shelf.getBook(req.getBookIsbn());
            if (book != null) {
                LibraryBookId bookId = book.getBookId();
                shelf.removeBook(bookId);
                userBooks.putIfAbsent(userId, new HashMap<>());
                userBooks.get(userId).put(bookId.getBookIsbn(), book);
                book.setReservedDate(date, isOpen);
                book.setCurrentState(LibraryBookState.APPOINTMENT_OFFICE, date);
                info.add(new LibraryMoveInfo(bookId, "bs", "ao", userId));
            }
        }
        requests.clear();
        return info;
    }

    public Book getBook(String uid, LibraryBookIsbn isbn) {
        if (userBooks.containsKey(uid)) {
            return userBooks.get(uid).get(isbn);
        }
        return null;
    }

    public void removeBook(String uid, LibraryBookIsbn isbn) {
        if (userBooks.containsKey(uid)) {
            userBooks.get(uid).remove(isbn);
        }
    }

    public ArrayList<LibraryMoveInfo> move2Shelf(BookShelf shelf, LocalDate date) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        for (String userId : userBooks.keySet()) {
            HashMap<LibraryBookIsbn, Book> books = userBooks.get(userId);
            books.entrySet().removeIf(entry -> {
                Book book = entry.getValue();
                if (book.noLongerReserved(date)) {
                    info.add(new LibraryMoveInfo(book.getBookId(), "ao", "bs"));
                    shelf.addBook(book);
                    book.setCurrentState(LibraryBookState.BOOKSHELF, date);
                    book.setReservedDate(null, true);
                    return true;
                }
                return false;
            });
        }
        return info;
    }
}
