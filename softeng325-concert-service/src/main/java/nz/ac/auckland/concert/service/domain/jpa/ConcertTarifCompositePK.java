package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.Concert;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
public class ConcertTarifCompositePK implements Serializable {

    @ManyToOne(cascade={CascadeType.PERSIST})
    @JoinColumn(name = "cid", nullable = false)
    private Concert concert;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated
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

}
