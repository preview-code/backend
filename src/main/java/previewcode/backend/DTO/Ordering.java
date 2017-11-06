package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;

public class Ordering {

    @JsonProperty("defaultGroup")
    public OrderingGroup defaultGroup;
    @JsonProperty("ordering")
    public List<OrderingGroup> ordering;

    public Ordering(OrderingGroup defaultGroup, List<OrderingGroup> ordering) {
        this.defaultGroup = defaultGroup;
        this.ordering = ordering;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ordering ordering1 = (Ordering) o;

        if (!defaultGroup.equals(ordering1.defaultGroup)) return false;
        return ordering.equals(ordering1.ordering);
    }

    @Override
    public int hashCode() {
        int result = defaultGroup.hashCode();
        result = 31 * result + ordering.hashCode();
        return result;
    }
}
