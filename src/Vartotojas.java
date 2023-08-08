public class Vartotojas {
    private static int kiekis = 0; // Ivestu vartotoju kiekis
    private int id;
    private String vardas;
    private String slaptazodis;
    private String email;
    private Lytis lytis;

    public Vartotojas(String vardas, String slaptazodis, String email, Lytis lytis) {
        this.id = kiekis++;
        this.vardas = vardas;
        this.slaptazodis = slaptazodis;
        this.email = email;
        this.lytis = lytis;
    }

    public static int getKiekis() {
        return kiekis;
    }

    @Override
    public String toString() {
        return String.format("Id: %d | Vardas: %s | Slaptazodis: %s | Email: %s | Lytis: %s",
                id, vardas, slaptazodis, email, lytis);
    }

    //<editor-fold desc="Getters/Setters">
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVardas() {
        return vardas;
    }

    public void setVardas(String vardas) {
        this.vardas = vardas;
    }

    public String getSlaptazodis() {
        return slaptazodis;
    }

    public void setSlaptazodis(String slaptazodis) {
        this.slaptazodis = slaptazodis;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Lytis getLytis() {
        return lytis;
    }

    public void setLytis(Lytis lytis) {
        this.lytis = lytis;
    }
    //</editor-fold>
}
