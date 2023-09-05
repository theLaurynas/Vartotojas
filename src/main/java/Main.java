import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static Scanner in = new Scanner(System.in);

    static TreeMap<Integer, Vartotojas> vartotojai = new TreeMap<>();

    public static void main(String[] args) {
        int pasirinkimas;

        uzkrautiVartotojus();

        menu:
        while (true) {
            System.out.print("""
                                        
                    ┌──────────────────────────────────┐
                    │               MENIU              │
                    ├──────────────────────────────────┤
                    │ 1 - Ivesti vartotoja             │
                    │ 2 - Pakeisti esama vartotoja     │
                    │ 3 - Trinti vartotoja             │
                    │ 4 - Atspausdinti vartotojus      │
                    │ 5 - Uzkrauti vartotojus is failo │
                    │ 6 - Issaugoti vartotojus i faila │
                    │ 7 - Baigti programa              │
                    └──────────────────────────────────┘
                      Jusu pasirinkimas:\s""");

            try {
                pasirinkimas = in.nextInt();
            } catch (InputMismatchException e) {
                pasirinkimas = -1;
            }

            in.nextLine();
            switch (pasirinkimas) {
                case 1 -> ivestiVartotoja();
                case 2 -> modifikuotiVartotoja();
                case 3 -> trintiVartotoja();
                case 4 -> spausdintiVartotojus();
                case 5 -> uzkrautiVartotojus();
                case 6 -> issaugotiVartotojus();
                case 7 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }
        issaugotiVartotojus();
        in.close();
        System.out.println("Programa baigia darba!");
    }

    private static String vardoIvestis() {
        String vardas;
        do {
            System.out.print("Iveskite varda: ");
            vardas = in.nextLine();
        } while (!isNameValid(vardas));
        return vardas;
    }

    private static String slaptazodzioIvestis() {
        String slaptazodis;
        String slaptazodis2;

        do {
            System.out.print("Iveskite slaptazodi: ");
            slaptazodis = in.nextLine();

            System.out.print("Iveskite slaptazodi(dar karta): ");
            slaptazodis2 = in.nextLine();
        } while (!isPassValid(slaptazodis, slaptazodis2));
        return slaptazodis;
    }

    private static String emailIvestis() {
        String email;
        do {
            System.out.print("Iveskite email: ");
            email = in.nextLine();
        } while (!isEmailValid(email));
        return email;
    }

    private static Lytis lytiesIvestis() {
        System.out.print("Iveskite lyti: ");
        String lytisString = in.nextLine();

        Lytis lytis;

        try {
            lytis = stringToLytis(lytisString);
        } catch (NetinkamaLytisException e) {
            System.out.println("Ivesta netinkama lytis, pritaikoma nezinoma!");
            lytis = Lytis.NEZINOMA;
        }
        return lytis;
    }

    private static LocalDate gimimoDatosIvestis() {
        String gimimoDataString;

        do {
            System.out.print("Iveskite gimimo data(yyyy-MM-dd): ");
            gimimoDataString = in.next();
        } while (!isDateOfBirthValid(gimimoDataString));

        return LocalDate.parse(gimimoDataString, DATE_FORMATTER);
    }

    private static void ivestiVartotoja() {
        String vardas = vardoIvestis();
        String slaptazodis = slaptazodzioIvestis();
        String email = emailIvestis();
        Lytis lytis = lytiesIvestis();
        LocalDate gimimoData = gimimoDatosIvestis();

        vartotojai.put(Vartotojas.getIdCounter(), new Vartotojas(vardas, slaptazodis, email, lytis, gimimoData));
        System.out.println("Vartotojas sukurtas.");
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
            int pasirinkimas;
            try {
                pasirinkimas = in.nextInt();
            } catch (InputMismatchException e) {
                pasirinkimas = -1;
            }
            in.nextLine();
            Vartotojas vart = vartotojai.get(keiciamasId);

            switch (pasirinkimas) {
                case 1 -> vart.setVardas(vardoIvestis());
                case 2 -> vart.setSlaptazodis(slaptazodzioIvestis());
                case 3 -> vart.setEmail(emailIvestis());
                case 4 -> vart.setLytis(lytiesIvestis());
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
        if (vartotojai.isEmpty()) {
            System.out.println("Nera ne vieno vartotojo!");
            return;
        }

        int pasirinkimas;
        System.out.print("""
                Kur norite isvesti vartotojus
                1 - I ekrana
                2 - I faila
                3 - I ekrana ir i faila
                Jusu pasirinkimas:\s""");
        try {
            pasirinkimas = in.nextInt();
        } catch (InputMismatchException e) {
            pasirinkimas = -1;
        }
        in.nextLine();
        switch (pasirinkimas) {
            case 1 -> isvestiIEkrana();
            case 2 -> issaugotiIFaila();
            case 3 -> {
                isvestiIEkrana();
                issaugotiIFaila();
            }
            default -> System.out.println("Blogas pasirinkimas!");
        }
    }

    private static void isvestiIEkrana() {
        for (var v : vartotojai.values()) {
            System.out.println(v);
        }
    }

    private static void issaugotiIFaila() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
        String filename = "vartotojai_" + LocalDateTime.now().format(dtf) + ".txt";
        File file = new File(filename);
        StringBuilder sb = new StringBuilder();

        for (var v : vartotojai.values()) {
            sb.append(v).append("\n");
        }

        try {
            Files.writeString(file.toPath(), sb, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            System.out.println("Failas issaugotas vardu: " + filename);
        } catch (IOException e) {
            System.out.println("Failo issaugoti nepavyko!");
        }
    }

    private static void uzkrautiVartotojus() {
        int maxId = 0;
        try {
            List<String> vartotojaiList = Files.readAllLines(new File("vartotojai.txt").toPath());
            for (String x : vartotojaiList) {
                String[] vartStr = x.split(",");
                int id = Integer.parseInt(vartStr[0]);
                String vardas = vartStr[1];
                String slaptazodis = vartStr[2];
                String email = vartStr[3];
                Lytis lytis;
                try {
                    lytis = stringToLytis(vartStr[4]);
                } catch (NetinkamaLytisException e) {
                    System.out.println("Ivesta netinkama lytis, pritaikoma nezinoma!");
                    lytis = Lytis.NEZINOMA;
                }

                LocalDate gimimoData = LocalDate.parse(vartStr[5], DATE_FORMATTER);
                LocalDateTime regData = LocalDateTime.parse(vartStr[6], DATE_TIME_FORMATTER);

                Vartotojas vart = new Vartotojas(id, vardas, slaptazodis, email, lytis, gimimoData, regData);
                vartotojai.put(id, vart);

                maxId = Math.max(maxId, id);
            }
            Vartotojas.setIdCounter(maxId + 1);
            System.out.println("Vartotojai uzkrauti is failo.");
        } catch (IOException e) {
            System.out.println("Vartotoju is failo uzkrauti nepavyko!");
        }

    }

    private static void issaugotiVartotojus() {
        String filename = "vartotojai.txt";
        File file = new File(filename);
        StringBuilder sb = new StringBuilder();

        for (var v : vartotojai.values()) {
            sb.append(v.toCsv()).append("\n");
        }

        try {
            Files.writeString(file.toPath(), sb, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            System.out.println("Failas issaugotas vardu: " + filename);
        } catch (IOException e) {
            System.out.println("Failo issaugoti nepavyko!");
        }
    }

    private static boolean isNameValid(String vardas) {

        if (vardas.length() < 3) {
            System.out.println("Vartotojo vardas per trumpas!");
            return false;
        }

        if (vardas.length() > 15) {
            System.out.println("Vartotojo vardas per ilgas!");
            return false;
        }

        if (vardas.matches(".*[^A-Za-z].*")) {
            System.out.println("Vardas turi netinkamų simbolių!");
            return false;
        }

        if (!vardas.matches("[A-Z][a-z]+")) {
            System.out.println("Vardas privalo prasidėti didžiąją raide ir toliau būti mažosiomis");
            return false;
        }

        return true;
    }

    private static boolean isPassValid(String slaptazodis, String slaptazodis2) {

        if (!slaptazodis.equals(slaptazodis2)) {
            System.out.println("Slaptazodziai nesutampa!");
            return false;
        }

        if (slaptazodis.length() < 5) {
            System.out.println("Slaptažodis per trumpas!");
            return false;
        }

        if (slaptazodis.matches(".*\\W.*")) {
            System.out.println("Slaptažodis turi neleidžiamų simbolių");
            return false;
        }

        if (!slaptazodis.matches(".*[a-z].*") || !slaptazodis.matches(".*[A-Z].*") || !slaptazodis.matches(".*\\d.*")) {
            System.out.println("Slaptažodis privalo turėti bent vieną didžiają raidę, mažąją raidę ir skaičių");
            return false;
        }

        return true;
    }

    private static boolean isEmailValid(String email) {

        if (email.contains(" ")) {
            System.out.println("El. pastas negali turetu tarpu!");
            return false;
        }

        if (!email.matches("[a-z\\d_.]+@[a-z\\d]+\\.[a-z]{2,10}")) {
            System.out.println("Neteisingas email formatas!");
            return false;
        }

        return true;
    }

    private static boolean isDateOfBirthValid(String dateOfBirthString) {
        LocalDate gimimoData;
        try {
            gimimoData = LocalDate.parse(dateOfBirthString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Blogas datos formatas!");
            return false;
        }
        if (gimimoData.isAfter(LocalDate.now().minusYears(18))) {
            System.out.println("Tau nera 18 metu!");
            return false;
        }

        return true;
    }

    private static Lytis stringToLytis(String lytisString) {
        return switch (lytisString.toLowerCase()) {
            case "vyras" -> Lytis.VYRAS;
            case "moteris" -> Lytis.MOTERIS;
            default -> throw new NetinkamaLytisException();
        };
    }
}