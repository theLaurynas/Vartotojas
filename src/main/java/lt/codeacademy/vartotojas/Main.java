package lt.codeacademy.vartotojas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
            addUserStatement = conn.prepareStatement("""
                    INSERT INTO vartotojai(vardas, slaptazodis, email, lytis, gimimo_data, registracijos_data)
                    VALUES(?,?,?,?,?,?);""");
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
                case 4 -> spausdintiVartotojus(true);
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
            addUserStatement.setString(1, vardas);
            addUserStatement.setString(2, slaptazodis);
            addUserStatement.setString(3, email);
            addUserStatement.setString(4, lytis);
            addUserStatement.setDate(5, Date.valueOf(gimimoData));
            addUserStatement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            addUserStatement.execute();
            System.out.println("Vartotojas sukurtas.");
        } catch (SQLException e) {
            System.err.println("Vartotojo sukurti nepavyko!");
        }
    }

    private static void modifikuotiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
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
                System.out.println("Vartotojas pakoreguotas.");
            } else {
                System.out.println("indeksas " + keiciamasId + " nerastas");
            }
        } catch (SQLException e) {
            System.err.println("Ivyko duomenu bazes klaida!");
        }

    }

    private static void trintiVartotoja() {
        spausdintiVartotojus(false); // Galima nieko neisvesti jei daug vartotoju.
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
        } finally {
            in.nextLine();
        }
    }

    private static void spausdintiVartotojus(boolean menu) {
        //TODO Prideti tikrinima ar yra vartotoju duomenu bazeje.

        String pasirinkimas = "1";

        if (menu) {
            System.out.print("""
                    Kur norite isvesti vartotojus
                    1 - I ekrana
                    2 - I faila
                    3 - I ekrana ir i faila
                    Jusu pasirinkimas:\s""");
            pasirinkimas = in.nextLine();
        }


        if (!pasirinkimas.equals("1") && !pasirinkimas.equals("2") && !pasirinkimas.equals("3")) {
            System.out.println("Blogas pasirinkimas!");
            return;
        }

        StringBuilder sb = new StringBuilder();

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
                String gimimoData = rs.getDate("gimimo_data").toLocalDate()
                        .format(DateTimeFormatter.ISO_DATE);
                String registracijosData = rs.getTimestamp("registracijos_data").toLocalDateTime()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                sb.append(String.format("%d | %s | %s | %s | %s | %s | %s\n",
                        id, vardas, slaptazodis, email, lytis,
                        gimimoData, registracijosData
                ));
            }
        } catch (SQLException ignored) {
            System.err.println("Nepavyko gauti vartotoju is duomenu bazes!");
        }

        String text = sb.toString();

        switch (pasirinkimas) {
            case "1" -> System.out.print(text);
            case "2" -> issaugotiIFaila(text);
            case "3" -> {
                System.out.print(text);
                issaugotiIFaila(text);
            }
        }
    }

    private static void issaugotiIFaila(String text) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
        String filename = "vartotojai_" + LocalDateTime.now().format(dtf) + ".txt";
        File file = new File(filename);

        try {
            Files.writeString(file.toPath(), text, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            System.out.println("Failas issaugotas vardu: " + filename);
        } catch (IOException e) {
            System.out.println("Failo issaugoti nepavyko!");
        }
    }
}