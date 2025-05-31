import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentCounter {
    private HashMap<String, HashMap<LibraryBookIsbn, Book>> userBooks = new HashMap<>();
    private ArrayList<LibraryReqCmd> requests = new ArrayList<>();

    public void addRequest(LibraryReqCmd req) {
        requests.add(req);
    }

    @Trigger(from = "BookShelf", to = "AppointmentOffice")
    @Trigger(from = "HotBookShelf", to = "AppointmentOffice")
    public ArrayList<LibraryMoveInfo> moveFromShelf(BookShelf shelf, HotBookShelf hotShelf,
        LocalDate date, boolean isOpen) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        for (LibraryReqCmd req : requests) {
            String userId = req.getStudentId();
            Book book = shelf.getBook(req.getBookIsbn());
            String fromLocation = "bs";

            // 如果普通书架没有，从热门书架找
            if (book == null) {
                book = hotShelf.getBook(req.getBookIsbn());
                fromLocation = "hbs";
            }

            if (book != null) {
                LibraryBookId bookId = book.getBookId();
                if (fromLocation.equals("bs")) {
                    shelf.removeBook(bookId);
                } else {
                    hotShelf.removeBook(bookId);
                }
                userBooks.putIfAbsent(userId, new HashMap<>());
                userBooks.get(userId).put(bookId.getBookIsbn(), book);
                book.setReservedDate(date, isOpen);
                book.setCurrentState(LibraryBookState.APPOINTMENT_OFFICE, date);
                info.add(new LibraryMoveInfo(bookId, fromLocation, "ao", userId));
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

    @Trigger(from = "AppointmentOffice", to = "BookShelf")
    public ArrayList<LibraryMoveInfo> move2Shelf(BookShelf shelf, LocalDate date,
        HashMap<String, Student> students) {
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
                    students.get(userId).setReservedNotfetch(false);
                    return true;
                }
                return false;
            });
        }
        return info;
    }
}
