package uk.ac.ed.learn9.bb.timetabling.controller;

/**
 * Abstract controller, which stores common elements used by other controllers.
 */
public abstract class AbstractController {
    /**
     * The ID of the plugin vendor; must match the value in the Building Block
     * manifest file bb-manifest.xml.
     */
    public static final String PLUGIN_VENDOR_ID = "uoe";
    /**
     * The ID of the plugin; must match the value in the Building Block
     * manifest file bb-manifest.xml.
     */
    public static final String PLUGIN_ID = "plgn-timetabling";
    
    /**
     * Character set name for the US-ASCII character set.
     */
    public static final String US_ASCII = "US-ASCII";
}
