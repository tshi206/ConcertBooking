package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.Concert;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
public class ConcertTarifCompositePK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "cid", nullable = false)
    private Concert concert;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "priceBand", nullable = false)
    private PriceBand priceBand;

    public ConcertTarifCompositePK(){}

    public ConcertTarifCompositePK(Concert concert, BigDecimal price, PriceBand priceBand) {
        this.concert = concert;
        this.price = price;
        this.priceBand = priceBand;
    }

    public Concert getConcert() {
        return concert;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public PriceBand getPriceBand() { return priceBand; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ConcertTarifCompositePK)) return false;

        ConcertTarifCompositePK that = (ConcertTarifCompositePK) o;

        return new EqualsBuilder()
                .append(concert, that.concert)
                .append(price, that.price)
                .append(priceBand, that.priceBand)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(concert)
                .append(price)
                .append(priceBand)
                .toHashCode();
    }
}
