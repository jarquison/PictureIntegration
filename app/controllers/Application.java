package controllers;

import models.User;
import play.Logger;
import play.libs.OAuth2;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;

import com.google.gson.JsonObject;

public class Application extends Controller {

    // The following keys correspond to a test application
    // registered on Facebook, and associated with the loisant.org domain.
    // You need to bind loisant.org to your machine with /etc/hosts to
    // test the application locally.

	public static OAuth2 FACEBOOK = new OAuth2(
            "https://graph.facebook.com/oauth/authorize",
            "https://graph.facebook.com/oauth/access_token",
            "566151883592285",
            "92a1ae36b2a00b92d7714b618fd6ae93"
    );
    
	public static void index() {
        login();
    }
	
	public static void home() {
        render();
    }
    
	public static void login() {
	    User u = connected();
	    JsonObject me = null;
	    if (u != null && u.access_token != null) {
	        me = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.access_token)).get().getJson().getAsJsonObject();
	    }
	    render(me);
	}
	
	public static void logout(JsonObject me) {

		index();
    }
	
	public static void auth() {
	    if (OAuth2.isCodeResponse()) {
	        User u = connected();
	        OAuth2.Response response = FACEBOOK.retrieveAccessToken(authURL());
	        u.access_token = response.accessToken;
	        u.save();
	        home();
	    }
	    FACEBOOK.retrieveVerificationCode(authURL());
	}
	
	@Before
	static void setuser() {
	    User user = null;
	    if (session.contains("uid")) {
	        Logger.info("existing user: " + session.get("uid"));
	        user = User.get(Long.parseLong(session.get("uid")));
	    }
	    if (user == null) {
	        user = User.createNew();
	        session.put("uid", user.uid);
	    }
	    renderArgs.put("user", user);
	}
	
	static String authURL() {
	    return play.mvc.Router.getFullUrl("Application.auth");
	}
	
	static User connected() {
	    return (User)renderArgs.get("user");
	}

}
