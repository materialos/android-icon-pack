package com.afollestad.polar.ui.base;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface ISelectionMode  {
    /**
     * Array to hold all the possible launcher 'extras' used in the 'pick icon' intent
     * indicating the launcher supports returning an {@link android.content.Intent.ShortcutIconResource}
     */
    String[] EXTRAS_PICKER_RESOURCE_MODE = new String[] {
            "org.adw.launcher.icons.ACTION_PICK_ICON_RESOURCE"
    };

    boolean inSelectionMode();

    boolean allowResourceResult();
}