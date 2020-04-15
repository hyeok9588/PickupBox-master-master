package app.project.com.pickupbox.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.Main_Page.PickupDetail;
import app.project.com.pickupbox.R;

public class SwipeRecyclerViewAdapter extends RecyclerSwipeAdapter<SwipeRecyclerViewAdapter.SimpleViewHolder> {
    private Context context;
    //private ArrayList<LocationExample> locationList;
    private ArrayList<UserBoxInfo> boxList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private int MAIN_PAGE_FLAG = 1;

    public SwipeRecyclerViewAdapter(Context context, ArrayList<UserBoxInfo>  boxList){
        this.context = context;
        this.boxList = boxList;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_location, parent, false);
        return new SimpleViewHolder(view);

    }

    String price="";
    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        holder.tvboxName.setText(boxList.get(position).getBoxName());
        holder.tvuserName.setText(boxList.get(position).getUserName());
        int pr = Integer.parseInt(boxList.get(position).getBoxPrice());
        price = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(pr);
        holder.tvboxPrice.setText(price+"원");
        holder.tvboxSize.setText(boxList.get(position).getBoxSize());
        holder.tvmyLatitude.setText(boxList.get(position).getMyLatitude());
        holder.tvmyLongitude.setText(boxList.get(position).getMyLongitude());
        holder.tvdestLatitude.setText(boxList.get(position).getDestLatitude());
        holder.tvdestLongitude.setText(boxList.get(position).getDestLongitude());
        holder.tvKeyValue.setText(boxList.get(position).getKeyValue());



        mItemManger.bindView(holder.itemView,position);


        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wrapper)); //왼쪽에서 끌어당김
        //holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.swipeLayout.findViewById(R.id.bottom_wrapper1));

        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {
            }

            @Override
            public void onClose(SwipeLayout layout) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) { //손을 댄 것
            }
        });


        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() { //리스트 아이템 클릭 할 경우
            @Override
            public void onClick(View v) {
                holder.swipeLayout.open(SwipeLayout.DragEdge.Right);

            }
        });


        holder.tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.swipeLayout.close(); //닫고
                Intent intent = new Intent(context,PickupDetail.class);
                intent.putExtra("boxName",boxList.get(position).getBoxName());
                intent.putExtra("myLatitude",boxList.get(position).getMyLatitude());
                intent.putExtra("myLongitude",boxList.get(position).getMyLongitude());

                intent.putExtra("boxSize",boxList.get(position).getBoxSize());
                intent.putExtra("boxPrice",boxList.get(position).getBoxPrice());

                intent.putExtra("pickupTime",boxList.get(position).getPickupTime());
                intent.putExtra("duration",boxList.get(position).getDuration());
                intent.putExtra("distance",boxList.get(position).getDistance());
                intent.putExtra("userName",boxList.get(position).getUserName());
                intent.putExtra("keyValue",boxList.get(position).getKeyValue());
                //Log.d("키값",boxList.get(position).getKeyValue());
                intent.putExtra("FLAG",MAIN_PAGE_FLAG);
                context.startActivity(intent);
            }
        });


        holder.tvDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "딜하기!", Toast.LENGTH_SHORT).show();
                holder.swipeLayout.close();
            }
        });

      /*  holder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.swipeLayout.close();

                //DB삭제하는 방법.
                database = FirebaseDatabase.getInstance(); //파이어 베이스 데이터베이스 연동
                databaseReference = database.getReference("BoxList");  //db테이블 연결

                String k =  boxList.get(position).getKeyValue();
                databaseReference.child(k).setValue(null);

                String a = boxList.get(position).getBoxName();


                Toast.makeText(context, k+" 삭제 !", Toast.LENGTH_SHORT).show();

                *//*DatabaseReference.child(removeNickName).setValue(null); //child는 하위값이 없으면 자동으로 삭제되는점 이용*//*

            }
        });*/









    }

    @Override
    public int getItemCount() {
        return (boxList != null ? boxList.size() : 0);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvboxName,tvboxSize, tvboxPrice, tvuserName, tvmyLatitude,tvmyLongitude, tvdestLatitude, tvdestLongitude, tvKeyValue;


        TextView tvEdit;
        TextView tvDeal;
        TextView tvPlus;
        SwipeLayout swipeLayout;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            this.tvboxName = itemView.findViewById(R.id.boxName);
            this.tvboxPrice = itemView.findViewById(R.id.boxPrice);
            this.tvboxSize = itemView.findViewById(R.id.boxSize);
            this.tvuserName = itemView.findViewById(R.id.userName);
            this.tvmyLatitude = itemView.findViewById(R.id.myLatitude);
            this.tvmyLongitude = itemView.findViewById(R.id.myLongitude);
            this.tvdestLatitude = itemView.findViewById(R.id.destLatitude);
            this.tvdestLongitude = itemView.findViewById(R.id.destLongitude);
            this.tvKeyValue = itemView.findViewById(R.id.IDkeyValue);



            this.tvEdit = (TextView) itemView.findViewById(R.id.tvEdit);
            this.tvDeal = (TextView) itemView.findViewById(R.id.tvDeal);
            this.tvPlus = (TextView) itemView.findViewById(R.id.tvPlus);
        }
    }
}
