import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;

import java.time.LocalDate;
import java.util.ArrayList;

public class BorrowReturnCounter {
    private ArrayList<Book> returnedBooks = new ArrayList<>();

    public void returnBook(Book book, LocalDate date) {
        book.setCurrentState(LibraryBookState.BORROW_RETURN_OFFICE, date);
        returnedBooks.add(book); }

    public ArrayList<LibraryMoveInfo> move2Shelf(BookShelf shelf, LocalDate date) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        for (Book book : returnedBooks) {
            info.add(new LibraryMoveInfo(book.getBookId(), "bro", "bs"));
            book.setCurrentState(LibraryBookState.BOOKSHELF, date);
            shelf.addBook(book);
        }
        returnedBooks.clear();
        return info;
    }
}
