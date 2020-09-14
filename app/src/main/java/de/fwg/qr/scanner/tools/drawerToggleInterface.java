package de.fwg.qr.scanner.tools;

/**
 * Interface to set the hamburger or back icon in the toolbar, needed in some fragments
 * implemented by {@link de.fwg.qr.scanner.activityMain}
 */
public interface drawerToggleInterface {

    /**
     *Show the Hamburger icon (3 horizontally stacked lines)
     */
    void showHamburgerIcon();

    /**
     * Show back arrow icon
     */
    void showBackIcon();
}
