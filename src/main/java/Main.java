import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.TreeMap;

public class Main {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static Scanner in = new Scanner(System.in);

    static TreeMap<Integer, Vartotojas> vartotojai = new TreeMap<>();

    private static Connection conn;
    private static PreparedStatement addUserStatement;

    public static void connectToDB() {
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/mano",
                    "postgres", "root");
        } catch (SQLException e) {
            System.err.println("Nepavyko prisijungti prie duomenu bazes!");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        int pasirinkimas;
        connectToDB();

        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COALESCE(MAX(id), 0) FROM vartotojai");
            rs.next();
            int maxId = rs.getInt(1);
            Vartotojas.setIdCounter(maxId + 1);
            addUserStatement = conn.prepareStatement("INSERT INTO vartotojai VALUES(?,?,?,?,?,?,?);");
        } catch (SQLException e) {
            System.out.println("Duombazes klaida!");
            System.exit(1);
        }


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
                    │ 5 - Baigti programa              │
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
                case 5 -> {
                    break menu;
                }
                default -> System.out.println("Blogas pasirinkimas!");
            }
        }
        in.close();
        try {
            conn.close();
        } catch (SQLException e) {
        }
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

    private static String lytiesIvestis() {
        System.out.print("Iveskite lyti: ");
        String lytis = in.nextLine().toUpperCase();
        return lytis.equals("VYRAS") || lytis.equals("MOTERIS") ? lytis : "NEZINOMA";
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
        String lytis = lytiesIvestis();
        LocalDate gimimoData = gimimoDatosIvestis();

        try {
            addUserStatement.setInt(1, Vartotojas.getAndIncrIdCounter());
            addUserStatement.setString(2, vardas);
            addUserStatement.setString(3, slaptazodis);
            addUserStatement.setString(4, email);
            addUserStatement.setString(5, lytis);
            addUserStatement.setDate(6, Date.valueOf(gimimoData));
            addUserStatement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            addUserStatement.execute();
            System.out.println("Vartotojas sukurtas.");
        } catch (SQLException e) {
            System.err.println("Vartotojo sukurti nepavyko!");
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
        //TODO Prideti tikrinima ar yra vartotoju duomenu bazeje.

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

        try {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("""
                    SELECT *
                    FROM vartotojai""");

            while (rs.next()) {
                int id = rs.getInt("id");
                String vardas = rs.getString("vardas");
                String slaptazodis = rs.getString("slaptazodis");
                String email = rs.getString("email");
                String lytis = rs.getString("lytis");
                LocalDate gimimoData = rs.getDate("gimimo_data").toLocalDate();
                LocalDateTime registracijosData = rs.getTimestamp("registracijos_data").toLocalDateTime();


                System.out.printf("%d | %s | %s | %s | %s | %s | %s\n",
                        id, vardas, slaptazodis, email, lytis,
                        gimimoData.format(DateTimeFormatter.ISO_DATE),
                        registracijosData.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        } catch (SQLException ignored) {
            System.err.println("Nepavyko gauti vartotoju is duomenu bazes!");
        }
    }

    private static void issaugotiIFaila() {

        throw new RuntimeException("Reikia perdaryti, neveikia su db!");

        /*DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
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
        }*/
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