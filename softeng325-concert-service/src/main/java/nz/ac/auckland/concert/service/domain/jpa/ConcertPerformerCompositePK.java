package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class ConcertPerformerCompositePK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "cid", nullable = false)
    private Concert concert;

    @ManyToOne
    @JoinColumn(name = "pid", nullable = false)
    private Performer performer;

    public ConcertPerformerCompositePK(Concert concert, Performer performer) {
        this.concert = concert;
        this.performer = performer;
    }

    public ConcertPerformerCompositePK(){}


    public Concert getConcert() {
        return concert;
    }

    public Performer getPerformer() {
        return performer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ConcertPerformerCompositePK)) return false;

        ConcertPerformerCompositePK that = (ConcertPerformerCompositePK) o;

        return new EqualsBuilder()
                .append(concert, that.concert)
                .append(performer, that.performer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(concert)
                .append(performer)
                .toHashCode();
    }
}
