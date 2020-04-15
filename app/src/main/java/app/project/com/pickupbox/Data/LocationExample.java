package app.project.com.pickupbox.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class LocationExample implements Parcelable, Serializable {
    private String name;
    private String latitude;
    private String longitude;

    public LocationExample (){} //생성장


    public LocationExample(Parcel in) {
        readFromParcel(in);
    }

    public LocationExample(String name, String latitude, String longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public LocationExample setName(String name) {
        this.name = name;
        return this;
    }

    public String getLatitude() {
        return latitude;
    }

    public LocationExample setLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getLongitude() {
        return longitude;
    }

    public LocationExample setLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(latitude);
        dest.writeString(longitude);
    }

    private void readFromParcel(Parcel in){
        name = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public LocationExample createFromParcel(Parcel in){
            return new LocationExample(in);
        }

        public LocationExample[] newArray(int size){
            return new LocationExample[size];
        }
    };


}
