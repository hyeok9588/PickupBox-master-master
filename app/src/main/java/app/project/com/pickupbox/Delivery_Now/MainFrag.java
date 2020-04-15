package app.project.com.pickupbox.Delivery_Now;


import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;

import app.project.com.pickupbox.R;
import app.project.com.pickupbox.Tab.MyPagerAdapter;


public class MainFrag extends AppCompatActivity {

    FragmentActivity factivity = null;

    Frag1 fragment1;
    Fragment fragment2;
    Frag3 fragment3;

    ImageView imageView;
    File file;
    Button button;
    TapActivity activity;
    FragmentPagerAdapter adapterViewPager;

    public static MainFrag newInstance(){
        return new MainFrag();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_main);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("배송시작"));
        tabs.addTab(tabs.newTab().setText("배송중"));
        tabs.addTab(tabs.newTab().setText("배송완료"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);

        //어댑터 설정
        final ViewPager vpPager = (ViewPager)findViewById(R.id.pager);
        final MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), 3);
        vpPager.setAdapter(pagerAdapter);

        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(vpPager));
        vpPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));



    }



    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.frag_main, container, false); //frag_main을 rootView로 쓰겠다고 사용
        //rootView에서 id값을 찾아와서 넣어줘야 함.

        //-----------------------------------------
  *//*      //sd외장하드 사용?
        File sdcard=Environment.getExternalStorageDirectory();
        file=new File(sdcard,"capture.jpg");
        //-----------------------------------------*//*


        //-----------------------------------------

 *//*       Toolbar toolbar= rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);*//*

        fragment1=new Frag1();
        fragment2=new Frag2();
        fragment3=new Frag3();

//        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.container,fragment1).commit();  //frag1을 넣는 부분




*//*
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position= tab.getPosition();
                Fragment selected= null;
                if(position==0){
                    selected=fragment1;
                }else if(position==1){
                    selected=fragment2;
                }else if(position==2){
                    selected=fragment3;
                }
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,selected).commit();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*//*
        //------------------------------------------------------------------------탭메뉴,상단바

        //------------------------------------------------------------------------배송품이미지 촬영
  *//*      imageView =rootView.findViewById(R.id.imageView);

        button = rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent,101);
            }
        });*//*

        TabLayout tabs = rootView.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("배송시작"));
        tabs.addTab(tabs.newTab().setText("배송중"));
        tabs.addTab(tabs.newTab().setText("배송완료"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);

        //어댑터 설정
        final ViewPager vpPager = (ViewPager) rootView.findViewById(R.id.pager);
        final PagerAdapter pagerAdapter = new PagerAdapter(SupportFragmentManager(), 3);
        vpPager.setAdapter(pagerAdapter);

        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(vpPager));
        vpPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        return rootView;
    }
*/
}
