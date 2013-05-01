package uk.ac.ed.learn9.bb.timetabling.cli;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Command-line client for synchronizing the RDB
 */
public class SynchronizeRDB extends Object {
    public static void main(final String[] argv) {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }
}
