package app.project.com.pickupbox.Data;

import java.util.HashMap;
import java.util.Map;

public class UserData {
    public String userEmailID;
    public String userName;
    public String userPhone;
    public String userAddr;
    public String userGender;
    public String fcmToken;

    public UserData(){}

    public UserData(String userEmailID, String userName, String userPhone, String userAddr, String userGender, String fcmToken) {
        this.userEmailID = userEmailID;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddr = userAddr;
        this.userGender = userGender;
        this.fcmToken = fcmToken;
    }

    public Map<String,Object> toMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("userEmaidID",userEmailID);
        result.put("userName",userName);
        result.put("userPhone",userPhone);
        result.put("userAddr",userAddr);
        result.put("userGender",userGender);
        result.put("fcmToken",fcmToken);


        return result;
    }
}
