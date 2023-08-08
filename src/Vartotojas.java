public class Vartotojas {
    private int id;
    private String vardas;
    private String slaptazodis;
    private String email;
    private Lytis lytis;

    public Vartotojas(int id, String vardas, String slaptazodis, String email, Lytis lytis) {
        this.id = id;
        this.vardas = vardas;
        this.slaptazodis = slaptazodis;
        this.email = email;
        this.lytis = lytis;
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
