package hiveudfs.udaf.statistics;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class Mode extends CounterBase {

    static final Logger LOG = LoggerFactory.getLogger(Mode.class.getName());

    @Override
    protected GenericUDAFEvaluator getMyEvaluator() {
        return new ModeEvaluator();
    }

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters) throws SemanticException {
        return super.getEvaluator(parameters);
    }

    public static class ModeEvaluator extends CounterEvaluatorBase {

        @Override
        protected ObjectInspector getCompleteReturnType() {
            return PrimitiveObjectInspectorFactory.getPrimitiveWritableObjectInspector(
                    inputOI.getPrimitiveCategory());
        }

        @Override
        protected ObjectInspector getFinalReturnType() {
            return PrimitiveObjectInspectorFactory.getPrimitiveWritableObjectInspector(
                    ((PrimitiveObjectInspector) counterFieldOI.getMapKeyObjectInspector()).getPrimitiveCategory());
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            CounterAgg myAgg = (CounterAgg) agg;
            Map.Entry<Object, Integer> maxEntry = Collections.max(myAgg.counter.entrySet(), new Comparator<Map.Entry<Object, Integer>>() {
                @Override
                public int compare(Map.Entry<Object, Integer> entry1, Map.Entry<Object, Integer> entry2) {
                    return entry1.getValue().compareTo(entry2.getValue());
                }
            });
            return maxEntry.getKey();
        }
    }
}
