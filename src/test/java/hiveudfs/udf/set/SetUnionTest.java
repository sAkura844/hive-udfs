package hiveudfs.udf.set;

import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardMapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUnionTest {
    private SetUnion udf;

    @Before
    public void before() {
        udf = new SetUnion();
    }

    @Test
    public void testEvaluateWithIntTypeList() throws Exception {
        ObjectInspector intOI = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector listOI = ObjectInspectorFactory.getStandardListObjectInspector(intOI);
        StandardListObjectInspector resultOI = (StandardListObjectInspector) udf.initialize(new ObjectInspector[]{listOI, listOI});

        List<Integer> list1 = new ArrayList<Integer>();
        list1.add(1);
        list1.add(2);
        list1.add(3);

        List<Integer> list2 = new ArrayList<>();
        list2.add(3);
        list2.add(4);
        list2.add(5);

        Object result = udf.evaluateList(new DeferredObject[]{new DeferredJavaObject(list1), new DeferredJavaObject(list2)});

        assertEquals(5, resultOI.getListLength(result));
        assertTrue(resultOI.getList(result).contains(1));
        assertTrue(resultOI.getList(result).contains(2));
        assertTrue(resultOI.getList(result).contains(3));
        assertTrue(resultOI.getList(result).contains(4));
        assertTrue(resultOI.getList(result).contains(5));
    }

    @Test
    public void testEvaluateWithStringTypeList() throws Exception {
        ObjectInspector strOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector listOI = ObjectInspectorFactory.getStandardListObjectInspector(strOI);
        StandardListObjectInspector resultOI = (StandardListObjectInspector) udf.initialize(new ObjectInspector[]{listOI, listOI});

        List<String> list1 = new ArrayList<>();
        list1.add("a");
        list1.add("b");
        list1.add("c");

        List<String> list2 = new ArrayList<>();
        list2.add("c");
        list2.add("d");
        list2.add("e");

        Object result = udf.evaluate(new DeferredObject[]{new DeferredJavaObject(list1), new DeferredJavaObject(list2)});

        assertEquals(5, resultOI.getListLength(result));
        assertTrue(resultOI.getList(result).contains("a"));
        assertTrue(resultOI.getList(result).contains("b"));
        assertTrue(resultOI.getList(result).contains("c"));
        assertTrue(resultOI.getList(result).contains("d"));
        assertTrue(resultOI.getList(result).contains("e"));
    }

    @Test
    public void testEvaluateWithIntTypeMap() throws Exception {
        ObjectInspector keyOI = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector valueOI = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector mapOI = ObjectInspectorFactory.getStandardMapObjectInspector(keyOI, valueOI);
        StandardMapObjectInspector resultOI = (StandardMapObjectInspector) udf.initialize(new ObjectInspector[]{mapOI, mapOI});

        HashMap<Integer, Integer> map1 = new HashMap<>();
        map1.put(1, 2);
        map1.put(2, 4);
        map1.put(3, 6);

        HashMap<Integer, Integer> map2 = new HashMap<>();
        map2.put(2, 3);
        map2.put(4, 5);
        map2.put(6, 7);

        Object result = udf.evaluate(new DeferredObject[]{new DeferredJavaObject(map1), new DeferredJavaObject(map2)});

        assertEquals(5, resultOI.getMapSize(result));
        assertTrue(resultOI.getMap(result).containsKey(1));
        assertTrue(resultOI.getMap(result).containsKey(2));
        assertTrue(resultOI.getMap(result).containsKey(3));
        assertTrue(resultOI.getMap(result).containsKey(4));
        assertTrue(resultOI.getMap(result).containsKey(6));
        assertEquals(2, resultOI.getMap(result).get(1));
        assertEquals(4, resultOI.getMap(result).get(2));
        assertEquals(6, resultOI.getMap(result).get(3));
        assertEquals(5, resultOI.getMap(result).get(4));
        assertEquals(7, resultOI.getMap(result).get(6));
    }

    @Test
    public void testEvaluateWithStringTypeMap() throws Exception {
        ObjectInspector keyOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector valueOI = PrimitiveObjectInspectorFactory.javaIntObjectInspector;
        ObjectInspector mapOI = ObjectInspectorFactory.getStandardMapObjectInspector(keyOI, valueOI);
        StandardMapObjectInspector resultOI = (StandardMapObjectInspector) udf.initialize(new ObjectInspector[]{mapOI, mapOI});

        Map<String, Integer> map1 = new HashMap<>();
        map1.put("a", 2);
        map1.put("b", 4);
        map1.put("c", 6);

        Map<String, Integer> map2 = new HashMap<>();
        map2.put("b", 3);
        map2.put("d", 5);
        map2.put("f", 7);

        Object result = udf.evaluate(new DeferredObject[]{new DeferredJavaObject(map1), new DeferredJavaObject(map2)});

        assertEquals(5, resultOI.getMapSize(result));
        assertTrue(resultOI.getMap(result).containsKey("a"));
        assertTrue(resultOI.getMap(result).containsKey("b"));
        assertTrue(resultOI.getMap(result).containsKey("c"));
        assertTrue(resultOI.getMap(result).containsKey("d"));
        assertTrue(resultOI.getMap(result).containsKey("f"));
        assertEquals(2, resultOI.getMap(result).get("a"));
        assertEquals(4, resultOI.getMap(result).get("b"));
        assertEquals(6, resultOI.getMap(result).get("c"));
        assertEquals(5, resultOI.getMap(result).get("d"));
        assertEquals(7, resultOI.getMap(result).get("f"));
    }
}
