package nl.dijkrosoft.depot;

import nl.bytesoflife.clienten.data.DefaultContact;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DefaultContactTest {

    @Test
    public void testEnum() {

        assertEquals( DefaultContact.EMAIL, DefaultContact.valueOf("EMAIL"));

    }

    @Test
    public void testEnumPost() {

        assertEquals( DefaultContact.POST, DefaultContact.valueOf("POST"));

    }


}
