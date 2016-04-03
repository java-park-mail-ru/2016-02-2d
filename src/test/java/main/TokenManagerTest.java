package main;

import constants.Constants;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenManagerTest {

    @Test
    public void testGetNewRandomSessionID() throws Exception {
        final String cookie1 = TokenManager.getNewRandomSessionID("random", "data");
        final String cookie2 = TokenManager.getNewRandomSessionID("for", "different", "cookies");
        final String cookie1duplicate = TokenManager.getNewRandomSessionID("random", "data");

        assertNotEquals(cookie1, cookie2);
        assertEquals(cookie1, cookie1duplicate);
    }

    @Test
    public void testGetSIDStringFromHeaders() throws Exception {
        assertEquals(Constants.USER_SESSION_ID, TokenManager.getSIDStringFromHeaders(Constants.FunctionalTestMocks.getOkCookieHeaders()));
        assertEquals(null, TokenManager.getSIDStringFromHeaders(Constants.FunctionalTestMocks.getNoCookieHeaders()));
    }
}