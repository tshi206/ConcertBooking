package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.Config;
import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.service.domain.jpa.SeatCompositePK;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import org.hibernate.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Path("/reservations")
public class ReservationResource {

    private static Logger _logger = LoggerFactory
            .getLogger(ReservationResource.class);

    private PersistenceManager persistenceManager = PersistenceManager.instance();

    private Timer timer;

    private static AtomicLong reservation_id = new AtomicLong();

    private Reservation reservation;

    private static Map<Long, Reservation> pendingReservation = new HashMap<>();

    @POST
    @Path("/reservation_request")
    @Consumes({MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response reservationRequest(ReservationRequestDTO reservationRequestDTO,
                                       @CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        if (cookie == null){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.UNAUTHENTICATED_REQUEST).build());
        }
        Response response;
        EntityManager entityManager = persistenceManager.createEntityManager();
        try{
            entityManager.getTransaction().begin();
            TypedQuery<User> queryForUser = entityManager.createQuery("select u from User u where " +
                    "u.token = :token", User.class).setParameter("token", cookie.toString());
            User user = queryForUser.getSingleResult();
            List<Object> properties = new ArrayList<>();
            properties.add(reservationRequestDTO.getConcertId());
            properties.add(reservationRequestDTO.getDate());
            properties.add(reservationRequestDTO.getNumberOfSeats());
            properties.add(reservationRequestDTO.getSeatType());
            properties.forEach(p -> {
                if (p == null)
                    throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                            entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS).build());
            });
            List<LocalDateTime> scheduledDates = entityManager.createQuery("select cd.date from ConcertDate cd " +
                    "where cd.concert._id = :cid", LocalDateTime.class).setParameter("cid", reservationRequestDTO
                    .getConcertId()).getResultList();
            if (!scheduledDates.contains(reservationRequestDTO.getDate())){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE).build());
            }
            List<Seat> seatsBookedAsList = entityManager.createQuery("select s from Seat s", Seat.class)
                    .getResultList();
            Set<SeatDTO> seatsBooked = new HashSet<>();
            if (!seatsBookedAsList.isEmpty()){
                seatsBookedAsList.forEach(seat -> seatsBooked.add(new SeatDTO(seat.getSeatCompositePK().getRow(),
                        seat.getSeatCompositePK().getNumber())));
            }
            Set<SeatDTO> seatsAvailable = TheatreUtility.findAvailableSeats(reservationRequestDTO.getNumberOfSeats(),
                    reservationRequestDTO.getSeatType(), seatsBooked);
            if (seatsAvailable.isEmpty()){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION).build());
            }
            ConcertDate concertDate = entityManager.createQuery("select cd from ConcertDate cd where " +
                    "cd.date = :bookingdate", ConcertDate.class).
                    setParameter("bookingdate", reservationRequestDTO.getDate()).
                    setLockMode(LockModeType.OPTIMISTIC).getSingleResult();
            ConcertTarif concertTarif = entityManager.createQuery("select ct from ConcertTarif ct where " +
                    "ct.concertTarifCompositePK.concert._id = :cid and " +
                    "ct.concertTarifCompositePK.priceBand = :priceBand", ConcertTarif.class).
                    setParameter("cid", reservationRequestDTO.getConcertId()).
                    setParameter("priceBand", reservationRequestDTO.getSeatType()).
                    setLockMode(LockModeType.OPTIMISTIC).getSingleResult();
            Set<Seat> seatsToBePersisted = new HashSet<>();
            seatsAvailable.forEach(seatDTO -> seatsToBePersisted.add(new Seat(new SeatCompositePK(
                    seatDTO.getRow(), seatDTO.getNumber()
            ))));
            reservation = new Reservation(reservation_id.incrementAndGet(), user,
                    concertDate, concertTarif, seatsToBePersisted
                    );
            response = Response.ok(new ReservationDTO(reservation.getRid(), reservationRequestDTO,
                    seatsAvailable)).build();
        }catch (NonUniqueResultException nonUniqueResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity("Integrity Violation: " +
                            "Found multiple records in the USER table with the same TOKEN attribute.").build());
        }catch (NoResultException noResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.BAD_AUTHENTICATON_TOKEN).build());
        }finally {
            if (entityManager!=null && entityManager.isOpen())
                entityManager.close();
        }
        pendingReservation.put(reservation.getRid(), reservation);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pendingReservation.remove(reservation.getRid());
            }
        }, (long) ConcertApplication.RESERVATION_EXPIRY_TIME_IN_SECONDS * 1000l);
        return response;
    }

    @POST
    @Path("/reservation_confirm")
    @Consumes({MediaType.APPLICATION_XML})
    public Response confirmReservation (ReservationDTO reservation,
                                        @CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        if (cookie == null){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.UNAUTHENTICATED_REQUEST).build());
        }
        Reservation reservationToBePersisted = pendingReservation.remove(reservation.getId());
        if (reservationToBePersisted == null){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.EXPIRED_RESERVATION).build());
        }
        Response response;
        EntityManager entityManager = persistenceManager.createEntityManager();
        try{
            entityManager.getTransaction().begin();
            TypedQuery<User> queryForUser = entityManager.createQuery("select u from User u where " +
                    "u.token = :token", User.class).setParameter("token", cookie.toString());
            User user = queryForUser.getSingleResult();
            List<CreditCard> creditCards = entityManager.createQuery("select cc from CreditCard cc where " +
                    "cc.user.uid = :uid", CreditCard.class).setParameter("uid", user.getUid()).
                    setLockMode(LockModeType.OPTIMISTIC).getResultList();
            if (creditCards.isEmpty()){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.CREDIT_CARD_NOT_REGISTERED).build());
            }
            entityManager.persist(reservationToBePersisted);
            entityManager.getTransaction().commit();
            response = Response.status(Response.Status.NO_CONTENT).build();
        }catch (NonUniqueResultException nonUniqueResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity("Integrity Violation: " +
                            "Found multiple records in the USER table with the same TOKEN attribute.").build());
        }catch (NoResultException noResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.BAD_AUTHENTICATON_TOKEN).build());
        }finally {
            if (entityManager!=null && entityManager.isOpen())
                entityManager.close();
        }
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getBookings(@CookieParam(Config.CLIENT_COOKIE) Cookie cookie) {
        if (cookie == null){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.UNAUTHENTICATED_REQUEST).build());
        }
        Response response;
        EntityManager entityManager = persistenceManager.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            TypedQuery<User> queryForUser = entityManager.createQuery("select u from User u where " +
                    "u.token = :token", User.class).setParameter("token", cookie.toString());
            User user = queryForUser.getSingleResult();
            List<Reservation> reservations = entityManager.createQuery("select r from Reservation r where " +
                    "r.user.uid = :uid", Reservation.class).setParameter("uid", user.getUid()).
                    setLockMode(LockModeType.OPTIMISTIC).getResultList();
            Set<BookingDTO> bookingDTOS = new HashSet<>();
            reservations.forEach(reserved -> {
                Concert concert = entityManager.createQuery("select c from Concert c where " +
                        "c._id = :id", Concert.class).
                        setParameter("id", reserved.getConcertDate().getConcert().getId()).
                        setLockMode(LockModeType.OPTIMISTIC).getSingleResult();
                Set<SeatDTO> bookedSeats = new HashSet<>();
                reserved.getBookedSeats().forEach(seat -> bookedSeats.add(
                        new SeatDTO(seat.getSeatCompositePK().getRow(), seat.getSeatCompositePK().getNumber())));
                bookingDTOS.add(new BookingDTO(concert.getId(), concert.getTitle(),
                        reserved.getConcertDate().getDate(), bookedSeats,
                        reserved.getConcertTarif().getConcertTarifCompositePK().getPriceBand()));
            });
            GenericEntity<Set<BookingDTO>> result = new GenericEntity<Set<BookingDTO>>(bookingDTOS){};
            response = Response.ok(result).build();
        }catch (NonUniqueResultException nonUniqueResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity("Integrity Violation: " +
                            "Found multiple records in the USER table with the same TOKEN attribute.").build());
        }catch (NoResultException noResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.BAD_AUTHENTICATON_TOKEN).build());
        }finally {
            if (entityManager!=null && entityManager.isOpen())
                entityManager.close();
        }
        return response;
    }
}
