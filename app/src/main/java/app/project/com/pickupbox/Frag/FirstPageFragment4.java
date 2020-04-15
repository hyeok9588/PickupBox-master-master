package app.project.com.pickupbox.Frag;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.ChainHead;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import app.project.com.pickupbox.Delivery_Now.MainFrag;
import app.project.com.pickupbox.Delivery_Now.TapActivity;
import app.project.com.pickupbox.Delivery_Now.TapActivity2;
import app.project.com.pickupbox.FirstPage;
import app.project.com.pickupbox.R;

public class FirstPageFragment4 extends Fragment {

    private String nick,userName;


    ViewGroup viewGroup;
    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //viewGroup = (ViewGroup) inflater.inflate(R.layout.firstpage_fragment4,container,false);
        View v =  inflater.inflate(R.layout.firstpage_fragment4, container, false);

        DialogDeliver();

        /*FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_view, TapActivity.newInstance()).commit();*/
        // Fragment로 사용할 MainActivity내의 layout공간을 선택합니다.

        return v;
    }

    public void replaceFragment(Fragment fragment) {

    }
    private void DialogDeliver() {
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setMessage("배송자 이신가요?").setCancelable(false)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.content_view, TapActivity.newInstance()).commit();
                    }
                }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_view, TapActivity2.newInstance()).commit();
            }
        });
        AlertDialog alert = ad.create();
        alert.show();
    }


}
