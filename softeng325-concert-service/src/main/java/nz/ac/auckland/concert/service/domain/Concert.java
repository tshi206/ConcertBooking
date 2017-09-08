package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "CONCERTS")
public class Concert implements Serializable{

    @Id
    @GeneratedValue
    @Column(name = "cid")
    private Long _id;
    @Column(name = "title")
    private String _title;

    public Concert() {
    }

    public Concert(String _title) {
        this._title = _title;
    }

    public Long getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
                append(_title, rhs._title).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_title).
                hashCode();
    }
}
