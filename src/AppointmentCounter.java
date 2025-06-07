import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.annotation.Trigger;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentCounter {
    private HashMap<String, HashMap<LibraryBookIsbn, Book>> userBooks = new HashMap<>();
    private ArrayList<LibraryReqCmd> requests = new ArrayList<>();

    @SendMessage(from = "AppointmentCounter", to = "Student")
    public void addRequest(LibraryReqCmd req) {
        requests.add(req);
    }

    @Trigger(from = "BookShelf", to = "AppointmentOffice")
    @Trigger(from = "HotBookShelf", to = "AppointmentOffice")
    @SendMessage(from = "AppointmentCounter", to = "BookShelf")
    @SendMessage(from = "AppointmentCounter", to = "HotBookShelf")
    @SendMessage(from = "AppointmentCounter", to = "Book")
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

    @SendMessage(from = "AppointmentCounter", to = "Book")
    public Book getBook(String uid, LibraryBookIsbn isbn) {
        if (userBooks.containsKey(uid)) {
            return userBooks.get(uid).get(isbn);
        }
        return null;
    }

    @SendMessage(from = "AppointmentCounter", to = "Student")
    public void removeBook(String uid, LibraryBookIsbn isbn) {
        if (userBooks.containsKey(uid)) {
            userBooks.get(uid).remove(isbn);
        }
    }

    @Trigger(from = "AppointmentOffice", to = "BookShelf")
    @SendMessage(from = "AppointmentCounter", to = "BookShelf")
    @SendMessage(from = "AppointmentCounter", to = "Student")
    @SendMessage(from = "AppointmentCounter", to = "Book")
    public ArrayList<LibraryMoveInfo> move2Shelf(BookShelf shelf, LocalDate date,
        HashMap<String, Student> students) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        for (String userId : userBooks.keySet()) {
            HashMap<LibraryBookIsbn, Book> books = userBooks.get(userId);
            books.entrySet().removeIf(entry -> {
                Book book = entry.getValue();
                if (book.noLongerReserved(date)) {
                    // 预约过期未取，扣15分
                    Student student = students.get(userId);
                    if (student != null) {
                        student.deductCreditScore(15);
                    }

                    info.add(new LibraryMoveInfo(book.getBookId(), "ao", "bs"));
                    shelf.addBook(book);
                    book.setCurrentState(LibraryBookState.BOOKSHELF, date);
                    book.setReservedDate(null, true);
                    student.setReservedNotfetch(false);
                    return true;
                }
                return false;
            });
        }
        return info;
    }

    // 处理过期预约的扣分
    @SendMessage(from = "AppointmentCounter", to = "Student")
    public void handleExpiredReservations(LocalDate date, HashMap<String, Student> students) {
        for (String userId : userBooks.keySet()) {
            HashMap<LibraryBookIsbn, Book> books = userBooks.get(userId);
            for (Book book : books.values()) {
                if (book.noLongerReserved(date)) {
                    // 预约过期未取，扣15分
                    Student student = students.get(userId);
                    if (student != null) {
                        student.deductCreditScore(15);
                    }
                }
            }
        }
    }
}