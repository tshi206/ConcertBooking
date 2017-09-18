package nz.ac.auckland.concert.client.service;

import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import nz.ac.auckland.concert.common.Config;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.client.service.util.AW3Downloader;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import javax.ws.rs.client.Invocation.Builder;

public class DefaultService implements ConcertService {

	private static Logger _logger = LoggerFactory
			.getLogger(DefaultService.class);

	private static String WEB_SERVICE_URI = "http://localhost:10000/services";

	private static Client _client; //in case we need a single _client instance to send multiple request
	private static Server _server; //in case we need a reference to the server instance established outside this class

	private Cookie clientId;

	private AtomicLong nextNewsId = new AtomicLong(-1);

	private AtomicBoolean isCanceled = new AtomicBoolean(false);

	public DefaultService() {
//		_client = ClientBuilder.newClient(); //in case we need a single _client instance to send multiple request
		_logger.info("Primary web target destination: " + WEB_SERVICE_URI);
	}

	public DefaultService(Client client, Server server) {
		_client = client;
		_server = server;
		_logger.info("Primary web target destination: " + _server.getURI());
	}

	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/concerts";
			_logger.info("trying to retrieve all concerts from destination: " + uri);
			return client.target(uri).request()
					.accept(MediaType.APPLICATION_XML)
					.get(new GenericType<Set<ConcertDTO>>() {
					});
		}catch (ServiceException serviceException){
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}finally {
			client.close();
		}
	}

	@Override
	public Set<PerformerDTO> getPerformers() throws ServiceException {
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/performers";
			_logger.info("trying to retrieve all performers from destination: " + uri);
			return client.target(uri).request()
					.accept(MediaType.APPLICATION_XML)
					.get(new GenericType<Set<PerformerDTO>>() {
					});
		}catch (ServiceException serviceException){
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}finally {
			client.close();
		}
	}

	@Override
	public UserDTO createUser(UserDTO newUser) throws ServiceException {
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/users";
			_logger.info("trying to create a user in destination: " + uri);
			Response response = client
					.target(uri).request()
					.post(Entity.xml(newUser));
			int responseCode = response.getStatus();
			_logger.info("response status: "+responseCode);
			switch (responseCode){
				case 400:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
				case 200:
					processCookieFromResponse(response);
					return response.readEntity(UserDTO.class);
				case 500:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
			}
		}catch (ServiceException serviceException){
			_logger.info(serviceException.getMessage());
			throw new ServiceException(errorMessage);
		}finally {
			client.close();
		}
		return null;
	}

	@Override
	public UserDTO authenticateUser(UserDTO user) throws ServiceException {
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/users/authenticate";
			_logger.info("trying to authenticate a user in destination: " + uri);
			Response response = client
					.target(uri).request()
					.post(Entity.xml(user));
			int responseCode = response.getStatus();
			_logger.info("response status: "+responseCode);
			switch (responseCode){
				case 200:
					processCookieFromResponse(response);
					UserDTO authenticatedUser = response.readEntity(UserDTO.class);
					return new UserDTO(authenticatedUser.getUsername(), authenticatedUser.getPassword(),
							authenticatedUser.getFirstname(), authenticatedUser.getLastname());
				case 500:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
				case 400:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
			}
		}catch (ServiceException serviceException){
			throw new ServiceException(errorMessage);
		}finally {
			client.close();
		}
		return null;
	}

	@Override
	public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
		String imageName = performer.getImageName();
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		try{
			AW3Downloader.initAW3Downloader();
			List<Image> images = AW3Downloader.fetchPerformerImage(imageName);
			if (images.isEmpty()){
				errorMessage = Messages.NO_IMAGE_FOR_PERFORMER;
				throw new ServiceException(errorMessage);
			}else {
				return images.get(0);
			}
		}catch (ServiceException serviceException){
			throw new ServiceException(errorMessage);
		}
	}

	@Override
	public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/reservations/reservation_request";
			_logger.info("trying to establish a reservation request in destination: " + uri);
			Builder builder = client.target(uri).request();
			addCookieToInvocation(builder);
			Response response = builder.post(Entity.xml(reservationRequest));
			int responseCode = response.getStatus();
			_logger.info("response status: "+responseCode);
			switch (responseCode){
				case 200:
					return response.readEntity(ReservationDTO.class);
				case 500:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
				case 400:
					errorMessage = response.readEntity(String.class);
					_logger.info(errorMessage);
					throw new ServiceException(errorMessage);
			}
		}catch (ServiceException serviceException){
			throw new ServiceException(errorMessage);
		}finally {
			client.close();
		}
		return null;
	}

	@Override
	public void confirmReservation(ReservationDTO reservation) throws ServiceException {
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/reservations/reservation_confirm";
			_logger.info("trying to confirm a reservation in destination: " + uri);
			Builder builder = client.target(uri).request();
			addCookieToInvocation(builder);
			Response response = builder.post(Entity.xml(reservation));
			int responseCode = response.getStatus();
			_logger.info("response status: "+responseCode);
			switch (responseCode){
				case 204:
					_logger.info("reservation has been successfully confirmed. Service Destination: " + uri);
					break;
				case 500:
					errorMessage = response.readEntity(String.class);
					_logger.info(errorMessage);
					throw new ServiceException(errorMessage);
				case 400:
					errorMessage = response.readEntity(String.class);
					_logger.info(errorMessage);
					throw new ServiceException(errorMessage);
			}
		}catch (ServiceException serviceException){
			throw new ServiceException(errorMessage);
		}finally {
			client.close();
		}
	}

	@Override
	public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/users/CreditCardRegistration";
			_logger.info("trying to register a CreditCard object in destination: " + uri);
			Builder builder = client.target(uri).request();
			addCookieToInvocation(builder);
			Response response = builder.post(Entity.xml(creditCard));
			int responseCode = response.getStatus();
			_logger.info("response status: "+responseCode);
			switch (responseCode){
				case 204:
					_logger.info("credit card details have been successfully registered. " +
							"Service Destination: " + uri);
					break;
				case 500:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
				case 400:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
			}
		}catch (ServiceException serviceException){
			throw new ServiceException(errorMessage);
		}finally {
			client.close();
		}
	}

	@Override
	public Set<BookingDTO> getBookings() throws ServiceException {
		String errorMessage = Messages.SERVICE_COMMUNICATION_ERROR;
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/reservations";
			_logger.info("trying to retrieve all reservations for the current user in destination: " + uri);
			Builder builder = client.target(uri).request();
			addCookieToInvocation(builder);
			Response response = builder.accept(MediaType.APPLICATION_XML).get();
			int responseCode = response.getStatus();
			switch (responseCode){
				case 200:
					return response.readEntity(new GenericType<Set<BookingDTO>>() {});
				case 500:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
				case 400:
					errorMessage = response.readEntity(String.class);
					throw new ServiceException(errorMessage);
			}
		}catch (ServiceException serviceException){
			throw new ServiceException(errorMessage);
		}finally {
			client.close();
		}
		return null;
	}

	@Override
	public void subscribeForNewsItems(NewsItemListener listener) {
		isCanceled.set(false);
		Client client = ClientBuilder.newClient();
		try{
			String uri = WEB_SERVICE_URI+"/newsItems/subscribe/";
			WebTarget target = client.target(uri + nextNewsId.incrementAndGet());
			target.request().async().get(new InvocationCallback<NewsItemDTO>() {
				@Override
				public void completed(NewsItemDTO newsItemDTO) {
					Client client = ClientBuilder.newClient();
					if (isCanceled.get()){
						_logger.info("subscription canceled. no further news item would arrive.\t" +
								"Cancellation with current subscription id: " + nextNewsId.get());
						client.close();
						return;
					}
					WebTarget nextSubscriptionTarget = client.target(uri + nextNewsId.incrementAndGet());
					nextSubscriptionTarget.request().async().get(this);
					listener.newsItemReceived(newsItemDTO);
					_logger.info("received news item with id: " + newsItemDTO.getId() + "\t" +
							"Currently subscribed id: " + nextNewsId.get() + "\t" + "Next subscription id: " +
							(nextNewsId.get()+1L) + " ......");
				}

				@Override
				public void failed(Throwable throwable) {
					_logger.info("oops. something went wrong...");
					throw new RuntimeException(throwable);
				}
			});
		}catch (ServiceException serviceException){
			throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
		}
	}

	@Override
	public void cancelSubscription() {
		isCanceled.set(true);
	}

	// Method to add any cookie previously returned from the Web service to an
	// Invocation.Builder instance.
	private void addCookieToInvocation(Builder builder) {
		if (clientId == null){
			_logger.info("this client does not have a token authenticated.");
			return;
		}
		builder.cookie(clientId);
	}

	// Method to extract any cookie from a Response object received from the
	// Web service. If there is a cookie named clientId (Config.CLIENT_COOKIE)
	// it is added to the _cookieValues set, which stores all cookie values for
	// clientId received by the Web service.
	private void processCookieFromResponse(Response response) {
		Map<String, NewCookie> cookies = response.getCookies();

		if(cookies.containsKey(Config.CLIENT_COOKIE)) {
			clientId = cookies.get(Config.CLIENT_COOKIE);
			_logger.info("received cookie toString: " + clientId.toString() + " ; value: " + clientId.getValue());
		}
	}
}
