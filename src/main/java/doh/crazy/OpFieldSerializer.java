package doh.crazy;

import doh.ds.KeyValueDataSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static doh.crazy.ClassUtils.isDouble;
import static doh.crazy.ClassUtils.isInteger;
import static doh.crazy.ClassUtils.isKeyValueDataSet;
import static doh.crazy.ClassUtils.isString;
import static doh.crazy.ClassUtils.isWritable;

public class OpFieldSerializer {

    public static void saveFieldToConf(Configuration conf, Object op, Field f) throws Exception {
        Object value = fieldValue(op, f);
        save(conf, parameterForOpField(op, f), value);
    }

    public static Class fieldClass(Field f) {
        return f.getType();
    }

    public static String parameterForOpField(Object op, Field f) {
        return "tmp." + op.getClass().getSimpleName() + "." + f.getName();
    }

    public static Object fieldValue(Object instance, Field f) throws Exception {
        return f.get(instance);
    }

    public static void setFieldValue(Object instance, Field f, Object value) throws Exception {
        f.set(instance, value);
    }

    public static <T> T load(Configuration conf, String paramName, Class<T> clazz) throws Exception {
        String s = conf.get(paramName);
        if (isWritable(clazz)) {
            return (T) new OpParameterSerDe.WritableOpParameterSerDe().de(s);
        }
        if (isInteger(clazz)) {
            return (T) (Integer) Integer.parseInt(s);
        }
        if (isDouble(clazz)) {
            return (T) (Double) Double.parseDouble(s);
        }
        if (isString(clazz)) {
            return (T) s;
        }
        if (isKeyValueDataSet(clazz)) {
            return (T) loadKVDS(conf, paramName, (Class<? extends KeyValueDataSet>) clazz);
        }
        throw new IllegalArgumentException();
    }

    public static <T> void save(Configuration conf, String paramName, T value) throws Exception {
        if (value instanceof Writable) {
            conf.set(paramName, new OpParameterSerDe.WritableOpParameterSerDe().ser((Writable) value));
            return;
        }
        if (value instanceof Integer) {
            conf.setInt(paramName, (Integer) value);
            return;
        }
        if (value instanceof String) {
            conf.set(paramName, (String) value);
            return;
        }
        if (value instanceof Double) {
            conf.set(paramName, value.toString());
            return;
        }
        if (value instanceof KeyValueDataSet) {
            saveKVDS(conf, paramName, (KeyValueDataSet) value);
            return;
        }
        throw new IllegalArgumentException("Unsupported parameter class: " + value.getClass());
    }


    public static <T extends KeyValueDataSet> void saveKVDS(Configuration conf, String paramName, T value)
            throws Exception {
        Path path = value.getPath();
        String paramValue = path.toString();
        conf.set(paramName, paramValue);
    }

    public static <T extends KeyValueDataSet> T loadKVDS(Configuration conf, String paramName, Class<T> clazz)
            throws Exception {
        String paramValue = conf.get(paramName);
        Path dsPath = new Path(paramValue);
        Constructor constructor = clazz.getDeclaredConstructor(Path.class);
        T kvds = (T) constructor.newInstance(dsPath);
        kvds.setContext(new Context(null, null, conf));
        return kvds;
    }

}
