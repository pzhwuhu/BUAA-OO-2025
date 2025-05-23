import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryCloseCmd;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryReqCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.oocourse.library1.LibraryIO.PRINTER;
import static com.oocourse.library1.LibraryIO.SCANNER;

public class Library {
    private BookShelf bookShelf;
    private AppointmentCounter appointmentCounter;
    private BorrowReturnCounter borrowCounter;
    private LocalDate date;
    private HashMap<String, Student> students = new HashMap<>();
    private HashMap<LibraryBookId, Book> allBooks = new HashMap<>();
    private HashMap<LibraryBookIsbn, ArrayList<Book>> isbnBooks = new HashMap<>();

    public Library(BookShelf bookShelf, AppointmentCounter appointmentCounter,
        BorrowReturnCounter borrowCounter) {
        this.bookShelf = bookShelf;
        this.appointmentCounter = appointmentCounter;
        this.borrowCounter = borrowCounter;
    }

    public void run() {
        //转换isbn->id
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        for (Map.Entry<LibraryBookIsbn, Integer> entry : bookList.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            int count = entry.getValue();
            ArrayList<Book> books = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                LibraryBookId bookId = new LibraryBookId(isbn.getType(),
                    isbn.getUid(), String.format("%02d", i));
                Book book = new Book(bookId);
                bookShelf.addBook(book);
                allBooks.put(bookId, book);
                books.add(book);
            }
            isbnBooks.put(isbn, books);
        }
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            date = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                open(command);
            } else if (command instanceof LibraryCloseCmd) {
                close(command);
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                students.putIfAbsent(req.getStudentId(), new Student(req.getStudentId()));
                LibraryReqCmd.Type type = req.getType(); // 指令对应的类型（查询/阅读/借阅/预约/还书/取书/归还）
                if (type.equals(LibraryReqCmd.Type.BORROWED)) { dealBorrow(req); }
                else if (type.equals(LibraryReqCmd.Type.ORDERED)) { dealOrder(req); }
                else if (type.equals(LibraryReqCmd.Type.PICKED)) { dealPick(req); }
                else if (type.equals(LibraryReqCmd.Type.QUERIED)) { dealquery(req); }
                else if (type.equals(LibraryReqCmd.Type.RETURNED)) { dealReturn(req); }
                else { System.out.println("Unknown command type: " + type); }
            }
        }
    }

    public void open(LibraryCommand req) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        infos.addAll(appointmentCounter.move2Shelf(bookShelf, date, students));
        infos.addAll(appointmentCounter.moveFromShelf(bookShelf, date, true));
        PRINTER.move(date, infos);
    }

    public void close(LibraryCommand req) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        infos.addAll(appointmentCounter.moveFromShelf(bookShelf, date, false));
        infos.addAll(borrowCounter.move2Shelf(bookShelf, date));
        PRINTER.move(date, infos);
    }

    public void dealBorrow(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);
        Book book = bookShelf.getBook(isbn);
        //bookShelf.print();
        //if (book == null) { System.out.println("Book not found and id is " + bookId); }
        if (book != null && student.canBorrowBook(book)) {
            LibraryBookId bookId = book.getBookId();
            bookShelf.removeBook(bookId);
            book.setCurrentState(LibraryBookState.USER, date);
            student.addBook(bookId.getBookIsbn(), book);
            PRINTER.accept(req, bookId);
        } else {
            PRINTER.reject(req);
        }
    }

    public void dealOrder(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);
        Book book;
        if (bookShelf.containsBook(isbn)) {
            book = bookShelf.getBook(isbn);
        } else {
            int index = isbnBooks.get(isbn).size();
            book = isbnBooks.get(isbn).get(new Random().nextInt(index));
        }
        if (student.canReserve(book)) {
            appointmentCounter.addRequest(req);
            student.setReservedNotfetch(true);
            PRINTER.accept(req);
        } else { PRINTER.reject(req); }
    }

    public void dealPick(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);
        Book book = appointmentCounter.getBook(userId, isbn);
        //if (book.noLongerReserved(date)) { System.out.println("Book was no longer reserved"); }
        if (book != null && student.canBorrowBook(book)) {
            book.setCurrentState(LibraryBookState.USER, date);
            student.setReservedNotfetch(false);
            student.addBook(isbn, book);
            appointmentCounter.removeBook(userId, isbn);
            PRINTER.accept(req, book.getBookId());
        } else {
            PRINTER.reject(req);
        }
    }

    public void dealquery(LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        Book book = allBooks.get(bookId);
        if (book != null) {
            PRINTER.info(date, bookId, book.getHistory());
        }
    }

    public void dealReturn(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        Student student = students.get(userId);
        LibraryBookIsbn isbn = bookId.getBookIsbn();
        Book book = student.getHeldBook(isbn);
        if (book != null) {
            student.removeBook(isbn);
            borrowCounter.returnBook(book, date);
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }
}
