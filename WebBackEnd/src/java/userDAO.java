

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import DTO.Contact;
import DTO.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;
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
@Path("/users")
public class userDAO {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hamada");

    @GET
    @Path("/register")
    public String registerUser(@QueryParam("email") String email,
             @QueryParam("phone") String phone,
             @QueryParam("mobile") String mobile,
             @QueryParam("password") String password,
             @QueryParam("userName") String userName,
             @QueryParam("fullName") String fullName) {
        System.out.println("ana hena da5lt el register");
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setMobile(mobile);
        user.setPhone(phone);
        user.setUserName(userName);
        user.setPassword(password);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        ////// check if a user exists with same email
        Query query = entityManager.createNamedQuery("User.findByEmail").setParameter("email", email);
        List<User> users = query.getResultList();
        if (!users.isEmpty())
        {
            return "ERROR";
        }
        else{
        
            entityManager.getTransaction().begin();
            entityManager.persist(user);
            entityManager.getTransaction().commit();
            return "DONE";
    
        }
        
    }

    @GET
    @Path("/login")
    @Produces("application/json")
    public String loginUser(@QueryParam("email") String email,
             @QueryParam("password") String password) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createNamedQuery("User.findByEmail").setParameter("email", email);
        List<User> users = query.getResultList();
        JsonObject jsonObject = new JsonObject();
        if (users.size() == 0) {
            jsonObject.addProperty("status", "FAILED");
            jsonObject.addProperty("result", "Sorry! This Email is not registered on ContactO!");
        } else {
            Query query2 = entityManager.createNamedQuery("User.findByEmailandPassword").setParameter("email", email).setParameter("password", password);
            List<User> userss = query2.getResultList();
            if (userss.size() == 0) {
                jsonObject.addProperty("status", "FAILED");
                jsonObject.addProperty("result", "Sorry! Wrong Password!");
            } else {
                jsonObject.addProperty("status", "SUCCESS");
                Gson gson=new Gson();
//                JsonArray jsonArray=new JsonArray();
//                for (User user : userss) {
//               
//                    jsonArray.add(JsonObj);
//                }
                jsonObject.add("result", getAllcontacts(email));
            }
        }
        return new Gson().toJson(jsonObject);
    }
    
    
    private JsonArray getAllcontacts(String email){
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
        return (JsonArray) new Gson().toJsonTree(users);
    }
}
