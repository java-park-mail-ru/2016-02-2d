package rest;

import main.AccountServiceImpl;
import main.TokenManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.*;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsersTest extends JerseyTest {

    @Test
    public void testOkCreateUser() {
         testCreateUser(RequestFactory.getCreateUserTestData(RequestFactory.CreateUserType.CREATE_OK), true);
    }
    @Test
    public void testAlreadyExistsCreateUser() {
         testCreateUser(RequestFactory.getCreateUserTestData(RequestFactory.CreateUserType.CREATE_USER_EXISTS), false);
    }

    @Test
    public void testOkGetUserByID() {
        testGetUserByID(RequestFactory.getUserByIDTestData(RequestFactory.GetByIDType.GET_BY_ID_OK));
    }
    @Test
    public void testNoUserGetUserByID() {
        testGetUserByID(RequestFactory.getUserByIDTestData(RequestFactory.GetByIDType.GET_BY_WRONG_ID));
    }

    @Test
    public void testOkUpdateUser() {
        testUpdateUser(RequestFactory.getUpdateUserTestData(RequestFactory.UpdateUserType.UPDATE_OK));
    }
    @Test
    public void testWrongCookieUpdateUser() {
        testUpdateUser(RequestFactory.getUpdateUserTestData(RequestFactory.UpdateUserType.UPDATE_ANOTHER_USER));
    }

    @Test
    public void testOkDeleteUser() {
        testDeleteUser(RequestFactory.getDeleteUserTestData(RequestFactory.DeleteUserType.DELETE_OK));
    }
    @Test
    public void testNoCookieDeleteUser() {
        testDeleteUser(RequestFactory.getDeleteUserTestData(RequestFactory.DeleteUserType.DELETE_NOT_LOGGED));
    }
    @Test
    public void testAnotherUserDeleteUser() {
        testDeleteUser(RequestFactory.getDeleteUserTestData(RequestFactory.DeleteUserType.DELETE_ANOTHER_USER));
    }




    public void testCreateUser(Triplet<String, HttpHeaders, Response> data, boolean shouldHaveCookie){
        Response response = users.createUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
        if (shouldHaveCookie)
            assertEquals(SID, response.getCookies().get(TokenManager.COOKIE_NAME).getValue());
        else
            assertEquals(false, response.getCookies().containsKey(TokenManager.COOKIE_NAME));
    }

    public void testGetUserByID(Pair<Long, Response> data) {
        Response response = users.getUserByID(data.getValue0());

        assertEquals(data.getValue1().toString(), response.toString());
        assertEquals(data.getValue1().getEntity().toString(), response.getEntity().toString());
    }

    public void testUpdateUser(Triplet<String, HttpHeaders, Response> data) {
        Response response = users.updateUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
    }

    public void testDeleteUser(Triplet<Long, HttpHeaders, Response> data) {
        Response response = users.deleteUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
    }

    // A bit of magic, without which nothing works.
    @Override
    protected Application configure() {
        AccountServiceImpl mockedAccountService = mock(AccountServiceImpl.class);
        UserProfile user = mock(UserProfile.class);
        users = new Users(mockedAccountService);

        when(mockedAccountService.createNewUser(LOGIN, PASSWORD)).thenReturn(user);
        when(mockedAccountService.createNewUser(PASSWORD, LOGIN)).thenReturn(null);
        when(mockedAccountService.getUser(LOGIN)).thenReturn(user);
        when(mockedAccountService.getUser(ID)).thenReturn(user);
        when(mockedAccountService.getBySessionID(SID)).thenReturn(user);
        when(mockedAccountService.hasSessionID(SID)).thenReturn(true);
        when(mockedAccountService.logoutUser(SID)).thenReturn(true);

        when(user.toJson()).thenReturn(new JSONObject().put("id", ID).put("login", LOGIN).put("score", 0L));
        when(user.getId()).thenReturn(ID);
        when(user.getLogin()).thenReturn(LOGIN);
        when(user.getPassword()).thenReturn(PASSWORD);
        when(user.getSessionID()).thenReturn(SID);

        Map<String, Cookie> noCookieMap = new HashMap<>();
        Map<String, Cookie> okCookieMap = new HashMap<>();
        okCookieMap.put(TokenManager.COOKIE_NAME, TokenManager.getNewCookieWithSessionID(SID));
        Map<String, Cookie> badCookieMap = new HashMap<>();
        badCookieMap.put(TokenManager.COOKIE_NAME, TokenManager.getNewCookieWithSessionID("ERRONEOUS DATA"));

        when(noCookieHeaders.getCookies()).thenReturn(noCookieMap);
        when(okCookieHeaders.getCookies()).thenReturn(okCookieMap);
        when(wrongCookieHeaders.getCookies()).thenReturn(badCookieMap);

        return new ResourceConfig(UsersTest.class);
    }

    private static class RequestFactory {
        public static Triplet<String, HttpHeaders, Response> getCreateUserTestData(CreateUserType type){
            switch (type)
            {
                case CREATE_OK:
                    return Triplet.with(okCreateJSON(), noCookieHeaders, okCreateResponse());
                case CREATE_USER_EXISTS:
                    return Triplet.with(wrongCreateJSON(), noCookieHeaders, userExistsCreateResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Pair<Long, Response> getUserByIDTestData(GetByIDType type) {
            switch (type) {
                case GET_BY_ID_OK:
                    return Pair.with(ID, okGetByIDResponse());
                case GET_BY_WRONG_ID:
                    return Pair.with(0L, wrongGetByIDResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Triplet<String, HttpHeaders, Response> getUpdateUserTestData(UpdateUserType type) {
            switch (type) {
                case UPDATE_OK:
                    return Triplet.with(okUpdateJSON(), okCookieHeaders, okUpdateResponse());
                case UPDATE_ANOTHER_USER:
                    return Triplet.with(okUpdateJSON(), wrongCookieHeaders, wrongUpdateResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Triplet<Long, HttpHeaders, Response> getDeleteUserTestData(DeleteUserType type) {
            switch (type) {
                case DELETE_OK:
                    return Triplet.with(ID, okCookieHeaders, okDeleteResponse());
                case DELETE_ANOTHER_USER:
                    return Triplet.with(0L, okCookieHeaders, wrongUserDeleteResponse());
                case DELETE_NOT_LOGGED:
                    return Triplet.with(ID, noCookieHeaders, noCookieDeleteResponse());
            }
            throw new IllegalArgumentException();
        }


        private static String okCreateJSON() {
            return new JSONObject().put("login", LOGIN).put("password", PASSWORD).toString();
        }
        private static String wrongCreateJSON() {
            return new JSONObject().put("login", PASSWORD).put("password", LOGIN).toString();       // Supposing this user is already registered;
        }
        private static Response okCreateResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static Response userExistsCreateResponse() {
            return WebErrorManager.accessForbidden("User already exists!");
        }

        private static Response okGetByIDResponse() {
            return Response.ok(new JSONObject().put("id", ID).put("login", LOGIN).put("score", 0L).toString()).build();
        }
        private static Response wrongGetByIDResponse() {
            return WebErrorManager.accessForbidden();
        }

        private static String okUpdateJSON() {
            return new JSONObject().put("login", LOGIN).put("password", PASSWORD).toString();
        }
        private static Response okUpdateResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static Response wrongUpdateResponse() {
            return WebErrorManager.authorizationRequired("Not logged in!");
        }

        private static Response okDeleteResponse() {
            return WebErrorManager.ok();
        }
        private static Response wrongUserDeleteResponse() {
            return WebErrorManager.accessForbidden("Not your user!");
        }
        private static Response noCookieDeleteResponse() {
            return WebErrorManager.authorizationRequired("Not logged in!");
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum CreateUserType {CREATE_OK, CREATE_USER_EXISTS}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum GetByIDType {GET_BY_ID_OK, GET_BY_WRONG_ID}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum UpdateUserType {UPDATE_OK, UPDATE_ANOTHER_USER}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum DeleteUserType {DELETE_OK, DELETE_ANOTHER_USER, DELETE_NOT_LOGGED}
    }


    private static HttpHeaders noCookieHeaders = mock(HttpHeaders.class);
    private static HttpHeaders okCookieHeaders = mock(HttpHeaders.class);
    private static HttpHeaders wrongCookieHeaders = mock(HttpHeaders.class);
    private Users users;
    private static final String LOGIN = "TEST_LOGIN";
    private static final String PASSWORD = "TEST_PASSWORD";
    private static final String SID = "TEST_SESSION_ID";
    @SuppressWarnings("ConstantNamingConvention")
    private static final long ID = 0xDEADBEEFL;


}
