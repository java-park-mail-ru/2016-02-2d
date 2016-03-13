package rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.javatuples.Triplet;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import main.AccountService;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.*;
import java.util.*;

public class SessionsTest extends JerseyTest {

    @Before
    public void refreshAccountService() {
        mockedAccountService = mock(AccountService.class);

        UserProfile user = new UserProfile("login", "password");

        mockedAccountService.createNewUser("login", "password");
        when(mockedAccountService.getUser("login")).thenReturn(user);
        when(mockedAccountService.getUser(user.getId())).thenReturn(user);
        when(mockedAccountService.getBySessionID(user.getSessionID())).thenReturn(user);

        sessions.setAccountService(mockedAccountService);
    }

    @Test
    public void testLoginCorrect() {
        Triplet request = RequestFactory.getTestData(RequestFactory.LoginRequestType.OK);
        Response response = sessions.loginUser((String) request.getValue0(), (HttpHeaders) request.getValue1());    // And? Why did I need tuples?..
        assertEquals(request.getValue2().toString(), response.toString());
        assertEquals(((Response) request.getValue2()).getEntity().toString(), response.getEntity().toString());
    }

    // A bit of magic, without which nothing works.
    @Override
    protected Application configure() {
        sessions = new Sessions(mockedAccountService);

        return new ResourceConfig(SessionsTest.class);
    }

    private AccountService mockedAccountService;
    private Sessions sessions;

    private static class RequestFactory {
        public static Triplet<String, HttpHeaders, Response> getTestData(LoginRequestType type){
            switch (type)
            {
                case OK:
                    return Triplet.with(okLoginJSON(), okLoginHeaders(), okLoginResponse());
                case WRONG_LOGIN:
                    break;
                case WRONG_PASSWORD:
                    break;
            }
            return Triplet.with(okLoginJSON(), okLoginHeaders(), okLoginResponse());
        }


        private static String okLoginJSON() {return new JSONObject().put("login", "login").put("password", "password").toString(); }
        private static HttpHeaders okLoginHeaders() {return new HttpHeaders() {
            @Override
            public List<String> getRequestHeader(String s) {
                return null;
            }

            @Override
            public String getHeaderString(String s) {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getRequestHeaders() {
                return null;
            }

            @Override
            public List<MediaType> getAcceptableMediaTypes() {
                return null;
            }

            @Override
            public List<Locale> getAcceptableLanguages() {
                return null;
            }

            @Override
            public MediaType getMediaType() {
                return null;
            }

            @Override
            public Locale getLanguage() {
                return null;
            }

            @Override
            public Map<String, Cookie> getCookies() {
                return new HashMap<>();
            }

            @Override
            public Date getDate() {
                return null;
            }

            @Override
            public int getLength() {
                return 0;
            }
        };}
        private static Response okLoginResponse() {return Response.ok(new JSONObject().put("id", 0).toString()).build();}

        public enum LoginRequestType {OK, WRONG_LOGIN, WRONG_PASSWORD}
    }

}
