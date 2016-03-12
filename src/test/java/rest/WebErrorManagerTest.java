package rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebErrorManagerTest {

    @Test
    public void testShowFieldsNotPresent() throws Exception {
        JSONObject json = new JSONObject().put("field_a", "");;


        JSONArray actualErrors = WebErrorManager.showFieldsNotPresent(json, new String[]{"field_a", "field_b"});
        JSONArray predictedErrors = new JSONArray().put( new JSONObject().put("field_b", "Field \"field_b\" not present!"));

        assertNotNull(actualErrors);
        assertEquals(predictedErrors.toString(), actualErrors.toString());
    }
}