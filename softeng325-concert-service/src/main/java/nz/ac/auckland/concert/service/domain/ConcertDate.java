package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.service.domain.jpa.LocalDateTimeConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CONCERT_DATES")
public class ConcertDate {

    @Id
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "date")
    private LocalDateTime date;

    @ManyToOne(cascade={CascadeType.PERSIST})
    @JoinColumn(name = "cid", nullable = false)
    private Concert concert;

    public ConcertDate(Concert concert, LocalDateTime date){
        this.concert = concert;
        this.date = date;
    }

    public ConcertDate(){}

    public LocalDateTime getDate() {
        return date;
    }

    public Concert getConcert() {
        return concert;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConcertDate))
            return false;
        if (obj == this)
            return true;

        ConcertDate rhs = (ConcertDate) obj;
        return new EqualsBuilder().
                append(concert, rhs.concert).
                append(date, rhs.date).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(concert.getTitle()).
                append(date).
                hashCode();
    }

}
