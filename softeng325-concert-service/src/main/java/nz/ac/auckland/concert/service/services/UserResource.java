package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.Config;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.User;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/users")
public class UserResource {

    private static Logger _logger = LoggerFactory
            .getLogger(UserResource.class);

    private PersistenceManager persistenceManager = PersistenceManager.instance();

    @POST
    @Consumes({MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response createUser(UserDTO newUser) {
        List<String> properties = new ArrayList<>();
        properties.add(newUser.getUsername());
        properties.add(newUser.getPassword());
        properties.add(newUser.getFirstname());
        properties.add(newUser.getLastname());
        properties.forEach(s -> {
            if (s == null || s.equals("")){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.CREATE_USER_WITH_MISSING_FIELDS).build());
            }
        });
        Response response;
        EntityManager entityManager = persistenceManager.createEntityManager();
        try{
            entityManager.getTransaction().begin();
            TypedQuery<User> query = entityManager.createQuery("select u from User u " +
                    "where u.username = :username", User.class).setParameter("username", properties.get(0));
            List<User> users = query.getResultList();
            if (!users.isEmpty()){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME).build());
            }
            NewCookie newCookie = makeCookie(null);
            entityManager.persist(new User(newUser.getUsername(), newUser.getPassword(), newUser.getFirstname(),
                    newUser.getLastname(), newCookie));
            entityManager.getTransaction().commit();
            response = Response.ok(newUser).cookie(newCookie).build();
        }catch (ConstraintViolationException | NonUniqueObjectException exception){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME).build());
        }finally {
            if (entityManager!=null && entityManager.isOpen())
            entityManager.close();
        }
        if (response==null){
            throw new BadRequestException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(Messages.SERVICE_COMMUNICATION_ERROR).build());
        }else {
            return response;
        }
    }

    @POST
    @Path("/authenticate")
    @Consumes({MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response authenticateUser (UserDTO userDTO) {
        List<String> properties = new ArrayList<>();
        properties.add(userDTO.getUsername());
        properties.add(userDTO.getPassword());
        properties.forEach(s -> {
            if (s == null || s.equals("")){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS).build());
            }
        });
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        Response response;
        EntityManager entityManager = persistenceManager.createEntityManager();
        try{
            entityManager.getTransaction().begin();
            TypedQuery<User> query = entityManager.createQuery("select u from User u where " +
                    "u.username = :username", User.class).setParameter("username", username);
            User user = query.getSingleResult();
            if (!user.getPassword().equals(password)){
                throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                        entity(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD).build());
            }
            UserDTO authenticatedUser = new UserDTO(username, password, user.getFirstname(), user.getLastname());
            _logger.info("authentication details: \n" +
                    "\tusername: " + username + "\n" +
                    "\tpassword: " + password + "\n" +
                    "\tfirstname: " + user.getFirstname() + "\n" +
                    "\tlastname: " + user.getLastname());
            NewCookie newCookie = new NewCookie(Config.CLIENT_COOKIE, user.getToken());
            response = Response.ok(authenticatedUser).cookie(newCookie).build();
        }catch (NonUniqueResultException nonUniqueResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity("Integrity Violation: " +
                            "Found multiple records in the USER table with the same USERNAME attribute.").build());
        }catch (NoResultException noResultException){
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).
                    entity(Messages.AUTHENTICATE_NON_EXISTENT_USER).build());
        }finally {
            if (entityManager!=null && entityManager.isOpen())
                entityManager.getTransaction().commit();
                entityManager.close();
        }
        if (response==null){
            throw new BadRequestException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(Messages.SERVICE_COMMUNICATION_ERROR).build());
        }else {
            return response;
        }
    }

    @POST
    @Path("/CreditCardRegistration")
    @Consumes({MediaType.APPLICATION_XML})
    public Response registerCreditCard (CreditCardDTO creditCardDTO,
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
                    "u.token = :token", User.class).setParameter("token", cookie.getValue());
            User user = queryForUser.getSingleResult();
            CreditCard.Type type = null;
            switch (creditCardDTO.getType()) {
                case Visa:
                    type = CreditCard.Type.Visa;
                    break;
                case Master:
                    type = CreditCard.Type.Master;
                    break;
            }
            CreditCard creditCard = new CreditCard(type, creditCardDTO.getName(), creditCardDTO.getNumber(),
                    creditCardDTO.getExpiryDate(), user);
            entityManager.persist(creditCard);
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


    /**
     * Helper method that can be called from every service method to generate a
     * NewCookie instance, if necessary, based on the clientId parameter.
     *
     * @param clientId the Cookie whose name is Config.CLIENT_COOKIE, extracted
     * from a HTTP request message. This can be null if there was no cookie
     * named Config.CLIENT_COOKIE present in the HTTP request message.
     *
     * @return a NewCookie object, with a generated UUID value, if the clientId
     * parameter is null. If the clientId parameter is non-null (i.e. the HTTP
     * request message contained a cookie named Config.CLIENT_COOKIE), this
     * method returns null as there's no need to return a NewCookie in the HTTP
     * response message.
     */
    private NewCookie makeCookie(Cookie clientId){
        NewCookie newCookie = null;

        if(clientId == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            _logger.info("Generated cookie: " + newCookie.getValue());
        }

        return newCookie;
    }

}
