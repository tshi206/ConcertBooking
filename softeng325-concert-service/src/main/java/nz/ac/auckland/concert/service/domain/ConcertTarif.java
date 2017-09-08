package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.service.domain.jpa.ConcertTarifCompositePK;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "CONCERT_TARIFS")
public class ConcertTarif {

    @EmbeddedId
    private ConcertTarifCompositePK concertTarifCompositePK;

    public ConcertTarif(){}

    public ConcertTarif(ConcertTarifCompositePK concertTarifCompositePK) {
        this.concertTarifCompositePK = concertTarifCompositePK;
    }

    public ConcertTarifCompositePK getConcertTarifCompositePK() {
        return concertTarifCompositePK;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConcertTarif))
            return false;
        if (obj == this)
            return true;

        ConcertTarif rhs = (ConcertTarif) obj;
        return new EqualsBuilder().
                append(concertTarifCompositePK.getConcert(), rhs.concertTarifCompositePK.getConcert()).
                append(concertTarifCompositePK.getPrice(), rhs.concertTarifCompositePK.getPrice()).
                append(concertTarifCompositePK.getPriceBand(), rhs.concertTarifCompositePK.getPriceBand()).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(concertTarifCompositePK.getConcert().getTitle()).
                append(concertTarifCompositePK.getPrice()).
                append(concertTarifCompositePK.getPriceBand()).
                hashCode();
    }
}
