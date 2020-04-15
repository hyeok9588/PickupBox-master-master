package app.project.com.pickupbox.Delivery_Now;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import app.project.com.pickupbox.R;

public class Frag5 extends Fragment {

    FloatingActionButton Fab;



    public static Frag5 newInstance() {
        Frag5 fragment = new Frag5();
        Bundle args = new Bundle();


        fragment.setArguments(args); //이런식으로 bundle 값 생성해도돼

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup)inflater.inflate(R.layout.frag5,container,false);



        ProgressBar progressBar = rootView.findViewById(R.id.spin_kit);
        Sprite wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);//프로그래스바

        Fab = rootView.findViewById(R.id.fab); //채팅버튼 불러오기

        /*Fab.setOnClickListener(new View.OnClickListener() { //채팅 버튼 클릭시 적용
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ChattingBot.class);
                startActivity(intent);
            }
        });
*/
        return rootView;
    }
}
