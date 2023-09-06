package lt.codeacademy.vartotojas;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

import static lt.codeacademy.vartotojas.Ivestis.*;

public class Main {
    static Scanner in = new Scanner(System.in);

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
            System.out.println("lt.codeacademy.vartotojas.Vartotojas sukurtas.");
        } catch (SQLException e) {
            System.err.println("Vartotojo sukurti nepavyko!");
        }
    }

    private static void modifikuotiVartotoja() {
        isvestiIEkrana(); // Galima nieko neisvesti jei daug vartotoju.
        System.out.print("Kuri vartotoja norite keisti: ");
        int keiciamasId = in.nextInt();
        in.nextLine();

        try {
            Statement stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stat.executeQuery("SELECT * FROM vartotojai WHERE id = " + keiciamasId);
            cond:
            if (rs.next()) {
                System.out.print("""
                        1 - vardas
                        2 - slaptazodis
                        3 - email
                        4 - lytis
                        Kuri lauka norite keisti:\s""");
                String pasirinkimas = in.nextLine();

                switch (pasirinkimas) {
                    case "1" -> rs.updateString("vardas", Ivestis.vardoIvestis());
                    case "2" -> rs.updateString("slaptazodis", Ivestis.slaptazodzioIvestis());
                    case "3" -> rs.updateString("email", Ivestis.emailIvestis());
                    case "4" -> rs.updateString("lytis", Ivestis.lytiesIvestis());
                    default -> {
                        System.out.println("Blogas pasirinkimas!");
                        break cond;
                    }
                }
                rs.updateRow();
                System.out.println("lt.codeacademy.vartotojas.Vartotojas pakoreguotas.");
            } else {
                System.out.println("indeksas " + keiciamasId + " nerastas");
            }
        } catch (SQLException e) {
            System.err.println("Ivyko duomenu bazes klaida!");
        }

    }

    private static void trintiVartotoja() {
        isvestiIEkrana(); // Galima nieko neisvesti jei daug vartotoju.
        System.out.print("Kuri vartotoja norite istrinti: ");
        try {
            int trinamasId = in.nextInt();
            Statement stat = conn.createStatement();
            boolean wasDeleted = stat.executeUpdate("DELETE FROM vartotojai WHERE id = " + trinamasId) != 0;

            if (wasDeleted)
                System.out.println("Vartotojas istrintas");
            else
                System.out.printf("Vartotojas su id %d nerastas\n", trinamasId);
        } catch (SQLException e) {
            System.err.println("Ivyko duombazes klaida!");
        } catch (InputMismatchException e) {
            System.err.println("Blogai nurodytas id!");
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
                    FROM vartotojai
                    ORDER BY id""");

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
}