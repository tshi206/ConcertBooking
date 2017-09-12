package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.service.domain.NewsItem;
import org.hibernate.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Timer;
import java.util.TimerTask;

@Path("/newsItems")
public class NewsResource {

    private static Logger _logger = LoggerFactory
            .getLogger(NewsResource.class);

    private PersistenceManager persistenceManager = PersistenceManager.instance();

    private Timer newsFetcher = new Timer();

    @GET
    @Path("/subscribe/{newsId}")
    @Produces(MediaType.APPLICATION_XML)
    public void subscribe (@PathParam("newsId") Long newsId, @Suspended AsyncResponse response){
        newsFetcher.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EntityManager entityManager = persistenceManager.createEntityManager();
                entityManager.getTransaction().begin();
                try{
                    NewsItem newsItem = entityManager.createQuery("select n from NewsItem n where " +
                            "n._id = :id", NewsItem.class).setParameter("id", newsId).
                            setLockMode(LockModeType.OPTIMISTIC).getSingleResult();
                    NewsItemDTO newsItemDTO = new NewsItemDTO(newsItem.getId(), newsItem.getTimetamp(),
                            newsItem.getContent());
                    if (response.resume(newsItemDTO)){
                        newsFetcher.cancel();
                    }else{
                        _logger.info("The request processing is not suspended and could not be resumed.");
                    }
                }catch (NonUniqueResultException nonUniqueResultException){
                    throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                            entity("Integrity Violation: " +
                                    "Found multiple records in the NEWS_ITEMS table with the same _ID attribute.")
                            .build());
                }catch (NoResultException noResultException){
                    _logger.debug("there is no news item with the id: {" + newsId + "} in the DB currently");
                }finally {
                    if (entityManager!=null && entityManager.isOpen())
                        entityManager.getTransaction().commit();
                        entityManager.close();
                }
            }
        }, 5000, 5000);
    }
}
