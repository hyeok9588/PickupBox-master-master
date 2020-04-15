package app.project.com.pickupbox.Tab;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import app.project.com.pickupbox.Delivery_Now.Frag4;
import app.project.com.pickupbox.Delivery_Now.Frag5;
import app.project.com.pickupbox.Delivery_Now.Frag6;

public class MyPagerAdapter2 extends FragmentPagerAdapter {

    int mNumOfTabs;

    public MyPagerAdapter2(FragmentManager fm, int numTabs){
        super(fm);
        this.mNumOfTabs = numTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Frag4 frag4 = new Frag4();
                return frag4;

            case 1:
                Frag5 frag5 = new Frag5();
                return frag5;

            case 2:
                Frag6 frag6 = new Frag6();
                return frag6;

            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
