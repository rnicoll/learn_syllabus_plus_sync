package uk.ac.ed.learn9.bb.timetabling.cli;

import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Command-line client for synchronizing the RDB
 */
public class SynchronizeRDB extends Object {
    public static void main(final String[] argv) {
        final FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("C:\\cygwin\\home\\jnicoll2\\buildingblocks\\learn_timetabling\\src\\main\\webapp\\WEB-INF\\applicationContext.xml");
    }
}
