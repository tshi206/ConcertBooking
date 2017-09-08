package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.service.domain.jpa.ConcertPerformerCompositePK;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "CONCERT_PERFORMER")
public class ConcertPerformer {

    @EmbeddedId
    private ConcertPerformerCompositePK compositePK;

    public ConcertPerformer(ConcertPerformerCompositePK concertPerformerCompositePK){
        compositePK = concertPerformerCompositePK;
    }

    public ConcertPerformer(){}


    public ConcertPerformerCompositePK getCompositePK() {
        return compositePK;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConcertPerformer))
            return false;
        if (obj == this)
            return true;

        ConcertPerformer rhs = (ConcertPerformer) obj;
        return new EqualsBuilder().
                append(compositePK.getConcert(), rhs.compositePK.getConcert()).
                append(compositePK.getPerformer(), rhs.compositePK.getPerformer()).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(compositePK.getConcert().getTitle()).
                append(compositePK.getPerformer().getName()).
                hashCode();
    }
}
