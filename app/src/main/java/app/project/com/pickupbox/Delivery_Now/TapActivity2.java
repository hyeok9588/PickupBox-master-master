package app.project.com.pickupbox.Delivery_Now;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import app.project.com.pickupbox.R;
import app.project.com.pickupbox.Tab.MyPagerAdapter2;

public class TapActivity2 extends Fragment {

    public static TapActivity2 newInstance() {
        TapActivity2 t = new TapActivity2();
        return t;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap);
    }*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.activity_tap2, container, false);


        TabLayout tabs = v.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("소비자_배송시작"));
        tabs.addTab(tabs.newTab().setText("소비자_배송중"));
        tabs.addTab(tabs.newTab().setText("소비자_배송완료"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);

        //어댑터 설정
        final ViewPager vpPager = (ViewPager)v.findViewById(R.id.pager);
        final MyPagerAdapter2 pagerAdapter = new MyPagerAdapter2(getActivity().getSupportFragmentManager(), 3);
        vpPager.setAdapter(pagerAdapter);
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(vpPager));
        vpPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        return v;

    }




        /*
         * 여기서 Fragment로 작업을 시작합니다.
         * */
       /* FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.content_layout, MainFrag.newInstance()).commit();*/






    /*
     * replaceFragment코드는 혹시 몰라서 넣어놨는데
     * Tab으로 움직이기에 별 쓸일 없을 거 같습니다.
     * */


}
