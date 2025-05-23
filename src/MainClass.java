public class MainClass {
    public static void main(String[] args) {
        BookShelf bookShelf = new BookShelf();
        AppointmentCounter appointmnetCounter = new AppointmentCounter();
        BorrowReturnCounter borrowCounter = new BorrowReturnCounter();
        Library library = new Library(bookShelf, appointmnetCounter, borrowCounter);
        library.run();
    }
}
