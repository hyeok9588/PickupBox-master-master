package app.project.com.pickupbox.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import app.project.com.pickupbox.Data.LocationExample;
import app.project.com.pickupbox.R;


import java.util.ArrayList;
import java.util.List;


public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> implements Filterable{
    private Context context;
    private ArrayList<LocationExample> locationList;
    private ArrayList<LocationExample> locationListFull;

    private ArrayList<LocationExample> arrayList;

    public class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvName = itemView.findViewById(R.id.tv_boxList_name);
        }
    }

    public LocationListAdapter(Context context, ArrayList<LocationExample> locationList){
        this.context = context;
        this.locationList = locationList;
        this.locationListFull = new ArrayList<>(locationList);

    }

    @NonNull
    @Override
    public LocationListAdapter.LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_list_location,parent,false);
        LocationViewHolder holder = new LocationViewHolder(view);



        return holder;
    }



    @Override
    public void onBindViewHolder(@NonNull LocationListAdapter.LocationViewHolder holder, final int position) {
        holder.tvName.setText(locationList.get(position).getName());

        //눌렀을 떄의 action
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  Intent intent = new Intent(context,PickupDetail.class);
                intent.putExtra("name",locationList.get(position).getName());
                intent.putExtra("latitude",locationList.get(position).getLatitude());
                intent.putExtra("longitude",locationList.get(position).getLongitude());
                context.startActivity(intent);*/
                String locaName  = locationList.get(position).getName();
                String destLati  = locationList.get(position).getLatitude();
                String destLongi = locationList.get(position).getLongitude();
                //Toast.makeText(context, ex_name+"눌림", Toast.LENGTH_SHORT).show();
                Intent someIntent = new Intent("names");
                someIntent.putExtra("location_Name",locaName);
                someIntent.putExtra("location_lati",destLati);
                someIntent.putExtra("location_longi",destLongi);
                LocalBroadcastManager.getInstance(context).sendBroadcast(someIntent);


            }
        });

    }



    @Override
    public int getItemCount() {
        return (locationList != null ? locationList.size() : 0);
    }



    @Override
    public Filter getFilter() {
        Log.d("테스트","getFilter");
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {

        ArrayList<LocationExample> filteringList;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String charString = constraint.toString();


            Log.d("테스트","exampleFilter 시작");



            if (charString.isEmpty()) {
                locationList = locationListFull;

            } else {
                 filteringList = new ArrayList<>();
                //String filterPatten = constraint.toString().toLowerCase().trim();
                Log.d("테스트 filterPattern",charString);

                for (LocationExample item : locationListFull) {
                    Log.d("테스트 locationListFull",item.getName());
                    if (item.getName().toLowerCase().contains(charString.toLowerCase())) {
                       //Log.d("테스트 locationListFull",item.getName().toString());
                        filteringList.add(item);
                    }
                }
                //log.d("테스트 location List full",locationListFull.get(0).getName());
                Log.d("테스트 filteredList 첫번째",filteringList.get(0).getName());
                //locationList = filteringList;
            }

            FilterResults results = new FilterResults();
            results.values = filteringList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            locationList.clear();
            //locationList.addAll((List) results.values);
            //locationList.addAll(locationListFull);
            List<LocationExample> arrayList = new ArrayList<>();

            if (results.values instanceof List){
                List list = (List)results.values;
                for (Object item : list){
                    if (item instanceof LocationExample){
                        locationList.add((LocationExample)item);

                    }
                }
            }


            //int a = results.count;
            Log.d("테스트 publishResults","퍼블리쉬 리절트 끝");

            notifyDataSetChanged();
        }
    };



}
