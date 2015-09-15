package org.codingmatters.poomjobs.zookeeper.test.utils;

import javassist.*;
import org.codingmatters.poomjobs.zookeeper.test.utils.embedded.ZookeeperEmbeddedEnsemble;
import org.slf4j.LoggerFactory;

/**
 * Code taken from apache curator (ASL 2.0).
 * http://curator.apache.org/
 */
public class JMXBug {
    public static void fix() {
        // NOP - only needed so that static initializer is run
        LoggerFactory.getLogger(JMXBug.class).info("JMX bug causing all in memory servers to stop when any other is stopped. Fix by hot changing zookeeper code.");
    }

    static {
        /*
            This ugliness is necessary. There is no way to tell ZK to not register JMX beans. Something
            in the shutdown of a QuorumPeer causes the state of the MBeanRegistry to get confused and
            generates an assert Exception.
         */
        ClassPool pool = ClassPool.getDefault();
        try {
            pool.appendClassPath(new javassist.LoaderClassPath(ZookeeperEmbeddedEnsemble.class.getClassLoader()));     // re: https://github.com/Netflix/curator/issues/11

            try {
                CtClass cc = pool.get("org.apache.zookeeper.server.ZooKeeperServer");
                fixMethods(cc, "registerJMX", "unregisterJMX");
            } catch (NotFoundException ignore) {
                // ignore
            }

            try {
                CtClass cc = pool.get("org.apache.zookeeper.server.quorum.LearnerZooKeeperServer");
                fixMethods(cc, "registerJMX", "unregisterJMX");
            } catch (NotFoundException ignore) {
                // ignore
            }

            try {
                CtClass cc = pool.get("org.apache.zookeeper.jmx.MBeanRegistry");
                fixMethods(cc, "register", "unregister");
            } catch (NotFoundException ignore) {
                // ignore
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fixMethods(CtClass cc, String... methodNames) throws CannotCompileException {
        for (CtMethod method : cc.getDeclaredMethods()) {
            for (String methodName : methodNames) {
                if (method.getName().equals(methodName)) {
                    method.setBody(null);
                }
            }
        }
        cc.toClass();
    }
}
