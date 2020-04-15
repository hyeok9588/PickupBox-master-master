package app.project.com.pickupbox.Tab;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import app.project.com.pickupbox.Delivery_Now.Frag1;
import app.project.com.pickupbox.Delivery_Now.Frag2;
import app.project.com.pickupbox.Delivery_Now.Frag3;

public class MyPagerAdapter extends FragmentPagerAdapter {

    int mNumOfTabs;

    public MyPagerAdapter(FragmentManager fm, int numTabs){
        super(fm);
        this.mNumOfTabs = numTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Frag1 frag1 = new Frag1();
                return frag1;

            case 1:
                Frag2 frag2 = new Frag2();
                return frag2;

            case 2:
                Frag3 frag3 = new Frag3();
                return frag3;

            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
