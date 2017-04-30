

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import DTO.Contact;
import DTO.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ehabm
 */
@Path("/contacts")
public class contactDAO {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hamada");

    @GET
    @Path("/delete")
    public String deleteContact(@QueryParam("contactId") int id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createNamedQuery("Contact.findByContactId").setParameter("contactId", id);
        Contact contact = (Contact) query.getSingleResult();
        entityManager.getTransaction().begin();
        entityManager.remove(contact);
        entityManager.getTransaction().commit();
        return "done";
    }

    @GET
    @Path("/modify")
    public String modifyContact() {
        return "done";
    }

    @GET
    @Path("/allContacts")
    @Produces("application/json")
    public String getAllContacts(@QueryParam("email") String email) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query1 = entityManager.createNamedQuery("User.findByEmail").setParameter("email", email);
        User user = (User) query1.getSingleResult();
        Query query = entityManager.createNamedQuery("Contact.findByUserId").setParameter("userId", user.getId());
        List<Contact> contacts = query.getResultList();
        ArrayList<User> users = new ArrayList<>();
        for (Contact contact : contacts) {
            Query query2 = entityManager.createNamedQuery("User.findById").setParameter("id", contact.getContactPK().getContactId());
            User user2 = (User) query2.getSingleResult();
            users.add(user2);
        }
        return new Gson().toJson(users);
    }

    @GET
    @Path("/add")
    public String addContact(@QueryParam("myEmail") String myEmail,
            @QueryParam("fEmail") String fEmail) {

        JsonObject jsonResponse = new JsonObject();

        /////// 0. Extract My ID from DB
        int userID = getUserId(myEmail);

        if (userID == -1) {
            //// internal error in DB
            jsonResponse.addProperty("status", "failed");
            jsonResponse.addProperty("result", "Errors with our Servers, Try again later!");

        } else {

            /////// 1. Check if user already exists
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Query query = entityManager.createNamedQuery("User.findByEmail").setParameter("email", fEmail);
            List<User> users = query.getResultList();

            if (!users.isEmpty()) {
                /////// 2. if YES
                ////////////////////////// add to contacts and return POSITIVE response

                User friend = users.get(0);

                int friendId = friend.getId();
                Contact contact = new Contact(userID, friendId);

                entityManager.getTransaction().begin();
                entityManager.persist(contact);
                entityManager.getTransaction().commit();

                ///// prepare response as JSON
                jsonResponse.addProperty("status", "success");

                Gson gson = new Gson();
                jsonResponse.addProperty("result", gson.toJson(friend));

            } else {
                /////// 3. if NO
                /////////////////////// return user not found NEGATIVE response
                jsonResponse.addProperty("status", "failed");
                jsonResponse.addProperty("result", "Corresponding user is not fount, Check his email");

            }

        }

        return jsonResponse.toString();
    }

    private int getUserId(String email) {

        int id = 0;

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createNamedQuery("User.findUserEmail").setParameter("email", email);
        List<User> users = query.getResultList();

        if (users.isEmpty()) {
            return -1;
        } else {

            return users.get(0).getId();
        }

    }
}
