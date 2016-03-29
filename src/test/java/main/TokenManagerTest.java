package main;

import org.junit.Test;

import static org.junit.Assert.*;

public class TokenManagerTest {

    @Test
    public void testGetNewRandomSessionID() throws Exception {
        final String cookie1 = TokenManager.getNewRandomSessionID();
        final String cookie2 = TokenManager.getNewRandomSessionID();

        assertNotSame(cookie1, cookie2);
    }
}