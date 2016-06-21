package ink.models;

/**
 * Created by USER on 2016-06-22.
 */
public class FriendsModel {
    private String name;
    private String imageLink;
    private String phoneNumber;


    public FriendsModel(String name, String imageLink, String phoneNumber) {
        this.name = name;
        this.imageLink = imageLink;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
