/**
 * 
 */
package org.bgu.ise.ddb.registration;



import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient; 
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;

/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController{
	
	
	/**
	 * The function checks if the username exist,
	 * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
	 * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param response
	 */
	@RequestMapping(value = "register_new_customer", method={RequestMethod.POST})
	public void registerNewUser(@RequestParam("username") String username,
			@RequestParam("password")    String password,
			@RequestParam("firstName")   String firstName,
			@RequestParam("lastName")  String lastName,
			HttpServletResponse response){
		System.out.println(username+" "+password+" "+lastName+" "+firstName);
		//:TODO
		MongoClient mongoClient = null;
		try {
			if(isExistUser(username)) {
				HttpStatus status = HttpStatus.CONFLICT;
				response.setStatus(status.value());
			}
			else {				
				mongoClient = new MongoClient("localhost" , 27017);
				MongoCollection<Document> collection = mongoClient.getDatabase("project_db").getCollection("users");
				Document new_user = new Document();
				Instant instant = Instant.now();
				//insert the new user to DB
				new_user.append("username", username);
				new_user.append("Password", password);
				new_user.append("Firstname", firstName);
				new_user.append("Lastname", lastName);
				new_user.append("Date", instant);
				collection.insertOne(new_user);
				
				HttpStatus status = HttpStatus.OK;
				response.setStatus(status.value());
				mongoClient.close();
			}						
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		 catch (MongoException e) {
				System.out.println(e);
		}
		 finally {
			 mongoClient.close();
			}
	}
	
	/**
	 * The function returns true if the received username exist in the system otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_user", method={RequestMethod.GET})
	public boolean isExistUser(@RequestParam("username") String username) throws IOException{
		System.out.println(username);
		boolean result = false;
		//:TODO
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost" , 27017);
			MongoCollection<Document> collection = mongoClient.getDatabase("project_db").getCollection("users");
			Document check_user = new Document();
			check_user.append("username", username);
			MongoCursor<Document> cursor = collection.find(check_user).iterator();
			if(cursor.hasNext()) { //TODO: check
				result = true;
			}
			mongoClient.close();
			
		} catch (MongoException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
			mongoClient.close();
		}
		return result;		
	}
	
	/**
	 * The function returns true if the received username and password match a system storage entry, otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "validate_user", method={RequestMethod.POST})
	public boolean validateUser(@RequestParam("username") String username,
			@RequestParam("password")    String password) throws IOException{
		System.out.println(username+" "+password);
		boolean result = false;
		//:TODO
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost" , 27017);
			MongoCollection<Document> collection = mongoClient.getDatabase("project_db").getCollection("users");
			Document validate = new Document();
			validate.append("username", username);
			validate.append("password", password);		
			MongoCursor<Document> cursor = collection.find(validate).iterator();
			if(cursor.hasNext()) { //TODO: check
				result = true;
			}
			mongoClient.close();
			
		} catch (MongoException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
			mongoClient.close();
		}		
		return result;		
	}
	
	/**
	 * The function retrieves number of the registered users in the past n days
	 * @param days
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "get_number_of_registred_users", method={RequestMethod.GET})
	public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException{
		System.out.println(days+"");
		int result = 0;
		//:TODO
		LocalDate date = LocalDate.now().minusDays(days);
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost" , 27017);
			MongoCollection<Document> collection = mongoClient.getDatabase("project_db").getCollection("users");			
			MongoCursor<Document> cursor = collection.find(Filters.gt("Date",date)).iterator();
			while(cursor.hasNext()) {
				result++;
				cursor.next();
			}
			mongoClient.close();
		}
		catch (MongoException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
			mongoClient.close();
		}		
		return result;
	}
	
	/**
	 * The function retrieves all the users
	 * @return
	 */
	@RequestMapping(value = "get_all_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(User.class)
	public  User[] getAllUsers(){
		//:TODO
		List <User> users = new ArrayList<>();
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost" , 27017);
			MongoCollection<Document> collection = mongoClient.getDatabase("project_db").getCollection("users");
			MongoCursor<Document> cursor = collection.find().iterator();
			while(cursor.hasNext()) {
				Document user = cursor.next();
				User temp = new User(user.get("username").toString(), (String)user.get("firstname").toString(), (String)user.get("lastname").toString());
				users.add(temp);				
			}
			mongoClient.close();			
		}
		catch (MongoException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
			mongoClient.close();
		}		
		//User u = new User("alex", "alex", "alex");
		//System.out.println(u);
		User[] u = new User[users.size()];
		return users.toArray(u);
		
	}

}
