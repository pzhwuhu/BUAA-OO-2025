public class MainClass {
    public static void main(String[] args) {
        BookShelf bookShelf = new BookShelf();
        HotBookShelf hotBookShelf = new HotBookShelf();
        ReadingRoom readingRoom = new ReadingRoom();
        AppointmentCounter appointmentCounter = new AppointmentCounter();
        BorrowReturnCounter borrowCounter = new BorrowReturnCounter();
        Library library = new Library(bookShelf, hotBookShelf, readingRoom,
                appointmentCounter, borrowCounter);
        library.run();
    }
}
