import java.io.Console;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static Scanner in = new Scanner(System.in);
    static HashMap<Integer, Vartotojas> vartotojai = new HashMap<>();
    static Console console = System.console();

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

        String slaptazodis;
        String slaptazodis2;

        if (console != null) {
            slaptazodis = new String(console.readPassword());
        } else {
            slaptazodis = in.next();
        }

        System.out.print("Iveskite slaptazodi(dar karta): ");

        if (console != null) {
            slaptazodis2 = new String(console.readPassword());
        } else {
            slaptazodis2 = in.next();
        }

        System.out.print("Iveskite email: ");
        String email = in.next();

        System.out.print("Iveskite lyti: ");
        String lytisString = in.next();

        Lytis lytis = stringToLytis(lytisString);

        System.out.print("Iveskite gimimo data(yyyy-MM-dd): ");
        String gimimoDataString = in.next();
        LocalDate gimimoData = LocalDate.parse(gimimoDataString, DATE_FORMATTER);

        if (isNameValid(vardas) && isPassValid(slaptazodis, slaptazodis2) && isEmailValid(email)) {
            vartotojai.put(Vartotojas.getIdCounter(), new Vartotojas(vardas, slaptazodis, email, lytis, gimimoData));
            System.out.println("Vartotojas sukurtas.");
        }
    }

    private static void modifikuotiVartotoja() {
        System.out.println("Paskutinis ivestas vartotojas yra indeksu " + (vartotojai.size() - 1));
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId = in.nextInt();
        if (vartotojai.containsKey(keiciamasId)) {
            System.out.print("""
                    1 - vardas
                    2 - slaptazodis
                    3 - email
                    4 - lytis
                    Kuri lauka norite keisti:\s""");

            int pasirinkimas = in.nextInt();
            Vartotojas vart = vartotojai.get(keiciamasId);

            switch (pasirinkimas) {
                case 1 -> {
                    System.out.print("Iveskite varda: ");
                    String vardas = in.next();
                    if (isNameValid(vardas))
                        vart.setVardas(vardas);
                }
                case 2 -> {
                    String slaptazodis;
                    String slaptazodis2;

                    System.out.print("Iveskite slaptazodi: ");

                    if (console != null) {
                        slaptazodis = new String(console.readPassword());
                    } else {
                        slaptazodis = in.next();
                    }

                    System.out.print("Iveskite slaptazodi(dar karta): ");

                    if (console != null) {
                        slaptazodis2 = new String(console.readPassword());
                    } else {
                        slaptazodis2 = in.next();
                    }

                    if (isPassValid(slaptazodis, slaptazodis2))
                        vart.setSlaptazodis(slaptazodis);
                }
                case 3 -> {
                    System.out.print("Iveskite email: ");
                    String email = in.next();
                    if (isEmailValid(email))
                        vart.setEmail(email);
                }
                case 4 -> {
                    System.out.print("Iveskite lyti: ");
                    String lytisString = in.next();
                    Lytis lytis = stringToLytis(lytisString);
                    vart.setLytis(lytis);
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }

            System.out.println("Vartotojas pakoreguotas.");
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

    private static boolean isNameValid(String vardas) {
        if (vardas.length() < 3) {
            System.out.println("Vartotojo vardas per trumpas!");
            return false;
        }

        return true;
    }

    private static boolean isPassValid(String slaptazodis, String slaptazodis2) {

        if (!slaptazodis.equals(slaptazodis2)) {
            System.out.println("Slaptazodziai nesutampa!");
            return false;
        }

        return true;
    }

    private static boolean isEmailValid(String email) {

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