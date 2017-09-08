package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.Genre;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    @Column(name = "pid")
    private Long _id;
    @Column(name = "name")
    private String _name;
    @Column(name = "imageName")
    private String _imageName;
    @Enumerated
    @Column(name = "genre")
    private Genre _genre;

    public Performer() {}

    public Performer(String _name, String _imageName, Genre _genre) {
        this._name = _name;
        this._imageName = _imageName;
        this._genre = _genre;
    }

    public Long getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getImageName() {
        return _imageName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
                append(_name, rhs._name).
                append(_imageName, rhs._imageName).
                append(_genre, rhs._genre).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_name).
                append(_imageName).
                append(_genre).
                hashCode();
    }
}
