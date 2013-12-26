package doh.op.mr;

import doh.ds.KVDataSet;
import doh.ds.RealKVDataSet;
import doh.op.OpSerializer;
import doh.op.ReflectionUtils;
import doh.op.ValueOnlyReduceOp;
import doh.op.kvop.CompositeMapOp;
import doh.op.kvop.CompositeReduceOp;
import doh.op.kvop.FlatMapOp;
import doh.op.kvop.KVUnoOp;
import doh.op.kvop.MapOp;
import doh.op.kvop.OpKVTransformer;
import doh.op.kvop.ReduceOp;
import doh.op.mr.FlatMapOpMapper;
import doh.op.mr.MapOpMapper;
import doh.op.mr.ReduceOpReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


import static doh.op.WritableObjectDictionaryFactory.getWritableClass;
import static doh.op.ReflectionUtils.isUnknown;

public class KVOpJobUtils {

    public static Job createCompositeMapReduceJob(RealKVDataSet origin, CompositeMapOp compositeMapOp, ReduceOp reduceOp) throws Exception {
        Job job = new Job(origin.getContext().getConf(), "Composite map simple reduce job");
        configureJob(job, origin, compositeMapOp, reduceOp);
        FileInputFormat.setInputPaths(job, origin.getPath());
        FileOutputFormat.setOutputPath(job, origin.getContext().nextTempPath());
        return job;
    }

    public static Job createCompositeMapCompositeReduceJob(RealKVDataSet origin, CompositeMapOp compositeMapOp, CompositeReduceOp compositeReduceOp) throws Exception {
        Job job = new Job(origin.getContext().getConf(), "Composite map simple reduce job");
        configureJob(job, origin, compositeMapOp, compositeReduceOp);
        FileInputFormat.setInputPaths(job, origin.getPath());
        FileOutputFormat.setOutputPath(job, origin.getContext().nextTempPath());
        return job;
    }

    public static Job createReduceOnlyJob(RealKVDataSet origin, ReduceOp reduceOp) throws Exception {
        Job job = new Job(origin.getContext().getConf(), "Simple reduce only job");
        configureJob(job, origin, reduceOp);
        FileInputFormat.setInputPaths(job, origin.getPath());
        FileOutputFormat.setOutputPath(job, origin.getContext().nextTempPath());
        return job;
    }

    public static Job createCompositeReduceOnlyJob(RealKVDataSet origin, CompositeReduceOp compositeReduceOp) throws Exception {
        Job job = new Job(origin.getContext().getConf(), "Composite reduce only job");
        configureJob(job, origin, compositeReduceOp);
        FileInputFormat.setInputPaths(job, origin.getPath());
        FileOutputFormat.setOutputPath(job, origin.getContext().nextTempPath());
        return job;
    }

    public static Job createCompositeMapOnlyJob(RealKVDataSet origin, CompositeMapOp compositeMapOp) throws Exception {
        Job job = new Job(origin.getContext().getConf(), "Composite map only job");
        configureJob(job, origin, compositeMapOp);
        FileInputFormat.setInputPaths(job, origin.getPath());
        FileOutputFormat.setOutputPath(job, origin.getContext().nextTempPath());
        return job;
    }


    public static void configureJob(Job job, FlatMapOp flatMapOp) throws Exception {
        OpSerializer.saveFlatMapOpToConf(job.getConfiguration(), flatMapOp);
        job.setMapperClass(FlatMapOpMapper.class);
        job.setMapOutputKeyClass(getWritableClass(flatMapOp.toKeyClass()));
        job.setMapOutputValueClass(getWritableClass(flatMapOp.toValueClass()));

        job.setOutputKeyClass(getWritableClass(flatMapOp.toKeyClass()));
        job.setOutputValueClass(getWritableClass(flatMapOp.toValueClass()));
    }

    public static void configureJob(Job job, MapOp mapOp) throws Exception {
        OpSerializer.saveMapOpToConf(job.getConfiguration(), mapOp);
        job.setMapperClass(MapOpMapper.class);
        job.setMapOutputKeyClass(getWritableClass(mapOp.toKeyClass()));
        job.setMapOutputValueClass(getWritableClass(mapOp.toValueClass()));

        job.setOutputKeyClass(getWritableClass(mapOp.toKeyClass()));
        job.setOutputValueClass(getWritableClass(mapOp.toValueClass()));
    }

    public static void configureJob(Job job, KVDataSet origin, ReduceOp reduceOp) throws Exception{
        job.setMapOutputKeyClass(origin.writableKeyClass());
        job.setMapOutputValueClass(origin.writableValueClass());


        OpSerializer.saveReduceOpToConf(job.getConfiguration(), reduceOp);
        job.setReducerClass(ReduceOpReducer.class);
        if (reduceOp instanceof ValueOnlyReduceOp) {
            job.setOutputKeyClass(origin.writableKeyClass());
        } else {
            job.setOutputKeyClass(getWritableClass(reduceOp.toKeyClass()));
        }
        job.setOutputValueClass(getWritableClass(reduceOp.toValueClass()));
    }

    public static void configureJob(Job job, KVDataSet origin, MapOp mapOp, ReduceOp reduceOp) throws Exception{
        OpSerializer.saveMapOpToConf(job.getConfiguration(), mapOp);
        job.setMapperClass(MapOpMapper.class);
        job.setMapOutputKeyClass(getWritableClass(mapOp.toKeyClass()));
        job.setMapOutputValueClass(getWritableClass(mapOp.toValueClass()));

        OpSerializer.saveReduceOpToConf(job.getConfiguration(), reduceOp);
        job.setReducerClass(ReduceOpReducer.class);
        if (reduceOp instanceof ValueOnlyReduceOp) {
            job.setOutputKeyClass(origin.writableKeyClass());
        } else {
            job.setOutputKeyClass(getWritableClass(reduceOp.toKeyClass()));
        }
        job.setOutputValueClass(getWritableClass(reduceOp.toValueClass()));

    }

    public static void configureJob(Job job, KVDataSet origin, FlatMapOp flatMapOp, ReduceOp reduceOp) throws Exception {
        OpSerializer.saveFlatMapOpToConf(job.getConfiguration(), flatMapOp);
        job.setMapperClass(FlatMapOpMapper.class);
        job.setMapOutputKeyClass(getWritableClass(flatMapOp.toKeyClass()));
        job.setMapOutputValueClass(getWritableClass(flatMapOp.toValueClass()));

        OpSerializer.saveReduceOpToConf(job.getConfiguration(), reduceOp);
        job.setReducerClass(ReduceOpReducer.class);
        if (reduceOp instanceof ValueOnlyReduceOp) {
            job.setOutputKeyClass(origin.writableKeyClass());
        } else {
            job.setOutputKeyClass(getWritableClass(reduceOp.toKeyClass()));
        }
        job.setOutputValueClass(getWritableClass(reduceOp.toValueClass()));
    }

    public static void setKeyValueClassesBasedOnMapReduce(Job job, KVDataSet origin, KVUnoOp mapOp, KVUnoOp reduceOp) throws Exception {
        Class<?> mapperInputKeyClass = origin.keyClass();
        Class<?> mapperInputValueClass = origin.valueClass();
        Class<?> mapperOutputKeyClass = ((OpKVTransformer) mapOp).toKeyClass();
        if (isUnknown(mapperOutputKeyClass)) {
            mapperOutputKeyClass = mapperInputKeyClass;
        }
        Class<?> mapperOutputValueClass = ((OpKVTransformer) mapOp).toValueClass();
        if (isUnknown(mapperOutputValueClass)) {
            mapperOutputValueClass = mapperInputValueClass;
        }
        Class<?> reduceOutputKeyClass = ((OpKVTransformer) reduceOp).toKeyClass();
        if (isUnknown(reduceOutputKeyClass)) {
            reduceOutputKeyClass = mapperOutputKeyClass;
        }
        Class<?> reduceOutputValueClass = ((OpKVTransformer) reduceOp).toValueClass();
        if (isUnknown(reduceOutputValueClass)) {
            reduceOutputValueClass = mapperOutputValueClass;
        }

        setUpJobKV(job,
                mapperInputKeyClass,
                mapperInputValueClass,
                mapperOutputKeyClass,
                mapperOutputValueClass,
                reduceOutputKeyClass,
                reduceOutputValueClass
        );
    }


    public static void setUpJobKV(Job job,
                                  Class<?> mapperInputKeyClass,
                                  Class<?> mapperInputValueClass,
                                  Class<?> mapperOutputKeyClass,
                                  Class<?> mapperOutputValueClass,
                                  Class<?> reduceOutputKeyClass,
                                  Class<?> reduceOutputValueClass) {
        job.setMapOutputKeyClass(getWritableClass(mapperOutputKeyClass));
        job.setMapOutputValueClass(getWritableClass(mapperOutputValueClass));

        job.setOutputKeyClass(getWritableClass(reduceOutputKeyClass));
        job.setOutputValueClass(getWritableClass(reduceOutputValueClass));

        OpSerializer.saveKVClassesToConf(job.getConfiguration(),
                mapperInputKeyClass,
                mapperInputValueClass,
                mapperOutputKeyClass,
                mapperOutputValueClass,
                reduceOutputKeyClass,
                reduceOutputValueClass
        );
    }

    public static void setKeyValueClassesBasedOnMap(Job job, KVDataSet origin, KVUnoOp mapOp) throws Exception {
        Class<?> mapperInputKeyClass = origin.keyClass();
        Class<?> mapperInputValueClass = origin.valueClass();
        Class<?> mapperOutputKeyClass = ((OpKVTransformer) mapOp).toKeyClass();
        if (isUnknown(mapperOutputKeyClass)) {
            mapperOutputKeyClass = mapperInputKeyClass;
        }
        Class<?> mapperOutputValueClass = ((OpKVTransformer) mapOp).toValueClass();
        if (isUnknown(mapperOutputValueClass)) {
            mapperOutputValueClass = mapperInputValueClass;
        }
        Class<?> reduceOutputKeyClass = mapperOutputKeyClass;
        Class<?> reduceOutputValueClass = mapperOutputValueClass;

        setUpJobKV(job,
                mapperInputKeyClass,
                mapperInputValueClass,
                mapperOutputKeyClass,
                mapperOutputValueClass,
                reduceOutputKeyClass,
                reduceOutputValueClass
        );
    }

    public static void setKeyValueClassesBasedOnReduce(Job job, KVDataSet origin, KVUnoOp reduceOp) throws Exception {
        Class<?> mapperInputKeyClass = origin.keyClass();
        Class<?> mapperInputValueClass = origin.valueClass();
        Class<?> mapperOutputKeyClass = mapperInputKeyClass;
        Class<?> mapperOutputValueClass = mapperInputValueClass;
        Class<?> reduceOutputKeyClass = ((OpKVTransformer) reduceOp).toKeyClass();
        if (isUnknown(reduceOutputKeyClass)) {
            reduceOutputKeyClass = mapperOutputKeyClass;
        }
        Class<?> reduceOutputValueClass = ((OpKVTransformer) reduceOp).toValueClass();
        if (isUnknown(reduceOutputValueClass)) {
            reduceOutputValueClass = mapperOutputValueClass;
        }

        setUpJobKV(job,
                mapperInputKeyClass,
                mapperInputValueClass,
                mapperOutputKeyClass,
                mapperOutputValueClass,
                reduceOutputKeyClass,
                reduceOutputValueClass
        );
    }

    public static void configureJob(Job job, KVDataSet origin, CompositeMapOp compositeMapOp, ReduceOp reduceOp) throws Exception {

        OpSerializer.saveCompositeMapOp(job.getConfiguration(), compositeMapOp);
        job.setMapperClass(CompositeGeneralMapOpMapper.class);

        OpSerializer.saveReduceOpToConf(job.getConfiguration(), reduceOp);
        job.setReducerClass(ReduceOpReducer.class);

        setKeyValueClassesBasedOnMapReduce(job, origin, compositeMapOp, reduceOp);
    }

    public static void configureJob(Job job, KVDataSet origin, CompositeMapOp compositeMapOp) throws Exception {
        OpSerializer.saveCompositeMapOp(job.getConfiguration(), compositeMapOp);
        job.setMapperClass(CompositeGeneralMapOpMapper.class);
        setKeyValueClassesBasedOnMap(job, origin, compositeMapOp);
    }

    public static void configureJob(Job job, KVDataSet origin, CompositeReduceOp reduceOp) throws Exception {
        OpSerializer.saveCompositeReduceOp(job.getConfiguration(), reduceOp);
        job.setReducerClass(CompositeReduceOpReducer.class);
        setKeyValueClassesBasedOnReduce(job, origin, reduceOp);
    }

    public static void configureJob(Job job, KVDataSet origin, CompositeMapOp compositeMapOp, CompositeReduceOp reduceOp) throws Exception {
        OpSerializer.saveCompositeMapOp(job.getConfiguration(), compositeMapOp);
        job.setMapperClass(CompositeGeneralMapOpMapper.class);
        OpSerializer.saveCompositeReduceOp(job.getConfiguration(), reduceOp);
        job.setReducerClass(CompositeReduceOpReducer.class);

        setKeyValueClassesBasedOnMapReduce(job, origin, compositeMapOp, reduceOp);
    }





}
