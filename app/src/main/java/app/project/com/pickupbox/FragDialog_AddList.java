package app.project.com.pickupbox;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

import app.project.com.pickupbox.Adapter.LocationListAdapter;
import app.project.com.pickupbox.Data.LocationExample;


public class FragDialog_AddList extends BottomSheetDialogFragment {
    public RecyclerView.Adapter adapter;
    private RecyclerView.Adapter Ladapter;
    private RecyclerView.LayoutManager layoutManager; //리사이클러뷰 사용을 위한 설정



    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ArrayList<LocationExample> dataList;

    private RecyclerView rcView2;

    private SearchView searchView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_frag_dialog__add_list, container, false);

        /*
        ArrayList<LocationExample> arList = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            arList = bundle.getParcelableArrayList("locationList");
            Log.d("테스트__addlist","번들값 있음");

        }
        Log.d("테스트__addlist","번들값 없음");*/


        /*action list에 넣기위한------------------*/
        final RecyclerView rcView2 = v.findViewById(R.id.rcView2);
        rcView2.setHasFixedSize(true); //리사이클러 뷰 기존성능 강황
        layoutManager = new LinearLayoutManager(getContext());
        rcView2.setLayoutManager(layoutManager);

        dataList = new ArrayList<>(); //LocationExample 객체를 담을 어레이 리스트 ( 어뎁터 쪽으로)



        /*DB 비동기 연결-----------------------*/
        DBConnTask dbConnTask = new DBConnTask();
        dbConnTask.execute();
        /*------------------------------------*/

        /*action list에 넣기위한------------------------------------------------*/
        adapter = new LocationListAdapter(getContext(), dataList);  //데이터 가져온 거 리사이클러뷰 보여주기 위해 어댑터 설정
        rcView2.setAdapter(adapter);
        Log.d("실행 순서", "adpater연결");
        /*------------------------------------------------------------------*/

        searchView = v.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LocationListAdapter listAdapter = new LocationListAdapter(getContext(),dataList);
                listAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                LocationListAdapter listAdapter = new LocationListAdapter(getContext(),dataList);
                listAdapter.getFilter().filter(newText);
                return false;
            }
        });




        return v;
    }



    //AsyncTask<doInBackground, onPreexecute, onPostexecute>
    public class DBConnTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                /*여기서부터-------------------------------------------------------------------------------------------*/
                database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
                databaseReference = database.getReference("location");  //db테이블 연결
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//파이어베이스 데이터 받아오는 곳

                        dataList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //반복문으로 데이터 가져오는 곳
                            LocationExample location = snapshot.getValue(LocationExample.class); //만들어뒀던 article 객체에 데이터 담기
                            dataList.add(location);

                        }
                        adapter.notifyDataSetChanged();
                        Log.d("실행 순서", "db list 다 가져옴");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //디비를 가져오던 중 에러 발생시
                        Log.e("FPF6", String.valueOf(databaseError.toException())); //에러문 출력
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("DB불러오는 중..");
            //show dialog
            Log.d("실행 순서","onPreExecute DB");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Log.d("실행 순서","onPostExecute DB");
        }
    }


}
