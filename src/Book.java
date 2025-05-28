import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryTrace;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Book {
    private final LibraryBookId bookId;
    private LocalDate reservedDate = null; // null表示未被预约
    private LibraryBookState currentState = LibraryBookState.BOOKSHELF; // 默认在书架上
    private ArrayList<LibraryTrace> history = new ArrayList<>(); // 历史轨迹

    public Book(LibraryBookId bookId) {
        this.bookId = bookId;
    }

    public LibraryBookId getBookId() { return bookId; }

    public LibraryBookIsbn getIsbn() { return bookId.getBookIsbn(); }

    public LibraryBookIsbn.Type getBookIsbnType() { return bookId.getType(); }

    public void setReservedDate(LocalDate reservedDate, boolean isOpen) {
        if (isOpen) {
            this.reservedDate = reservedDate;
        } else {
            this.reservedDate = reservedDate.plusDays(1);
        }
    }

    public boolean noLongerReserved(LocalDate date) {
        long days = ChronoUnit.DAYS.between(reservedDate, date);
        //System.out.println("today: " + date + " reserve: " + reservedDate + " days: " + days);
        return days >= 5;
    }

    public LibraryBookState getCurrentState() { return currentState; }

    public void setCurrentState(LibraryBookState state, LocalDate date) {
        if (currentState != state) {
            history.add(new LibraryTrace(date, currentState, state));
            currentState = state;
        }
    }

    public ArrayList<LibraryTrace> getHistory() { return history; }
}
