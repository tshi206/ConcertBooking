package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.ConcertDate;
import nz.ac.auckland.concert.service.domain.ConcertPerformer;
import nz.ac.auckland.concert.service.domain.ConcertTarif;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Path("/concerts")
public class ConcertResource {

    private static Logger _logger = LoggerFactory
            .getLogger(ConcertResource.class);

    private EntityManager entityManager = PersistenceManager.instance().createEntityManager();;


    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response retrieveAllConcerts() {
        _logger.info("Retrieving all concerts...");
        Set<ConcertDTO> concertDTOS = new HashSet<>();
        try{
            entityManager.getTransaction().begin();
            TypedQuery<Concert> query = entityManager.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = query.getResultList();
            concerts.stream().forEach(concert -> {
                Long id = concert.getId();
                TypedQuery<ConcertDate> queryForDates = entityManager.createQuery("select cd from ConcertDate cd" +
                                " where cd.concert._id = :id",
                        ConcertDate.class).setParameter("id", id);
                List<ConcertDate> concertDates = queryForDates.getResultList();
                Set<LocalDateTime> localDateTimes = new HashSet<>();
                for (ConcertDate concertDate : concertDates){
                    localDateTimes.add(concertDate.getDate());
                }
                TypedQuery<ConcertPerformer> queryForPerformers = entityManager.createQuery("select cp from " +
                        "ConcertPerformer cp where cp.compositePK.concert._id = :id", ConcertPerformer.class).
                        setParameter("id", id);
                List<ConcertPerformer> concertPerformers = queryForPerformers.getResultList();
                Set<Long> performersIds = new HashSet<>();
                for (ConcertPerformer concertPerformer : concertPerformers){
                    performersIds.add(concertPerformer.getCompositePK().getPerformer().getId());
                }
                TypedQuery<ConcertTarif> queryForTarifs = entityManager.createQuery("select ct from ConcertTarif ct" +
                        " where ct.concertTarifCompositePK.concert._id = :id", ConcertTarif.class).
                        setParameter("id", id);
                List<ConcertTarif> concertTarifs = queryForTarifs.getResultList();
                Map<PriceBand, BigDecimal> priceBandBigDecimalMap = new HashMap<>();
                for (ConcertTarif concertTarif : concertTarifs){
                    priceBandBigDecimalMap.put(concertTarif.getConcertTarifCompositePK().getPriceBand(),
                            concertTarif.getConcertTarifCompositePK().getPrice());
                }
                concertDTOS.add(new ConcertDTO(id, concert.getTitle(), localDateTimes, priceBandBigDecimalMap,
                        performersIds));
                entityManager.clear();
                _logger.debug("retrieved concert with id "+id);
            });
        }catch (ServiceException serviceException){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Messages
                    .SERVICE_COMMUNICATION_ERROR).build();
        }finally {
            if (entityManager!=null && entityManager.isOpen())
            entityManager.close();
        }
        GenericEntity<Set<ConcertDTO>> result = new GenericEntity<Set<ConcertDTO>>(concertDTOS){};
        _logger.info("retrieved all concerts successfully");
        return Response.ok(result).build();
    }

}
