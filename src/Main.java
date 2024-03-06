import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Dictionary dictionary;

        try {
            dictionary = new Dictionary();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }

        while(true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("SEARCH");
            System.out.println("\nTITLE: ");
            String title = scanner.nextLine();
            System.out.println("\nAUTHOR: ");
            String author = scanner.nextLine();
            System.out.println("\nBODY: ");
            String body = scanner.nextLine();
            dictionary.search(title, author, body);
        }
    }
}