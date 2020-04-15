package app.project.com.pickupbox.Data;

import android.os.Parcel;
import android.os.Parcelable;


import java.io.Serializable;

public class UserBoxInfo implements Parcelable, Serializable {

    private String BoxName;
    private String BoxPrice;
    private String BoxSize;
    private String PickupTime;
    private String myLatitude;
    private String myLongitude;
    private String destLatitude;
    private String destLongitude;
    private String userName;
    private String keyValue;
    private String Distance;
    private String Duration;

    public UserBoxInfo() {}

    public UserBoxInfo(Parcel in) {
        readFromParcel(in);
    }

    public UserBoxInfo(String boxName, String boxPrice, String boxSize, String pickupTime, String myLatitude, String myLongitude, String destLatitude, String destLongitude, String userName, String keyValue, String distance, String duration) {
        BoxName = boxName;
        BoxPrice = boxPrice;
        BoxSize = boxSize;
        PickupTime = pickupTime;
        this.myLatitude = myLatitude;
        this.myLongitude = myLongitude;
        this.destLatitude = destLatitude;
        this.destLongitude = destLongitude;
        this.userName = userName;
        this.keyValue = keyValue;
        Distance = distance;
        Duration = duration;
    }

    public String getDistance() {
        return Distance;
    }

    public UserBoxInfo setDistance(String distance) {
        Distance = distance;
        return this;
    }

    public String getDuration() {
        return Duration;
    }

    public UserBoxInfo setDuration(String duration) {
        Duration = duration;
        return this;
    }

    public static Creator getCREATOR() {
        return CREATOR;
    }

    public String getBoxName() {
        return BoxName;
    }

    public UserBoxInfo setBoxName(String boxName) {
        BoxName = boxName;
        return this;
    }

    public String getBoxPrice() {
        return BoxPrice;
    }

    public UserBoxInfo setBoxPrice(String boxPrice) {
        BoxPrice = boxPrice;
        return this;
    }

    public String getBoxSize() {
        return BoxSize;
    }

    public UserBoxInfo setBoxSize(String boxSize) {
        BoxSize = boxSize;
        return this;
    }

    public String getPickupTime() {
        return PickupTime;
    }

    public UserBoxInfo setPickupTime(String pickupTime) {
        PickupTime = pickupTime;
        return this;
    }

    public String getMyLatitude() {
        return myLatitude;
    }

    public UserBoxInfo setMyLatitude(String myLatitude) {
        this.myLatitude = myLatitude;
        return this;
    }

    public String getMyLongitude() {
        return myLongitude;
    }

    public UserBoxInfo setMyLongitude(String myLongitude) {
        this.myLongitude = myLongitude;
        return this;
    }

    public String getDestLatitude() {
        return destLatitude;
    }

    public UserBoxInfo setDestLatitude(String destLatitude) {
        this.destLatitude = destLatitude;
        return this;
    }

    public String getDestLongitude() {
        return destLongitude;
    }

    public UserBoxInfo setDestLongitude(String destLongitude) {
        this.destLongitude = destLongitude;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserBoxInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public UserBoxInfo setKeyValue(String keyValue) {
        this.keyValue = keyValue;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(BoxName);
        dest.writeString(BoxPrice);
        dest.writeString(BoxSize);
        dest.writeString(PickupTime);
        dest.writeString(myLatitude);
        dest.writeString(myLongitude);
        dest.writeString(destLatitude);
        dest.writeString(destLongitude);
        dest.writeString(userName);
        dest.writeString(keyValue);
        dest.writeString(Duration);
        dest.writeString(Distance);
    }

    private void readFromParcel(Parcel in){
        BoxName = in.readString();
        BoxPrice = in.readString();
        BoxSize = in.readString();
        PickupTime = in.readString();
        myLatitude = in.readString();
        myLongitude = in.readString();
        destLatitude = in.readString();
        destLongitude = in.readString();
        userName = in.readString();
        keyValue = in.readString();
        Duration = in.readString();
        Distance = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public UserBoxInfo createFromParcel(Parcel in){
            return new UserBoxInfo(in);
        }

        public UserBoxInfo[] newArray(int size){
            return new UserBoxInfo[size];
        }
    };
}
