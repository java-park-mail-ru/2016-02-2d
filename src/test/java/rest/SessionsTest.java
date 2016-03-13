package rest;

import main.TokenManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.*;
import main.AccountService;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.*;
import java.util.*;

public class SessionsTest extends JerseyTest {

    @Before
    public void refreshAccountService() {

    }

    @Test
    public void testOkLogin() {
        testLogin(RequestFactory.getLoginTestData(RequestFactory.LoginRequestType.LOGIN_OK), true);
    }
    @Test
    public void testWrongLoginLogin() {
        testLogin(RequestFactory.getLoginTestData(RequestFactory.LoginRequestType.LOGIN_WRONG_LOGIN), false);
    }
    @Test
    public void testWrongPasswordLogin() {
        testLogin(RequestFactory.getLoginTestData(RequestFactory.LoginRequestType.LOGIN_WRONG_PASSWORD), false);
    }
    @Test
    public void testOkIsAuthenticated() {
        testIsAuthenticated(RequestFactory.getIsAuthenticatedTestData(RequestFactory.IsAuthRequestType.IS_AUTH_OK));
    }
    @Test
    public void testNoCookieIsAuthenticated() {
        testIsAuthenticated(RequestFactory.getIsAuthenticatedTestData(RequestFactory.IsAuthRequestType.IS_AUTH_NO_COOKIE));
    }
    @Test
    public void testWrongCookieIsAuthenticated() {
        testIsAuthenticated(RequestFactory.getIsAuthenticatedTestData(RequestFactory.IsAuthRequestType.IS_AUTH_WRONG_COOKIE));
    }
    @Test
    public void testOkLogout() {
        testLogout(RequestFactory.getLogoutTestData(RequestFactory.LogoutRequestType.LOGOUT_LOGGED));
    }
    @Test
    public void testNoCookieLogout() {
        testLogout(RequestFactory.getLogoutTestData(RequestFactory.LogoutRequestType.LOGOUT_NOT_LOGGED));
    }
    @Test
    public void testWrongCookieLogout() {
        testLogout(RequestFactory.getLogoutTestData(RequestFactory.LogoutRequestType.LOGOUT_WRONG_COOKIE));
    }




    public void testLogin(Triplet<String, HttpHeaders, Response> data, boolean shouldHaveCookie){
        Response response = sessions.loginUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
        if (shouldHaveCookie)
            assertEquals(SID, response.getCookies().get(TokenManager.COOKIE_NAME).getValue());
        else
            assertEquals(false, response.getCookies().containsKey(TokenManager.COOKIE_NAME));
    }

    public void testIsAuthenticated(Pair<HttpHeaders, Response> data) {
        Response response = sessions.isAuthenticated(data.getValue0());

        assertEquals(data.getValue1().toString(), response.toString());
        assertEquals(data.getValue1().getEntity().toString(), response.getEntity().toString());
    }

    public void testLogout(Pair<HttpHeaders, Response> data) {
        Response response = sessions.logoutUser(data.getValue0());

        assertEquals(data.getValue1().toString(), response.toString());
        assertEquals(data.getValue1().getEntity().toString(), response.getEntity().toString());
        assertNotSame(SID, response.getCookies().get(TokenManager.COOKIE_NAME).getValue());
    }

    // A bit of magic, without which nothing works.
    @Override
    protected Application configure() {
        AccountService mockedAccountService = mock(AccountService.class);
        UserProfile user = mock(UserProfile.class);
        sessions = new Sessions(mockedAccountService);

        when(mockedAccountService.getUser(LOGIN)).thenReturn(user);
        when(mockedAccountService.getUser(ID)).thenReturn(user);
        when(mockedAccountService.getBySessionID(SID)).thenReturn(user);
        when(mockedAccountService.hasSessionID(SID)).thenReturn(true);
        when(mockedAccountService.logoutUser(SID)).thenReturn(true);

        when(user.getId()).thenReturn(ID);
        when(user.getLogin()).thenReturn(LOGIN);
        when(user.getPassword()).thenReturn(PASSWORD);
        when(user.getSessionID()).thenReturn(SID);

        sessions.setAccountService(mockedAccountService);

        return new ResourceConfig(SessionsTest.class);
    }

    private static class RequestFactory {
        public static Triplet<String, HttpHeaders, Response> getLoginTestData(LoginRequestType type){
            switch (type)
            {
                case LOGIN_OK:
                    return Triplet.with(okLoginJSON(), noCookieHeaders(), okLoginResponse());
                case LOGIN_WRONG_LOGIN:
                    return Triplet.with(wrongLoginLoginJSON(), noCookieHeaders(), wrongLoginLoginResponse());
                case LOGIN_WRONG_PASSWORD:
                    return Triplet.with(wrongPasswordLoginJSON(), noCookieHeaders(), wrongPasswordLoginResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Pair<HttpHeaders, Response> getIsAuthenticatedTestData(IsAuthRequestType type) {
            switch (type) {
                case IS_AUTH_OK:
                    return Pair.with(okCookieHeaders(), okIsAuthResponse());
                case IS_AUTH_NO_COOKIE:
                    return Pair.with(noCookieHeaders(), wrongIsAuthResponse());
                case IS_AUTH_WRONG_COOKIE:
                    return Pair.with(wrongCookieHeaders(), wrongIsAuthResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Pair<HttpHeaders, Response> getLogoutTestData(LogoutRequestType type) {
            switch (type) {
                case LOGOUT_LOGGED:
                    return Pair.with(okCookieHeaders(), okLogoutResponse());
                case LOGOUT_NOT_LOGGED:
                    return Pair.with(noCookieHeaders(), wrongLogoutResponse());
                case LOGOUT_WRONG_COOKIE:
                    return Pair.with(wrongCookieHeaders(), wrongLogoutResponse());
            }
            throw new IllegalArgumentException();
        }


        private static String okLoginJSON() {
            return new JSONObject().put("login", LOGIN).put("password", PASSWORD).toString();
        }
        private static Response okLoginResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static String wrongLoginLoginJSON() {
            return new JSONObject().put("login", "").put("password", PASSWORD).toString();
        }
        private static Response wrongLoginLoginResponse() {
            return WebErrorManager.authorizationRequired("Wrong login!");
        }
        private static String wrongPasswordLoginJSON() {
            return new JSONObject().put("login", LOGIN).put("password", "").toString();
        }
        private static Response wrongPasswordLoginResponse() {
            return WebErrorManager.authorizationRequired("Wrong login-password pair!");
        }
        private static Response okIsAuthResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static Response wrongIsAuthResponse() {
            return WebErrorManager.authorizationRequired();
        }
        private static Response okLogoutResponse() {
            return WebErrorManager.okRaw("You have succesfully logged out.").cookie(TokenManager.getNewNullCookie()).build();
        }
        private static Response wrongLogoutResponse() {
            return WebErrorManager.ok("You was not logged in.");
        }

        private static HttpHeaders noCookieHeaders() {
            return new HttpHeaders() {
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
            };
        }
        private static HttpHeaders okCookieHeaders() {
            return new HttpHeaders() {
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
                    Map<String, Cookie> map = new HashMap<>();
                    map.put(TokenManager.COOKIE_NAME, TokenManager.getNewCookieWithSessionID(SID));
                    return map;
                }

                @Override
                public Date getDate() {
                    return null;
                }

                @Override
                public int getLength() {
                    return 0;
                }
            };
        }
        private static HttpHeaders wrongCookieHeaders() {
            return new HttpHeaders() {
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
                    Map<String, Cookie> map = new HashMap<>();
                    map.put(TokenManager.COOKIE_NAME, TokenManager.getNewCookieWithSessionID("ERRONEOUS DATA"));
                    return map;
                }

                @Override
                public Date getDate() {
                    return null;
                }

                @Override
                public int getLength() {
                    return 0;
                }
            };
        }

        public enum LoginRequestType {LOGIN_OK, LOGIN_WRONG_LOGIN, LOGIN_WRONG_PASSWORD}
        public enum IsAuthRequestType {IS_AUTH_OK, IS_AUTH_WRONG_COOKIE, IS_AUTH_NO_COOKIE}
        public enum LogoutRequestType {LOGOUT_LOGGED, LOGOUT_NOT_LOGGED, LOGOUT_WRONG_COOKIE}
    }


    private Sessions sessions;
    private static final String LOGIN = "TEST_LOGIN";
    private static final String PASSWORD = "TEST_PASSWORD";
    private static final String SID = "TEST_SESSION_ID";
    private static final long ID = 0xDEADBEEFL;


}
