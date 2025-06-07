import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.annotation.Trigger;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class BorrowReturnCounter {
    private ArrayList<Book> returnedBooks = new ArrayList<>();

    @SendMessage(from = "BorrowReturnCounter", to = "Book")
    public void returnBook(Book book, LocalDate date) {
        book.setCurrentState(LibraryBookState.BORROW_RETURN_OFFICE, date);
        returnedBooks.add(book);
    }

    @Trigger(from = "BorrowReturnOffice", to = "BookShelf")
    @Trigger(from = "BorrowReturnOffice", to = "HotBookShelf")
    public ArrayList<LibraryMoveInfo> move2Shelf(BookShelf shelf, HotBookShelf hotShelf,
            LocalDate date, HashSet<LibraryBookIsbn> hotBooks) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        for (Book book : returnedBooks) {
            if (hotBooks.contains(book.getIsbn())) {
                // 热门书籍移动到热门书架
                info.add(new LibraryMoveInfo(book.getBookId(), "bro", "hbs"));
                book.setCurrentState(LibraryBookState.HOT_BOOKSHELF, date);
                hotShelf.addBook(book);
            } else {
                // 非热门书籍移动到普通书架
                info.add(new LibraryMoveInfo(book.getBookId(), "bro", "bs"));
                book.setCurrentState(LibraryBookState.BOOKSHELF, date);
                shelf.addBook(book);
            }
        }
        returnedBooks.clear();
        return info;
    }

    @Trigger(from = "ReadingRoom", to = "BookShelf")
    @Trigger(from = "ReadingRoom", to = "HotBookShelf")
    public ArrayList<LibraryMoveInfo> moveReadingRoom2Shelf(ReadingRoom readingRoom,
            BookShelf shelf, HotBookShelf hotShelf, LocalDate date, HashSet<LibraryBookIsbn> hotBooks) {
        ArrayList<LibraryMoveInfo> info = new ArrayList<>();
        ArrayList<Book> allReadingBooks = readingRoom.getAllBooks();
        for (Book book : allReadingBooks) {
            if (hotBooks.contains(book.getIsbn())) {
                // 热门书籍移动到热门书架
                info.add(new LibraryMoveInfo(book.getBookId(), "rr", "hbs"));
                book.setCurrentState(LibraryBookState.HOT_BOOKSHELF, date);
                hotShelf.addBook(book);
            } else {
                // 非热门书籍移动到普通书架
                info.add(new LibraryMoveInfo(book.getBookId(), "rr", "bs"));
                book.setCurrentState(LibraryBookState.BOOKSHELF, date);
                shelf.addBook(book);
            }
        }
        readingRoom.clear();
        return info;
    }
}
