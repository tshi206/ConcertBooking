package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class SeatCompositePK implements Serializable {

    @Enumerated
    @Column(name = "row", nullable = false)
    private SeatRow _row;

    @Convert(converter = SeatNumberConverter.class)
    @Column(name = "seatNumber", nullable = false)
    private SeatNumber _number;

    public SeatCompositePK() {}

    public SeatCompositePK(SeatRow row, SeatNumber number) {
        _row = row;
        _number = number;
    }

    public SeatRow getRow() {
        return _row;
    }

    public SeatNumber getNumber() {
        return _number;
    }
}
