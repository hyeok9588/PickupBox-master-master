package app.project.com.pickupbox.Sample_Code;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Adapter.LocationListAdapter;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.R;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class fragList extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager; //리사이클러뷰 사용을 위한 설정

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<LocationExample> locationList;

    private ViewFlipper v_flipper;

    public static fragList newInstance(){
        return  new fragList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
        /*View view = inflater.inflate(R.layout.fragment_frag1, null);*/
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_list, container, false);

        v_flipper = (ViewFlipper)rootView.findViewById(R.id.image_slide);

        int images[] = {R.drawable.theater, R.drawable.back, R.drawable.movie2};

        for (int image : images){
            flipperImages(image);
        }


        Button button1 = (Button)rootView.findViewById(R.id.btnGoMap); //버튼 클릭시 frag2로 이동 후 지도를 보여준다.

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView7);

        recyclerView.setHasFixedSize(true); //리사이클러 뷰 기존성능 강황
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        locationList = new ArrayList<>(); //article 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)

        /*여기서부터-------------------------------------------------------------------------------------------*/
        database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
        databaseReference = database.getReference("location");  //db테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//파이어베이스 데이터 받아오는 곳
                locationList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                    LocationExample location = snapshot.getValue(LocationExample.class); //만들어뒀던 article 객체에 데이터 담기
                    locationList.add(location);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //디비를 가져오던 중 에러 발생시
                Log.e("Frag1", String.valueOf(databaseError.toException())); //에러문 출력
            }
        });
        /*여기까지 리사이클러 뷰를 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

        adapter = new LocationListAdapter(getActivity(), (ArrayList<LocationExample>) locationList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        recyclerView.setAdapter(adapter);  //리사이클러뷰에 어뎁터 연결


        button1.setOnClickListener(new View.OnClickListener() { //버튼 클릭시 지도로 넘어감
            @Override
            public void onClick(View v) {
                /*여기서부터-------------------------------------------------------------------------------------------*/
 /*               database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
                databaseReference = database.getReference("location");  //db테이블 연결
                locationList = new ArrayList<>();

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        locationList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                            LocationExample location = snapshot.getValue(LocationExample.class); //만들어뒀던 article 객체에 데이터 담기
                            locationList.add(location);
                        }
                        Toast.makeText(getActivity(), "DB 불러오는 중...", Toast.LENGTH_SHORT).show();
                        String example= locationList.get(0).getName();
                        Toast.makeText(getActivity(), example, Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), "DB 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
*/
                ((MainActivity)getActivity()).addFragment(fragMap.newInstance(locationList));
                //((MainActivity)getActivity()).replaceFragment(fragMap.newInstance(locationList));

                /*여기까지 frag2에 데이터 넘겨서 지도에 보여주기 위한 DB연동--------------------------------------------------------------------------------------*/

            }
        });
        return  rootView;



    }

    public void flipperImages(int image){
        ImageView imageView = new ImageView(getActivity());
        imageView.setBackgroundResource(image);

        v_flipper.addView(imageView);
        v_flipper.setFlipInterval(2000);
        v_flipper.setAutoStart(true);

        //animation
        v_flipper.setInAnimation(getActivity(),android.R.anim.slide_in_left);
        v_flipper.setOutAnimation(getActivity(),android.R.anim.slide_out_right);
    }
}
