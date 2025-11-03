
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.*;

public class JarClasses {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java JarClasses <jarfile>");
            return;
        }

        String jarPath = args[0];
        File f = new File(jarPath);
        if (!f.exists()) {
            System.out.println("Error: file not found");
            return;
        }

        ArrayList<String> list = new ArrayList<>();

        // read all class names from jar
        try {
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String n = entry.getName();
                if (n.endsWith(".class") && !n.contains("module-info")) {
                    n = n.substring(0, n.length() - 6).replace('/', '.');
                    list.add(n);
                }
            }
            jar.close();
        } catch (Exception e) {
            System.out.println("Failed to read jar file.");
            return;
        }

        Collections.sort(list);

        try {
            URLClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{new URL("jar:file:" + f.getAbsolutePath() + "!/")});

            for (String name : list) {
                try {
                    Class<?> c = loader.loadClass(name);
                    Stats s = Counter.doCount(c);
                    print(name, s);
                } catch (Throwable t) {
                    // just skip classes that cannot be loaded
                }
            }

            loader.close();
        } catch (Exception e) {
            System.out.println("Error loading classes.");
        }
    }

    private static void print(String clsName, Stats s) {
        System.out.println("----------" + clsName + "----------");
        System.out.println("  Public methods: " + s.pub);
        System.out.println("  Private methods: " + s.pri);
        System.out.println("  Protected methods: " + s.prot);
        System.out.println("  Static methods: " + s.stat);
        System.out.println("  Fields: " + s.fields);
    }
}

class Stats {
    int pub;
    int pri;
    int prot;
    int stat;
    int fields;
}

class Counter {
    static Stats doCount(Class<?> c) {
        Stats s = new Stats();
        Method[] ms = c.getDeclaredMethods();
        for (Method m : ms) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod)) s.pub++;
            if (Modifier.isPrivate(mod)) s.pri++;
            if (Modifier.isProtected(mod)) s.prot++;
            if (Modifier.isStatic(mod)) s.stat++;
        }
        s.fields = c.getDeclaredFields().length;
        return s;
    }
}
