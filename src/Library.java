import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.annotation.Trigger;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static com.oocourse.library3.LibraryIO.PRINTER;
import static com.oocourse.library3.LibraryIO.SCANNER;

public class Library {
    private BookShelf bookShelf;
    private HotBookShelf hotBookShelf;
    private ReadingRoom readingRoom;
    private AppointmentCounter appointmentCounter;
    private BorrowReturnCounter borrowCounter;
    private LocalDate date;
    private LocalDate lastOpenDate = null;
    private HashMap<String, Student> students = new HashMap<>();
    private HashMap<LibraryBookId, Book> allBooks = new HashMap<>();
    private HashMap<LibraryBookIsbn, ArrayList<Book>> isbnBooks = new HashMap<>();
    private HashSet<LibraryBookIsbn> lastHotBooks = new HashSet<>();

    public Library(BookShelf bookShelf, HotBookShelf hotBookShelf, ReadingRoom readingRoom,
        AppointmentCounter appointmentCounter, BorrowReturnCounter borrowCounter) {
        this.bookShelf = bookShelf;
        this.hotBookShelf = hotBookShelf;
        this.readingRoom = readingRoom;
        this.appointmentCounter = appointmentCounter;
        this.borrowCounter = borrowCounter;
    }

    @Trigger(from = "InitState", to = "BookShelf")
    @SendMessage(from = "Library", to = "BookShelf")
    @SendMessage(from = "Library", to = "Student")
    public void run() {
        // 转换isbn->id
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
            } else if (command instanceof LibraryQcsCmd) {
                // 处理查询信用分命令
                LibraryQcsCmd qcsCmd = (LibraryQcsCmd) command;
                String userId = qcsCmd.getStudentId();
                students.putIfAbsent(userId, new Student(userId));
                Student student = students.get(userId);
                if (student != null) {
                    student.handleOverdueDeduction(date); // 在查询前先处理逾期扣分
                    PRINTER.info(qcsCmd, student.getCreditScore());
                }
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                students.putIfAbsent(req.getStudentId(), new Student(req.getStudentId()));

                LibraryReqCmd.Type type = req.getType(); // 指令对应的类型（查询/阅读/借阅/预约/还书/取书/归还）
                if (type.equals(LibraryReqCmd.Type.BORROWED)) {
                    dealBorrow(req);
                } else if (type.equals(LibraryReqCmd.Type.ORDERED)) {
                    orderNewBook(req);
                } else if (type.equals(LibraryReqCmd.Type.PICKED)) {
                    getOrderedBook(req);
                } else if (type.equals(LibraryReqCmd.Type.QUERIED)) {
                    dealquery(req);
                } else if (type.equals(LibraryReqCmd.Type.RETURNED)) {
                    dealReturn(req);
                } else if (type.equals(LibraryReqCmd.Type.READ)) {
                    dealRead(req);
                } else if (type.equals(LibraryReqCmd.Type.RESTORED)) {
                    dealRestore(req);
                } else {
                    System.out.println("Unknown command type: " + type);
                }
            }
        }
    }

    @SendMessage(from = "Library", to = "BorrowReturnCounter")
    @SendMessage(from = "Library", to = "ReadingRoom")
    @SendMessage(from = "Library", to = "AppointmentCounter")
    @SendMessage(from = "Library", to = "BookShelf")
    @SendMessage(from = "Library", to = "HotBookShelf")
    public void open(LibraryCommand req) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();

        // 每次开馆时，上次记录的热门书籍用于本次整理，然后清空准备记录新的热门书籍
        HashSet<LibraryBookIsbn> currentHotBooks = new HashSet<>(lastHotBooks);
        lastHotBooks.clear(); // 清空，准备记录本次开馆的热门书籍

        // 清理借还处
        infos.addAll(borrowCounter.move2Shelf(bookShelf, hotBookShelf, date, currentHotBooks));

        // 清理阅览室
        infos.addAll(borrowCounter.moveReadingRoom2Shelf(readingRoom,
            bookShelf, hotBookShelf, date, currentHotBooks));

        // 整理热门书架
        infos.addAll(arrangeHotBooks(currentHotBooks));

        // 清理预约处
        infos.addAll(appointmentCounter.move2Shelf(bookShelf, date, students));
        // 整理热门书架
        infos.addAll(arrangeHotBooks(currentHotBooks));
        // 处理预约
        infos.addAll(appointmentCounter.moveFromShelf(bookShelf, hotBookShelf, date, true));

        PRINTER.move(date, infos);
        lastOpenDate = date;
    }

    @SendMessage(from = "Library", to = "ReadingRoom")
    @SendMessage(from = "Library", to = "AppointmentCounter")
    @SendMessage(from = "Library", to = "Student")
    public void close(LibraryCommand req) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();

        // 处理阅读不归还扣分
        handleReadingNotReturnedPenalty();

        // 清理阅览室，将书籍移动到书架
        infos.addAll(borrowCounter.moveReadingRoom2Shelf(readingRoom,
            bookShelf, hotBookShelf, date, new HashSet<>()));

        // 处理预约不取扣分
        handleReservationNotPickedPenalty();

        infos.addAll(appointmentCounter.moveFromShelf(bookShelf, hotBookShelf, date, false));
        PRINTER.move(date, infos);
    }

    // 处理阅读不归还扣分
    private void handleReadingNotReturnedPenalty() {
        for (String userId : readingRoom.getAllReaders()) {
            Student student = students.get(userId);
            if (student != null) {
                student.deductCreditScore(10); // 阅读不归还扣10分
            }
        }
    }

    // 处理预约不取扣分
    private void handleReservationNotPickedPenalty() {
        // 这个逻辑需要在AppointmentCounter中实现
        appointmentCounter.handleExpiredReservations(date, students);
    }

    private int getBorrowPeriod(Book book) {
        LibraryBookId bookId = book.getBookId();
        if (bookId.isTypeA()) {
            return 0; // A类书不可借阅
        } else if (bookId.isTypeB()) {
            return 30; // B类书30天
        } else if (bookId.isTypeC()) {
            return 60; // C类书60天
        }
        return 0;
    }

    @Trigger(from = "BookShelf", to = "HotBookShelf")
    @Trigger(from = "HotBookShelf", to = "BookShelf")
    private ArrayList<LibraryMoveInfo> arrangeHotBooks(HashSet<LibraryBookIsbn> currentHotBooks) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();

        // 获取当前普通书架和热门书架上的所有书籍
        ArrayList<Book> normalShelfBooks = new ArrayList<>(bookShelf.getAllBooks());
        bookShelf.clear();
        ArrayList<Book> hotShelfBooks = new ArrayList<>(hotBookShelf.getAllBooks());
        hotBookShelf.clear();

        // 重新分配书籍
        for (Book book : normalShelfBooks) {
            if (currentHotBooks.contains(book.getIsbn())) {
                // 热门书籍移动到热门书架
                infos.add(new LibraryMoveInfo(book.getBookId(), "bs", "hbs"));
                book.setCurrentState(LibraryBookState.HOT_BOOKSHELF, date);
                hotBookShelf.addBook(book);
            } else {
                // 保留在普通书架
                bookShelf.addBook(book);
            }
        }

        for (Book book : hotShelfBooks) {
            if (currentHotBooks.contains(book.getIsbn())) {
                // 保留在热门书架
                hotBookShelf.addBook(book);
            } else {
                // 非热门书籍移动到普通书架
                infos.add(new LibraryMoveInfo(book.getBookId(), "hbs", "bs"));
                book.setCurrentState(LibraryBookState.BOOKSHELF, date);
                bookShelf.addBook(book);
            }
        }

        return infos;
    }

    @Trigger(from = "HotBookShelf", to = "User")
    @Trigger(from = "BookShelf", to = "User")
    @SendMessage(from = "Library", to = "BookShelf")
    @SendMessage(from = "Library", to = "HotBookShelf")
    @SendMessage(from = "Library", to = "Student")
    public void dealBorrow(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);

        // 先处理逾期扣分，确保信用分是最新的
        student.handleOverdueDeduction(date);

        // 先从普通书架找，再从热门书架找
        Book book = bookShelf.getBook(isbn);
        String fromLocation = "bs";
        if (book == null) {
            book = hotBookShelf.getBook(isbn);
            fromLocation = "hbs";
        }

        if (book != null && student.canBorrowBook(book)) {
            LibraryBookId bookId = book.getBookId();
            if (fromLocation.equals("bs")) {
                bookShelf.removeBook(bookId);
            } else {
                hotBookShelf.removeBook(bookId);
            }
            book.setCurrentState(LibraryBookState.USER, date);
            student.addBook(bookId.getBookIsbn(), book, date); // 传入借阅日期
            lastHotBooks.add(isbn); // 标记为热门
            PRINTER.accept(req, bookId);
        } else {
            PRINTER.reject(req);
        }
    }

    @SendMessage(from = "Library", to = "Student")
    @SendMessage(from = "Library", to = "BookShelf")
    @SendMessage(from = "Library", to = "HotBookShelf")
    @SendMessage(from = "Library", to = "AppointmentCounter")
    public void orderNewBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);

        // 先处理逾期扣分，确保信用分是最新的
        student.handleOverdueDeduction(date);

        Book book;
        if (bookShelf.containsBook(isbn) || hotBookShelf.containsBook(isbn)) {
            book = bookShelf.getBook(isbn);
            if (book == null) {
                book = hotBookShelf.getBook(isbn);
            }
        } else {
            int index = isbnBooks.get(isbn).size();
            book = isbnBooks.get(isbn).get(new Random().nextInt(index));
        }
        if (student.canReserve(book)) {
            appointmentCounter.addRequest(req);
            student.setReservedNotfetch(true);
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }

    @SendMessage(from = "Library", to = "AppointmentCounter")
    @SendMessage(from = "Library", to = "Student")
    public void getOrderedBook(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);
        Book book = appointmentCounter.getBook(userId, isbn);
        // if (book.noLongerReserved(date)) { System.out.println("Book was no longer
        // reserved"); }
        if (book != null && student.canBorrowBook(book)) {
            book.setCurrentState(LibraryBookState.USER, date);
            student.setReservedNotfetch(false);
            student.addBook(isbn, book, date); // 传入取书日期作为借阅日期
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

    @Trigger(from = "User", to = "BorrowReturnOffice")
    @SendMessage(from = "Library", to = "Student")
    @SendMessage(from = "Library", to = "BorrowReturnCounter")
    public void dealReturn(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        Student student = students.get(userId);
        LibraryBookIsbn isbn = bookId.getBookIsbn();
        Book book = student.getHeldBook(isbn);
        if (book != null) {
            // 先处理逾期扣分
            student.handleOverdueDeduction(date);

            boolean isOverdue = student.isOverdue(isbn, date);

            if (!isOverdue) {
                // 按时还书，加10分
                student.addCreditScore(10);
            }

            student.removeBook(isbn);
            borrowCounter.returnBook(book, date);
            PRINTER.accept(req, isOverdue ? "overdue" : "not overdue");

        } else {
            PRINTER.reject(req);
        }
    }

    @Trigger(from = "HotBookShelf", to = "ReadingRoom")
    @Trigger(from = "BookShelf", to = "ReadingRoom")
    @SendMessage(from = "Library", to = "Student")
    @SendMessage(from = "Library", to = "BookShelf")
    @SendMessage(from = "Library", to = "HotBookShelf")
    @SendMessage(from = "Library", to = "ReadingRoom")
    public void dealRead(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookIsbn isbn = req.getBookIsbn();
        Student student = students.get(userId);

        // 先处理逾期扣分，确保信用分是最新的
        student.handleOverdueDeduction(date);

        // 检查用户是否已有阅读中的书籍
        if (readingRoom.hasReadingBook(userId)) {
            PRINTER.reject(req);
            return;
        }

        // 先从普通书架找，再从热门书架找
        Book book = bookShelf.getBook(isbn);
        String fromLocation = "bs";
        if (book == null) {
            book = hotBookShelf.getBook(isbn);
            fromLocation = "hbs";
        }

        if (book != null && student.canRead(book)) {
            LibraryBookId bookId = book.getBookId();
            if (fromLocation.equals("bs")) {
                bookShelf.removeBook(bookId);
            } else {
                hotBookShelf.removeBook(bookId);
            }
            book.setCurrentState(LibraryBookState.READING_ROOM, date);
            readingRoom.addBook(userId, book);
            lastHotBooks.add(isbn); // 标记为热门
            PRINTER.accept(req, bookId);
        } else {
            PRINTER.reject(req);
        }
    }

    @Trigger(from = "ReadingRoom", to = "BorrowReturnOffice")
    @SendMessage(from = "Library", to = "ReadingRoom")
    @SendMessage(from = "Library", to = "Student")
    @SendMessage(from = "Library", to = "BorrowReturnCounter")
    public void dealRestore(LibraryReqCmd req) {
        String userId = req.getStudentId();
        LibraryBookId bookId = req.getBookId();
        Book book = readingRoom.getBook(userId, bookId);

        if (book != null) {
            readingRoom.removeBook(userId, bookId);
            borrowCounter.returnBook(book, date);

            // 阅读后主动归还，加10分
            Student student = students.get(userId);
            if (student != null) {
                student.addCreditScore(10);
            }
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }
}
