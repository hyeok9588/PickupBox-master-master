package app.project.com.pickupbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import app.project.com.pickupbox.Frag.FirstPageFragment1;


public class BottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener{
    private Context context;
    private Button btnOBM, btnOBP, btnOBT;
    private FirstPageFragment1 firstPageFragment1;
    private Bundle bundle;

    private Fragment fragment;
    private Button btnSC;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.bottom_sheet_dialog, container, false);

        btnOBM = v.findViewById(R.id.btnOBM);
        btnOBT = v.findViewById(R.id.btnOBT);
        btnOBP = v.findViewById(R.id.btnOBP);



        btnOBM.setOnClickListener(this);
        btnOBT.setOnClickListener(this);
        btnOBP.setOnClickListener(this);

        firstPageFragment1 = new FirstPageFragment1();
        bundle = new Bundle();

        return v;
    }






    @Override
    public void onClick(View v) {
        Intent someIntent = new Intent("Sort_Rule");
        switch (v.getId()){
            case R.id.btnOBM:
                //Toast.makeText(getContext(), "가격별", Toast.LENGTH_SHORT).show();
                someIntent.putExtra("Sort_Rule","가격 정렬");
                LocalBroadcastManager.getInstance(context).sendBroadcast(someIntent);

                break;


            case R.id.btnOBT:
                //Toast.makeText(getContext(), "시간별", Toast.LENGTH_SHORT).show();
                someIntent.putExtra("Sort_Rule","시간 정렬");
                LocalBroadcastManager.getInstance(context).sendBroadcast(someIntent);
                break;


            case R.id.btnOBP:
                //Toast.makeText(getContext(), "사이즈별", Toast.LENGTH_SHORT).show();
                someIntent.putExtra("Sort_Rule","크기 정렬");
                LocalBroadcastManager.getInstance(context).sendBroadcast(someIntent);
                break;
        }
        dismiss();

    }







}
