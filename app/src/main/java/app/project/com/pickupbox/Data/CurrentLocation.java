package app.project.com.pickupbox.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class CurrentLocation implements Parcelable, Serializable {
    private float distance;
    private String chk_latitude;
    private String chk_longitude;

    public CurrentLocation() {}

    public CurrentLocation(Parcel in) {
        readFromParcel(in);
    }

    public CurrentLocation(float distance, String chk_latitude, String chk_longitude) {
        this.distance = distance;
        this.chk_latitude = chk_latitude;
        this.chk_longitude = chk_longitude;
    }

    public String getChk_latitude() {
        return chk_latitude;
    }

    public CurrentLocation setChk_latitude(String chk_latitude) {
        this.chk_latitude = chk_latitude;
        return this;
    }

    public float getDistance() {
        return distance;
    }

    public CurrentLocation setDistance(float distance) {
        this.distance = distance;
        return this;
    }

    public static Creator getCREATOR() {
        return CREATOR;
    }

    public String getChk_longitude() {
        return chk_longitude;
    }

    public CurrentLocation setChk_longitude(String chk_longitude) {
        this.chk_longitude = chk_longitude;
        return this;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(chk_latitude);
        dest.writeString(chk_longitude);
        dest.writeFloat(distance);

    }

    private void readFromParcel(Parcel in){
        chk_latitude = in.readString();
        chk_longitude = in.readString();
        distance = in.readFloat();


    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public CurrentLocation createFromParcel(Parcel in){
            return new CurrentLocation(in);
        }

        public CurrentLocation[] newArray(int size){
            return new CurrentLocation[size];
        }
    };
}
