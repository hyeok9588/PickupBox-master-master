package app.project.com.pickupbox.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Data.UserBoxInfo;
import app.project.com.pickupbox.R;

public class DetailRecyclerViewAdapter extends RecyclerView.Adapter<DetailRecyclerViewAdapter.DetailViewHolder> {

    private Context context;
    private ArrayList<UserBoxInfo> boxList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    public DetailRecyclerViewAdapter (Context context, ArrayList<UserBoxInfo> boxList){
        this.context = context;
        this.boxList = boxList;
    }


    @NonNull
    @Override
    public DetailRecyclerViewAdapter.DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_rec_item, parent, false);

        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailRecyclerViewAdapter.DetailViewHolder holder, int position) {
        holder.tvDDetailUserName.setText(boxList.get(position).getUserName());
        holder.tvDDetailBoxName.setText(boxList.get(position).getBoxName());
        holder.tvDDetailBoxSize.setText(boxList.get(position).getBoxSize());
        holder.tvDDetailBoxPrice.setText(boxList.get(position).getBoxPrice());

    }

    @Override
    public int getItemCount() {
        return (boxList != null ? boxList.size() : 0);
    }

    public class DetailViewHolder extends RecyclerView.ViewHolder{
        TextView tvDDetailUserName, tvDDetailBoxName, tvDDetailBoxPrice, tvDDetailBoxSize;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvDDetailBoxName = itemView.findViewById(R.id.tvDDetailBoxName);
            this.tvDDetailBoxPrice = itemView.findViewById(R.id.tvDDetailBoxPrice);
            this.tvDDetailBoxSize = itemView.findViewById(R.id.tvDDetailBoxSize);
            this.tvDDetailUserName = itemView.findViewById(R.id.tvDDetailUserName);

        }
    }
}
