package imcCore.Utils;

import org.junit.Assert;

import java.lang.reflect.Array;

public class GeneralTestUtils {
    public static void assertUnknownObj(Object obj, Object methodPocketObject) {
        if (obj == null) {
            Assert.assertNull(methodPocketObject);
        } else {
            if (obj.getClass().isArray()) {
                if (obj.getClass().getComponentType().isPrimitive()) {
                    int size = Array.getLength(obj);
                    Assert.assertEquals(size, Array.getLength(methodPocketObject));
                    for (int i = 0; i < size; i++) {
                        Assert.assertEquals(Array.get(obj, i), Array.get(methodPocketObject, i));
                    }
                } else {
                    Assert.assertArrayEquals((Object[]) obj, (Object[]) methodPocketObject);
                }
            } else {
                Assert.assertEquals(obj, methodPocketObject);
            }
        }
    }
}
