package nz.ac.auckland.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application subclass for the Concert Web service.
 * 
 * 
 *
 */
@ApplicationPath("/services")
public class ConcertApplication extends Application {

	private static Logger _logger = LoggerFactory
			.getLogger(ConcertResource.class);

	// This property should be used by your Resource class. It represents the 
	// period of time, in seconds, that reservations are held for. If a
	// reservation isn't confirmed within this period, the reserved seats are
	// returned to the pool of seats available for booking.
	//
	// This property is used by class ConcertServiceTest.
	public static final int RESERVATION_EXPIRY_TIME_IN_SECONDS = 5;

	private Set<Object> _singletons = new HashSet<Object>();
	private Set<Class<?>> _classes = new HashSet<Class<?>>();

	public ConcertApplication() {
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.createQuery("delete from CreditCard ").executeUpdate();
			entityManager.createQuery("delete from Reservation ").executeUpdate();
			entityManager.createQuery("delete from User ").executeUpdate();
		}catch (Exception e){
			_logger.debug(e.getMessage());
		}finally {
			if (entityManager!=null && entityManager.isOpen()){
				entityManager.close();
			}
		}
		_singletons.add(PersistenceManager.instance());
		_classes.add(ConcertResource.class);
		_classes.add(PerformerResource.class);
		_classes.add(UserResource.class);
		_classes.add(ReservationResource.class);
	}

	@Override
	public Set<Object> getSingletons() {
		return _singletons;
	}

	@Override
	public Set<Class<?>> getClasses() {
		return _classes;
	}
}
