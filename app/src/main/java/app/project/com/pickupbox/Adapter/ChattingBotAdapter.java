package app.project.com.pickupbox.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import app.project.com.pickupbox.Data.ChatData;
import app.project.com.pickupbox.R;

public class ChattingBotAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<ChatData> chatData;
    private LayoutInflater inflater;
    private String userName;
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("a h:mm", Locale.getDefault());

    public ChattingBotAdapter(Context applicationContext, int talklist, ArrayList<ChatData> list, String userName) {
        this.context = applicationContext;
        this.layout = talklist;
        this.chatData = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.userName= userName;
    }

    @Override
    public int getCount() { // 전체 데이터 개수
        return chatData.size();
    }

    @Override
    public Object getItem(int position) { // position번째 아이템
        return chatData.get(position);
    }

    @Override
    public long getItemId(int position) { // position번째 항목의 id인데 보통 position
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //항목의 index, 전에 inflate 되어있는 view, listView

//첫항목을 그릴때만 inflate 함 다음거부터는 매개변수로 넘겨줌 (느리기때문) : recycle이라고 함
        ViewHolder holder;

        if(convertView == null){
        //어떤 레이아웃을 만들어 줄 것인지, 속할 컨테이너, 자식뷰가 될 것인지
            //convertView = inflater.inflate(layout, parent, false); //아이디를 가지고 view를 만든다
            convertView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_chatbot, parent, false);
            holder = new ViewHolder();

            //챗봇꺼
            holder.TextView_nickname = convertView.findViewById(R.id.TextView_nickname);
            holder.TextView_msg = convertView.findViewById(R.id.TextView_msg);
            holder.TextView_time = convertView.findViewById(R.id.TextView_time);
            holder.img_profile = convertView.findViewById(R.id.img_profile);

            //내꺼
            holder.txt_userName = convertView.findViewById(R.id.txt_userName);
            holder.txt_message = convertView.findViewById(R.id.txt_message);
            holder.txt_time = convertView.findViewById(R.id.txt_time);

            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }

//누군지 판별
        ChatData chat = chatData.get(position);

        if(chatData.get(position).userName.equals(userName)){
            holder.TextView_nickname.setVisibility(View.GONE);
            holder.TextView_msg.setVisibility(View.GONE);
            holder.TextView_time.setVisibility(View.GONE);
            holder.img_profile.setVisibility(View.GONE);



            holder.txt_message.setVisibility(View.VISIBLE);
            holder.txt_time.setVisibility(View.VISIBLE);

            holder.txt_time.setText(mSimpleDateFormat.format(chat.time));
            //
            holder.txt_message.setText(chat.message);
        }else{
            holder.TextView_nickname.setVisibility(View.VISIBLE);
            holder.TextView_msg.setVisibility(View.VISIBLE);
            holder.TextView_time.setVisibility(View.VISIBLE);
            holder.img_profile.setVisibility(View.VISIBLE);

            holder.txt_message.setVisibility(View.GONE);
            holder.txt_time.setVisibility(View.GONE);


            //holder.img_profile.setImageResource(chat.userPhotoUrl); // 해당 사람의 프사 가져옴
            holder.TextView_msg.setText(chat.message);
            holder.TextView_time.setText(mSimpleDateFormat.format(chat.time));
            holder.TextView_nickname.setText(chat.userName);
        }


        return convertView;
    }

    //뷰홀더패턴
    public class ViewHolder{
        public TextView TextView_nickname;
        public TextView TextView_msg;
        public TextView TextView_time;
        public ImageView img_profile;
        public View rootView;
        public TextView txt_userName,txt_message,txt_time;
    }
}
