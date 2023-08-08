import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    static final int MAX_KIEKIS = 100;

    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static Scanner in = new Scanner(System.in);

    static Vartotojas[] vartotojai = new Vartotojas[MAX_KIEKIS];

    public static void main(String[] args) {
        int pasirinkimas;

        menu:
        while (true) {
            System.out.print("""
                    1 - Ivesti vartotoja
                    2 - Pakeisti esama vartotoja
                    3 - Atspausdinti vartotojus
                    4 - Baigti programa
                    Jusu pasirinkimas: \s""");

            pasirinkimas = in.nextInt();
            switch (pasirinkimas) {
                case 1 -> ivestiVartotoja();
                case 2 -> modifikuotiVartotoja();
                case 3 -> spausdintiVartotojus();
                case 4 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }

        System.out.println("Programa baigia darba!");
        in.close();
    }

    private static void ivestiVartotoja() {
        if (Vartotojas.getKiekis() == MAX_KIEKIS) {
            System.out.println("Daugiau vartotoju ivesti nebegalima!");
            return;
        }

        System.out.print("Iveskite varda: ");
        String vardas = in.next();

        System.out.print("Iveskite slaptazodi: ");
        String slaptazodis = in.next();

        System.out.print("Iveskite slaptazodi(dar karta): ");
        String slaptazodis2 = in.next();

        System.out.print("Iveskite email: ");
        String email = in.next();

        System.out.print("Iveskite lyti: ");
        String lytisString = in.next();

        Lytis lytis = stringToLytis(lytisString);

        System.out.print("Iveskite gimimo data(yyyy-MM-dd): ");
        String gimimoDataString = in.next();
        LocalDate gimimoData = LocalDate.parse(gimimoDataString, DATE_FORMATTER);

        if (validation(vardas, slaptazodis, slaptazodis2, email)) {
            System.out.println("Vartotojas sukurtas.");
            vartotojai[Vartotojas.getKiekis()] = new Vartotojas(vardas, slaptazodis, email, lytis, gimimoData);
        }
    }

    private static void modifikuotiVartotoja() {
        System.out.println("Paskutinis ivestas vartotojas yra indeksu " + (Vartotojas.getKiekis() - 1));
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId = in.nextInt();
        if (keiciamasId < Vartotojas.getKiekis()) {

            System.out.print("Iveskite varda: ");
            String vardas = in.next();

            System.out.print("Iveskite slaptazodi: ");
            String slaptazodis = in.next();

            System.out.print("Iveskite slaptazodi(dar karta): ");
            String slaptazodis2 = in.next();

            System.out.print("Iveskite email: ");
            String email = in.next();

            System.out.print("Iveskite lyti: ");
            String lytisString = in.next();

            Lytis lytis = stringToLytis(lytisString);

            if (validation(vardas, slaptazodis, slaptazodis2, email)) {
                System.out.println("Vartotojas pakeistas.");
                Vartotojas vart = vartotojai[keiciamasId];
                vart.setVardas(vardas);
                vart.setSlaptazodis(slaptazodis);
                vart.setEmail(email);
                vart.setLytis(lytis);
            }
        } else {
            System.out.println("indeksas " + keiciamasId + " yra netinkamas. Galimos ribos tarp 0 ir " + (Vartotojas.getKiekis() - 1));
        }
    }

    private static void spausdintiVartotojus() {
        for (int i = 0; i < Vartotojas.getKiekis(); i++) {
            System.out.println(vartotojai[i]);
        }
    }

    private static boolean validation(String vardas, String slaptazodis, String slaptazodis2, String email) {
        if (vardas.length() < 3) {
            System.out.println("Vartotojo vardas per trumpas!");
            return false;
        }

        if (!slaptazodis.equals(slaptazodis2)) {
            System.out.println("Slaptazodziai nesutampa!");
            return false;
        }

        if (!email.contains("@")) {
            System.out.println("Neteisingas email formatas!");
            return false;
        }

        return true;
    }

    private static Lytis stringToLytis(String lytisString) {
        return switch (lytisString.toLowerCase()) {
            case "vyras" -> Lytis.VYRAS;
            case "moteris" -> Lytis.MOTERIS;
            default -> Lytis.NEZINOMA;
        };
    }
}