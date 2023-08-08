import java.util.Scanner;

public class Main {

    static final int MAX_KIEKIS = 100;
    static Scanner in = new Scanner(System.in);

    static Vartotojas[] vartotojai = new Vartotojas[MAX_KIEKIS];

    static int n = 0; // Ivestu vartotoju kiekis

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
        if (n == MAX_KIEKIS) {
            System.out.println("Daugiau vartotoju ivesti nebegalima!");
            return;
        }

        System.out.print("Iveskite id: ");
        int id = in.nextInt();

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
            System.out.println("Vartotojas sukurtas.");
            vartotojai[n] = new Vartotojas(id, vardas, slaptazodis, email, lytis);
            n++;
        }
    }

    private static void modifikuotiVartotoja() {
        System.out.println("Paskutinis ivestas vartotojas yra indeksu " + (n - 1));
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId = in.nextInt();
        if (keiciamasId < n) {
            System.out.print("Iveskite id: ");
            int id = in.nextInt();

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
                vart.setId(id);
                vart.setVardas(vardas);
                vart.setSlaptazodis(slaptazodis);
                vart.setEmail(email);
                vart.setLytis(lytis);
            }
        } else {
            System.out.println("indeksas " + keiciamasId + " yra netinkamas. Galimos ribos tarp 0 ir " + (n - 1));
        }
    }

    private static void spausdintiVartotojus() {
        Vartotojas vart;
        for (int i = 0; i < n; i++) {
            vart = vartotojai[i];
            System.out.printf("Id: %d | Vardas: %s | Slaptazodis: %s | Email: %s | Lytis: %s\n",
                    vart.getId(), vart.getVardas(), vart.getSlaptazodis(), vart.getEmail(), vart.getLytis());
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