import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryBookIsbn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

public class BorrowReturnCounter {
    private ArrayList<Book> returnedBooks = new ArrayList<>();

    public void returnBook(Book book, LocalDate date) {
        book.setCurrentState(LibraryBookState.BORROW_RETURN_OFFICE, date);
        returnedBooks.add(book);
    }

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
