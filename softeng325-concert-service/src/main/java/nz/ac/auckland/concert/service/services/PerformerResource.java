package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.ConcertPerformer;
import nz.ac.auckland.concert.service.domain.Performer;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/performers")
public class PerformerResource {

    private static Logger _logger = LoggerFactory
            .getLogger(PerformerResource.class);

    private PersistenceManager persistenceManager = PersistenceManager.instance();

    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response retrieveAllPerformers() {
        _logger.info("Retrieving all performers...");
        Set<PerformerDTO> performerDTOS = new HashSet<>();
        EntityManager entityManager = persistenceManager.createEntityManager();
        entityManager.getTransaction().begin();
        try{
            TypedQuery<Performer> query = entityManager.createQuery("select p from Performer p", Performer.class);
            List<Performer> performers = query.getResultList();
            performers.stream().forEach(performer -> {
                Long id = performer.getId();
                TypedQuery<ConcertPerformer> queryForConcerts = entityManager.createQuery("select cp from " +
                        "ConcertPerformer cp where cp.compositePK.performer._id = :id", ConcertPerformer.class).
                        setParameter("id", id);
                List<ConcertPerformer> concertPerformers = queryForConcerts.getResultList();
                Set<Long> concertIds = new HashSet<>();
                for (ConcertPerformer concertPerformer : concertPerformers){
                    concertIds.add(concertPerformer.getCompositePK().getConcert().getId());
                }
                performerDTOS.add(new PerformerDTO(id, performer.getName(), performer.getImageName(),
                        performer.getGenre(), concertIds));
                entityManager.clear();
                _logger.debug("retrieved performer with id "+id);
            });
        }catch (ServiceException serviceException){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Messages
                    .SERVICE_COMMUNICATION_ERROR).build();
        }finally {
            if (entityManager!=null && entityManager.isOpen())
                entityManager.getTransaction().commit();
                entityManager.close();
        }
        GenericEntity<Set<PerformerDTO>> result = new GenericEntity<Set<PerformerDTO>>(performerDTOS){};
        _logger.info("retrieved all performers successfully");
        return Response.ok(result).build();
    }
}
