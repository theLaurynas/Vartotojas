import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static Scanner in = new Scanner(System.in);

    static HashMap<Integer, Vartotojas> vartotojai = new HashMap<>();

    public static void main(String[] args) {
        int pasirinkimas;

        menu:
        while (true) {
            System.out.print("""
                    1 - Ivesti vartotoja
                    2 - Pakeisti esama vartotoja
                    3 - Trinti vartotoja
                    4 - Atspausdinti vartotojus
                    5 - Baigti programa
                    Jusu pasirinkimas:\s""");

            pasirinkimas = in.nextInt();
            switch (pasirinkimas) {
                case 1 -> ivestiVartotoja();
                case 2 -> modifikuotiVartotoja();
                case 3 -> trintiVartotoja();
                case 4 -> spausdintiVartotojus();
                case 5 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }

        System.out.println("Programa baigia darba!");
        in.close();
    }

    private static void ivestiVartotoja() {

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
            vartotojai.put(Vartotojas.getIdCounter(), new Vartotojas(vardas, slaptazodis, email, lytis, gimimoData));
            System.out.println("Vartotojas sukurtas.");
        }
    }

    private static void modifikuotiVartotoja() {
        System.out.println("Paskutinis ivestas vartotojas yra indeksu " + (vartotojai.size() - 1));
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId = in.nextInt();
        if (vartotojai.containsKey(keiciamasId)) {

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
                Vartotojas vart = vartotojai.get(keiciamasId);
                vart.setVardas(vardas);
                vart.setSlaptazodis(slaptazodis);
                vart.setEmail(email);
                vart.setLytis(lytis);
                System.out.println("Vartotojas pakoreguotas.");
            }
        } else {
            System.out.println("indeksas " + keiciamasId + " nerastas");
        }
    }

    private static void trintiVartotoja() {
        System.out.println("Paskutinis ivestas vartotojas yra indeksu " + (vartotojai.size() - 1));
        System.out.print("Kuri vartotoja norite istrinti: ");
        int trinamasId = in.nextInt();
        if (vartotojai.containsKey(trinamasId)) {
            vartotojai.remove(trinamasId);
            System.out.println("Vartotojas istrintas.");
        } else {
            System.out.println("indeksas " + trinamasId + " nerastas");
        }
    }

    private static void spausdintiVartotojus() {
        for (var v : vartotojai.values()) {
            System.out.println(v);
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