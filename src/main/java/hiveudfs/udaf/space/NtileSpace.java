package hiveudfs.udaf.space;

import com.google.common.collect.Lists;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class NtileSpace extends SpaceBase {

    static final Logger LOG = LoggerFactory.getLogger(NtileSpace.class.getName());

    @Override
    protected GenericUDAFEvaluator getIntEvaluator() {
        return new NtileSpaceIntEvaluator();
    }

    @Override
    protected GenericUDAFEvaluator getDoubleEvaluator() {
        return new NtileSpaceDoubleEvaluator();
    }

    @Override
    protected GenericUDAFEvaluator getDecimalEvaluator() {
        return new NtileSpaceDecimalEvaluator();
    }

    public static class NtileSpaceIntEvaluator extends SpaceEvaluatorBase {
        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            SpaceAgg myAgg = (SpaceAgg) agg;
            TreeMap<Integer, Integer> newMap = new TreeMap<>();
            myAgg.counter.forEach((k, v) -> newMap.put(((IntWritable)k).get(), v));

            int start = newMap.firstKey();
            int stop = newMap.lastKey();
            int total = newMap.values().stream().reduce(0, Integer::sum);

            List<Double> doubleCumulative = Lists.newArrayList(
                new hiveudfs.utils.LinSpace(
                    (double) 0, (double) total, myAgg.num + 1, true).iterator()).subList(1, myAgg.num + 1);
            List<Integer> cumulative =
                new ArrayList<>(new LinkedHashSet<>(
                    doubleCumulative.stream().map(v -> (int) Math.ceil(v)).collect(Collectors.toList())));
            List<IntWritable> samples = new ArrayList<>(cumulative.size());
            samples.add(new IntWritable(newMap.firstKey()));
            List<Integer> count = new ArrayList<>(Collections.nCopies(cumulative.size(), 0));

            int i = 0;
            int c = 0;
            for (Map.Entry<Object, Integer> entry : myAgg.counter.entrySet()) {
                while (i < count.size()) {
                    if (c < cumulative.get(i)) {
                        count.set(i, count.get(i) + entry.getValue());
                        c += entry.getValue();
                        break;
                    } else {
                        i++;
                        samples.add((IntWritable) entry.getKey());
                    }
                }
            }

            Object[] result = new Object[2];
            result[0] = samples;
            result[1] = count.stream().map(IntWritable::new).collect(Collectors.toList());
            return result;
        }
    }

    public static class NtileSpaceDoubleEvaluator extends SpaceEvaluatorBase {
        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            SpaceAgg myAgg = (SpaceAgg) agg;
            TreeMap<Double, Integer> newMap = new TreeMap<>();
            myAgg.counter.forEach((k, v) -> newMap.put(((DoubleWritable) k).get(), v));

            double start = newMap.firstKey();
            double stop = newMap.lastKey();

            List<Double> doubleSamples = Lists.newArrayList(
                new hiveudfs.utils.LinSpace(
                    (double) start, (double) stop, myAgg.num + 1, true).iterator());
            List<Double> samples = new ArrayList<>(new LinkedHashSet<>(doubleSamples));
            List<Integer> count = new ArrayList<>(Collections.nCopies(samples.size() - 1, 0));

            int i = 0;
            for (Map.Entry<Object, Integer> entry : myAgg.counter.entrySet()) {
                int value = ((IntWritable) entry.getKey()).get();
                while (i < count.size() - 1) {
                    if (value < samples.get(i + 1)) {
                        count.set(i, count.get(i) + entry.getValue());
                        break;
                    } else {
                        i++;
                    }
                }
                if (i == count.size() - 1) {
                    count.set(i, count.get(i) + entry.getValue());
                }
            }

            Object[] result = new Object[2];
            result[0] = samples.subList(0, samples.size() - 1).stream().map(DoubleWritable::new).collect(Collectors.toList());
            result[1] = count.stream().map(IntWritable::new).collect(Collectors.toList());
            return result;
        }
    }

    public static class NtileSpaceDecimalEvaluator extends SpaceEvaluatorBase {
        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            SpaceAgg myAgg = (SpaceAgg) agg;
            TreeMap<HiveDecimal, Integer> newMap = new TreeMap<>(new Comparator<HiveDecimal>() {
                @Override
                public int compare(HiveDecimal hiveDecimal, HiveDecimal t1) {
                    return hiveDecimal.compareTo(t1);
                }
            });
            myAgg.counter.forEach((k, v) -> newMap.put(((HiveDecimalWritable) k).getHiveDecimal(), v));

            HiveDecimal start = newMap.firstKey();
            HiveDecimal stop = newMap.lastKey();

            List<Double> doubleSamples = Lists.newArrayList(
                new hiveudfs.utils.LinSpace(
                    start.doubleValue(), stop.doubleValue(), myAgg.num + 1, true).iterator());
            List<HiveDecimal> samples =
                new ArrayList<>(new LinkedHashSet<>(
                    doubleSamples.stream().map(v ->  HiveDecimal.create(BigDecimal.valueOf(v))).collect(Collectors.toList())));
            List<Integer> count = new ArrayList<>(Collections.nCopies(samples.size() - 1, 0));

            int i = 0;
            for (Map.Entry<Object, Integer> entry : myAgg.counter.entrySet()) {
                HiveDecimal value = ((HiveDecimalWritable) entry.getKey()).getHiveDecimal();
                while (i < count.size() - 1) {
                    if (value.compareTo(samples.get(i + 1)) < 0) {
                        count.set(i, count.get(i) + entry.getValue());
                        break;
                    } else {
                        i++;
                    }
                }
                if (i == count.size() - 1) {
                    count.set(i, count.get(i) + entry.getValue());
                }
            }

            Object[] result = new Object[2];
            result[0] = samples.subList(0, samples.size() - 1).stream().map(HiveDecimalWritable::new).collect(Collectors.toList());
            result[1] = count.stream().map(IntWritable::new).collect(Collectors.toList());
            return result;
        }
    }
}
