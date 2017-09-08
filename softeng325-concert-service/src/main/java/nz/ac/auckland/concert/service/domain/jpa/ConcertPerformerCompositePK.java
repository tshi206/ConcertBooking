package nz.ac.auckland.concert.service.util;

import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class ConcertPerformerCompositePK implements Serializable {

    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
    @JoinColumn(name = "cid", nullable = false)
    private Concert concert;

    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
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

}
