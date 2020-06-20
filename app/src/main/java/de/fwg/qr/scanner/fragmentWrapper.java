package de.fwg.qr.scanner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.drawerToggleInterface;
import de.fwg.qr.scanner.tools.network;

public abstract class fragmentWrapper extends Fragment {
    network net;
    //WeakReference<networkCallbackInterface> ref;
    Activity a;
    Context c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = requireActivity();
        c = requireContext();
        net = new network(c);
        //ref=new WeakReference<>((networkCallbackInterface) this);
        setHasOptionsMenu(true);
    }

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
