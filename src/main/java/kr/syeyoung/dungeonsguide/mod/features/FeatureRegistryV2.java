package kr.syeyoung.dungeonsguide.mod.features;

import cc.polyfrost.oneconfig.events.EventManager;
import com.google.common.reflect.ClassPath;
import lombok.val;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class FeatureRegistryV2 {
    List<SimpleFeatureV2> features;

    public void runFeateureDiscovery(){
        try {
            val names = findFeatureNames();
            features = loadModules(names);
        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static final Logger logger = LogManager.getLogger("FeatureRestry");


    static List<SimpleFeatureV2> loadModules(List<String> moduleindex) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArrayList<SimpleFeatureV2> loadedModules = new ArrayList<>();
        for (String moduleclassname : moduleindex) {
            Class<?> clazz = Class.forName(moduleclassname);
            boolean exists = false;

            for (SimpleFeatureV2 bm : loadedModules) {
                if (bm.getClass().getName().equals(clazz.getName())) {
                    exists = true;
                    logger.info("feature " + bm.getId() + " was already loaded");
                    break;
                }
            }

            if (!exists) {
                Constructor<?> ctor = clazz.getConstructor();
                Object object = ctor.newInstance();
                loadedModules.add((SimpleFeatureV2) object);
                MinecraftForge.EVENT_BUS.register(object);
                EventManager.INSTANCE.register(object);
                logger.info("loaded feature: " + ((SimpleFeatureV2) object).getId());
            }


        }

        return loadedModules;
    }


    static List<String> findFeatureNames() throws IOException {
        List<String> clazzlist = new ArrayList<>();
        val loader = Thread.currentThread().getContextClassLoader();

        for (val info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().startsWith("kr.syeyoung.dungeonsguide.mod.features.impl")) {
                val clazz = info.load();
                if (SimpleFeatureV2.class.isAssignableFrom(clazz) && !clazzlist.contains(clazz.getName())) {
                    logger.info("found feature: " + clazz.getName());
                    clazzlist.add(clazz.getName());
                }
            }

        }

        return new ArrayList<>(clazzlist);
    }
}
