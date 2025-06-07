import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class Student {
    private final String id;
    private HashMap<LibraryBookIsbn, Book> heldBooks = new HashMap<>();
    private HashMap<LibraryBookIsbn, LocalDate> borrowDates = new HashMap<>(); // 记录借阅日期
    private HashMap<LibraryBookIsbn, LocalDate> lastOverdueCheckDate = new HashMap<>();// 记录上次逾期检查日期
    private HashMap<LibraryBookIsbn, Integer> deductedDays = new HashMap<>(); // 记录已扣分的逾期天数
    private boolean reservedNotfetch = false;
    private int creditScore = 100; // 初始信用分100

    public Student(String id) {
        this.id = id;
    }

    public void addBook(LibraryBookIsbn isbn, Book book) {
        heldBooks.put(isbn, book);
        borrowDates.put(isbn, LocalDate.now()); // 记录借阅日期
    }

    public void addBook(LibraryBookIsbn isbn, Book book, LocalDate borrowDate) {
        heldBooks.put(isbn, book);
        borrowDates.put(isbn, borrowDate); // 记录指定的借阅日期
    }

    public void removeBook(LibraryBookIsbn isbn) {
        heldBooks.remove(isbn);
        borrowDates.remove(isbn);
        lastOverdueCheckDate.remove(isbn);
        deductedDays.remove(isbn);
    }

    public Book getHeldBook(LibraryBookIsbn isbn) {
        return heldBooks.get(isbn);
    }

    public LocalDate getBorrowDate(LibraryBookIsbn isbn) {
        return borrowDates.get(isbn);
    }

    // 获取所有借阅日期的映射
    public HashMap<LibraryBookIsbn, LocalDate> getBorrowDates() {
        return borrowDates;
    }

    // 检查是否逾期
    public boolean isOverdue(LibraryBookIsbn isbn, LocalDate currentDate) {
        LocalDate borrowDate = borrowDates.get(isbn);
        if (borrowDate == null) {
            return false;
        }

        Book book = heldBooks.get(isbn);
        if (book == null) {
            return false;
        }

        int borrowPeriod = getBorrowPeriod(book);
        if (borrowPeriod == 0) {
            return false; // A类书不可借阅
        }

        LocalDate dueDate = borrowDate.plusDays(borrowPeriod);
        // 在还书期限最后一天（dueDate）还书不算逾期，超过这一天才算逾期
        return currentDate.isAfter(dueDate);
    }

    // 获取借阅期限
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

    public boolean canBorrowBook(Book book) {
        LibraryBookId bookId = book.getBookId();

        // 检查信用分权限
        if (creditScore < 60) {
            return false; // 信用分低于60不能借阅
        }

        if (bookId.isTypeA()) {
            return false;
        } else if (bookId.isTypeB()) {
            return !hadBookB();
        } else if (bookId.isTypeC()) {
            return !hadBookC(book);
        }
        System.err.println("Book's type is wrong!");
        return false;
    }

    public boolean canReserve(Book book) {
        // 检查信用分权限
        if (creditScore < 100) {
            return false; // 信用分低于100不能预约
        }

        return canBorrowBook(book) && !reservedNotfetch;
    }

    public boolean canRead(Book book) {
        LibraryBookId bookId = book.getBookId();

        if (creditScore <= 0) {
            return false; // 信用分为0不能阅读
        }

        if (bookId.isTypeA()) {
            return creditScore >= 40; // A类书需要信用分>=40
        } else {
            return true; // B/C类书只要信用分>0就可以阅读
        }
    }

    public boolean hadBookB() {
        for (Book book : heldBooks.values()) {
            if (book.getBookId().isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean hadBookC(Book book) {
        LibraryBookIsbn bookIsbn = book.getBookId().getBookIsbn();
        return heldBooks.containsKey(bookIsbn);
    }

    public void setReservedNotfetch(boolean reservedNotfetch) {
        this.reservedNotfetch = reservedNotfetch;
    }

    // 信用分管理
    public int getCreditScore() {
        return creditScore;
    }

    public void addCreditScore(int points) {
        creditScore = Math.min(creditScore + points, 180);
    }

    public void deductCreditScore(int points) {
        creditScore = Math.max(creditScore - points, 0);
    }

    // 处理逾期扣分，返回是否有扣分
    public boolean handleOverdueDeduction(LocalDate currentDate) {
        boolean deductionOccurred = false;
        for (LibraryBookIsbn isbn : new HashMap<>(borrowDates).keySet()) {
            LocalDate borrowDate = borrowDates.get(isbn);
            Book book = heldBooks.get(isbn);
            if (book != null && borrowDate != null) {
                int borrowPeriod = getBorrowPeriod(book);
                if (borrowPeriod > 0) {
                    LocalDate dueDate = borrowDate.plusDays(borrowPeriod);

                    // 如果当前日期已经逾期
                    if (currentDate.isAfter(dueDate)) {
                        // 计算总逾期天数
                        long totalOverdueDays = ChronoUnit.DAYS.between(dueDate, currentDate);

                        // 获取已扣分的天数
                        int alreadyDeductedDays = deductedDays.getOrDefault(isbn, 0);

                        // 计算需要新扣分的天数
                        long newDeductDays = totalOverdueDays - alreadyDeductedDays;

                        if (newDeductDays > 0) {
                            deductCreditScore((int) (newDeductDays * 5));
                            deductionOccurred = true;
                            deductedDays.put(isbn, (int) totalOverdueDays);
                        }
                    }
                }
            }
        }
        return deductionOccurred;
    }
}
