package previewcode.backend.database;

/**
 * Represents an identifier of some entity in the database.
 */
public class DatabaseID {

    public final Long id;

    public DatabaseID(Long id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseID that = (DatabaseID) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +"{" +
                "id=" + id +
                '}';
    }


}
