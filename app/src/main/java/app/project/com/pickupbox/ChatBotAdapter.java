package app.project.com.pickupbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Data.ChatData;
import app.project.com.pickupbox.R;

public class ChatBotAdapter  extends RecyclerView.Adapter<ChatBotAdapter.MyViewHolder> {
    private List<ChatData> mDataset;
    private String nickName;
    LinearLayout v;

    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("a h:mm", Locale.getDefault());

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView TextView_nickname;
        public TextView TextView_msg;
        public TextView TextView_time;
        public ImageView img_profile;
        public View rootView;
        public TextView txt_userName,txt_message,txt_time;

        public MyViewHolder(View v) {
            super(v);
            TextView_nickname = v.findViewById(R.id.TextView_nickname);
            TextView_msg = v.findViewById(R.id.TextView_msg);
            TextView_time = v.findViewById(R.id.TextView_time);
            img_profile = v.findViewById(R.id.img_profile);


            txt_userName = v.findViewById(R.id.txt_userName);
            txt_message = v.findViewById(R.id.txt_message);
            txt_time = v.findViewById(R.id.txt_time);


            rootView = v;

        }

    }

    public ChatBotAdapter(List<ChatData> myDataset, Context context, String userName) { //ChattingBot.java에서 userName을 넘겨준다. 받는 위치
        //{"1","2"}
        mDataset = myDataset;
        this.nickName = userName;

    }

    @NonNull
    @Override
    public ChatBotAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatData chats = mDataset.get(viewType);
        v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_chatbot, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ChatData chat = mDataset.get(position);

        holder.TextView_nickname.setText(chat.userName);
        holder.TextView_msg.setText(chat.message);
        holder.TextView_time.setText(mSimpleDateFormat.format(chat.time));

        if(chat.userName.equals(this.nickName)) {
           /* holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            holder.TextView_time.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);*/
            holder.TextView_nickname.setVisibility(View.INVISIBLE);
            holder.img_profile.setVisibility(View.INVISIBLE);
        }
        else {
            holder.TextView_msg.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.TextView_time.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.TextView_nickname.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.img_profile.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);


        }

    }

    @Override
    public int getItemCount() {
        //삼항 연산자
        return mDataset == null ? 0 :  mDataset.size();
    }

    public ChatData getChat(int position) {
        return mDataset != null ? mDataset.get(position) : null;
    }

    public void addChat(ChatData chat) {
        mDataset.add(chat);
        notifyItemInserted(mDataset.size()-1); //갱신
    }

}
