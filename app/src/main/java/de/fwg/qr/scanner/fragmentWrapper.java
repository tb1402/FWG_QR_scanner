package de.fwg.qr.scanner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.drawerToggleInterface;
import de.fwg.qr.scanner.tools.network;

/**
 * wrapper class for resources used in all fragments
 */
public abstract class fragmentWrapper extends Fragment {
    network net;
    Activity a;
    Context c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = requireActivity();
        c = requireContext();
        net = network.getInstance(c);
        setHasOptionsMenu(true);
    }

    /**
     * (un)lock ui, used during network request to prevent errors
     * @param state (un)lock
     */
    void lockUI(boolean state) {
        if (state) {
            a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    void showStartIcon() {
        WeakReference<drawerToggleInterface> ic = new WeakReference<>((drawerToggleInterface) a);
        final drawerToggleInterface ici = ic.get();
        ici.showHamburgerIcon();
    }

    void showBackIcon() {
        WeakReference<drawerToggleInterface> ic = new WeakReference<>((drawerToggleInterface) a);
        final drawerToggleInterface ici = ic.get();
        ici.showBackIcon();
    }
}
